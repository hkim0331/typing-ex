FROM clojure:lein

RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install --no-install-recommends \
           git npm postgresql-client-14 2>&1

COPY project.clj /usr/src/app/
WORKDIR /usr/src/app
RUN lein deps

COPY . /usr/src/app

ENTRYPOINT [ "lein", "repl", ":headless" ]
