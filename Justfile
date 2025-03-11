set dotenv-load

watch:
    npx shadow-cljs watch app

repl:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh start-repl.sh

uberjar:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 lein uberjar

bash:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 bash

# need improve
run:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh run.sh

down:
    # must stop shadow-cljs
    docker compose down
