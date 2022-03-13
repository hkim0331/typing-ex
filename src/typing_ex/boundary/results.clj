(ns typing-ex.boundary.results
  (:require
    [duct.database.sql]
    [next.jdbc.sql :as sql]
    [typing-ex.boundary.utils :refer [ds-opt]]))

(defprotocol Results
  (insert-pt [db rcv])
  (find-max-pt [db n])
  (fetch-records [db nick])
  (active-users [db n])
  (delete-result-by-id [db id]))

(extend-protocol Results
  duct.database.sql.Boundary

  (insert-pt [db nick-pt]
    (sql/insert! (ds-opt db) :results nick-pt))

  (find-max-pt [db n]
    (let [ret (sql/query
               (ds-opt db)
               ;; FIXME: 7 days を引数にとる。
               ;;        ["?" n] で n が渡らない。
               ["select users_nick, max(pt) from
                (select * from results where
                   timestamp > CURRENT_TIMESTAMP - interval '30 days') as rslt
                 group by users_nick
                 order by max(pt) desc"])]
      ret))

  (fetch-records [db nick]
    (let [ret (sql/query
               (ds-opt db)
               ["select pt, timestamp from results
                 where users_nick=?
                 order by id asc"
                nick])]
      ret))

  (active-users [db n]
   (let [ret (sql/query
              (ds-opt db)
              ["select users_nick, timestamp from results order by id desc"])]
        (->> ret
           (partition-by :users_nick)
           (take n))))

  (delete-result-by-id [db id]
   1))
