#!/bin/sh
pg_dump -h localhost -U postgres typing_ex > `date +typing_ex-%F.sql`
