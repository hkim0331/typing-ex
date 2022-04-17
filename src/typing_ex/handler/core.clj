(ns typing-ex.handler.core
  (:require
   [ataraxy.response :as response]
   [buddy.hashers :as hashers]
   [clojure.string :as str]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.users   :as users]
   [typing-ex.boundary.results :as results]
   ;; FIXME: refer
   [typing-ex.view.page :as view]
   [integrant.core :as ig]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))


;; FIXME: データベースに持っていこ。
(defn admin? [s]
  (let [admins #{"hkimura" "ayako" "login888"}]
    (get admins s)))

(defn get-login
  "request ヘッダの id 情報を文字列で返す。エラーの場合は nil."
  [req]
  (try
    (name (get-in req [:session :identity]))
    (catch Exception _ nil)))

;; login
(defmethod ig/init-key :typing-ex.handler.core/login [_ _]
  (fn [req]
    (view/login-page req)))

(defn auth? [db login password]
  (let [ret (users/find-user-by-login db login)]
    (timbre/debug "auth?" login password)
    (and (some? ret)
         ;;(= (:password ret) password)
         (hashers/check password (:password ret)))))

(defmethod ig/init-key :typing-ex.handler.core/login-post [_ {:keys [db]}]
  (fn [{[_ {:strs [login password]}] :ataraxy/result}]
    (timbre/debug "/login-post" login password)
    (if (and (seq login) (auth? db login password))
      (do
        (timbre/debug "login success")
        (-> (redirect "/scores")
            (assoc-in [:session :identity] (keyword login))))
      (-> (redirect "/login")
          (assoc :flash "login failure")))))

(defmethod ig/init-key :typing-ex.handler.core/logout [_ _]
  (fn [_]
    (-> (redirect "/login")
        (assoc :session {}))))

;; signon, login は l22 に持って行った。
;;
;; (defmethod ig/init-key :typing-ex.handler.core/sign-on [_ _]
;;   (fn [_]
;;     (timbre/debug "/sign-on")
;;     ;; Changed: 新規登録はまた来年。
;;     ;;(sign-on-page)
;;     (view/sign-on-stop)))

;; (defmethod ig/init-key :typing-ex.handler.core/sign-on-post [_ {:keys [db]}]
;;   (fn [{{:strs [sid login password]} :form-params}]
;;     (let [user {:sid sid :login login :password password}]
;;       (if (users/insert-user db user)
;;         [::response/found "/login"]
;;         [::response/found "/sign-on"]))))

(defn login-field [login]
  (str/replace "<input type='hidden' id='login' value='xxx'>"
               #"xxx"
               login))

;; index. anti-forgery-field と login を埋め込む。
(defmethod ig/init-key :typing-ex.handler.core/typing [_ _]
  (fn [req]
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
      (login-field (get-login req))
      "<div class='container'>
    <div id='app'>
      Shadow-cljs rocks!
    </div>
    <script src='js/compiled/main.js' type='text/javascript'></script>
    <script>typing_ex.typing.init();</script>
    </div>
  </body>
</html>")]))

;; POST works!
(defmethod ig/init-key :typing-ex.handler.core/score-post [_ {:keys [db]}]
  (fn [{{:strs [pt]} :form-params :as req}]
    (let [login (get-login req)
          rcv {:pt (Integer/parseInt pt) :login login}]
      (results/insert-pt db rcv)
      [::response/ok (str rcv)])))

(def DAYS 7)

(defmethod ig/init-key :typing-ex.handler.core/scores [_ {:keys [db]}]
  (fn [req]
    (let [login (get-login req)
          ret (results/find-max-pt db DAYS)
          days (results/find-ex-days db)]
      ;;(timbre/debug "days" days)
      ;;(timbre/debug "filter" (filter #(= (:login %) "hkimura") days))
      (view/scores-page ret login DAYS days))))

;; 200 日間のデータを「全てのデータ」としてよい。授業は半年だ。
(defmethod ig/init-key :typing-ex.handler.core/scores-all [_ {:keys [db]}]
  (fn [req]
    (let [login (get-login req)
          ret (results/find-max-pt db 200)]
      (view/scores-page ret login 200 {}))))

(defmethod ig/init-key :typing-ex.handler.core/drill [_ {:keys [db]}]
  (fn [_]
    (let [ret (drills/fetch-drill db)]
      [::response/ok ret])))

;; admin 以外、自分のレコードしか見れない。
(defmethod ig/init-key :typing-ex.handler.core/record [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result :as req}]
    (if (or (= login "hkimura")
            (= login (get-login req))
            (admin? (get-login req)))
      (view/svg-self-records login (results/fetch-records db login))
      [::response/forbidden
       "<h2>Admin Only</h2><p>自分のと hkimura の記録が見れます。"])))

;; req から login をとるのはどうかな。
(defmethod ig/init-key :typing-ex.handler.core/todays [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result}]
    (let [results (results/todays-score db login)]
      [::response/ok (str results)])))

;;(defmethod ig/init-key :typing-ex.handler.core/ban-index [_ _]
;;  (fn [_]
;;    [::response/forbidden "access not allowed"]))

(defmethod ig/init-key :typing-ex.handler.core/trials [_ {:keys [db]}]
  (fn [req]
    (if (admin? (get-login req))
      (view/active-users-page (results/active-users db 40))
      [::response/forbidden
       "<h1>Admin Only</h1><p>Only admin can view this page. Sorry.</p>"])))

(defn- probe [in]
 (timbre/debug "probe" in)
 in)

(defmethod ig/init-key :typing-ex.handler.core/todays-act [_ {:keys [db]}]
  (fn [req]
    (if (admin? (get-login req))
      (let [ret (->> (results/todays-act db)
                     (partition-by :login)
                     (map first)
                     (sort-by :timestamp)
                     reverse)]
        (view/todays-act-page ret))
      [::response/forbidden
       "<h1>Admin Only</h1>
        <p>Only admin can view. Had better open to students?</p>"])))

