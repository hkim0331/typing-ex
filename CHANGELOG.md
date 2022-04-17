# typing-ex

* Clojure/ClojureScript
* duct
* shadow-cljs
* yarn
* postgresql
* docker, docker-compose

## Unreleased
- 各行ごとに集計。
- タイプ終わりを自動判断する。
- 日本語例文。
- improve trials. todays にするか？


## 1.3.6-SNAPSHOT

## 1.3.5 - 2022-04-17
### Changed
- bouundary.results/find-max-pt
  文字列 tmp を引数で書き換えてSQL文にして渡す。
### Added
- 練習日数を表示。

## 1.3.4

## 1.3.3 - 2022-04-14
- 7 days
- admin only にちょい説明文

## 1.3.2 - 2022-04-13
- 最終練習日時。

## 1.3.1 - 2022-04-03
- m1 の hasher でエラー。データベースが壊れたか？m2 からコピーして復活。
  hashers が例外を出して画面が驚く。驚かせる画面を予防するにはどうすっか？
### Changed
- (defounce updater (js/setInterval ...)
  これを declare 使って typing.cljs の先頭付近に持っていくとカウントダウンしなくなる。
- 残り秒数の表示を monospace に変更した。これでも 10 -> 9 になるときに縮むよ。

## 1.3.0 - 2022-03-26
### Added
- 本日グラフをタイプ練習のページに表示する。
  正しくは、エンドポイント /api に munntaja 等のミドルウェアを伏せといてデータ交換だが、
  str したデータを plain/text で受け取って read-string して解決。

## 1.2.6 - 2022-03-25
### Bugfix
- docker postgresql の l22.results テーブルのコラム login が nick に戻っていた。
### Added
- login 失敗時に flash メッセージを表示する。

## 1.2.5 - 2022-03-24
### Changed
- scores に意味のあるリンク（ボタン）
- スコアのマイナスをフィルタして JS コンソールにエラーを出さないように。

## 1.2.4 - 2022-03-17
### Changed
- password hash
- in development, use 3023/tcp

## 1.2.3 - 2022-03-16
### Changed
- カウントが 0 の次にブランクになるのをフィックス。
- カウント(count) に代わって seconds を使った。
- textarea の幅が足りない例文があるため、660px 120px に変更した。
### Milestone
- nick を login に変更。milestone.

## 1.2.2 - 2022-03-15
- バージョン上がっているかチェック。
- detail ではなく、回数、平均、最高点を表示する。

## 1.2.1 - 2022-03-15
### Changed
- textarea の高さを 100px に変更した。
- textarea の font-family を見本テキストと同じ monospace に変更した。
- login 関係の不要なメッセージを消した。
- shadow-cljs release app を実行せずにデプロイしたらどうなるかのチェック。
- app.melt へのデプロイ。
- font-size: 12pt;


## 1.2.0 - 2022-03-15
### Changed
- typing.cljs: scores ボタンを logout ボタンの左に。
- タイプを始めると自動でスタートする。
- boudary/users.clj: ds をやめ、全て ds-opt を使うようにした。
- send-score omit zero test
### Milestone
- cljs から post. clj 側から埋め込んだ anti-forgery-token を cljs で読んで、
  {:form-params {:__anti-forgery-token token}}
  のようにパラメータに埋め戻して post する。

## 1.1.0 - 2022-03-14
### Changed
- utils/ds-opt
- renamed initdb.d/*.bb as initdb.d/*.clj
- グラフに 100 点の赤線

## 1.0.0 - 2022-03-11
2022 version started. 昨年のバージョンをベースに改良を加える。
### Changed
- typing データベースから l22 データベースに変更。成績資料のテーブルも入れる予定で。
- lein ancient upgrade
- reagent 1.0.0 -> 1.1.1
- devtools 0.9.4 -> 1.0.5, なんに使うんだ？
- Dockerfile の ENTRYPOINT [ "lein", "repl", ":headless" ] のコンマを忘れる。
- https://github.com/hkim0331/typing-ex.git

---

## 2021 Unreleased
- delete score/:id
- delete users/:id
- 汎用の error ページを作り、password 間違ったり、
  ニックネームがかぶったりがどうなるか説明する。
- test にレベルを入れる
- log のタイムスタンプが UTC だよ。
- FIXME: backspace 以外に C-h で消せる。
- 単語がズレるとマイナスがでかい。マイナスの理由を表示する。
  あるいは根本的に練習の方法を変えるか。

- FIXME: (:anti-forgery-token req) を利用できないか？
- FIXME: post /users
- FIXME: サブクエリに引数 boundary.results/find-max-pt

## 0.10.2 - 2021-09-10
tp.melt が動かなくなっていた。
shadow-cljs rocks だったか？表示は。

ローカルに yarn release してみるとエラー。

    $ yarn release
    The required namespace "react" is not available,
    it was required by "reagent/core.cljs".

https://clojurians-log.clojureverse.org/shadow-cljs/2019-08-25
にしたがって、

    $ npm init -y
    $ npm install react react-dom create-react-class
    $ yarn release

動き出した。

## 0.10.1 - 2021-09-10
## Changed
- sign-on を無効にする。
  sign-on-stop で"新規登録はまた来年"を表示。
- デバッグメッセージを減らす。

## 0.10.0 - 2021-08-20
### Changed
- 7days -> 30 days

## 0.9.31 - 2021-08-06
### Changed
- トップページに ex, ul へのリンク（ボタン）作成

## 0.9.29 - 2021-07-17
### Removed
- レポート終了に伴うメッセージ変更。

## 0.9.28 - 2021-07-13
### Added
- gs.melt へのリンク

## 0.9.27 - 2021-07-08
### Changed
- レポート警告を少なく。

## 0.9.26 - 2021-07-03
### Changed
- typing.cljs: 「レポートやってるか？」

## 0.9.25 - 2021-07-02
こうでは？

  (js/alert "タイピングは平常点(20%)の内側、みんながんばってる。
             次のレポートは 30%、ウェート高いよ。
             レポートに目鼻が着いてから、再度タイピングに燃えても遅くない。")

## 0.9.24 - 2021-07-02
### Changed

  (js/alert "タイピングは平常点(20%)の内側、みんながんばってる。
             レポートは 30%、日頃の学習の成果を披露しなさい。
             授業の内容理解してんの？と思われるレポートは当たり前だが不合格。
             タイピングできるようになったんで、コピペなんてしないよ。自分の力で。")

## 0.9.23 - 2021-07-01
### Changed
- polish up texts.

## 0.9.22 - 2021-07-01
### Added
- package.json: "license": "UNLICENSED"
- 連続七回ごとにレポートアラート。
### Changed
- change drills.txt where id=303, 143, 185.

## 0.9.21 - 2021-07-01
- alert "レポートやってる？" in every 10 trial.

## 0.9.20 - 2021-06-30
- empty 対応完了。

## 0.9.19 - 2021-06-29
### added
- link to ul.melt

## 0.9.18-SNAPSHOT
### Changed
- hkimura のバックグラウンドカラーを blue に。

## 0.9.17 - 2021-06-24
- update bump-version.sh
- コメント修正
- btn#counter {font-family: monospace;}


## 0.9.16 - 2021-06-19
- 文章の添削
- プロットのバッグクラウンドをちょっと明るく
- hkimura のスコア（グラフ）は見せてもいい。
  FIXME: case で３つに場合分けするが、3色で nick を色分けできない。

## 0.9.15 - 2021-06-18
- qa.melt をリンクする。

## 0.9.16
- 一般ユーザでもhkimura のスコア（グラフ）が見える。

## 0.9.14 - 2=
- /users に auth.

## 0.9.12 - 2021-06-17
- 最近10日のベストスコアを表示。
  最近10日以内にチャレンジしてない学生はリストに載らないが、いいやろ。
- db-backup フォルダに dump.sh, restore.sh を作成した。
- 連続するバックスペースは一個と数える。ま、いいか。
- FIXME: mac だと、backspace 以外に C-h で消せる。どうする？

## 0.9.11 - 2021-06-14
- counter がマイナスで得点計算に入ったらincしてゼロにする。

## 0.9.10 - 2021-06-11
- 文言（ページタイトル）の修正 typing test -> challenge はどうか？
- 一つのキーもタイプされてない時は、チャレンジ忘れてね？を表示する。

## 0.9.9 - 2021-06-10
- buttons bootstrap

## 0.9.8 - 2021-06-10
- [auth]
- ページデザイン調整

## 0.9.7 - 2021-06-09
- [BUGFIX] テーブルに外部キー制約ついてて nick を変更できなかった。制限を外す。
- admin? の定義、間違った。修正。

## 0.9.5 - 2021-06-08
- display avarage of last 10 trials.

## 0.9.4 - 2021-06-08
- cheer-up message

## 0.9.3 - 2021-06-07
- added `/users` endpoint
- recent trials - results テーブル見れば一発。

## 0.9.2 - 2021-06-07
- textarea の on-key-up イベントでバックスペースを判別
- error^2 をスコアから引く。

## 0.9.1 - 2021-06-06
- changed: typing.cljs, boundary/drills.clj: cljs からではなく、
  boundary 側でテキストをランダムに選ぶ。
- changed .gitignore

  /utils/excerpt/out.txt
  !/utils/excerpt/out/.placeholder

## 0.9.0 - 2021-06-06
- test は 4 行を基本とする
- test をもっと増やす。現在 648 個。オープン戦終了。
- utils/excerpt. まだインサートしてない。drop/create/92- でどうだ？
- postgres owner/user の関係。テーブルに grant 与えないとダメ。
- records 秒以下を表示しない。
- fix typo: 35-drop-constraint.sql
- create: initdb.d/49-drills.down.sql
- changed: initdb.d/92-seed-drills.bb/insert-drills が引数 dir をとる。

## 0.8.0 - 2021--6-05
- self score を逆順に。[:ol {:reversed "reversed"}]
- レコード表示をSVGで。

## 0.7.5 - 2021-06-05
- self score のページはscoreから自分をクリックしたとき。
- ol 中のli作成に map で書いていたのを into で置き換えた。

## 0.7.4 - 2021-06-04
- 自分のスコアの背景を赤で表示

## 0.7.3 - 2021-06-04
- password を変更できる。
- nickname を変更できる。ちょっと面倒。foreign key 制限つけてた。
  面倒なので、
  => alter table results drop constraint results_users_nick_fkey;
  foreign key は将来的に変更の可能性がないフィールドに付与しよう。

## 0.7.2 - 2021-06-03
- admin は受講生のスコアを見れる。

## 0.6.0
- ちょっとましな評価関数
- display self record

## 0.5.3-SNAPSHOT
- typing-ex.boundary.utils ネームスペース。
- 評価関数 残り時間を足す。
  v = (g/a - b/g)*100 + c

## 0.5.2 - 2021-06-02
- tp.melt にデプロイ。
- FIX: send-score が数発出て行ったが、FIX できたか？
  並列、あるいは非同期というんだろうが、今までにないケアをしないと。

## 0.5.1 - 2021-06-02
- go ルーチンをかくと、並列に動く部分が出てきて、気をつけないと混乱する。

## 0.5.0 - 2021-06-01
- counter=0 で発射。
- テキストエリアのサイズ調整。

## 0.5.0-SNAPSHOT - 2021-06-01
- テストを採点する。(+ (- goods bads) (quot counter 2)) じゃあんまりか。
- Fix: js/setInterval instead of js/setTimeout.
- babashka のインストールはターミナルで１行。app.melt でやってしまう。

  $ bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)

## 0.4.0 - 2021-06-1
- 間違って、initdb.d/gtypist を git に入れた。
- gtypist からテスト（シードではない）のデータを用意する。
- テストのデータをプッシュする。（改行\n)はどう扱うか？

## 0.3.2 - 2021-06-01
- [improve] bump-version.sh
- seed-users, seed-results by babashka.
- seedinb by babashka.
- [fixed] scores をレンダリングできない。
  このさい hiccup で。- スコア一覧を表示できる。

## 0.3.1 - 2021-06-01
- tp.melt.kyutech.ac.jp にデプロイ。
  ClojureScript がまだ non-release バージョンのためか、
  Shadow-cljs と接続しようとする。
- プロダクションでは docker compose わず、uberjar + postgres でいく。
  jar もコンテナと考えられないか？
  開発も emacs+cider とか、ターミナルで lein repl の方が手軽。

## 0.3.0 - 2021-06-01
- login/sign-on
- feature/db ブランチ
- debug を整理

## 0.2.0 - 2021-06-01
- auth が見せかけだけど動く。
- submit したらタイマーリセット。
- FIXME: CSRF.

## 0.1.3 - 2021-05-31
- create typing-ex.view.page ネームスペース。

## 0.1.2 - 2021-05-31
- uberjar に後付けでポートを指定するには PORT=3000 java -jar ... のように。
- feature typing-cljs.
- FIX typo: s/init\.db\.d/initdb.d/ .gitignore

## 0.1.1 - 2021-05-31
- docker compose でデータベースを永続化させてないため、
  docker up で毎回 initdb.d が走る。
  バインドマウント（macOSのファイルシステムに書き出す。）
  ボリュームマウント（Docker 内部の領域に書き出す。速度的にはこっちだろ。）
- アクセス禁止、認証必要のテスト。public でなく、handler から index を出す。
  `ex_tpyping/public/index.html` から`typing_ex/handler/index.html` に移動。

## 0.1.0 - 2021-05-31
- fix bump-version.sh: -i オプションを忘れていた。
- typing が見せかけでも動くようになったので 0.1.x のタグを打つ。
  auth に通ったら / に行き、cljs のページを表示できる。
  勇気づけのための 0.1.0. まだ typing-ex の動作はしない。

## 0.1.0-SNAPSHOT - 2021-05-30
- duct から shadow-cljs で作成したページを読めるようになった。
- docker compose
- basic routing. login/logout, /typing, score/:id, scores.
- src/core.cljs -> src/typing.cljs.
  should modify public/index.html at the same time.
- reagent 1.0.0.
  reagent/render-component(0.8.0) -> reagent.dom/render(1.0.0)
- lein new duct typing-ex +site +ataraxy +postgres
- lein new shadow-cljs typing-ex :force +reagent
- mv public/ resources/
- bump-version.sh
