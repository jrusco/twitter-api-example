package org.interview.twitter.client.response;


import com.google.gson.Gson;

import java.util.*;
import java.util.stream.Collectors;


public class SearchResponse {

    private final Map<TweetAuthor, List<TweetMessage>> tweetsByAuthor;

    /**
     * Your application should return the messages grouped by user (users sorted chronologically, ascending)
     * The messages per user should also be sorted chronologically, ascending
     */
    public SearchResponse(List<TweetAuthor> authors, List<TweetMessage> tweets){
        Map<TweetAuthor, List<TweetMessage>> tweetsByAuthor = new LinkedHashMap<>(authors.size() + 1, 1f);
        authors.sort(Comparator.comparingLong(TweetAuthor::getCreatedAtEpochMilli));
        for (TweetAuthor author : authors) {
            String authorId = author.getId();
            List<TweetMessage> authorSortedTweets = tweets.stream()
                    .filter(tweet -> authorId.equals(tweet.getAuthor()))
                    .sorted(Comparator.comparingLong(TweetMessage::getCreatedAtEpochMilli))
                    .collect(Collectors.toList());
            tweetsByAuthor.putIfAbsent(author, authorSortedTweets);
        }
        this.tweetsByAuthor = tweetsByAuthor;
    }

    public SearchResponse() {
        this.tweetsByAuthor = Collections.emptyMap();
    }

    public Map<TweetAuthor, List<TweetMessage>> getTweetsByAuthor() {
        return new LinkedHashMap<>(tweetsByAuthor);
    }

    public int getHitCount(){
        return tweetsByAuthor.values().size();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
