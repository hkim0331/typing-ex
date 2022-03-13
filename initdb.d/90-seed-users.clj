#!/usr/bin/env bb
(require '[babashka.pods :as pods])

;; load from pod registry:
(pods/load-pod 'org.babashka/postgresql "0.0.4")
;; or load from system path:
;; (pods/load-pod "pod-babashka-postgresql")
;; or load from a relative or absolute path:
;; (pods/load-pod "./pod-babashka-postgresql")

(require '[pod.babashka.postgresql :as pg])

(def db {:dbtype   "postgresql"
         :host     "localhost"
         :dbname   "typing"
         :user     (System/getenv "TYPING_USER")
         :password (System/getenv "TYPING_PASS")
         :port     5432})

(defn insert-user [[sid nick pass]]
 (pg/execute!
  db
  ["insert into users (sid, nick, password) values (?, ?, ?)"
   sid nick pass]))

(defn insert-users [v]
   (dorun
    (map insert-user v)))

(def data
 [[120, "apple", "ap"]
  [121, "banana", "ba"]
  [122, "pear", "pe"]
  [123, "orange", "or"]
  [124, "grape", "gr"]])

(insert-users data)