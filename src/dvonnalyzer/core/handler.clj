(ns dvonnalyzer.core.handler
  (:require [compojure.core :refer :all]
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

(defn- parse-game-file
  [game-file]
  game-file)

(defroutes app-routes
  (GET "/" [] (render-file "templates/home.html"
                           {:csrf (anti-forgery-field)}))
  (POST "/game" [game-id]
        (let [real-game-id (get-game-id game-id)]
          (redirect (str "/game/" real-game-id))))
  (GET "/game/demo" []
       (let [content (slurp "resources/games/demo.txt")]
         (parse-game-file content)))
  (GET "/game/:game-id" [game-id]
       (let [url (little-golem-game-url game-id)
             content (:body (client/get url))]
         (parse-game-file content)))
  (route/not-found "<h1>Not Found</h1>"))

(def app
  (wrap-defaults app-routes site-defaults))
