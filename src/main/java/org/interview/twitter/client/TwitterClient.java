package org.interview.twitter.client;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.JSON;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.FilteredStreamingTweet;
import com.twitter.clientlib.model.TweetSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.interview.twitter.client.response.ResponseMapper;
import org.interview.twitter.client.response.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class TwitterClient {

    private static final String LOG_ERROR_API_RESPONSE = "msg=[Failed to get response from Twitter API], httpStatus=[%s], error=[%s]";
    private static final String LOG_ERROR_METRIC_PARSE = "msg=[Failed to parse response data to create metric], error=[%s]";
    private static final String LOG_ERROR_STREAM_PARSING = "msg=[Failed to parse response stream line], error=[%s]";
    public static final String LOG_METRIC_DATA_POINT = "msg=[Successful API response], timestamp=[%s], timeTakenSecs=[%s], hitCount=[%s], avgHitsPerSec=[%s]\n";

    private final TwitterApi api;

    @Autowired
    public TwitterClient(String bearerToken) {
        this.api = new TwitterApi();
        api.setTwitterCredentials(new TwitterCredentialsBearer(bearerToken));
    }

    public SearchResponse searchRecentTweets(String query) {
        if (StringUtils.isEmpty(query)){
            throw new IllegalArgumentException("query cannot be empty");
        }
        Set<String> expansions = Collections.singleton("author_id");
        Set<String> userFields = Collections.singleton("created_at");
        Set<String> tweetFields = Collections.singleton("created_at");
        OffsetDateTime startTime = OffsetDateTime.now().minus(1L, ChronoUnit.HOURS);
        Integer maxResults = 100;
        String sortOrder = "recency";
        try {
            long stopwatchStart = System.currentTimeMillis();
            TweetSearchResponse apiResponse = api.tweets().tweetsRecentSearch(query, startTime, null, null,
                    null, maxResults, sortOrder, null, null, expansions, tweetFields, userFields,
                    null, null, null);
            if (Objects.nonNull(apiResponse)) {
                SearchResponse mappedResponse = ResponseMapper.map(apiResponse);
                logMetric(stopwatchStart, mappedResponse);
                return mappedResponse;
            }
        } catch (ApiException e) {
            log.warn(String.format(LOG_ERROR_API_RESPONSE, e.getCode(), e.getMessage()));
        }
        return new SearchResponse();
    }

    public SearchResponse searchStream(int waitThresholdInSeconds, int hitCountLimit){
        if (waitThresholdInSeconds < 1){
            throw new IllegalArgumentException("waitThresholdInSeconds cannot be < 1");
        }
        if (hitCountLimit < 1){
            throw new IllegalArgumentException("hitCountLimit cannot be < 1");
        }
        Set<String> expansions = Collections.singleton("author_id");
        Set<String> tweetFields = new HashSet<>(Arrays.asList("id", "created_at", "text", "author_id"));
        Set<String> userFields = new HashSet<>(Arrays.asList("id", "created_at", "name", "username"));
        try {
            long stopwatchStart = System.currentTimeMillis();
            InputStream inputStream = api.tweets().searchStream(expansions, tweetFields, userFields, null, null, null, null);
            List<FilteredStreamingTweet> parsedStream = parse(inputStream, waitThresholdInSeconds, hitCountLimit);
            SearchResponse mappedResponse = ResponseMapper.map(parsedStream);
            logMetric(stopwatchStart, mappedResponse);
            return mappedResponse;
        } catch (ApiException e) {
            log.warn(String.format(LOG_ERROR_API_RESPONSE, e.getCode(), e.getMessage()));
        }
        return new SearchResponse();
    }

    private List<FilteredStreamingTweet> parse(InputStream tweetStream, Integer waitThresholdInSeconds, Integer hitCountLimit) {
        List<FilteredStreamingTweet> parsedTweets = new ArrayList<>();
        JSON json = new JSON();
        try{
            long startStopwatch = System.currentTimeMillis();
            int hits = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(tweetStream));
            String line = reader.readLine();
            while (line != null && hits < hitCountLimit && calculateSecondsUntilNow(startStopwatch) < waitThresholdInSeconds) {
                if(line.isEmpty()) {
                    line = reader.readLine();
                    continue;
                }
                FilteredStreamingTweet parsedTweet = json.getGson().fromJson(line, FilteredStreamingTweet.class);
                if (Objects.nonNull(parsedTweet)){
                    parsedTweets.add(parsedTweet);
                    hits++;
                }
                line = reader.readLine();
            }
        }catch (Exception e) {
            log.warn(String.format(LOG_ERROR_STREAM_PARSING, e.getMessage()));
        }
        return parsedTweets;
    }

    private void logMetric(long stopwatchStart, SearchResponse apiResponse) {
        try {
            long diffInSecs = calculateSecondsUntilNow(stopwatchStart);
            int hitCount = apiResponse.getHitCount();
            double hitsPerSec = (double) hitCount / (double) diffInSecs;
            log.info(String.format(LOG_METRIC_DATA_POINT,  System.currentTimeMillis(), diffInSecs,  hitCount, hitsPerSec));
        } catch (Exception e){
            log.info(String.format(LOG_ERROR_METRIC_PARSE, e.getMessage()));
        }
    }

    private long calculateSecondsUntilNow(long startEpochMilli){
        return  Duration.between(Instant.ofEpochMilli(startEpochMilli), Instant.now()).getSeconds();
    }


}

