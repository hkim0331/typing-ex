(ns typing-ex.handler.core
  (:refer-clojure :exclude [abs])
  (:require
   [ataraxy.response :as response]
   [buddy.hashers :as hashers]
   [clojure.string :as str]
   [environ.core :refer [env]]
   [hato.client :as hc]
   [integrant.core :as ig]
   [java-time.api :as jt]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.util.response :refer [redirect]]
   [typing-ex.boundary.drills  :as drills]
   [typing-ex.boundary.roll-calls :as roll-calls]
   [typing-ex.boundary.restarts :as restarts]
   [typing-ex.boundary.results :as results]
   [typing-ex.view.page :as view]
   ;;
   [taoensso.carmine :as car :refer [wcar]]
   [taoensso.telemere :as t]
   [clojure.edn :as edn]))

;; (add-tap prn)
;; (remove-tap prn)

(def ^:private l22 "https://l22.melt.kyutech.ac.jp/api/user/")

(comment
  (:body (hc/get (str l22 "hkimura")))
  :rcf)

(defonce my-conn-pool (car/connection-pool {}))
(def     my-conn-spec {:uri "redis://db:6379"})
(def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})
(defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))

(comment
  (wcar* (car/set "a" "hello")) ;=> ConnectionException: Connection refused
  :rcf)

(def ^:private redis-expire 3600)

(def typing-start (or (env :tp-start) "2025-01-01"))

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

;; exam!
(defmethod ig/init-key :typing-ex.handler.core/exam! [_ _]
  (fn [{{:keys [login count pt]} :params}]
    ;; (println "handler.core " login count pt)
    (let [key (str login ":" (mod (Integer/parseInt count) 3))]
      (wcar* (car/set key pt))
      [::response/ok "exam!"])))

(defmethod ig/init-key :typing-ex.handler.core/exam [_ {:keys [db]}]
  (fn [{[_ login ct] :ataraxy/result}]
    (let [key (str login ":" ct)
          ret (wcar* (car/get key))]
      (prn "exam " key ret)
      [::response/ok ret])))

;; alert
(defmethod ig/init-key :typing-ex.handler.core/alert [_ _]
  (fn [_]
    [::response/ok (wcar* (car/get "alert"))]))

(defmethod ig/init-key :typing-ex.handler.core/alert-form [_ _]
  (fn [req]
    (if (admin? (get-login req))
      (view/alert-form req)
      [::response/forbidden "admin only"])))

(defmethod ig/init-key :typing-ex.handler.core/alert! [_ _]
  (fn [{{:keys [alert]} :params}]
    (wcar* (car/set "alert" alert))
    ;; [::response/ok (str "alert!:" alert)]
    (redirect "/total/7")))

;; login
(defmethod ig/init-key :typing-ex.handler.core/login [_ _]
  (fn [req]
    (view/login-page req)))

(defn- find-user [login]
  (let [url (str l22 login)
        body (:body (hc/get url {:as :json}))]
    body))

(defn auth?
  "when TP_DEV defined, check login only."
  [login password]
  (if (env :tp-dev)
    (and (= "hkimura" login) true)
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

(defn- remote-ip [req]
  (or
   (get-in req [:headers "cf-connecting-ip"])
   (get-in req [:headers "x-real-ip"])
   (get req :remote-addr)))

(defn- roll-call-time? []
  (->  (wcar* (car/get "stat"))
       (= "roll-call")))

(defn typing-ex [req]
  [::response/ok
   (str
    "<!DOCTYPE html>
  <html>
    <head>
      <meta charset='UTF-8'>
      <meta name='viewport' content='width=device-width, initial-scale=1'>
      <link href='/css/bootstrap.min.css' rel='stylesheet'>
      <link href='/css/style.css' rel='stylesheet' type='text/css'>
      <link rel='icon' href='/favicon.ico'>
    </head>
    <body>"
    ;; DON'T FORGET.
    (anti-forgery-field)
    (login-field (get-login req))
    "<div class='container'>
      <div id='app'>
        core/typing
      </div>
      <script src='/js/bootstrap.bundle.min.js' type='text/javascript'></script>
      <script src='/js/compiled/main.js' type='text/javascript'></script>
      <script>typing_ex.typing.init();</script>
      </div>
    </body>
  </html>")])

(defmethod ig/init-key :typing-ex.handler.core/typing [_ _]
  (fn [req]
    (if (roll-call-time?)
      (try
        (let [addr (str (remote-ip req))]
          (t/log! :info addr)
          ;; debug
          ;; (when (str/starts-with? addr "0:0")
          ;;   (throw (Exception. addr)))
          ;; (when (str/starts-with? addr "150.69.90.34")
          ;;   (throw (Exception. addr)))
          (when-not (or
                     (str/starts-with? addr "0:0") ; debug
                     (str/starts-with? addr "150.69"))
            (throw (Exception. (str "when-not:" addr))))
          (when (str/starts-with? addr "150.69.77")
            (throw (Exception. (str "when;" addr))))
          (typing-ex req))
        (catch Exception msg (t/log! :info msg)
               [::response/ok
                "背景が黄色の時、ログインできるのは教室内の WiFi です。VPN 不可。"]))
      (typing-ex req))))

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
          ex-days "dummy"]
      (view/scores-page max-pt ex-days login days))))

(defn days
  "ユーザloginがn回以上練習した日数"
  [all login n]
  (let [ret (filter (fn [x] (= login (:login x))) all)]
    (->> ret
         (group-by :timestamp)
         (map (fn [x] (count (val x))))
         (filter #(<= n %))
         count)))

(defn- users-all [db]
  (if-let [users-all (wcar* (car/get "users-all"))]
    (edn/read-string users-all)
    (let [ret (results/users db)]
      (wcar* (car/setex "users-all" redis-expire (str ret)))
      ret)))

;; ----------------------
;; FIXME: tagged literal
;; ----------------------
;; (defn- login-timestamp [db]
;;   (if-let [login-timestamp (wcar* (car/get "login-timestamp"))]
;;     (edn/read-string {:readers *data-readers*} login-timestamp)
;;     (let [ret (results/login-timestamp db)]
;;       (wcar* (car/setex "login-timestamp" 30 (str ret)))
;;       ret)))

(defn- training-days
  "redis キャッシュを有効にする。"
  [n req db]
  (tap> "training-days")
  (if-let [training-days (wcar* (car/get "training-days"))]
    (do
      (tap> "hit")
      training-days)
    (let [logins (users-all db)
          all (results/login-timestamp db)
          training-days (->> (for [{:keys [login]} logins]
                               [login (days all login n)])
                             (sort-by second >))]
      (tap> "miss")
      (wcar* (car/setex "training-days" redis-expire training-days))
      training-days)))

(defmethod ig/init-key :typing-ex.handler.core/ex-days [_ {:keys [db]}]
  (fn [req]
    ;; changed: takes a day parameter, 30. 2024-08-24
    (let [training-days (training-days 30 req db)]
      (view/ex-days-page
       (get-login req)
       training-days))))

;; meta endpoint, dispatches to /total, /days and /max.
(defmethod ig/init-key :typing-ex.handler.core/recent [_ _]
  (fn [req]
    (let [days (get-in req [:params :n])
          kind (get-in req [:query-params "kind"])]
      (case kind
        "total" (redirect (str "/total/" days))
        "training days"  (redirect (str "/days/" days))
        "max"   (redirect (str "/max/" days))))))

(defmethod ig/init-key :typing-ex.handler.core/scores-no-arg [_ _]
  (fn [_]
    (let [days 7]
      (redirect (format "/scores/%d" days)))))

(defn non-empty-text [db]
  (let [ret (drills/fetch-drill db)]
    (if (re-find #"\S" ret)
      ret
      (non-empty-text db))))

(defmethod ig/init-key :typing-ex.handler.core/drill [_ {:keys [db]}]
  (fn [_]
    [::response/ok (non-empty-text db)]))

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

(defn- current-stat []
  (if-let [stat (wcar* (car/get "stat"))]
    stat
    "normal"))

(defmethod ig/init-key :typing-ex.handler.core/stat-page [_ {:keys [db]}]
  (fn [req]
    (if (= "hkimura" (get-login req))
      (view/stat-page (current-stat))
      [::response/forbidden "ACCESS FORBIDDEN"])))

(defmethod ig/init-key :typing-ex.handler.core/stat [_ {:keys [db]}]
  (fn [_]
    [::response/ok (current-stat)]))

(defmethod ig/init-key :typing-ex.handler.core/stat! [_ {:keys [db]}]
  (fn [{{:keys [stat minutes]} :params}]
    (wcar* (car/setex "stat"
                      (* 60 (Long/parseLong minutes))
                      stat))
    (redirect "/")))

;; (defn- date-only
;;   "datetime is a java.time.LocalDateTime object"
;;   [datetime]
;;   (first (str/split (str datetime) #"T")))

(defn- time-str
  "Returns a string representation of a datetime in the local time zone."
  [instant]
  (jt/format
   (jt/with-zone (jt/formatter "yyyy-MM-dd hh:mm a") (jt/zone-id))
   instant))

(defmethod ig/init-key :typing-ex.handler.core/rc [_ {:keys [db]}]
  (fn [req]
    (let [login (get-login req)
          ret (->> (roll-calls/rc db login)
                   (map :created_at)
                   (map time-str)
                   dedupe)]
      (view/rc-page ret login))))

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
