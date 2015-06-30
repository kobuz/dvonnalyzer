(ns dvonnalyzer.core
  (:require [clojure.string :refer [capitalize]]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]
            [dvonnalyzer.game :as game]
            [dvonnalyzer.parser :as parser]))

(defn nav-component [move-id]
  [:div
   [:p "Move id is " @move-id]
   [:input {:type "button" :value "Prev" :on-click #(swap! move-id dec)}]
   [:input {:type "button" :value "Next" :on-click #(swap! move-id inc)}]])

(defn metadata-component [metadata]
  [:p "Metadata"
   [:ul
    (for [[mkey mval] metadata]
      ^{:key mkey} [:li (str (-> mkey name capitalize) ": " mval)])]])

(defn board-component
  [board move-id]
  (let [x (+ @move-id 2)]
    [:p (str "board " x)]))

(defn dvonn-component [game-data]
  (let [move-id (atom 0)]
    [:div
      [metadata-component (:metadata game-data)]
      [nav-component move-id]
      [board-component 3 move-id]]))

(defn render-dvonn
  [game-data]
  (reagent/render-component [dvonn-component game-data]
                            (js/document.getElementById "app")))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn handler [response]
  (let [game-data (parser/parse-file response)
        moves (game/apply-all-moves (:moves-by-phase game-data))]
    (render-dvonn game-data)))

(GET "/game/demo-content" {:handler handler
                           :error-handler error-handler})
