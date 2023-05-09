(ns typing-ex.boundary.bg
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Status
  (bg [db])
  (update-bg! [db color]))

(extend-protocol Status
  duct.database.sql.Boundary
  (bg [db]
    (let [ret (first
               (sql/query
                (ds-opt db)
                ["select name, b from status where name = 'mt'"]))]
      ret))
  (update-bg! [db color]
   (let [ret (jdbc/execute-one!
              (ds-opt db)
              ["update status set b = not b where name='mt'"]
              {:return-keys true})]
     ret)))
