(ns dvonnalyzer.core
  (:require [clojure.string :refer [capitalize join]]
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

(defn draw-hex
  [hex]
   (if (game/blank? hex)
     [:span.circle.empty " "]
     (let [{:keys [dvonn stack color] :or {dvonn 0}} hex]
       (if (some? color)
         [:span {:class (->> ["circle" (name color) (if (> dvonn 0) "has-dvonn")]
                             (filter some?) (join " "))} stack]
         [:span.circle.full-dvonn " "]))))

(defn draw-board
  [board]
  (.log js/console "draw board")
  (let [order (->> board keys sort (group-by #(second (name %))))]
    (for [[idx values] order]
      ^{:key (str "row-" idx)} [:p (for [coord values]
            ^{:key (str "ble" coord)} [draw-hex (get board coord)])])))

(defn board-component
  [moves move-id]
  (let [{:keys [board number move player]} (nth moves @move-id)]
    [:div.board (draw-board board)]))

(defn dvonn-component [game-data]
  (let [move-id (atom 0)
        moves (game/apply-all-moves (:moves-by-phase game-data))]
    [:div
      [metadata-component (:metadata game-data)]
      [nav-component move-id]
      [board-component (:all moves) move-id]]))

(defn render-dvonn
  [game-data]
  (reagent/render-component [dvonn-component game-data]
                            (js/document.getElementById "app")))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn handler [response]
  (let [game-data (parser/parse-file response)]
    (render-dvonn game-data)))

(GET "/game/demo-content" {:handler handler
                           :error-handler error-handler})
