(ns typing-ex.boundary.drills
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Drills
  ;; FIXME: n is not used
  (fetch-drill [db]))

(extend-protocol Drills
  duct.database.sql.Boundary
  (fetch-drill [db]
    (let [m (first
             (sql/query
               (ds-opt db)
               ["select (random() * max(id))::int from drills"]))
          ret (first
               (sql/query
                 (ds-opt db)
                 ["select * from drills where id = ?" (:int4 m)]))]
      (:text ret))))
