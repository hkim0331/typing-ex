(ns typing-ex.boundary.results
  (:require
    [duct.database.sql]
    [next.jdbc.sql :as sql]
    [typing-ex.boundary.utils :refer [ds-opt]]))

(defprotocol Results
  (insert-pt [db rcv])
  (find-max-pt [db n])
  (fetch-records [db login])
  (todays-score [db login])
  (active-users [db n])
  (delete-result-by-id [db id]))

(extend-protocol Results
  duct.database.sql.Boundary
  (insert-pt [db login-pt]
    (sql/insert! (ds-opt db) :results login-pt))

  (find-max-pt [db n]
    (let [ret (sql/query
               (ds-opt db)
               ;; FIXME: 7 days を引数にとる。
               ;;        ["?" n] で n が渡らない。
               ["select login, max(pt) from
                (select * from results where
                   timestamp > CURRENT_TIMESTAMP - interval '30 days') as rslt
                 group by login
                 order by max(pt) desc"])]
      ret))

  (fetch-records [db login]
    (let [ret (sql/query
               (ds-opt db)
               ["select pt, timestamp from results
                 where login=?
                 order by id asc"
                login])]
      ret))

  ;; fixme: and where `timestamp is today`
  (todays-score [db login]
    (let [ret (sql/query
               (ds-opt db)
               ["select pt, timestamp from results
                 where login=?
                 order by id asc"
                login])]
      ret))

  (active-users [db n]
    (let [ret (sql/query
               (ds-opt db)
               ["select login, timestamp from results order by id desc"])]
      (->> ret
           (partition-by :login)
           (take n))))

  (delete-result-by-id [db id]
    1))
