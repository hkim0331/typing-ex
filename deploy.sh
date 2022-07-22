#!/bin/sh
#yarn release
#lein uberjar
if [ -z "$1" ]; then
	echo usage: $0 target/file.jar
	exit 1
fi
BN=`basename $1`
scp $1 app.melt:tp/ && \
ssh app.melt "(cd tp && ln -sf ${BN} tp.jar)" && \
ssh app.melt sudo systemctl restart typing-ex && \
ssh app.melt systemctl status typing-ex
