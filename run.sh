#!/bin/sh
PORT=3023 \
  TAOENSSO_TIMBRE_MIN_LEVEL_EDN=':warn' \
  DATABASE_URL=jdbc:postgresql://db/l22?user=postgres \
  lein run
#  java -jar target/typing-ex-1.6.3-SNAPSHOT-standalone.jar

