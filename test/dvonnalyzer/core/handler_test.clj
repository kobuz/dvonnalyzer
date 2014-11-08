(ns dvonnalyzer.core.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [dvonnalyzer.core.handler :as handler]))

(deftest test-app
  (testing "main route"
    (let [response (handler/app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (re-find #"Welcome to Dvonnalyzer" (:body response)))))

  (testing "not-found route"
    (let [response (handler/app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
