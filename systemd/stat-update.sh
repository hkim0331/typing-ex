#!/bin/sh
echo `date +%T` $PGPASSWORD >> /tmp/log
psql -U postgres -h localhost -d typing_ex \
   -c "update stat set stat='$1', updated_at=now()"
