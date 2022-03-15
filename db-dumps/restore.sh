#!/bin/sh
if [ -z "$1" ]; then
  echo "usage: $0 file.sql"
  exit
fi

psql -h localhost -U postgres -W l22 < $1
