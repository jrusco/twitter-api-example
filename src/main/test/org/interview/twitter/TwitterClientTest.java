package org.interview.twitter;


import org.interview.twitter.client.TwitterClient;
import org.interview.twitter.client.response.SearchResponse;
import org.interview.twitter.client.response.TweetAuthor;
import org.interview.twitter.client.response.TweetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
class TwitterClientTest {

    private final TwitterClient client;
    private final PrintStream resultStream;

    @Autowired
    public TwitterClientTest(TwitterClient client, PrintStream resultStream) {
        this.client = client;
        this.resultStream = resultStream;
    }

    @Test
    void test_searchRecent() {
        String keyword = "bieber";
        SearchResponse response = client.searchRecentTweets(keyword);
        resultStream.println(response);
        Assertions.assertFalse(response.getTweetsByAuthor().isEmpty(), "A valid test case should contain at least 1 tweet and its author info");
        Assertions.assertTrue(getTweetAuthorIds(response).containsAll(getAuthorIds(response)), "All tweet authors should have their related extended data");
        Assertions.assertTrue(isAscendingChronologicalOrder(getAuthorDates(response)), "Authors should be sorted in ascending chronological order");
    }

    @Test
    void test_searchStream()  {
        int hitCountLimit = 100;
        int waitThresholdInSeconds = 30;
        SearchResponse response = client.searchStream(waitThresholdInSeconds, hitCountLimit);
        resultStream.println(response);
        Assertions.assertFalse(response.getTweetsByAuthor().isEmpty(), "A valid test case should contain at least 1 tweet and its author info");
        Assertions.assertFalse(response.getHitCount() > hitCountLimit, "The amount of tweets should be equal or lower than the specified limit");
        Assertions.assertTrue(getTweetAuthorIds(response).containsAll(getAuthorIds(response)), "All tweet authors should have their matching extended data");
        Assertions.assertTrue(isAscendingChronologicalOrder(getAuthorDates(response)), "Authors should be sorted in ascending chronological order");
    }

    private Set<String> getAuthorIds(SearchResponse response){
        return response.getTweetsByAuthor().keySet().stream().map(TweetAuthor::getId).collect(Collectors.toSet());
    }

    private List<Long> getAuthorDates(SearchResponse response){
        return response.getTweetsByAuthor().keySet().stream().map(TweetAuthor::getCreatedAtEpochMilli).collect(Collectors.toList());
    }

    private Set<String> getTweetAuthorIds(SearchResponse response){
        return response.getTweetsByAuthor().values().stream().flatMap(List::stream).map(TweetMessage::getAuthor).collect(Collectors.toSet());
    }

    private boolean isAscendingChronologicalOrder(List<Long> timestamps){
        long prev = 0;
        for (Long current : timestamps){
            if (Objects.isNull(current) || prev > current){
                return false;
            } else {
                prev = current;
            }
        }
        return true;
    }

}