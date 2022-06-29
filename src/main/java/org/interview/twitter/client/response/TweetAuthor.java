package org.interview.twitter.client.response;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * For each author, we will need the following:
 * The user ID
 * The creation date of the user as epoch value
 * The name of the user
 * The screen name of the user
 */
@Getter
@AllArgsConstructor
public class TweetAuthor {
    private final String id;
    private final Long createdAtEpochMilli;
    private final String name;
    private final String screenName;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
