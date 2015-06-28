(ns dvonnalyzer.hello
  (:require [clojure.string :refer [capitalize]]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]
            [dvonnalyzer.game :as game]
            [dvonnalyzer.parser :as parser]))

(def move-id (atom 0))

(defn nav-component []
  [:div
   [:p "Move id is " @move-id]
   [:input {:type "button" :value "Prev" :on-click #(swap! move-id dec)}]
   [:input {:type "button" :value "Next" :on-click #(swap! move-id inc)}]])

(defn metadata-component [metadata]
  [:p "Metadata"
   [:ul
    (for [[mkey mval] metadata]
      ^{:key mkey} [:li (str (-> mkey name capitalize) ": " mval)])]])

(defn dvonn-component [game-data]
  [metadata-component (:metadata game-data)])

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn handler [response]
  (let [game-data (parser/parse-file response)
        moves (game/apply-all-moves (:moves-by-phase game-data))]
    (render-dvonn game-data)))

(GET "/game/demo-content" {:handler handler
                           :error-handler error-handler})

(defn render-dvonn
  [game-data]
  (reagent/render-component [dvonn-component game-data]
                            (js/document.getElementById "app")))
