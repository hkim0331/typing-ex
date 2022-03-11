#!/bin/sh
pg_dump -h localhost -U postgres -W l22 > `date +l22-%F.dump`
