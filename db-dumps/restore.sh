#!/bin/sh
if [ -z "$1" ]; then
  echo "usage: $0 file.sql"
  exit
fi

psql -h db -U postgres typing_ex < $1
