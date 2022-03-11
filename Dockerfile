FROM clojure:lein

COPY project.clj /usr/src/app/
WORKDIR /usr/src/app
RUN lein deps

COPY . /usr/src/app

ENTRYPOINT [ "lein", "repl", ":headless" ]
