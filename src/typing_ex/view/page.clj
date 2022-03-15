(ns typing-ex.view.page
  (:require
   [ataraxy.response :as response]
   [hiccup.page :refer [html5]]
   [hiccup.form :refer [form-to text-field password-field submit-button label]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [taoensso.timbre :as timbre :refer [debug]]))

(def ^:private version "1.2.1")

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
     :href "css/style.css"}]
   [:title "Typing TEST"]
   [:body
    [:div {:class "container"}
      contents
     [:hr]
     "hkimura, " version "."]])])

(defn login-page []
  (page
    [:h2 "Typing: Login"]
    (form-to
      [:post "/login"]
      (anti-forgery-field)
      (text-field {:placeholder "ニックネーム"} "nick")
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

;; FIXME: l22 アプリに飛ばそう。2022-03-11
(defn sign-on-stop []
  (page
   [:h2 "Typing"]
   [:p "新規登録はまた来年"]))

(defn sign-on-page []
  (page
   [:h2 "Typing: 登録"]
   (form-to
    [:post "/sign-on"]
    (anti-forgery-field)
    (text-field {:placeholder "学生番号"} "sid")
    (label "sid" "半角小文字。全角文字は不可。")
    [:br]
    (text-field {:placeholder "ニックネーム"} "nick")
    (label "nick" "他ユーザと違う 8 文字以内の文字列。ログインに使います。")
    [:br]
    (text-field {:placeholder "パスワード"} "password")
    (label "password" "エコーバックします。何かに記録しておく。")
    [:br]
    (submit-button "登録"))
   [:br]
   [:ul
    [:li "こういうアカウント作成のときに軽気で日本語入れる人いる。"
     "ま、少ないけど。"]
    [:li "google, facebook, instagram でやったらどんな感じになる？"
     "次週のお勉強にとっておく。"]
    [:li "メールアドレス、サーバ名(ドメイン名)とかに"
     "日本語はまだまだ通用しないじゃね？"]
    [:li "成績つかないなど、"
     "その他の不利を理解しているなら強気で行ってよい。"]
    [:li "ニックネーム変更できない時は 214 行く🦌。"]]
   [:p [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]))

(defn nickname-page [nick]
  (page
   [:h2 "Typing: Nickname"]
   [:p "ニックネームを変更すると過去データが消える。"
    [:br]
    "正確には、過去データと自分の学生番号とのつながりが切れる。"
    "復旧できない。"]
   [:p]
   [:p "現在ニックネーム： " nick]
   (form-to
    [:post "/nickname"]
    (anti-forgery-field)
    (label "new-nick" "新ニックネーム： ")
    (text-field {:placeholder "半角英数字、8文字以内"} "new-nick")
    (submit-button "change"))
   [:p [:a {:href "/"} "back"]]))

(defn password-page []
  (page
   [:h2 "Typing: Password"]
   [:p "パスワード忘れると当たり前にログインできない。"]
   (form-to
    [:post "/password"]
    (anti-forgery-field)
    (text-field {:placeholder "旧パスワード"} "old-pass")
    (text-field {:placeholder "新パスワード"} "pass")
    (submit-button "change"))
   [:p [:a {:href "/"} "back"]]))

;; see handler.core/scores
;; 7days, 30days must sync with the code.
(defn scores-page [ret nick days]
  (timbre/debug "scores-page")
  (page
   [:h2 "Typing: Scores(last " days " days)"]
   [:p
    [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]
    " "
    [:a {:href "/users" :class "btn btn-danger btn-sm"} "users"]
    [:span {:class "mmm"} " "]
    [:a {:href "http://qa.melt.kyutech.ac.jp/"
         :class "btn btn-info btn-sm"}
     "QA"]
    " "
    [:a {:href "http://ul.melt.kyutech.ac.jp/"
         :class "btn btn-info btn-sm"}
     "UL"]
    " "
    ;; [:a {:href "http://gs.melt.kyutech.ac.jp/"
    ;;      :class "btn btn-info btn-sm"}
    ;;  "GS"]
    [:a {:href "http://ex.melt.kyutech.ac.jp/"
         :class "btn btn-info btn-sm"}
     "EX"]]

   [:p "直近の " days " 日間、練習したユーザのリスト。名前をクリックするとグラフ表示。"]
   (into [:ol
          (for [{:keys [max users_nick]} ret]
            [:li
             max
             " "
             [:a {:href (str "/record/" users_nick)
                  ;; FIXME: case を書くと予想したようには動作しない。本当か？
                  :class (cond
                           (= users_nick nick) "yes"
                           (= users_nick "hkimura") "hkimura"
                           :else "other")}
              users_nick]])])
   [:p
    [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]))

(defn plot [w h coll]
  (let [n (count coll)
        dx (/ w (count coll))]
    ;;(timbre/debug "plot: " coll)
    (into
     [:svg {:width w :height h :viewbox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]
      [:line {:x1 0 :y1 (- h 100) :x2 w :y2 (- h 100) :stroke "red"}]]
     (for [[x y] (map list (range n) (map :pt coll))]
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "green"}]))))

(defn- ss [s]
  (subs (str s) 0 19))

;; 平均を求めるのに、DB 引かなくても ret から求めればいい。
(defn svg-self-records [nick ret]
  (let [rev-ret (reverse ret)
        avg (/ (reduce + (map :pt (take 10 rev-ret)))
               10.0)]
    (page
     [:h2 "Typing: " nick " records"]
     [:p "過去の最高得点よりも最近の平均が大切。" "現在値は " avg "."
         [:br]
         "付け焼き刃はもろい。毎日、10分、練習しよう。"]
     (plot 300 150 ret)
     [:p]
     [:p [:a {:href "/" :class "btn btn-primary btn-sm"} "Go!"]]
     [:h4 nick " detail"]
     [:p "detail よりも、回数、平均、最高点を表示したら？"]
     (into
      [:ol {:reversed "reversed"}]
      (for [{:keys [pt timestamp]} rev-ret]
        [:li pt ", " (ss timestamp)])))))

(defn active-users-page [ret]
  (page
   [:h2 "Typing: active users"]
   ;;(debug "active-users-page" ret)
   (into [:ol]
         (for [[u & _] ret]
           [:li (:users_nick u) " " (ss (:timestamp u))]))))
