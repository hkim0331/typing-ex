set dotenv-load

watch:
    npx shadow-cljs watch app

repl:
    lein repl

uberjar:
    lein uberjar

docker-repl:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh start-repl.sh

docker-bash:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 bash

docker-uberjar:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 lein uberjar
    need improve

docker-run:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh run.sh

down:
    # must stop shadow-cljs
    docker compose down

# SERV := app.melt
SERV := app.melt.kyutech.ac.jp
DEST := ubuntu@{{SERV}}:typing-ex

prep:
    scp systemd/typing-ex.service {{DEST}}/
    scp systemd/typing-ex_roll-call.* {{DEST}}/

deploy: uberjar
    scp target/typing-ex-*-standalone.jar {{DEST}}/tp.jar
    ssh {{SERV}} sudo systemctl restart typing-ex
    ssh {{SERV}} systemctl status typing-ex
