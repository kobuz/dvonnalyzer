(ns dvonnalyzer.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clj-http.lite.client :as client]
            [dvonnalyzer.parser :as parser]))

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
  (GET "/game/demo" []
       (render-file "templates/game.html" {}))
  (GET "/game/:game-id" [game-id]
       (render-file "templates/game.html" {}))
  (GET "/content/demo" []
       (let [content (slurp "resources/games/demo.txt")
             parsed-game (parser/parse-file content)]
         (pr-str parsed-game)))
  (route/resources "/")
  (route/not-found "<h1>Not Found</h1>"))

(def app
  (wrap-defaults app-routes site-defaults))
