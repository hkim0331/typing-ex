(ns typing-ex.handler.core
  (:require
   ;;[ataraxy.core :as ataraxy]
   [ataraxy.response :as response]
   [clojure.java.io :as io]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.users   :as users]
   [typing-ex.boundary.results :as results]
   ;; FIXME: refer
   [typing-ex.view.page :as view]
   [integrant.core :as ig]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(def DAYS 30)

;; FIXME: データベースに持っていこ。
(defn admin? [s]
  (let [admins #{"hkimura" "ayako" "nick888"}]
    ;;(debug "admin?" s)
    (get admins s)))

(defn get-nick
  "request ヘッダの id 情報を文字列で返す"
  [req]
  (name (get-in req [:session :identity])))

;; login
(defmethod ig/init-key :typing-ex.handler.core/login [_ _]
  (fn [_]
    (view/login-page)))

(defn auth? [db nick password]
  (let [ret (users/find-user-by-nick db nick)]
    (and (some? ret) (= (:password ret) password))))

(defmethod ig/init-key :typing-ex.handler.core/login-post [_ {:keys [db]}]
  (fn [{[_ {:strs [nick password]}] :ataraxy/result}]
    (timbre/debug "/login-post" nick)
    (if (and (seq nick) (auth? db nick password))
      (-> (redirect "/scores")
          (assoc-in [:session :identity] (keyword nick)))
      [::response/found "/login"])))

(defmethod ig/init-key :typing-ex.handler.core/logout [_ _]
  (fn [_]
    (-> (redirect "/login")
        (assoc :session {}))))

(defmethod ig/init-key :typing-ex.handler.core/sign-on [_ _]
  (fn [_]
    (timbre/debug "/sign-on")
    ;; Changed: 新規登録はまた来年。
    ;;(sign-on-page)
    (view/sign-on-stop)))

(defmethod ig/init-key :typing-ex.handler.core/sign-on-post [_ {:keys [db]}]
  (fn [{{:strs [sid nick password]} :form-params}]
    (let [user {:sid sid :nick nick :password password}]
      (if (users/insert-user db user)
        [::response/found "/login"]
        [::response/found "/sign-on"]))))

;; index. anti-forgery-field を埋め込むために。
(defmethod ig/init-key :typing-ex.handler.core/typing [_ _]
  (fn [_]
    [::response/ok
     (str
      "<!DOCTYPE html>
<html>
  <head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1'>
    <link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css' integrity='sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2' crossorigin='anonymous'>
    <link href='css/style.css' rel='stylesheet' type='text/css'>
    <link rel='icon' href='https://clojurescript.org/images/cljs-logo-icon-32.png'>
  </head>
  <body>"
      (anti-forgery-field)
      "<div class='container'>
    <div id='app'>
      Shadow-cljs rocks!
    </div>
    <script src='js/compiled/main.js' type='text/javascript'></script>
    <script>typing_ex.typing.init();</script>
    </div>
  </body>
</html>")]))

;; works!
(defmethod ig/init-key :typing-ex.handler.core/score-post [_ {:keys [db]}]
  (fn [{{:strs [pt]} :form-params :as req}]
    (let [nick (get-nick req)
          rcv {:pt (Integer/parseInt pt) :login nick}]
      (timbre/debug "/score-post" rcv)
      (results/insert-pt db rcv)
      [::response/ok (str rcv)])))

(defmethod ig/init-key :typing-ex.handler.core/scores [_ {:keys [db]}]
  (fn [req]
    ;; 30 days max.
    ;; must fix view/page.clj at the same time.
    (let [ret (results/find-max-pt db DAYS)
          nick (get-nick req)]
      (view/scores-page ret nick DAYS))))

(defmethod ig/init-key :typing-ex.handler.core/drill [_ {:keys [db]}]
  (fn [_]
    (let [ret (drills/fetch-drill db)]
      [::response/ok ret])))

;; FIXME: 関数の役目がわからない。特に Admin Only とか。
(defmethod ig/init-key :typing-ex.handler.core/record [_ {:keys [db]}]
  (fn [{[_ nick] :ataraxy/result :as req}]
    ;; この if は何をしてるか？ 自分のレコードしか見せないってこと？
    ;; ログインしているユーザが/record/nick と一致するかってこと？
    (if (or (= nick "hkimura")
            (= nick (get-nick req))
            (admin? (get-nick req)))
      (let [ret (results/fetch-records db nick)]
        ;;(self-records-page nick ret)
        (view/svg-self-records nick ret))
      [::response/forbidden "<h2>Admin Only</h2>"])))

(defmethod ig/init-key :typing-ex.handler.core/ban-index [_ _]
  (fn [_]
    [::response/forbidden "access not allowed"]))

(defmethod ig/init-key :typing-ex.handler.core/nickname [_ _]
  (fn [req]
    (let [nick (get-nick req)]
      (view/nickname-page nick))))

;; nick は foreign key のため、アップデートで変更できない。
;; https://takeokunn.xyz/blog/post/postgresql-remove-foreign-key-constraint
;; テーブルから foreign key 制約を外した。外道の対応だ。
;; 今後、foreign key は将来的に変更の可能性がないフィールドに付与しよう。
(defmethod ig/init-key :typing-ex.handler.core/nickname-post [_ {:keys [db]}]
  (fn [{[_ {:strs [new-nick]}] :ataraxy/result :as req}]
    (let [nick (get-nick req)
          user (users/find-user-by-nick db nick)]
      (if (users/update-user db {:nick new-nick} (:id user))
        [::response/found "/login"]
        [::response/forbidden "something wrong happened"]))))

(defmethod ig/init-key :typing-ex.handler.core/password [_ _]
  (fn [_]
    (view/password-page)))

(defmethod ig/init-key :typing-ex.handler.core/password-post [_ {:keys [db]}]
  (fn [{[_ {:strs [old-pass pass]}] :ataraxy/result :as req}]
    (let [nick (get-nick req)
          user (users/find-user-by-nick db nick)]
      ;;(debug "user" user)
      ;;(debug "old-pass" old-pass "pass" pass)
      (if (= old-pass (:password user))
        (do
          (users/update-user db {:password pass} (:id user))
          [::response/found "/login"])
        [::response/forbidden "password does not match."]))))

(defmethod ig/init-key :typing-ex.handler.core/users [_ {:keys [db]}]
  (fn [req]
    (if (admin? (get-nick req))
      (view/active-users-page (results/active-users db 40))
      [::response/forbidden "<h1>Admin Only</h1>"])))
