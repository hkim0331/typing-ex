(ns typing-ex.boundary.utils
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

;; interface to duct.database/sql
(defn ds [db]
  (-> db :spec :datasource))

(defn ds-opt [db]
  (jdbc/with-options (ds db) {:builder-fn rs/as-unqualified-lower-maps}))

;; no use.
;; (def b-fn {:builder-fn rs/as-unqualified-lower-maps})
