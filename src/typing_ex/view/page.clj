(ns typing-ex.view.page
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   #_[clojure.string :as str]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button]]
   [java-time]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [typing-ex.plot :refer [scatter]]
   [clojure.test :as t]))

(def ^:private version "1.19.3")

(defn page [& contents]
  [::response/ok
   (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
    [:link
     {:rel "stylesheet"
      :href "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
      :integrity "sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
      :crossorigin "anonymous"}]
    [:link
     {:rel "stylesheet"
      :href "/css/style.css"}]
    [:title "Typing-Ex"]
    [:body
     [:div {:class "container"}
      contents
      [:hr]
      "hkimura, " version "."]])])

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
      [:a {:href "/rc" :class "btn btn-info btn-sm"} "RC"]
      "&nbsp;"
      [:a {:href "https://wil.melt.kyutech.ac.jp/"
           :class "btn btn-info btn-sm"}
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
           :class "btn btn-info btn-sm"}
       "L22"]
      "&nbsp;"
      [:a {:href "/logout" :class "btn btn-warning btn-sm"} "Logout"]]]
     [:div.row
      [:div.d-inline-flex
       [:a {:href "/todays" :class "btn btn-danger btn-sm"}
        "todays"]
       "&nbsp;"
       (form-to
        [:get "/recent"]
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
                       "days")
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
         "(タイプの正確さ) + (あまりの秒数)なので、理論的な最高得点は 159。"]
    (into [:ol
           (for [{:keys [max login]} max-pt]
             [:li
              max
              " "
              [:a {:href (str "/record/" login)
                   :class (if (= login user) "yes" "other")}
               login]])])]
   (headline days)))

(defn- count-ex-days
  [days login]
  ;; 引数 days の中身は、[{:login ... :date ...} ...]
  ;; (println "days:" (str days))
  (->> days
       (filter #(= (:login %) login))
       count))

(defn ex-days-page
  "ex-days: 練習日数
   user: アカウント
   days: 何日間のデータか？"
  [ex-days user days]
  (let [logins (->> ex-days (map :login) distinct)
        data (->> (for [login logins]
                    [(count-ex-days ex-days login) login])
                  (sort-by first)
                  reverse)]
    (page
     [:h2 "Typing: Last " days " days Maxes"]
     (headline days)
     [:div {:style "margin-left:1rem;"}
      [:p "「1 日 10 回未満はノーカウント」て言うと 10 回で終わる人いるからなあ。" [:br]
       "そういうスタンスじゃ上手にならんやろ。"]
      (into [:ol
             (for [[count login] data]
               [:li
                (format "(%d) " count)
                " "
                [:a {:href (str "/record/" login)
                     :class (if (= login user) "yes" "other")}
                 login]])])]
     (headline days))))

;; FIXME
(defn- ss
  "shorten string"
  [s]
  (subs (str s) 0 16))

(defn today? [ts]
  (= (java-time/local-date)
     (java-time/local-date ts)))

(defn- select-count-distinct
  "select count(distinct(timestamp::DATE)) from results
  where login='hkimura'; を clojure で。"
  [ret]
  (count (->> ret
              (map :timestamp)
              (map java-time/local-date)
              (map str)
              (group-by identity))))

;; ret is a  lazySeq. should use mapv?
(defn svg-self-records
  [login ret _me? _admin?]
  (let [positives (map #(assoc % :pt (max 0 (:pt %))) ret)
        avg (/ (reduce + (map :pt (take 10 (reverse positives)))) 10.0)
        todays (filter #(today? (:timestamp %)) ret)
        ]
    (page
     [:h2 "Typing: " login " Records"]
     [:p "付け焼き刃はもろい。毎日 10 分 x 3 セット。"]
     [:div.d-inline-flex
      [:div.px-2.mx-auto
       (scatter 300 150 (map :pt positives))
       [:br]
       [:b "TOTAL"]]
      (when (< 9 (count todays))
        [:div.px-2.mx-auto
         (scatter 300 150 (map :pt todays))
         [:br]
         [:b "TODAYS"]])
      [:div.px-2]]
     [:br]
     [:br]
     (when true ;; (or me? admin?)
       [:ul
        [:li "Max " (apply max (map :pt positives))]
        [:li "Average (last 10) " avg]
        [:li "Exercise days " (select-count-distinct ret)]
        [:li "Exercises (today/total) " (count todays) "/" (count positives)]
        [:li [:a {:href (str "/restarts-page/" login)} "Today's Go!"]]
        [:li "Last Exercise " (ss (str (:timestamp (last ret))))]])
     [:p [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]])))

;; using?
(defn active-users-page [ret]
  (page
   [:h2 "Typing: Last 40 trials"]
   [:p "最近の Typing ユーザ 40 件。連続するセッションを１つとカウントするが、
        セッションの間に別ユーザが割り込むと別セッションに。改良するか？"]
   (into [:ol]
         (for [[u & _] ret]
           [:li (ss (:timestamp u)) " " (:login u)]))))

;; view of /todays
(defn todays-act-page [ret login]
  ;;(println "todays-act-page " (str ret))
  (page
   [:h2 "Typing: Todays"]
   (headline 7)
   [:div {:style "margin-left:1rem;"}
    [:p "平常点が必要な人は見せかけじゃなく、実質的に平常から回数を重ねないと。" [:br]
     "練習したフリはタメにならない。"]
    (into [:ol]
          (for [r ret]
            [:li (ss (java-time/local-date-time (:timestamp r)))
             " "
             [:a {:href (str "/record/" (:login r))
                  :class (if (= login (:login r)) "yes" "other")}
              (:login r)]]))]
   (headline 7)))

(defn sums-page [ret user n]
  (page
   [:h2 "Typing: Last " n " days Totals"]
   (headline n)
   [:div {:style "margin-left:1rem;"}
    [:p "毎日ちょっとずつ点数アップが一番。一度にたくさんやっても身につかないよ。"]
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
   (headline n)))

(defn stat-page
  "stat は現在値が渡ってくる。
   返すべき値は [normal roll-call exam] のどれか。"
  [stat]
  ;; (println "stat " stat)
  (page
   [:h2 "Typing: Stat"]
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
    [:input.btn.btn-primary.btn-sm {:type "submit" :value "change"}])))

;; roll-call
;; FIXME: 表示で工夫するよりも、データベースに入れる時に加工するか？
(defn rc-page [ret]
  (page
   [:h2 "Typing: 出席データ(" (-> ret first :login) ")"]
   [:p "タイピングの背景が黄色い間にタイプ練習終了した時刻を記録している。"
    "タイピングのバージョンが 1.16.7 より低い時は"
    "2 週目でやった「閲覧履歴の消去」でバージョンアップしよう。"
    [:a {:href "/stat-page"} "admin only"]]
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
