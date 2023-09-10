(ns typing-ex.handler.core
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   [buddy.hashers :as hashers]
   [clojure.string :as str]
   [environ.core :refer [env]]
   [hato.client :as hc]
   [integrant.core :as ig]
   #_[integrant.repl.state :refer [system]]
   [java-time.api :as jt]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.util.response :refer [redirect]]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.roll-calls :as roll-calls]
   [typing-ex.boundary.restarts :as restarts]
   [typing-ex.boundary.results :as results]
   [typing-ex.boundary.stat :as stat]
   [typing-ex.view.page :as view]))

(comment
  (env :tp-dev)
  :rcf)

(def ^:private l22 "https://l22.melt.kyutech.ac.jp/api/user/")

(def typing-start (or (env :tp-start) "2023-04-01"))

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

(defn- find-user [login]
  (let [url (str l22 login)
        body (:body (hc/get url {:as :json}))]
    body))

;; FIXME: 環境変数以外の方法は？
(defn auth? [login password]
  (or
   (= "true" (env :tp-dev))
   (let [ret (find-user login)]
     (and (some? ret)
          (hashers/check password (:password ret))))))

(defmethod ig/init-key :typing-ex.handler.core/login-post [_ _]
  (fn [{[_ {:strs [login password]}] :ataraxy/result}]
    (if (and (seq login) (auth? login password))
      (-> (redirect "/total/7")
          (assoc-in [:session :identity] (keyword login)))
      (-> (redirect "/login")
        (dissoc :session)
        (assoc :flash "login failure")))))

(defmethod ig/init-key :typing-ex.handler.core/logout [_ _]
  (fn [_]
    (-> (redirect "/login")
        (assoc :session {}))))

(defn login-field [login]
  (str/replace "<input type='hidden' id='login' value='xxx'>"
               #"xxx"
               login))

;; index.
;; DON't FORGET: anti-forgery-field と login を埋め込む。
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
      ;; DON'T FORGET
      (anti-forgery-field)
      (login-field (get-login req))
      ;;
      "<div class='container'>
    <div id='app'>
      Shadow-cljs rocks!
    </div>
    <script src='js/compiled/main.js' type='text/javascript'></script>
    <script>typing_ex.typing.init();</script>
    </div>
  </body>
</html>")]))

(defmethod ig/init-key :typing-ex.handler.core/total [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result :as req}]
    (let [n (Integer/parseInt n)
          ret (results/sum db n)
          user (get-login req)]
      (view/sums-page ret user n))))

(defmethod ig/init-key :typing-ex.handler.core/score-post [_ {:keys [db]}]
  (fn [{{:strs [pt]} :form-params :as req}]
    (let [login (get-login req)
          rcv {:pt (Integer/parseInt pt) :login login}]
      (results/insert-pt db rcv)
      [::response/ok (str rcv)])))

(defmethod ig/init-key :typing-ex.handler.core/scores [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result :as req}]
    (let [days (Integer/parseInt n)
          login (get-login req)
          max-pt (results/find-max-pt db days)
          ;;ex-days (results/find-ex-days db days)
          ex-days "dummy"
          ]
      (view/scores-page max-pt ex-days login days))))

(defmethod ig/init-key :typing-ex.handler.core/ex-days [_ {:keys [db]}]
  (fn [{[_ n] :ataraxy/result :as req}]
    (let [days (Integer/parseInt n)
          login (get-login req)
          ex-days (results/find-ex-days db days)
          ]
      (view/ex-days-page ex-days login days))))

;; meta endpoint, dispatches to /total, /days and /max.
(defmethod ig/init-key :typing-ex.handler.core/recent [_ _]
  (fn [req]
    (let [days (get-in req [:params :n])
          kind (get-in req [:query-params "kind"])]
      ;; (println "kind" kind)
      (case kind
        "total" (redirect (str "/total/" days))
        "days"  (redirect (str "/days/" days))
        "max"   (redirect (str "/max/" days))))))

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
    (view/display-records login
                          ;; (results/fetch-records db login)
                          (results/fetch-records-since db login typing-start)
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
     (view/todays-act-page ret (get-login req)))))

(defmethod ig/init-key :typing-ex.handler.core/stat [_ {:keys [db]}]
  (fn [_]
    [::response/ok (:stat (stat/stat db))]))

(defmethod ig/init-key :typing-ex.handler.core/stat-page [_ {:keys [db]}]
  (fn [req]
    (if (= "hkimura" (get-login req))
      (view/stat-page (:stat (stat/stat db)))
      [::response/forbidden "ACCESS FORBIDDEN"])))

(defmethod ig/init-key :typing-ex.handler.core/stat! [_ {:keys [db]}]
  (fn [{{:keys [stat]} :params}]
    (stat/stat! db stat)
    (redirect "/")))

(defn- date-only
  "datetime is a java.time.LocalDateTime object"
  [datetime]
  (first (str/split (str datetime) #"T")))

(defmethod ig/init-key :typing-ex.handler.core/rc [_ {:keys [db]}]
  (fn [req]
    (let [ret (->> (roll-calls/rc db (get-login req))
                   (map :created_at)
                   (map date-only)
                   dedupe)]
      ;; (println (str ret))
      (view/rc-page ret))))

(defmethod ig/init-key :typing-ex.handler.core/rc! [_ {:keys [db]}]
  (fn [{{:keys [pt]} :params :as req}]
    (let [ret (roll-calls/rc! db (get-login req) (Integer/parseInt pt))]
      (if ret
        [::response/ok (str pt (get-login req) (java.util.Date.))]
        [::response/bad-request "rc! errored"]))))

(defmethod ig/init-key :typing-ex.handler.core/restarts! [_ {:keys [db]}]
  (fn [req]
    (let [login (get-login req)
          ret (restarts/restarts! db login)]
      (if ret
        [::response/ok (str "/restarts! " login)]
        [::response/bad-request "restarts! errored"]))))

(defmethod ig/init-key :typing-ex.handler.core/restarts-page [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result}]
    (let [ret (restarts/restarts db login)]
      (view/restarts-page login ret))))

;; returns millis
(defmethod ig/init-key :typing-ex.handler.core/restarts [_ {:keys [db]}]
  (fn [{[_ login] :ataraxy/result}]
    (let [created_at (-> (restarts/restarts db login)
                         last
                         :created_at
                         jt/sql-timestamp
                         jt/to-millis-from-epoch)]
      [::response/ok (str created_at)])))

(comment
  ;; jit を得る。
  (-> (jt/local-date-time)
      jt/sql-timestamp ;; can not remove this!
      jt/to-millis-from-epoch)
  ;; 現在時間なら、
  (-> (jt/instant)
      jt/to-millis-from-epoch); => 1685318564122
  :rcf)
