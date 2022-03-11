# typing-ex

情報リテラシー 2021 授業用タイプ練習アプリ。

Duct(ataraxy, postgres, buddy-auth) でバックエンドを書き、
ClojureScript でフロントを書く練習。

    % lein new duct typing-ex +site +ataraxy +postgres
    % lein new shadow-cljs typing-ex :force +reagent

## backend(duct)

buddy-auth でセッション認証し、
cljs の typing フロントエンドを呼び出す。

スコア表示も cljs でできないか？ できそう。その方法を探る。

    % lein repl
    > (dev)
    > (go)

## frontend(shadow-cljs)

public を duct の resouces 以下に移動した。

### Run

    % yarn
    % yarn watch

### Clean

    % yarn clean

### Release

    % yarn release


## License

Copyright © 2021 hkimura

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
