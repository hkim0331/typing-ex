# typing-ex

## Requires
* Clojure/ClojureScript
* duct
* shadow-cljs
* yarn
* postgresql
* redis
* (docker, docker-compose)

## Unreleased
- セッション終わりに間違った単語を表示。
- 日本語例文。
- 頑張ってる人、頑張ってない人にコメントを出せる。redis? pub/sub?
- 環境変数の参照以外に develop/production を判定する方法。
  devcontainer 時、(env :tp-dev) が解決できてない。
- @app-state を廃止したい。
- drills 一括ダウンロード。
- n 回で強制的にやめさすには？
- テストモード、3回の平均で ⭕️ ❌ をつける。
- 「授業中は練習なしよ」モード
  -> 授業中の空き時間にタイプしている人もいる。
- スコアが二度提出されることがある。
- good/bad last word
- exam 記録に時刻。時刻よりも日付。
- プロダクションでは (js.console/log) を出さないの？
- want change stat by httpie, but,
```
% http -pb :3000 stat=exam minites=1
<h1>Invalid anti-forgery token</h1>
```


## v2.18-snapshot-2

- 出席取れるのは大学WiFiから。VPN 不可。
- 09-10 失敗: systemd-timer 起動せず。
- 09-10 失敗: 214 から roll-call できる。
- bump version to v2.18-snapshot-2
- redis: `stat` は説明不足。tying-ex-statくらいが適当。
- (NG) redis-cli setex stat roll-call ex 900
- (GOOD) redis-cli set stat roll-call ex 900

## v2.17.1008 / 2024-09-09

- changed systemd/typing_ex_roll_call
  start was saved on postgres,
  now is saved on redis with expiration period, 900 second.

## v2.16.1003 / 2024-09-06
- updated libraries

| :file       | :name                     | :current | :latest |
|------------ | ------------------------- | -------- | --------|
| project.clj | hato/hato                 | 0.9.0    | 1.0.0   |
|             | org.clojure/clojure       | 1.11.3   | 1.12.0  |
|             | org.postgresql/postgresql | 42.7.3   | 42.7.4  |

- change display MT -> MX according to site url change.
- npm install
- npm audit fix

## v2.15.998 / 2024-08-31
- day-by-day のスタート日...環境変数でセットするのが楽。
```
(def typing-start (or (env :tp-start) "2024-04-01"))
```

## v2.14.991 / 2024-08-24
- re-re-exam.
  データベースをバックアップし、typing_ex.results テーブルの全データをデリートした。
- update core.clj/day to take a new argument `n`.
```
(defn days
  "ユーザloginがn回以上練習した日数"
  [all login n]
  (let [ret (filter (fn [x] (= login (:login x))) all)]
    (->> ret
         (group-by :timestamp)
         (map (fn [x] (count (val x))))
         (filter #(<= n %))
         count)))
```
- core.clj/ex-days calls days as
```
(training-days 30 req db)
```

## v2.13.984 / 2024-06-20
### Changed
- redis://redis:6379 -> redis://db:6379
- use redis administrating `stat`.

## v2.12.975 / 2024-06-19
### Fixed
- systemd timer.

## v2.12.965 / 2024-06-13
- added `systemd` dir. to provide realtime timer facilities.
  to enter `roll-call` mode at 08:45 and leave back to `normal` mode at 09:00.
  Duct requires anti-forgery-token provided when POST.
  Directry change typing_ex.stat value from script invoked from systemd-timer.
- updated libraries.

| :file       | :name                             | :current | :latest |
|------------ | --------------------------------- | -------- | --------|
| project.clj | com.github.seancorfield/next.jdbc | 1.3.925  | 1.3.939 |
|             | com.taoensso/carmine              | 3.3.2    | 3.4.1   |

## v2.11.952 / 2024-06-04
- fixed something wrong char in moby-dick inside 'ago-never'.

## v2.11.945 / 2024-06-04
- enabled links to report pages again.
- set (str "login:" (mod (Integer/parseInt count) 3) pt) to redis.

## v2.10.932 / 2024-06-03
- postgresql 14.12
## v2.9.923 / 2024-05-02
- link to rp.melt in header.

## v2.9.917 / 2024-05-02
- `% git remote prune origin`
- added 5 lyrics.
- improve drills/fetch-drill

## v2.9.907 / 2024-05-01
- post "/exam"
  - resources/typing_ex/config.edn:
  [post "/exam"]
  - src/typing_ex/handler/core.clj:
  (defmethod exam!)
  - src/typing_ex/typing.cljs:
  (defn- exam-point! [login count pt])
- get "/exam/:login/:ct"

## v2.8.893 / 2024-04-29
- display end `datetime` in `/rc`.
- 小窓の font-size 12pt.

## v2.8.888 / 2024-04-29
- input alert message from /alert-form.
  when empty string is set, alert will not appear.
  the alert message is hold on redis as "alert" key.
- roll call background yellow.
- display login on RC page.

## v2.7.883 / 2024-04-29
- removed magic numbers.
  ```
  (def ^:private redis-expire 3600)
  ```

## v2.7.880 / 2024-04-29
- Courier monospace では細すぎ、monospace に戻す。
- font-size 10pt

## v2.7.876 / 2024-04-29
- redis 入りコンテナ。
- font monospace -> Courier, monospace
  Courier New は細すぎた。

## v2.6.870 / 2024-04-28
- com.taoennso/carmine 3.3.2
- core/users-all with redis
- core/login-timestamp with redis
- core/training-days
  timestamp が上手く扱えないので回避策。HTML をごっそりキャッシュする。
  タイムアウト 600 秒。

## v2.5.858 / 2024-04-26
- vscode alert.

## v2.5.849 / 2024-04-25
- (dev) (go) でエラーになることがある。lein clean で直るが。

## v2.5.848 / 2024-04-25
- today's GO を表示しない。これは例文を流すやつ対策で作ったものだった。

## v2.5.836 / 2024-04-21
- hkim0331/typing-ex:0.4.0
- apt-get -y install --no-install-recommends git npm postgresql-client
```
  environment:
    TP_DEV: true
```
- bootstrap CDN やめて、ローカルに5.2.3コピーを持つ。

## v2.4.829 / 2024-04-18
- merge に手こずった。
- "traning days" 10 回以上、トレーニングに入った日をカウント。


## 2.3-822 / 2024-04-15
- typing.cljs/pt: map -> mapv
- fix a bug: forgot {:form-params}
- rnamed fetch-display to reset-display!

## 2.2.803 / 2024-04-13
- typing.clls: display next word.
- core.clj: defined `non-empty-text`.
  delete した影響で空のdrill.textを17個作ってしまったに対応した。

## 2.1.794 / 2024-04-10
- fix: korosuke で Internal Server Error

## 2.0.781 / 2024-04-09
- dependency l22, env-var で．

- found a miss to update libaries.
  ```
  clojure -Tantq outdated :upgrade true :force true
  ```

## 1.23-778 / 2024-04-09
- タイプはじめの1文字待ち．
- 最後の1ワード
- 少数タイプでの点数
- updated libraries

| :file           | :name                               | :current | :latest |
|---------------- | ----------------------------------- | -------- | --------|
| project.clj     | cheshire/cheshire                   | 5.11.0   | 5.13.0  |
|                 | clojure.java-time/clojure.java-time | 1.3.0    | 1.4.2   |
|                 | com.github.seancorfield/next.jdbc   | 1.3.883  | 1.3.925 |
|                 | duct/core                           | 0.8.0    | 0.8.1   |
|                 | org.clojure/clojure                 | 1.11.1   | 1.11.2  |
|                 | org.postgresql/postgresql           | 42.6.0   | 42.7.3  |
| shadow-cljs.edn | cljs-http                           | 0.1.46   | 0.1.48  |

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

## 1.22.0 - 2023-09-10
- fixed: DAY BY DAY プロット
  環境変数 TP_START で集計スタート日を与えるよう変更した。
- clojure -Tantq outdated :upgrade true

| :file       | :name                               | :current | :latest |
|------------ | ----------------------------------- | -------- | --------|
| project.clj | buddy/buddy-hashers                 | 1.8.158  | 2.0.167 |
|             | clojure.java-time/clojure.java-time | 1.2.0    | 1.3.0   |
|             | com.github.seancorfield/next.jdbc   | 1.3.874  | 1.3.883 |
|             | integrant/repl                      | 0.3.2    | 0.3.3   |


## 1.21.0 - 2023-09-06
- hkim0331/typing-ex:0.3
- postgres:14.9

## 1.20.0 - 2023-08-10
- moby dick.

## 1.19.6 - 2023-06-30
- rename page.clj/svg-self-records to page.clj/display-records
- day by day plot

## 1.23.778 / 2024-04-08
- jetty port 3002
```
  :duct.server.http/jetty {:port 3002}
```
## 1.19.4 - 2023-05-29
- busy wait. wait するのはユーザのブラウザだ。

## 1.19.3 - 2023-05-29
- duct は (java.util.Date.) を local-date-time に変えてストアする？
- 前回の記録から 20 秒以内の再読み込みはアラート。

## 1.19.2 - 2023-05-28
- error-component やめて🟡を (:results @app-state)に入れる

## 1.19.1 - 2023-05-26
- Go! と reload 直後の 1 回目は記録しない。

## 1.19.0 - 2023-05-25
- created table restarts
- post /restarts
- get /restarts shows today's restarted (launched) times

## 1.18.10 - 2023-05-23
- return to /todays page
/total/7 だと、上位20位以外の人はスクロールしないと自分の行にたどりつかない。
/todays だと一番上付近に見つかる。

## 1.18.9 - 2023-05-21
- asking wil start time only

## 1.18.8 - 2023-05-20
- encourage wil

## 1.18.7 - 2023-05-19
- link to /stat-page from `/rc`. only admin can follow the link.

## 1.18.6 - 2023-05-18
- radio button /stat-page

## 1.18.5 - 2023-05-18
- /rc の表示する日時を dedupe する。

## 1.18.4 - 2023-05-16
- FIXED: plot 中で (map :pt data) は役割分担としておかしい。

## 1.18.3 - 2023-05-16
- todays, total 入れ替え

## 1.18.2 - 2023-05-16
ダミーリリース。「ブラウザの履歴をクリア」のチェック。

## 1.18.1 - 2023-05-16
- plot todays when (< 9 (count todays))
- /todays に (headline 7)

## 1.18.0 - 2023-05-16
- double plots. need adjust margin and position

## 1.17.3 - 2023-05-15
### refactor
- /daily => /todays
- /sum => /total
- /ex-days => /days
- /scores => /max
- remove /scores-no-arg
- link from /record/:login to /todays/:login

## 1.17.2 - 2023-05-15
- sum, ex-days, max に分割。

## 1.17.1 - 2023-05-14
- can see other's details

## 1.17.0 - 2023-05-14
- 回帰直線

## 1.16.9 - 2023-05-14
### Removed
- exam_mode.clj
- users.clj
- timbre
- useless println

## 1.16.8 - 2023-05-14
- headline two lines

## 1.16.7 - 2023-05-14
- メッセージの修正
- Bootstrap 5.2.3
- forget to update integrity in bootstrap

## 1.16.5 - 2023-05-14
- **bugfix** /stat は hkimura only ではいけない

## 1.16.4 - 2023-05-14
- /rc ボタンに、Clear Browsin Data... してねのメッセージ

## 1.16.3 - 2023-05-14
- /stat と /stat-page admin only に制限

## 1.16.1 - 2023-05-13
- /rc に誰の出席データかを表示

## 1.16.0 - 2023-05-13
roll-calls
- ブランチを作らず、1.15.13 の上に足してしまった。
- table stat
- boundary.stat
- style.css .stat class
- change background color in normal/roll-call/exam as white/yello/pink


## 1.15.13 - 2023-05-08
- default 7 days

## 1.15.12 - 2023-05-08
- DB のクエリを伴う関数自身が timbre/log しなくても、duct がログする。
- headline に 引数 n

## 1.15.11 - 2023-05-07
- clean up typing-ex.boundary.drills
- extracted random-id from fetch-drill, log produced random ids.

## 1.15.10 - 2023-05-07
## Fixed
- adding [integrant/repl "0.3.2"] to dependencies.
integralt/repl is only for development?

```
1.15.9: Syntax error macroexpanding at (core.clj:1:1).
Execution error (FileNotFoundException) at typing-ex.handler.core/loading (core.clj:1).
Could not locate integrant/repl/state__init.class, integrant/repl/state.clj or integrant/repl/state.cljc on classpath.
```

## 1.15.9 - 2023-05-07
- docker container yarn
- develop 時は auth? を省略
```
(defn auth? [login password]
  (or
   (= :development (:duct.core/environment system))
   ...
 ))
```

## 1.15.8 - 2023-05-05
- 合格点は 3500

## 1.15.7 - 2023-05-01
### FIXME
- core/auth? FIXME: env 以外、system をみてスイッチしたい

### Added
- WIL を headline に追加した

### Removed
- typing-ex.boundary.users を core から外した

## 1.15.6 - 2023-04-27
- /daily で、自分を赤表示する

## 1.15.5
- hkim0331/luminus:latest と postgres:14.7
  postgres:latest は 15.2 まで行っちゃって dump/restore がうまくいかないことある。

## 1.15.4 - 2023-04-25
- '7 days max' で過去7日間の練習日数を表示。今までは過去のすべての日数だった。
- postgres:15.2 でイメージをリストア後、postgres:14.7 にすると
  接続してくれない。app.melt と同じバージョンの 14.7 で restore からやり直す。
- yarn 使っているのに、コンテナの設定に入ってない。
  コンテナ立ち上げてから npm で入る。
  $ npm install --global yarn
  コンテナで動き出した。
- initdb.d から初期データをすんなり入れられなくちゃ。

## 1.15.3 - 2023-04-23
- CHANGELOG.md を 80 コラム未満で折り返した。
- D.P ボタンやめた。日付入れて total, max をクリックで。

## 1.15.2 - 2023-04-23
### Addded
- db-dumps/fetch.sh

## Changed
- db-dumps/{dump,restore}.sh: removed `-w` switch, depends on `~/.pgpass`.

## 1.15.1 - 2023-04-14
### Fixed
(:todays @app-state)'s initial value is []. not {}.

## 1.15.0 - 2023-04-14
### Fixed
- typing.cljs: (:todays @app-state) の更新を send- に移動。
  プロットの時間差を解消した。
- L22 データベースに依存を止める。
- BS で減点やめようか。
- devcontainer まだだったか？
- bug: can not record scores
- yarn watch してるのにブラウザに反映しない

### Updated
- clojure -Tantq outdated
|           :file |                               :name | :current | :latest |
| --------------- | ----------------------------------- | -------- | ------- |
|     project.clj | clojure.java-time/clojure.java-time |    1.1.0 |   1.2.0 |
|                 |   com.github.seancorfield/next.jdbc |  1.3.834 | 1.3.865 |
|                 |                 com.taoensso/timbre |    5.2.1 |   6.1.0 |
|                 |                       eftest/eftest |    0.5.9 |   0.6.0 |
|                 |           org.postgresql/postgresql |   42.5.0 |  42.6.0 |
| shadow-cljs.edn |                  binaryage/devtools |    1.0.6 |   1.0.7 |
|                 |                 com.taoensso/timbre |    5.2.1 |   6.1.0 |
|                 |                             reagent |    1.1.1 |   1.2.0 |


## 1.14.3 - 2023-04-12
- bump-version.sh calls `num install` to update package-lock.json
  is it right?

## 1.14.0 - 2022-10-15
- clj -Tantq outdated :upgrade true

## 1.14.1 - 2023-04-12
- 「1.14.0 はすでに使ってしまった」メッセージ

## 1.14.0 - 2023-04-12
- l22 api auth
- added Makefile

## 1.13.2 - 2022-08-06
- send-fetch-reset! を send- と fetch-reset! に分割した。
- ペーストを (empty? (:results @app-state)) で判定する。

## 1.13.0 - 2022-08-06
- ペーストを (< goods 10) で判定する。

## 1.23.778 / 2024-04-08
- add more examples(drills)
- fix asnync get

## 1.12.0 - 2022-07-22
- dialog debug: typings.cljs/show-score に pt 渡しているところ変えないと、
  ダイアログで pt をどう計算したかを表示できない。
- atom points-debug を導入。2番目の alert ダイアログの最終行に、
  (str @points-debug) "=>" pt) を入れた。
- ハートブレークを赤丸に戻す。
- deploy.sh で yarn, lein uberjar を呼ばない。

## 1.11.1
- グラフは見せるが、詳細は隠す。FK06
- fixed: hkimura でログインしても詳細が見えない。
- fixed: ユーザが自分の詳細は見える。

## 1.11.0 - 2022-07-19
- FK06 の意見を取り入れ、各ユーザの詳細は見せないが、グラフは見せる。

## 1.10.2 - 2022-06-15
- 「Python tutorial、やってるか」のメッセージ

## 1.10.1 - 2022-06-14
- read week-09
- forget `yarn release`.
  should update the deploy script.

## 1.9.1 - 2022-06-08
- bump-version.sh updates js version number in `index.html`.

## 1.23.778 / 2024-04-08
中間試験モード

## 1.8.4
- "レポート進んでいるか🐥"

## 1.8.3 - 2022-05-25
- rp へのリンク追加

## 1.8.2 - 2022-05-17
code polish up
- 開始直後の「タイプ忘れた？」を表示しない

## 1.8.1 - 2022-05-16
- results も confirm に表示。

## 1.8.0 - 2022-05-16
- confirm cancel でタイプデータの表示（をしてから練習に復帰）。

## 1.7.4 - 2022-05-15
- 正しく bar-chart

## 1.7.3 - 2022-05-12
### Hotfix
- go で並列性が入った時、直感と違う動きをしてバグった時の修正が難しい。
  re-frame できるようになったら改善するか？
  それとも cljs-http を clj-ajax に取り替えてみるか？

## 1.7.2
### Enbug
- timeout 6 sec
- counter does not stop at 0

## 1.23.778 / 2024-04-08
- go ブロックは呼んだ順に実行されるとは限らない。
- reset! を swap! に戻す。
- defonce を１つに。除く setInterval.

## 1.7.0 - 2022-05-12
code polish up. not improved.

## 1.6.5 - 2022-05-11
- reagent/reagent-dom 18.0.0
- package.json 中の "version": を bump-version.sh でケアする。
- 素点をログ。cljs 側では login 名がわからない。token として埋め込むか？
- react 17 に戻した。
  ReactDOM.render is no longer supported in React 18. Use createRoot instead.
  Until you switch to the new API, your app will behave as if it’s running
   React 17. Learn more: https://reactjs.org/link/switch-to-createroot
- build.sh
  export TAOENSSO_TIMBRE_NS_PATTERN_EDN=
   '{:deny #{"duct.database.sql.hikaricp" "duct.middleware.web"}}'
- reset-todays! を独立させた

## 1.6.4 - 2022-05-07
## 1.23.778 / 2024-04-08

## 1.6.3 - 2022-05-07
- 時々、「今日の練習グラフ」がアップデートされない。`get` の順番で改善できるか？
- next.jdbc 1.2.780
- lein-duct 0.12.3
- postgresql 42.3.5
- devtools 1.0.6
- plot.cljs DRY!

## 1.6.1 - 2022-05-04
### Added
- インタラクティブフィードバック、単語ごとに。
  (if (= target typed) "🟢" "🔴")
- タイプ終わりを自動判定。

## 1.5.10 - 2022-05-03
### Changed
- 過去グラフを bar-graph から scatter-gram に変更。本日グラフは bar graph のまま。

## 1.5.9 - 2022-05-03
### Added
- 60 点に青線
- 30 点に緑線

## 1.5.8 - 2022-05-03
### Added
- D.P. に exercise days を表示する。

## 1.5.7 - 2022-05-03
### Changed
- typing.cljs send-score から reset-app-state! を分離した。
- デバッグ用に (def timeout 6).
- bump-version 時に (def timeout 60) に書き換える。

## 1.5.6 - 2022-05-02
- display todays/total tp counts.

## 1.5.5 - 2022-05-02
### Changed
- n days [max] リターン打つのが面倒だ。ボタンに。

## 1.5.4 - 2022-04-28
- typing からの戻りを /scores から /sum/1 に変更した。
- 1.5.2 で消えたはずの warning が再び。
  #'medley.core/abs, #'io.aviso.exception/update-keys, #'taoensso.encore/abs

## 1.5.3 - 2022-04-26
- users からも /record/login をリンク
- localhost の開発用 typint-ex パスワードをアップデート

## 1.5.2 - 2022-04-26
### Fixed
- [com.taoensso.timbre "5.2.1"] で abs の warning が止まった。
  #'io.aviso.exception/update-keys の warning が止まったのは謎。
  まだ #'medley.core/abs が残っている。
- [Go!] が左にはみ出ているのを margin-left:1rem で無理やり修正。
### Changed
- page description

## 1.5.1 - 2022-04-25
### Changed
- resume todays button(red)

## 1.5.0 - 2022-04-25
リスタートコード整理。

## 1.4.6 - 2022-04-25
### Fixed
- page.clj インデントがずれて、{ } がずれた。fixed.
- sum/1 と /daily のユーザが2倍違う。24時間以内と日付が同じとどっち？
  => 本日＋昨日のタイピングポイントを表示する。さすれば、日付が代わっても
     練習しているユーザは表示が0 点にならない。

## 1.4.4 - 2022-04-25
### Added, code polish up
- page/headline

## 1.4.3 - 2022-04-24
### Added
- 最近 n 日のスコアの合計を /sum/n で。
### Changed
- (text-field {:style "text-align:right"})

## 1.4.2 - 2022-04-24
### Removed
- /scores のヘッダから last40 を削除。QA に 1.4.2 のアナウンス。

## 1.4.1 - 2022-04-24
### Added
- /recent URL をいじらずに、フォームから n-days を入力。
  綺麗じゃないコード。get /recent を追加した。

## 1.4.0 - 2022-04-23
- /scores-200 をやめ、/scores/:days を作った。

## 1.3.12 - 2022-04-22
### Added
- テキストエリア id="drill" にカーソルを入れる。

## 1.3.11 - 2022-04-21
### Changed
- first-key をやめる。これは元の動作でもある。
  カウンターがスタートする前に例文をゆっくりコピーする手がある。

## 1.3.10 - 2022-04-21
### Added
- typing-ex/plot.cljc 初めての cljc
### Changed
- (resume) 10回おきに「休憩すれば？」をダイアログ表示する。

## 1.3.9 - 2022-04-20
### Changed
- マイナスを記録しない。

## 1.3.8 - 2022-04-19
- 説明文修正。「直近 7 日間のスコア順リスト。カッコは通算練習日数。」

## 1.3.7 - 2022-04-17
### hotfix
- todays に auth がかかってない？

## 1.3.6 - 2022-04-17
### Addded
- todays trials as `todays`
### Changed
- rename button `trials` to `last40`

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

## 1.23.778 / 2024-04-08
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

## 0.9.14
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

## 1.23.778 / 2024-04-08
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

## 1.23.778 / 2024-04-08
- テストを採点する。(+ (- goods bads) (quot counter 2)) じゃあんまりか。
- Fix: js/setInterval instead of js/setTimeout.
- babashka のインストールはターミナルで１行。app.melt でやってしまう。

  $ bash <
    <(curl
      -s
      https://raw.githubusercontent.com/babashka/babashka/master/install)

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
