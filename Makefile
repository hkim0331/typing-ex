SERV=app.melt
DEST=${SERV}:typing-ex/tp.jar

build:
	docker build -t hkim0331/typing-ex .

clean:
	${RM} -rf target

uberjar: clean

	yarn release
	lein uberjar

deploy: uberjar
	scp target/typing-ex-*-standalone.jar ${DEST} && \
	ssh ${SERV} sudo systemctl restart typing-ex && \
	ssh ${SERV} systemctl status typing-ex
