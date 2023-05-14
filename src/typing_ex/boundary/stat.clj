(ns typing-ex.boundary.stat
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Stat
  (stat [db])
  (stat! [db stat]))

(extend-protocol Stat
  duct.database.sql.Boundary

  (stat [db]
    (let [ret (first
               (sql/query
                (ds-opt db)
                ["select stat from stat"]))]
      ;; (println "ret" (str ret))
      ret))

  (stat! [db stat]
   (let [ret (jdbc/execute-one!
              (ds-opt db)
              ["update stat set stat=?, updated_at=?"
               stat
               (java.util.Date.)]
              {:return-keys true})]
     ret)))

(comment
  (java.util.Date.)
  :rcf)
