(ns dvonnalyzer.core
  (:require [clojure.string :refer [capitalize join]]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]
            [dvonnalyzer.game :as game]
            [dvonnalyzer.parser :as parser]))

(defonce move-id (atom 0))

(defn draw-move-jumper [current-move current? move-id]
  (.log js/console "jumper ")
  (let [{:keys [number player move]} current-move
        color (str (name player) "-move")]
    [:input.move
     {:type "button"
      :class (join " " [color (if current? "current-move")])
      :value (game/prn-move move)
      :on-click #(reset! move-id number)}]))

(defn nav-component [moves move-id]
  (let [move-number @move-id]
    [:div
     [:input {:type "button" :value "Prev" :on-click #(swap! move-id dec)}]
     [:input {:type "button" :value "Next" :on-click #(swap! move-id inc)}]
     [:br]
     (for [move moves
           :let [number (:number move)
                 current? (= number move-number)]]
       ^{:key (str "goto-" number)} [draw-move-jumper move current? move-id])]))

(defn metadata-component [metadata]
  [:p "Metadata"
   [:ul
    (for [[mkey mval] metadata]
      ^{:key mkey} [:li (str (-> mkey name capitalize) ": " mval)])]])

(defn curr-hex? [move coord]
  (cond
   (keyword? move) (= move coord)
   (vector? move) (or (curr-hex? (first move) coord)
                      (curr-hex? (second move) coord))))

(defn draw-hex [hex current?]
  (.log js/console "draw hex ")
  (cond
   (nil? hex) [:span.unused-hex]
   (game/blank? hex) [:span.circle.empty {:class (when current? "current-hex")}]
   :else
   (let [{:keys [dvonn stack color] :or {dvonn 0}} hex]
     (if (some? color)
       [:span.circle {:class (->> [(name color)
                                   (when (> dvonn 0) "has-dvonn")
                                   (when current? "current-hex")]
                                  (filter some?) (join " "))}
        (when (> stack 1) stack)]
       [:span.circle.full-dvonn {:class (when current? "current-hex")}]))))

(defn draw-row [board move idx coords]
  (.log js/console "draw row")
  [:div {:class (str "board-row-" idx)}
   (for [coord coords
         :let [current-move (curr-hex? move coord)]]
     ^{:key (str "hex-" coord)} [draw-hex (get board coord) current-move])])

(defn draw-board [board move]
  (.log js/console "draw board")
  (let [order (->> (game/rect-grid) sort (group-by #(second (name %))) reverse)]
    (for [[idx coords] order]
      ^{:key (str "row-" idx)} [draw-row board move idx coords])))

(defn board-component
  [moves move-id]
  (let [{:keys [board number move player]} (nth moves @move-id)]
    [:div.board (draw-board board move)]))

(defn dvonn-component [game-data]
  (let [moves (game/apply-all-moves (:moves-by-phase game-data))]
    [:div {:on-key-down #(condp = (.-which %)
                           37 (swap! move-id dec)
                           39 (swap! move-id inc)
                           nil)}
      [metadata-component (:metadata game-data)]
      [nav-component (:all moves) move-id]
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
