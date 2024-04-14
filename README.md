# typing-ex

情報リテラシー 2022 授業用タイプ練習アプリ。

Duct(ataraxy, postgres, buddy-auth) でバックエンドを書き、
ClojureScript でフロントを書く練習。

    % lein new duct typing-ex +site +ataraxy +postgres
    % lein new shadow-cljs typing-ex :force +reagent

## develop

check postgres alive,

    $ yarn watch
    $ lein repl
    user> (dev)
    user> (go)

    $ open http://localhost:3002


or from VScode, choose  REPL -> Leiningen after `yarn watch`.

## deploy

    $ make deploy

## License

Copyright © 2021-2024 hkimura

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
