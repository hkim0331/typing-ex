SERV=app.melt
DEST=${SERV}:typing-ex/tp.jar

clean:
	${RM} -rf target

build: clean
	yarn release
	lein uberjar

deploy: build
	scp target/typing-ex-*-standalone.jar ${DEST} && \
	ssh ${SERV} sudo systemctl restart typing-ex && \
	ssh ${SERV} systemctl status typing-ex
