(ns typing-ex.handler.example
  (:require
   [ataraxy.response :as response]
   [clojure.java.io :as io]
   [integrant.core :as ig]))

(defmethod ig/init-key :typing-ex.handler/example [_ _]
  (fn [_]
    [::response/ok (io/resource "typing_ex/handler/example/example.html")]))
