(ns typing-ex.boundary.utils
  (:require
   [next.jdbc.result-set :as rs]))

(defn ds [db]
  (-> db :spec :datasource))

(def b-fn {:builder-fn rs/as-unqualified-lower-maps})
