#!/bin/sh
export PORT=3023
export DATABASE_URL='jdbc:postgresql://db/l22?user=postgres'
#  TAOENSSO_TIMBRE_MIN_LEVEL_EDN=':warn' \
#  lein run
java -jar target/typing-ex-1.6.5-standalone.jar


