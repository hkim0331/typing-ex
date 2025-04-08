watch:
  # npm install
  npx shadow-cljs watch app

dev:
  lein repl

dev-container:
  docker compose up -d
  docker exec -it typing-ex-typing-ex-1 sh start-repl.sh

stop:
  # must stop shadow-cljs
  docker compose down

clean:
  rm -r target

uberjar: clean
  npx shadow-cljs release app
  lein uberjar

SERV:='app.melt'
DEST:='app.melt:typing-ex/tp.jar'
deploy: uberjar
  scp target/typing-ex-*-standalone.jar {{DEST}} && \
  ssh {{SERV}} sudo systemctl restart typing-ex && \
  ssh {{SERV}} systemctl status typing-ex
