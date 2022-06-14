package org.interview.twitter.client.response;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * For each message, we will need the following:
 * The message ID
 * The creation date of the message as epoch value
 * The text of the message
 * The author of the message
 */
@Getter
@AllArgsConstructor
public class TweetMessage {
    private final String id;
    private final Long createdAtEpochMilli;
    private final String text;
    private final String author;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
