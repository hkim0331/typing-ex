#!/bin/sh
PORT=3023 \
    DATABASE_URL=jdbc:postgresql://db/l22?user=postgres \
#    lein run
    java -jar target/typing-ex-1.6.3-SNAPSHOT-standalone.jar

