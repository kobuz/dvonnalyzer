(ns dvonnalyzer.handler
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clj-http.lite.client :as client]
            [dvonnalyzer.parser :as parser]))

(defn- get-game-id
  [id-or-url]
  (re-find #"\d+" id-or-url))

(defn- little-golem-game-url
  [game-id]
  (str "http://www.littlegolem.net/servlet/sgf/"
       game-id "/game" game-id ".txt"))

(defn- str->vec-of-ints
  "Change symbolic coordinates to vector of ints, eg. \"b5\" -> [1, 4]"
  [s]
  (vector (- (int (first s)) (int \a))
          (- (int (second s)) (int \1))))

(defn- parse-game-file
  [game-file]
  (let [game-data (parser/parse-file game-file)]
    (render-file "templates/game.html"
                 {:game-data game-data
                  :game-name "demo"})))

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
