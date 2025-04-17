# typing-ex

情報リテラシー授業用タイプ練習アプリ。

Duct(ataraxy, postgres, buddy-auth) でバックエンドを書き、
ClojureScript でフロントを書く練習。

started by:

    lein new duct typing-ex +site +ataraxy +postgres
    lein new shadow-cljs typing-ex :force +reagent

## Develop

use docker container.

    npm install
    npx shadow-cljs watch app
    docker-compose up -d
    docker exec -it typing-ex-typing-ex-1 sh start-repl.sh
    > (dev)
    > (go)
    open http://localhost:3000

## Requirements

* Clojure/ClojureScript
* duct
* shadow-cljs
* postgresql
* redis

## Deploy

    just deploy

## requires

* postgresql
* redis
* jre


## License

Copyright © 2021-2025 hkimura

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
