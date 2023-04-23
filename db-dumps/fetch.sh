#!/bin/sh

SERV=app.melt
DUMP=typing-ex/db-dumps
TODAY=`date +%F`

ssh ${SERV} "cd ${DUMP} && ./dump.sh"
scp ${SERV}:${DUMP}/typing_ex-${TODAY}.sql .

