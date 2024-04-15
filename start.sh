#!/bin/sh
PORT=3002 \
TP_START=2024-04-01 \
DATABASE_URL='jdbc:postgresql://db/typing_ex?user=postgres&password=password' \
java -jar tp.jar >> log/ex-typing.log

