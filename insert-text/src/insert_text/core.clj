(ns insert-text.core
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs])
  (:gen-class))

(def db {:dbtype   "postgresql"
         :dbhost   "localhost"
         :dbname   (System/getenv "TP_DB")
         :port     5432
         :user     (System/getenv "TP_USER")
         :password (System/getenv "TP_PASSWORD")})


(def ds (jdbc/with-options
          (jdbc/get-datasource db)
          {:builder-fn  rs/as-unqualified-lower-maps
           :return-keys true}))

(comment
  (sql/query ds ["select now()"])
  (sql/query ds ["select * from drills limit 2"])
  :rcf)

;; FIXME: need format
(defn insert
  "SQL: insert into drills (text) values (s)"
  [s]
  (tap> s)
  (dotimes [_ 2]
    (let [ret (sql/insert! ds :drills {:text s})]
      ret)))

(defn insert-from-file
  [filename]
  (insert (slurp filename)))

(defn -main
  [filename]
  (if (.exists (io/file filename))
    (insert-from-file filename)
    (println "does not exist" filename)))

(comment
  (-main "resources/la_lune.txt")
  (-main "resources/marry_you.txt")
  (-main "resources/count_on_me.txt")
  (-main "resources/shape_of_you.txt")
  (-main "resources/suddenly.txt")
  (sql/query ds ["select text from drills where id=?" 702])
  :rcf)
