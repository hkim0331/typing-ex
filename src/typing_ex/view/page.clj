(ns typing-ex.view.page
  (:require
   [ataraxy.response :as response]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button label]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [taoensso.timbre :as timbre]))

(def ^:private version "1.3.4")

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

(defn scores-page [ret user days ex-days]
  ;;(timbre/debug ex-days)
  (page
   [:h2 "Typing: Scores (last " days " days)"]
   [:p
    [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]
    " "
    [:a {:href "/trials" :class "btn btn-danger btn-sm"} "trials"]
    [:span {:class "mmm"} " "]
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
     "L22"]]

   [:p "直近の " days " 日間に練習したユーザのリスト。スコア順。カッコは練習日数。"]
   (into [:ol
          (for [{:keys [max login]} ret]
            [:li
             max
             "("
             (count-ex-days ex-days login)
             ") "
             [:a {:href (str "/record/" login)
                  :class (cond
                           (= login user) "yes"
                           :else "other")}
              login]])])
   [:p
    [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]))

(defn plot [w h coll]
  (let [n (count coll)
        dx (/ w (count coll))]
    (into
     [:svg {:width w :height h :viewbox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]
      [:line {:x1 0 :y1 (- h 110) :x2 w :y2 (- h 110) :stroke "red"}]]
     (for [[x y] (map list (range n) (map :pt coll))]
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "green"}])))) ;; was green

;; not good
(defn- ss [s]
  (subs (str s) 0 19))

;; 平均を求めるのに、DB 引かなくても ret から求めればいい。
;; ret は lazySeq
(defn svg-self-records [login ret]
  (let [positives (map #(assoc % :pt (max 0 (:pt %))) ret)
        avg (/ (reduce + (map :pt (take 10 (reverse positives)))) 10.0)]
    (page
     [:h2 "Typing: " login " records"]
     [:p "付け焼き刃はもろい。毎日、10分、練習しよう。"]
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
   [:h2 "Typing: last trials"]
   (into [:ol]
         (for [[u & _] ret]
           [:li (:login u) " " (ss (:timestamp u))]))))
