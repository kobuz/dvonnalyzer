(ns dvonnalyzer.handler
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clj-http.lite.client :as client]))

(defn- get-game-id
  [id-or-url]
  (re-find #"\d+" id-or-url))

(defn- little-golem-game-url
  [game-id]
  (str "http://www.littlegolem.net/servlet/sgf/"
       game-id "/game" game-id ".txt"))

(defroutes app-routes
  (GET "/" [] (render-file "templates/home.html"
                           {:csrf (anti-forgery-field)}))
  (POST "/game" [game-id]
        (let [real-game-id (get-game-id game-id)]
          (redirect (str "/game/" real-game-id))))
  (GET "/game/demo" []
       (render-file "templates/game.html" {}))
  (GET "/game/demo-content" []
       (slurp "resources/games/demo.txt"))
  (GET "/game/:game-id" [game-id]
       (render-file "templates/game.html" {}))
  (route/not-found "<h1>Not Found</h1>"))

(def app
  (wrap-defaults app-routes site-defaults))
