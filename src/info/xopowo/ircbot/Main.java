package info.xopowo.ircbot;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.IrcException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import twitter4j.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static info.xopowo.ircbot.TwitterUtils.sleep;

public class Main {

    @Option(name="-c",usage="path to configuration file",metaVar = "filename")
    private File confFile = null;

    public static void main(String[] args) throws Exception {
        new Main().doMain(args);
    }

    private void doMain(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);

        try{
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("IrcBot [-c <filename>] [-i]");
            parser.printUsage(System.err);
            System.err.println();
            System.exit(-1);
        }

        Properties prop = new Properties();
	prop.load(this.getClass().getResourceAsStream("default.properties"));
        if (confFile != null) {
	    prop.load(new FileInputStream(confFile));
        }
        
        String botNick = prop.getProperty("bot.nick");
        String server = prop.getProperty("bot.server");
        int port = Integer.parseInt(prop.getProperty("bot.port"));
        String encoding = prop.getProperty("bot.encoding");
        String channel = prop.getProperty("bot.channel");
        String source = prop.getProperty("bot.tweet.source");
        long refreshRate = Long.parseLong(prop.getProperty("bot.tweet.refreshrate")) * 1000;

	System.err.println("[Settings]");
	System.err.println("Bot nick: " + botNick);
	System.err.println("Server: " + server);
	System.err.println("Port: " + port);
	System.err.println("Channel: " + channel);
	System.err.println("Encoding: " + encoding);
	System.err.println("Source: " + source);
	System.err.println("Timeline Refresh Rate [msec]: " + refreshRate);
	System.err.println("----------------------------------");

        Tweet2IrcBot bot = new Tweet2IrcBot(botNick);
        bot.setEncoding(encoding);
        bot.connect(server, port);
        bot.joinChannel(channel);
        sleep(1000);
	TwitterClient client = new TwitterClient(new TwitterFactory().getInstance(), bot);

	while (true) {
	    client.getTimeline(source);
	    sleep(refreshRate);
	}
    }
}
