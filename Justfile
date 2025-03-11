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

