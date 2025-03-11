set dotenv-load

watch:
    npx shadow-cljs watch app

repl:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh start-repl.sh

uberjar:
    docker compose up -d

# need improve
run:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh run.sh

echo:
    @echo hello world ${DATABASE_URL}

stop:
    # must stop shadow-cljs
    docker compose down
