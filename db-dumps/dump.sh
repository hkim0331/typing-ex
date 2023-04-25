#!/bin/sh
pg_dump -h db -U postgres typing_ex > `date +typing_ex-%F.sql`
