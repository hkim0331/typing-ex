(ns typing-ex.boundary.status
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Status
  (mt [db])
  (toggle-mt [db]))

(extend-protocol Status
  duct.database.sql.Boundary
  (mt [db]
    (let [ret (first
               (sql/query
                (ds-opt db)
                ["select name, b from status where name = 'mt'"]))]
      ret))
  (toggle-mt [db]
   (let [ret (jdbc/execute!
                (ds-opt db)
                ["update status set b = not b where name='mt'"]
                {:return-keys true})])))