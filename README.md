# typing-ex

情報リテラシー 2022 授業用タイプ練習アプリ。

Duct(ataraxy, postgres, buddy-auth) でバックエンドを書き、
ClojureScript でフロントを書く練習。

    % lein new duct typing-ex +site +ataraxy +postgres
    % lein new shadow-cljs typing-ex :force +reagent

## develop

check postgres alive,

    $ npm install
    $ npx shadow-cljs watch app
    $ lein repl
    user> (dev)
    user> (go)

    $ open http://localhost:3000


or from VScode, choose  REPL -> Leiningen after `yarn watch`.

## deploy

    $ make deploy

## require

* postgresql 14
* redis 6
* jre >= 11


## License

Copyright © 2021-2024 hkimura

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
