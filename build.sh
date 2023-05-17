#!/bin/sh
#
# FIXME: not work well
#
TAOENSSO_TIMBRE_MIN_LEVEL_EDN=':info'
TAOENSSO_TIMBRE_NS_PATTERN_EDN='{:deny #{"duct.middleware.web*"}}'
# syntax error?
#export TAOENSSO_TIMBRE_NS_PATTERN_EDN='{:deny #{"duct.database.sql.hikaricp" "duct.middleware.web"}}'
#yarn release
#lein uberjar
DATABASE_URL='jdbc:postgresql://db/typing_ex?user=postgres&password=password'
java -jar target/typing-ex-1.15.13-standalone.jar
