#!/bin/sh
PORT=3023 \
    DATABASE_URL=jdbc:postgresql://db/l22?user=postgres \
    lein run
 #    java -jar target/typing-ex-1.2.4-standalone.jar

