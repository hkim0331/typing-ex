(ns typing-ex.handler.core
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   [buddy.hashers :as hashers]
   [clojure.string :as str]
   [hato.client :as hc]
   [integrant.core :as ig]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.results :as results]
   [typing-ex.boundary.status  :as status]
   [typing-ex.boundary.users   :as users]
   [typing-ex.view.page :as view]
   ))

;; FIXME: データベースに持っていかねば。
(defn admin? [s]
  (let [admins #{"hkimura"}]
    (get admins s)))

(defn get-login
  "request ヘッダの id 情報を文字列で返す。
   id がない場合は例外でつかまえた後 nil を返す。"
  [req]
  (try
    (name (get-in req [:session :identity]))
    (catch Exception _ nil)))

;; login
(defmethod ig/init-key :typing-ex.handler.core/login [_ _]
  (fn [req]
    (view/login-page req)))

(def ^:private l22 "https://l22.melt.kyutech.ac.jp/api/user/")
(defn- find-user [login]
  (let [url (str l22 login)
        body (:body (hc/get url {:as :json}))]
    ;;(timbre/debug "find-user url" url)
    ;;(timbre/debug "find-user body" body)
    body))

;;
(defn auth? [db login password]
  (let [;;ret (users/find-user-by-login db login)
        ret (find-user login)]
    (timbre/debug "auth?" login)
    (and (some? ret)
         (hashers/check password (:password ret)))))

(defmethod ig/init-key :typing-ex.handler.core/login-post [_ {:keys [db]}]
  (fn [{[_ {:strs [login password]}] :ataraxy/result}]
    (if (and (seq login) (auth? db login password))
      (do
        (timbre/debug "login success" login)
        (-> (redirect "/sum/1")
            (assoc-in [:session :identity] (keyword login))))
      (do
        (timbre/debug "login failure" login)
        (-> (redirect "/login")
            (dissoc :session)
            (assoc :flash "login failure"))))))

(defmethod ig/init-key :typing-ex.handler.core/logout [_ _]
  (fn [_]
    (-> (redirect "/login")
        (assoc :session {}))))

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

(defmethod ig/init-key :typing-ex.handler.core/sum [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result :as req}]
    (let [ret (results/sum db n)
          user (get-login req)]
      (view/sums-page ret user))))

;; POST works!
(defmethod ig/init-key :typing-ex.handler.core/score-post [_ {:keys [db]}]
  (fn [{{:strs [pt]} :form-params :as req}]
    (let [login (get-login req)
          rcv {:pt (Integer/parseInt pt) :login login}]
      (timbre/debug "/score-post rcv:" rcv)
      (results/insert-pt db rcv)
      [::response/ok (str rcv)])))

(defmethod ig/init-key :typing-ex.handler.core/scores [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result :as req}]
    (let [days (Integer/parseInt n)
          login (get-login req)
          max-pt (results/find-max-pt db days)
          ex-days (results/find-ex-days db days)]
      ;;(timbre/debug "n" n)
      (view/scores-page max-pt ex-days login days))))


(defmethod ig/init-key :typing-ex.handler.core/recent [_ _]
  (fn [req]
    (timbre/debug "recent keys query-params" (keys (:query-params req)))
    (let [days  (get-in req [:params :n])]
      (if (get (:query-params req) "max")
        (redirect (str "/scores/" days))
        (redirect (str "/sum/" days))))))

(defmethod ig/init-key :typing-ex.handler.core/scores-no-arg [_ _]
  (fn [_]
    (let [days 7]
      (redirect (format "/scores/%d" days)))))

(defmethod ig/init-key :typing-ex.handler.core/drill [_ {:keys [db]}]
  (fn [_]
    (let [ret (drills/fetch-drill db)]
      [::response/ok ret])))

;; admin 以外、自分のレコードしか見れない。
(defmethod ig/init-key :typing-ex.handler.core/record [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result :as req}]
    (view/svg-self-records login
                           (results/fetch-records db login)
                           (= (get-login req) login)
                           (= (get-login req) "hkimura"))))

;; req から login をとるのはどうかな。
(defmethod ig/init-key :typing-ex.handler.core/todays [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result}]
    (let [results (results/todays-score db login)]
      [::response/ok (str results)])))

(defmethod ig/init-key :typing-ex.handler.core/trials [_ {:keys [db]}]
  (fn [req]
    (if (admin? (get-login req))
      (view/active-users-page (results/active-users db 40))
      [::response/forbidden
       "<h1>Admin Only</h1><p>Only admin can view this page. Sorry.</p>"])))

(defmethod ig/init-key :typing-ex.handler.core/todays-act [_ {:keys [db]}]
  (fn [req]
   (let [ret (->> (results/todays-act db)
                  (partition-by :login)
                  (map first)
                  (sort-by :timestamp)
                  reverse)]
     (view/todays-act-page ret))))

;; midterm exam
(defmethod ig/init-key :typing-ex.handler.core/mt [_ {:keys [db]}]
  (fn [_]
    (let [ret (status/mt db)]
      [::response/ok (str ret)])))

(defmethod ig/init-key :typing-ex.handler.core/toggle-mt [_ {:keys [db]}]
  (fn [_]
    (let [ret (status/toggle-mt! db)]
      (timbre/debug "toggle-mt returns " ret)
      [::response/ok (str (dissoc ret :id :i :s :updated_at))])))
