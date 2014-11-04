(ns dvonnalyzer.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defroutes app-routes
  (GET "/" [] (render-file "templates/home.html" {:csrf (anti-forgery-field)}))
  (POST "/game" [game-id] (str "poscik" game-id))
  (GET "/game/:game-id" [game-id] (str game-id))
  (route/not-found "<h1>Not Found</h1>"))

(def app
  (wrap-defaults app-routes site-defaults))
