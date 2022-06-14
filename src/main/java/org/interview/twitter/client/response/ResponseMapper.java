package org.interview.twitter.client.response;

import com.twitter.clientlib.model.FilteredStreamingTweet;
import com.twitter.clientlib.model.TweetSearchResponse;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResponseMapper {

    public static SearchResponse map(TweetSearchResponse apiResponse) {

        if (Objects.isNull(apiResponse.getData()) ||
                Objects.isNull(apiResponse.getIncludes()) ||
                Objects.isNull(apiResponse.getIncludes().getUsers())){
            return new SearchResponse();
        }

        List<TweetMessage> tweets = apiResponse.getData().stream()
                .map(originalTweet -> new TweetMessage(
                        originalTweet.getId(),
                        toEpochMilliDefault(originalTweet.getCreatedAt()),
                        originalTweet.getText(),
                        originalTweet.getAuthorId()))
                .collect(Collectors.toList());
        List<TweetAuthor> authors = apiResponse.getIncludes().getUsers().stream()
                .map(originalUser -> new TweetAuthor(
                        originalUser.getId(),
                        toEpochMilliDefault(originalUser.getCreatedAt()),
                        originalUser.getUsername(),
                        originalUser.getName()))
                .collect(Collectors.toList());
        return new SearchResponse(authors, tweets);
    }

    public static SearchResponse map(List<FilteredStreamingTweet> apiResponse) {
        if (Objects.isNull(apiResponse) || apiResponse.isEmpty()){
            return new SearchResponse();
        }

        List<TweetMessage> tweets = new ArrayList<>();
        List<TweetAuthor> authors = new ArrayList<>();

        apiResponse.forEach(originalTweet -> {
            if (Objects.nonNull(originalTweet.getData())) {
                tweets.add(new TweetMessage(
                        originalTweet.getData().getId(),
                        toEpochMilliDefault(originalTweet.getData().getCreatedAt()),
                        originalTweet.getData().getText(),
                        originalTweet.getData().getAuthorId()));
            }

            String authorId = StringUtils.isNotEmpty(originalTweet.getData().getAuthorId())? originalTweet.getData().getAuthorId() : "";
            if (Objects.nonNull(originalTweet.getIncludes()) && Objects.nonNull(originalTweet.getIncludes().getUsers())) {
                originalTweet.getIncludes().getUsers().stream()
                        .filter(user -> authorId.equals(user.getId()))
                        .findFirst()
                        .ifPresent(author ->
                                authors.add(new TweetAuthor(
                                        author.getId(),
                                        toEpochMilliDefault(author.getCreatedAt()),
                                        author.getUsername(),
                                        author.getName())));
            }
        });

        return new SearchResponse(authors, tweets);
    }

    private static long toEpochMilliDefault(OffsetDateTime offsetDateTime){
        if (Objects.isNull(offsetDateTime)){
            return System.currentTimeMillis();
        } else {
            return offsetDateTime.toEpochSecond() * 1000;
        }
    }
}
