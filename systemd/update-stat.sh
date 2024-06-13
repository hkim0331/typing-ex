#!/bin/sh
psql -U postgres -h localhost typing_ex -c "update stat set stat='$1'"
