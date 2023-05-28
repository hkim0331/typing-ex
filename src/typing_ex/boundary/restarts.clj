(ns typing-ex.boundary.restarts
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   ;; [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Restarts
  (restarts  [db login])
  (restarts! [db login]))

(extend-protocol Restarts
  duct.database.sql.Boundary

  (restarts [db login]
    (let [ret (sql/query
               (ds-opt db)
               ["select * from restarts
                  where login = ? and
                  date(created_at) = CURRENT_DATE" login])]
      ret))

  (restarts! [db login]
    (let [ret (sql/insert!
               (ds-opt db)
               :restarts
               {:login login :created_at (java.util.Date.)}
               {:return-keys true})]
      ret)))
