(ns typing-ex.boundary.drills
  (:require
   [typing-ex.boundary.utils :refer [ds b-fn]]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Drills
  ;; FIXME: n is not used
  (fetch-drill [db n]))

(extend-protocol Drills
  duct.database.sql.Boundary
  (fetch-drill [db n]
    (let [m (first
             (sql/query
               (ds db)
               ["select (random() * max(id))::int from drills"]
               b-fn))
          ret (first
               (sql/query
                 (ds db)
                 ["select * from drills where id = ?" (:int4 m)]
                 b-fn))]
      ;;(debug "ret" ret)
      (:text ret))))
