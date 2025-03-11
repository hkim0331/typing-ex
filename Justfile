watch:
    npx shadow-cljs watch app

repl:
    docker compose up -d
    docker exec -it typing-ex-typing-ex-1 sh start-repl.sh

stop:
    # must stop shadow-cljs
    docker compose down
