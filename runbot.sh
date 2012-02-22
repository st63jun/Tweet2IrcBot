#!/bin/sh

java -Xbootclasspath/a:lib/args4j-2.0.19.jar:lib/pircbot.jar:lib/twitter4j-core-2.2.5.jar -jar dist/IrcBot-*-SNAPSHOT.jar $@
