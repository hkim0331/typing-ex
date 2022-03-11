#!/usr/bin/env bb
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/postgresql "0.0.4")
(require '[pod.babashka.postgresql :as pg])

(def db {:dbtype   "postgresql"
         :host     "localhost"
         :dbname   "typing"
         :user     (System/getenv "TYPING_USER")
         :password (System/getenv "TYPING_PASS")
         :port     5432})

(defn insert-result [nick pt]
 (pg/execute!
  db
  ["insert into results (users_nick, pt) values (?, ?)" nick pt]))

(def nicks ["apple" "banana" "pear" "orange" "grape"])

(defn rand-nick []
  (let [c (count nicks)]
    (get nicks (rand-int c))))

(defn insert-results [n]
  (dotimes [_ n]
    (insert-result (rand-nick) (rand-int 40))))

(insert-results 100)
