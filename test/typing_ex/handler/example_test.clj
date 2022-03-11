(ns typing-ex.handler.example-test
  (:require [clojure.test :refer [deftest testing is]]
            [integrant.core :as ig]
            [ring.mock.request :as mock]
            [typing-ex.handler.example]))

(deftest smoke-test
  (testing "example page exists"
    (let [handler  (ig/init-key :typing-ex.handler/example {})
          response (handler (mock/request :get "/example"))]
      (is (= :ataraxy.response/ok (first response)) "response ok"))))
