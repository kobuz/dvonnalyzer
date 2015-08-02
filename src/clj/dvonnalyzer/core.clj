(ns dvonnalyzer.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [dvonnalyzer.handler :refer [app]]))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app {:port port})))
