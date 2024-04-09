#!/bin/sh
kill `ps ax | grep '[t]p.jar' | awk '{print $1}'`
