#!/bin/sh
psql -U postgres -h localhost typing_ex -c "select * from stat"
