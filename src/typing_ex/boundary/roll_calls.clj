(ns typing-ex.boundary.roll-calls
 (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   #_[next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Roll-Calls
  (rc [db login])
  (rc! [db login pt]))

(extend-protocol Roll-Calls
  duct.database.sql.Boundary

  (rc [db login]
    (let [ret (sql/query
               (ds-opt db)
               ["select * from roll_calls where login=?" login])]
      (println "roll-calls/rc ret:" (str ret))
      ret))

  (rc! [db login pt]
    (sql/insert!
     (ds-opt db)
     :roll_calls
     {:login login :pt pt :created_at (java.util.Date.)})))
