(ns typing-ex.boundary.status
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Status
  (mt [db]))

(extend-protocol Status
  duct.database.sql.Boundary
  (mt [db]
    (let [ret (first
               (sql/query
                (ds-opt db)
                ["select * from status where name = 'mt'"]))]
      ret)))
