build:
	yarn release
	lein uberjar

deploy:
	scp target/typing-ex-*-standalone.jar app.melt:typing-ex/tp.jar && \
	ssh app.melt sudo systemctl restart typing-ex && \
	ssh app.melt systemctl status typing-ex
