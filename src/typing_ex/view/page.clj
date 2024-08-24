(ns typing-ex.view.page
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   #_[clojure.string :as str]
   [environ.core :refer [env]]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button]]
   [java-time.api :as jt]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [typing-ex.plot :refer [scatter]]
   #_[clojure.test :as t]))

(def ^:private version "v2.14.991")

;--------------------------------
(defn- ss
  "shorten string"
  [s]
  (subs (str s) 0 16))

(defn- db-aux [from to ret]
  (if (pos? (.compareTo from to))
    ret
    (db-aux (-> (jt/plus (jt/local-date from) (jt/days 1)) str)
            to
            (conj ret from))))

(defn days-between [from to]
  (db-aux from to []))

;--------------------------------

(defn page [& contents]
  [::response/ok
   (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
    [:link
     {:rel "stylesheet"
      :href "/css/bootstrap.min.css"
      :type "text/css"}]
    [:link
     {:rel "stylesheet"
      :href "/css/style.css"}]
    [:title "Typing-Ex"]
    [:body
     [:div {:class "container"}
      contents
      [:hr]
      "hkimura, " version "."]])])

(defn alert-form [_]
  (page
   [:h2 "Typing: Alert"]
   (form-to
    [:post "/alert"]
    (anti-forgery-field)
    (text-field {:placeholder "alert" :size 70} "alert")
    [:br]
    (submit-button  "set"))))

(defn login-page [req]
  (page
   [:h2 "Typing: Login"]
   [:div.text-danger (:flash req)]
   (form-to
    [:post "/login"]
    (anti-forgery-field)
    (text-field     {:placeholder "アカウント"} "login")
    (password-field {:placeholder "パスワード"} "password")
    (submit-button  "login"))
   [:br]
   [:ul
    [:li "焦らず、ゆっくり、正しい指使いがタイピングが上達の早道。"]
    [:li "10 分練習したら休憩入れよう。"]
    [:li "練習しないと平常点にならない。"]]))

(defn- headline
  "scores-page の上下から呼ぶ。ボタンの並び。他ページで使ってもよい。"
  [n]
  [:div {:style "margin-left:1rem;"}
   [:div.row
    [:div.d-inline-
     [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
     "&nbsp;"
     ;;py99
    ;;  [:a {:href "https://py99.melt.kyutech.ac.jp/"
    ;;       :class "btn btn-info btn-sm"}
    ;;   "Py99"]
    ;;  "&nbsp;"
     [:a {:href "/rc" :class "btn roll-call btn-sm"} "RC"]
     "&nbsp;"
     [:a {:href "https://wil.melt.kyutech.ac.jp/"
          :class "btn btn-success btn-sm"}
      "WIL"]
     "&nbsp;"
     [:a {:href "http://qa.melt.kyutech.ac.jp/"
          :class "btn btn-info btn-sm"}
      "QA"]
     "&nbsp;"
     [:a {:href "http://mt.melt.kyutech.ac.jp/"
          :class "btn btn-info btn-sm"}
      "MT"]
     "&nbsp;"
     [:a {:href "http://l22.melt.kyutech.ac.jp/"
          :class "btn btn-success btn-sm"}
      "L22"]
     "&nbsp;"
     [:a {:href "http://rp.melt.kyutech.ac.jp/"
          :class "btn btn-success btn-sm"}
      "RP"]
     "&nbsp;"
     [:a {:href "/logout" :class "btn btn-warning btn-sm"} "Logout"]]]
   [:div.row
    [:div.d-inline-flex
     [:a {:href "/todays" :class "btn btn-danger btn-sm"}
      "todays"]
     "&nbsp;"
     (form-to
      [:get "/recent"]
      (submit-button {:class "btn btn-primary btn-sm"
                      :name "kind"}
                     "training days")
      "&nbsp;"
      (text-field {:size 2
                   :value n
                   :style "text-align:right"}
                  "n")
      " days → "
      (submit-button {:class "btn btn-primary btn-sm"
                      :name "kind"}
                     "total")
      "&nbsp;"
      (submit-button {:class "btn btn-primary btn-sm"
                      :name "kind"}
                     "max"))]]])

(defn scores-page
  "maxpt: 最高点
   ex-days: 練習日数
   user: アカウント
   days: 何日間のデータか？"
  [max-pt _ex-days user days]
  (page
   [:h2 "Typing: Last " days " days Maxes"]
   (headline days)
   [:div {:style "margin-left:1rem;"}
    [:p "瞬間最大風速。" [:br]
     "[正確さ] + [残し秒数] + [ボーナス] でプログラム上の最高点は 169。"]
    (into [:ol
           (for [{:keys [max login]} max-pt]
             [:li
              max
              " "
              [:a {:href (str "/record/" login)
                   :class (if (= login user) "yes" "other")}
               login]])])]))

(defn- count-ex-days
  [days login]
  ;; 引数 days の中身は、[{:login ... :date ...} ...]
  ;; (println "days:" (str days))
  (->> days
       (filter #(= (:login %) login))
       count))

(defn ex-days-page
  "self はログインアカウント、
   data はソーティング済みの[[login days] ...]"
  [self data]
  (page
   [:h2 "Typing: 30 回以上練習した日数"]
   (headline 7)
   [:div {:style "margin-left:1rem;"}
    [:p "毎日ちょっとずつが伸びる秘訣。"]
    (into [:ol
           (for [[login n] data]
             [:li
              (format "(%d) " n)
              " "
              [:a {:href (str "/record/" login)
                   :class (if (= login self) "yes" "other")}
               login]])])]))

(defn- select-count-distinct
  "select count(distinct(timestamp::DATE)) from results
  where login='hkimura'; を clojure で。"
  [ret]
  (count (->> ret
              (map :timestamp)
              (map jt/local-date)
              (map str)
              (group-by identity))))

(defn- today? [ts]
  (= (jt/local-date)
     (jt/local-date ts)))

(defn- average [coll]
  (/ (reduce + coll) (count coll)))

;; データがない日もあるので、from は外から与えないといけない。
(defn- average-day-by-day
  [from to scores]
  ;; (prn "page/averagge-day-by-day" from to (str scores))
  (let [averages (->> scores
                      (map (fn [x] [(:pt x) (subs (str (:timestamp x)) 0 10)]))
                      (group-by second)
                      (map (fn [x] [(key x) (int (average (map first (val x))))]))
                      (into {}))]
    (map #(averages % 0) (days-between from to))))

(defn display-records
  [login scores _me? _admin?]
  (let [avg (/ (reduce + (map :pt (take 10 (reverse scores)))) 10.0)
        todays (filter #(today? (:timestamp %)) scores)]
    (page
     [:h2 "Typing: " login " Records"]
     [:p "付け焼き刃はもろい。毎日 10 分 x 3 セット。"
      [:br]
      "TOTAL は全スコア、TODAYS は本日分（10回以上練習）、
          DAY BY DAY は一日平均。"]
     ;; start date
     [:div.d-inline-flex
      [:div.px-2.mx-auto
       (scatter 300 150 (map :pt scores))
       [:br]
       [:b "TOTAL"]]
      (when (< 9 (count todays))
        [:div.px-2.mx-auto
         (scatter 300 150 (map :pt todays))
         [:br]
         [:b "TODAYS"]])
      [:div.px-2]]

     ;; 最初の日から今日までの日付を横軸とするグラフを（別に）書く。
     ;; start date を合わせなくちゃ。
     ;; 欠測の日もあるので、scores からは start-day を出せない。
     [:div.px-2
      (scatter 300 150 (average-day-by-day
                        (or (env :tp-start) "2023-04-01")
                        (str (jt/local-date))
                        scores))
      [:br]
      [:b "DAY BY DAY"]]
     [:br]
     [:br]
     (when true ;; (or me? admin?)
       [:ul
        [:li "Max " (apply max (map :pt scores))]
        [:li "Average (last 10) " avg]
        [:li "Exercise days " (select-count-distinct scores)]
        [:li "Exercises (today/total) " (count todays) "/" (count scores)]
        ;; [:li [:a {:href (str "/restarts-page/" login)} "Today's Go!"]]
        [:li "Last Exercise " (ss (str (:timestamp (last scores))))]])
     [:p [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]])))

;; use in core.clj.
(defn active-users-page [ret]
  (page
   [:h2 "Typing: Last 40 trials"]
   [:p "最近の Typing ユーザ 40 件。連続するセッションを１つとカウントするが、
        セッションの間に別ユーザが割り込むと別セッションに。改良するか？"]
   (into [:ol]
         (for [[u & _] ret]
           [:li (ss (:timestamp u)) " " (:login u)]))))

(defn- todays-msg
  []
  (let [msg ["好き嫌い言わずになんでも食べるのが健康の元だ。"
             "一日一回、QAも見るんだぞ。答えられる Q には A をつけよう。"
             "ガンバッてる人とそうでない人と、割れてきたように見えないか？"]]
    (get msg (rand-int (count msg)))))

;; view of /todays
(defn todays-act-page [ret login]
  ;;(println "todays-act-page " (str ret))
  (page
   [:h2 "Typing: Todays"]
   (headline 7)
   [:div {:style "margin-left:1rem;"}
    [:p (todays-msg)]
    (into [:ol]
          (for [r ret]
            [:li (ss (jt/local-date-time (:timestamp r)))
             " "
             [:a {:href (str "/record/" (:login r))
                  :class (if (= login (:login r)) "yes" "other")}
              (:login r)]
             "&nbsp;"
             ;; 2024-05-12, need VPN
             [:a {:href (str "https://hp.melt.kyutech.ac.jp/" (:login r))}
              "(RP)"]]))]))

(defn sums-page [ret user n]
  (page
   [:h2 "Typing: Last " n " days Totals"]
   (headline n)
   [:div {:style "margin-left:1rem;"}
    [:p "まとめてやっても平常点にはならない。平常点は平常につく。当たり前。"]
    (into [:ol]
          (for [r ret]
            (let [login (:login r)
                  sum (:sum r)]
              (when (< -1 sum)
                [:li sum
                 " "
                 [:a {:href (str "/record/" login)
                      :class (if (= user login) "yes" "other")}
                  login]]))))]
   ;; [:p "from " (env :tp-start)]
   ;;(headline n)
   ))

(defn stat-page
  "stat は現在値が渡ってくる。
   返すべき値は [normal roll-call exam] のどれか。"
  [stat]
  ;; (println "stat " stat)
  (page
   [:h2 "Typing: Stat (Redis)"]
   (form-to
    [:post "/stat"]
    (anti-forgery-field)
    (for [val ["normal" "roll-call" "exam"]]
      [:div
       [:input
        (if (= stat val)
          {:type "radio" :name "stat" :value val :checked "checked"}
          {:type "radio" :name "stat" :value val})
        val]])
    [:input {:name "minutes" :value "15"}]
    [:input.btn.btn-primary.btn-sm {:type "submit" :value "change"}])))

;; roll-call
;; FIXME: 表示で工夫するよりも、データベースに入れる時に加工するか？
(defn rc-page [ret login]
  (page
   [:h2 "Typing: 出席データ(" login ")"]
   [:p "タイピングの背景が黄色い間にタイプ練習終了した時刻を記録している。"
    "タイピングのバージョンが 1.16.7 より低い時は"
    "「閲覧履歴の消去」でバージョンアップしよう。"
    [:a {:href "/stat-page"} "[admin only]"]]
   [:ul {:class "roll-call"}
    (for [r ret]
      [:li r])]))

(defn restarts-page [_login ret]
  (page
   [:h2 "Typing: Today's Go!"]
   [:p "苦手を流しちゃ練習にならんやろ。"]
   [:ol
    (for [r ret]
      [:li (:login r) " " (:created_at r)])]))
