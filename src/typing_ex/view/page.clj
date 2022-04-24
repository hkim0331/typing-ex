(ns typing-ex.view.page
  (:require
   [ataraxy.response :as response]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button label]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [taoensso.timbre :as timbre]
   [typing-ex.plot :refer [plot]]))

(def ^:private version "1.4.2")

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
    (text-field {:placeholder "アカウント"} "login")
    (password-field {:placeholder "パスワード"} "password")
    (submit-button "login"))
   [:br]
   [:ul
    [:li "ログイン後、スコア一覧に飛ぶ。"
     "スコア一覧上下二箇所の Go! のいずれかからチャレンジ開始。"]
    [:li "コンスタントに練習しないと成績は落ちる。"]
    [:li "やらない人の「できません」はいただけない。"
     "成績に影響しない欠席ひとつに神経質になるより、"
     "しっかりタイピング平常点稼いだ方が建設的。"]]))

(defn- count-ex-days [days login]
  (->> days
       (filter #(= (:login %) login))
       count))

(defn scores-page [max-pt ex-days user days]
  ;;(timbre/debug ex-days)
  (page
   [:h2 "Typing: Last " days " days scores"]
   [:p
    [:div.row
     [:div.d-inline
      [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
      " "
      [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]
      " "
      #_[:a {:href "/trials" :class "btn btn-danger btn-sm"} "last40"]
      " "
      [:a {:href "/daily" :class "btn btn-danger btn-sm"} "todays"]
      " last "]
     [:div.d-inline
      (form-to [:get "/recent"]
               (text-field {:size 2
                            :value "7"
                            :style "text-align:right"} "n"))]
     [:div.d-inline
      " days, "
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
       "L22"]]]]
   [:p "直近 " days " 日間のスコア順リスト。カッコは通算練習日数。"]
   (into [:ol
          (for [{:keys [max login]} max-pt]
            [:li
             max
             (format "(%d) " (count-ex-days ex-days login))
             [:a {:href (str "/record/" login)
                  :class (cond
                           (= login user) "yes"
                           :else "other")}
              login]])])
   [:p
    [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]))

;; not good
(defn- ss [s]
  (subs (str s) 0 16))

;; 平均を求めるのに、DB 引かなくても ret から求めればいい。
;; ret は lazySeq
(defn svg-self-records [login ret]
  (let [positives (map #(assoc % :pt (max 0 (:pt %))) ret)
        avg (/ (reduce + (map :pt (take 10 (reverse positives)))) 10.0)]
    (page
     [:h2 "Typing: " login " records"]
     [:p "付け焼き刃はもろい。毎日、10 分、練習しよう。"]
     [:div (plot 300 150 positives)]
     [:br]
     [:ul
      [:li "Max " (apply max (map :pt positives))]
      [:li "Average (last 10) " avg]
      [:li "Exercises " (count positives)]
      [:li "Last Exercise " (ss (str (:timestamp (last ret))))]]
     [:p [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]])))

(defn active-users-page [ret]
  (page
   [:h2 "Typing: Last 40 trials"]
   [:p "最近の Typing ユーザ 40 件。連続するセッションを１つとカウントするが、
        セッションの間に別ユーザが割り込むと別セッションに。改良するか？"]
   (into [:ol]
         (for [[u & _] ret]
           [:li (ss (:timestamp u)) " " (:login u)]))))

(defn todays-act-page [ret]
  ;;(timbre/debug ret)
  (page
   [:h2 "Typing: todays"]
   [:p "本日の Typing ユーザ。重複を省いて最終利用時間で並べ替え。"]
   (into [:ol]
         (for [r ret]
           [:li (ss (:timestamp r)) " " (:login r)]))))