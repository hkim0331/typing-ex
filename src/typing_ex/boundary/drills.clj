(ns typing-ex.boundary.drills
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc.sql :as sql]
   [duct.database.sql]
   [taoensso.timbre :as timbre]))

(defprotocol Drills
  (fetch-drill [db]))

;; FIXME: distributed evenly?
(defn random-id
  [db]
  (let [rid (-> (sql/query
                 (ds-opt db)
                 ["select (1 + (random() * (max(id)-1)))::int from drills"])
                first
                :int4)]
    (timbre/debug "random-id" rid)
    rid))

(extend-protocol Drills
  duct.database.sql.Boundary
  (fetch-drill
   [db]
   (-> (sql/query
        (ds-opt db)
        ["select * from drills where id=?" (random-id db)])
       first
       :text)))
