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

# amd/arm　multi　binaries
# need amd?
docker-hub: security clean manifest

security:
	security -v unlock-keychain ~/Library/Keychains/login.keychain-db

manifest: arm64 amd64
	docker manifest create --amend ${TAG} ${TAG}-amd64 ${TAG}-arm64
	docker manifest push ${TAG}
	# docker manifest tag ${TAG}:${VER}
	# docker manifest push ${TAG}:${VER}

amd64:
	docker buildx build --platform linux/$@ --push -t ${TAG}-amd64 .

arm64:
	docker buildx build --platform linux/$@ --push -t ${TAG}-arm64 .
