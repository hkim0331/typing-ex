(ns typing-ex.view.page
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button]]
   [java-time]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   #_[taoensso.timbre :as timbre]
   [typing-ex.plot :refer [scatter]]))

(def ^:private version "1.16.0")

(defn page [& contents]
  [::response/ok
   (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
    [:link
     {:rel "stylesheet"
      :href "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"
      :integrity "sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk"
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
    [:li "焦らず、ゆっくり、正確にタイピングが上達の早道。"]
    [:li "10 分練習したら休憩入れよう。"]
    [:li "練習しないと平常点にならない。"]]))

;; right place, here?
(defn- count-ex-days [days login]
  (->> days
       (filter #(= (:login %) login))
       count))

(defn- headline
  "scores-page の上下から呼ぶ。ボタンの並び。他ページで使ってもよい。"
  [n]
  [:div {:style "margin-left:1rem;"}
    [:div.row
     [:div.d-inline
      [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
      " "
      #_[:a {:href "/sum/7" :class "btn btn-primary btn-sm"} "D.P."]]
     "&nbsp;|&nbsp;"
     [:div.d-inline
      (form-to
       [:get "/recent"]
       (text-field {:size 2
                    :value n
                    :style "text-align:right"}
                   "n")
       "days "
       (submit-button {:class "btn btn-primary btn-sm"
                       :name "total"}
                      "total")
       " "
       (submit-button {:class "btn btn-primary btn-sm"
                       :name "max"}
                      "max"))]
     "&nbsp;|&nbsp;"
     [:div.d-inline
      [:a {:href "/daily" :class "btn btn-danger btn-sm"}
       "Today"]
      " "
      [:a {:href "https://wil.melt.kyutech.ac.jp/"
           :class "btn btn-info btn-sm"}
       "WIL"]
      " "
      [:a {:href "http://qa.melt.kyutech.ac.jp/"
           :class "btn btn-info btn-sm"}
       "QA"]
      " "
      [:a {:href "http://mt.melt.kyutech.ac.jp/"
           :class "btn btn-info btn-sm"}
       "MT"]
      " "
      [:a {:href "http://l22.melt.kyutech.ac.jp/"
           :class "btn btn-info btn-sm"}
       "L22"]
      " "
      [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]]])

(defn scores-page [max-pt ex-days user days]
  (page
   [:h2 "Typing: Last " days " days Maxes"]
   (headline days)
   #_[:p "直近 " days " 日間のユーザ毎最高得点。カッコは通算練習日数。<br>
情報リテラシー以外の科目も大切にしよう。"]
   [:p "スコア伸びないのは練習足りない？ 情報リテラシー以外の科目も大切に。"]
   (into [:ol
          (for [{:keys [max login]} max-pt]
            [:li
             max
             (format "(%d) " (count-ex-days ex-days login))
             [:a {:href (str "/record/" login)
                  :class (if (= login user) "yes" "other")}
              login]])])
   (headline days)))

;; not good
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

;; 平均を求めるのに、DB 引かなくても ret から求めればいい。
;; ret は lazySeq
;; 1.5.8 Exercise days
(defn svg-self-records [login ret me? admin?]
  (let [positives (map #(assoc % :pt (max 0 (:pt %))) ret)
        avg (/ (reduce + (map :pt (take 10 (reverse positives)))) 10.0)
        todays (filter #(today? (:timestamp %)) ret)]
    (page
     [:h2 "Typing: " login " records"]
     [:p "付け焼き刃はもろい。毎日 10 分 x 3 セット。"]
     [:div (scatter 300 150 positives)]
     [:br]
     (when (or me? admin?)
       [:ul
        [:li "Max " (apply max (map :pt positives))]
        [:li "Average (last 10) " avg]
        [:li "Exercise days " (select-count-distinct ret)]
        [:li "Exercises (today/total) " (count todays) "/" (count positives)]
        [:li "Last Exercise " (ss (str (:timestamp (last ret))))]])
     [:p [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]])))

(defn active-users-page [ret]
  (page
   [:h2 "Typing: Last 40 trials"]
   [:p "最近の Typing ユーザ 40 件。連続するセッションを１つとカウントするが、
        セッションの間に別ユーザが割り込むと別セッションに。改良するか？"]
   (into [:ol]
         (for [[u & _] ret]
           [:li (ss (:timestamp u)) " " (:login u)]))))

(defn todays-act-page [ret login]
  (page
   [:h2 "Typing: todays"]
   [:p "本日の Typing ユーザ。重複を省いて最終利用時間で並べ替え。"]
   (into [:ol]
         (for [r ret]
           [:li (ss (java-time/local-date-time (:timestamp r)))
            " "
            [:a {:href (str "/record/" (:login r))
                 :class (if (= login (:login r)) "yes" "other")}
             (:login r)]]))))

(defn sums-page [ret user n]
  (page
   [:h2 "Typing: Daily Points"]
   (headline n)
   [:p "7 days で 3000 点が合格ラインだったのは先週。今週は 3500 くらいか。"]
   (into [:ol]
         (for [r ret]
           (let [login (:login r)]
             [:li (:sum r)
              " "
              [:a {:href (str "/record/" login)
                   :class (if (= user login) "yes" "other")}
               login]])))
   (headline n)))

(defn stat-page [stat]
  (page
   [:h2 "Typing: Stat"]
   (form-to
    [:post "/stat"]
    (anti-forgery-field)
    [:input {:name "stat" :placeholder stat}]
    [:p "normal, roll-call, exam"]
    [:input {:type "submit"}])))
