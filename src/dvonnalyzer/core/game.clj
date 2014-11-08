(ns dvonnalyzer.core.game)

(defn char-range
  "Return range of characters including end."
  [start end]
  (map char (range (int start) (inc (int end)))))

(defn empty-board
  "Generate empty board as hashmap with values of :empty."
  []
  (let [all-coords (for [x (char-range \a \k)
                         y (range 1 6)]
                     (keyword (str x y)))
        final-coords (remove #{:a4 :a5 :b5 :j1 :k1 :k2}
                             all-coords)]
    (zipmap final-coords (repeat :empty))))

(defn can-put-piece?
  [board place]
  (= (place board) :empty))

(defn place-dvonn-piece
  [board place]
  (assoc board place {:dvonn 1}))

(defn put-piece
  [board place player]
  (assoc board place {:color player
                      :stack 1
                      :dvonn 0}))

(defn move-piece
  [board from to player]
  (let [getter (fn [key] map #(get-in [% key]) [from to])
        stack (reduce + (getter :stack))
        dvonn (reduce + (getter :dvonn))]
    (merge board {from :empty
                  to {:color player
                      :stack stack
                      :dvonn dvonn}})))
