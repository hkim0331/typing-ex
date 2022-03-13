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

(defn insert-drill [file]
  (let [text (slurp file)]
    (pg/execute! db ["insert into drills (text) values (?)" text])))

(defn entries [dir]
  (map #(str dir "/" %) (-> dir io/file .list seq)))

(defn insert-drills [dir]
  (dorun
    (map insert-drill (entries dir))))

;; 2021-06-06
;;(insert-drills "gtypist")
(insert-drills "../utils/excerpt/out")
