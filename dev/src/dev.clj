(ns dev
  (:refer-clojure :exclude [test abs])
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.repl :refer :all]
   [fipp.edn :refer [pprint]]
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.java.io :as io]
   [duct.core :as duct]
   [duct.core.repl :as duct-repl :refer [auto-reset]]
   [eftest.runner :as eftest]
   [integrant.core :as ig]
   [integrant.repl :refer [clear halt go init prep reset]]
   [integrant.repl.state :refer [config system]]
   [taoensso.timbre :as timbre]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "typing_ex/config.edn")))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

;;; add by me.

;;https://github.com/duct-framework/docs/blob/master/GUIDE.rst
(defn db []
  (-> system (ig/find-derived-1 :duct.database/sql) val :spec))

(defn q [sql]
  (jdbc/query (db) sql))

(comment
  (keys system)
  (db)
  (q "select * from results limit 5")
  :rcf)
