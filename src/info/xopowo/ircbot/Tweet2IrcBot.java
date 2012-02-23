package info.xopowo.ircbot;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.RateLimitStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static info.xopowo.ircbot.TwitterUtils.sleep;

public class Tweet2IrcBot extends PircBot implements IStatusListener {

    public static final long IRC_POST_INTERVAL = 3000;
    public static final int API_LIMIT_WARN_THRESHOLD = 45;

    public Tweet2IrcBot(String nick) {
        setName(nick);
    }

    @Override
    public void onReceiveStatuses(ResponseList<Status> list) {
        Collections.reverse(list);
	RateLimitStatus limit = list.getRateLimitStatus();
	if (limit.getRemainingHits() < API_LIMIT_WARN_THRESHOLD) {
	    String message = new StringBuilder()
		.append("WARN: API limit is ")
		.append(limit.getRemainingHits())
		.append(" remains. Reset time is ")
		.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(limit.getResetTime()))
		.append(" .")
		.toString();
	    System.err.println(message);
	    for (String ch : getChannels()) {
		sendMessage(ch, message);
		sleep(IRC_POST_INTERVAL);
	    }
	}

        for (Status status : list) {
            String message = new StringBuilder()
		.append(status.getUser().getScreenName())
		.append(" : ")
		.append(status.getText().replace('\n', ' '))
		.toString();
            System.err.println(status.getCreatedAt() + " | " + message);
            for (String ch : getChannels()) {
                sendMessage(ch, message);
		sleep(IRC_POST_INTERVAL);
            }
        }
    }

    @Override
    public void onCaughtTwitterException(TwitterException e) {
        BufferedReader reader = new BufferedReader(new StringReader(e.getMessage()));
        String message = "";
        try {
            message = new StringBuilder()
		.append(reader.readLine())
		.append(TwitterUtils.rateLimitToString(e.getRateLimitStatus()))
		.toString();
	    reader.close();
        } catch (IOException e1) {}
        System.err.println(message);
        for (String ch : getChannels()) {
            sendMessage(ch, "API error.");
	    sleep(IRC_POST_INTERVAL);
        }
    }
}
