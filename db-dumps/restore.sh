#!/bin/sh
if [ -z "$1" ]; then
  echo "usage: $0 file.sql"
  exit
fi

#PSQL="psql -h db -U postgres --port=55432"
PSQL="psql -h db -U postgres"
${PSQL} -c "drop database typing_ex"
${PSQL} -c "create database typing_ex owner=postgres"
${PSQL} typing_ex < $1
