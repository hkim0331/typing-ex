#!/bin/sh
psql -U postgres -h localhost -d typing_ex \
   -c "update stat set stat='$1', updated_at=now()"

echo `date` typing-ex stat $1 >> systemd.log
