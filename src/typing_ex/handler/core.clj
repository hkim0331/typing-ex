(ns typing-ex.handler.core
  (:require
   ;;[ataraxy.core :as ataraxy]
   [ataraxy.response :as response]
   [clojure.java.io :as io]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.users   :as users]
   [typing-ex.boundary.results :as results]
   [typing-ex.view.page :refer
    [login-page sign-on-page scores-page
     nickname-page password-page svg-self-records active-users-page
     sign-on-stop]]
   [integrant.core :as ig]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre :refer [debug]]))

(def DAYS 30)

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
    (login-page)))

(defn auth? [db nick password]
  (let [ret (users/find-user-by-nick db nick)]
    (and (some? ret) (= (:password ret) password))))

(defmethod ig/init-key :typing-ex.handler.core/login-post [_ {:keys [db]}]
  (fn [{[_ {:strs [nick password]}] :ataraxy/result}]
    (debug "/login-post" nick)
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
    (debug "/sign-on")
    ;; Changed: 新規登録はまた来年。
    ;;(sign-on-page)
    (sign-on-stop)))

(defmethod ig/init-key :typing-ex.handler.core/sign-on-post [_ {:keys [db]}]
  (fn [{{:strs [sid nick password]} :form-params}]
    (let [user {:sid sid :nick nick :password password}]
      (if (users/insert-user db user)
        [::response/found "/login"]
        [::response/found "/sign-on"]))))

(defmethod ig/init-key :typing-ex.handler.core/typing [_ _]
  (fn [{token :anti-forgery-token :as req}]
    ;; FIXME これを利用できないか？
    ;;(debug "/typing (:anti-forgery-token req):" token)
    [::response/ok (io/resource "typing_ex/handler/index.html")]))

;; FIXME: CSRF post
(defmethod ig/init-key :typing-ex.handler.core/score-post [_ {:keys [db]}]
  (fn [req]
    (let [pt (get-in req [:params :pt])
          nick (get-nick req)
          rcv {:pt (Integer/parseInt pt) :users_nick nick}]
      (debug "/score-post pt" rcv)
      (results/insert-pt db rcv)
      [::response/ok (str rcv)])))

(defmethod ig/init-key :typing-ex.handler.core/scores [_ {:keys [db]}]
  (fn [req]
    ;; 30 days max.
    ;; must fix view/page.clj at the same time.
    (let [ret (results/find-max-pt db DAYS)
          nick (get-nick req)]
      (scores-page ret nick DAYS))))

(defmethod ig/init-key :typing-ex.handler.core/drill [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result}]
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
        (svg-self-records nick ret))
      [::response/forbidden "<h2>Admin Only</h2>"])))

(defmethod ig/init-key :typing-ex.handler.core/ban-index [_ _]
  (fn [_]
    [::response/forbidden "access not allowed"]))

(defmethod ig/init-key :typing-ex.handler.core/nickname [_ _]
  (fn [req]
    (let [nick (get-nick req)]
      (nickname-page nick))))

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
    (password-page)))

(defmethod ig/init-key :typing-ex.handler.core/password-post [_ {:keys [db]}]
  (fn [{[_ {:strs [old-pass pass]}] :ataraxy/result :as req}]
    (let [nick (get-nick req)
          user (users/find-user-by-nick db nick)]
      (debug "user" user)
      (debug "old-pass" old-pass "pass" pass)
      (if (= old-pass (:password user))
        (do
          (users/update-user db {:password pass} (:id user))
          [::response/found "/login"])
        [::response/forbidden "password does not match."]))))

(defmethod ig/init-key :typing-ex.handler.core/users [_ {:keys [db]}]
  (fn [req]
    (if (admin? (get-nick req))
      (active-users-page (results/active-users db 40))
      [::response/forbidden "<h1>Admin Only</h1>"])))
