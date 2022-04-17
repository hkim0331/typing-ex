(ns typing-ex.boundary.results
  (:require
   [clojure.string :as str]
   [duct.database.sql]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as timbre]
   [typing-ex.boundary.utils :refer [ds-opt]]))

(defprotocol Results
  (insert-pt [db rcv])
  (find-max-pt [db n])
  (fetch-records [db login])
  (todays-score [db login])
  (active-users [db n])
  (find-ex-days [db]))

(extend-protocol Results
  duct.database.sql.Boundary
  (insert-pt [db login-pt]
    (sql/insert! (ds-opt db) :results login-pt))

  (find-max-pt [db n]
    (let [tmp "select login, max(pt) from
                (select * from results where
                   timestamp > CURRENT_TIMESTAMP - interval 'XX days') as rslt
                 group by login
                 order by max(pt) desc"
          sql (str/replace tmp #"XX" (str n))]
      (sql/query (ds-opt db) [sql])))

  (fetch-records [db login]
    (sql/query
     (ds-opt db)
     ["select pt, timestamp from results
                 where login=?
                 order by id asc" login]))

  (todays-score [db login]
    (sql/query
     (ds-opt db)
     ["select pt from results
                 where login=? and date(timestamp) = CURRENT_DATE
                 order by id asc" login]))

  (active-users [db n]
    (let [ret (sql/query
               (ds-opt db)
               ["select login, timestamp from results order by id desc"])]
      (->> ret
           (partition-by :login)
           (take n))))

  (find-ex-days [db]
    (let [ret (sql/query
               (ds-opt db)
               ["select login, date(timestamp) from results
                 group by login, date(timestamp)"])]
      ret)))