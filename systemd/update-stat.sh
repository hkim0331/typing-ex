#!/bin/sh
# changed 2024-09-09
# psql -U postgres -h localhost -d typing_ex \
#    -c "update stat set stat='$1', updated_at=now()"
# echo `date` typing-ex stat $1

redis-cli set stat roll-call ex 900

echo `date` exec typing-ex/systemd/update-stat.sh
