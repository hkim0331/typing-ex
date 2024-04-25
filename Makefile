SERV=app.melt
DEST=${SERV}:typing-ex/tp.jar
TAG=hkim0331/typing-ex:0.4.0

build:
	docker build --pull -t ${TAG} .

clean:
	${RM} -r target

uberjar:
	make clean
	yarn release
	lein uberjar

deploy: uberjar
	scp target/typing-ex-*-standalone.jar ${DEST} && \
	ssh ${SERV} sudo systemctl restart typing-ex && \
	ssh ${SERV} systemctl status typing-ex
