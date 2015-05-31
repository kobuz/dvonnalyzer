(ns dvonnalyzer.game)

(defn abs [n] (if (pos? n) n (- n)))

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
  [board to]
  (= (to board) :empty))

(defn put-dvonn-piece
  [board to]
  (assoc board to {:stack 1
                   :dvonn 1}))

(defn put-piece
  [board to player]
  (assoc board to {:color player
                   :stack 1
                   :dvonn 0}))

(defn distance
  [from to]
  (let [transform (fn [x] (map int (name x)))
        [fromX fromY] (transform from)
        [toX toY] (transform to)
        diffX (abs (- fromX toX))
        diffY (abs (- fromY toY))]
    (cond
      (zero? diffX) diffY
      (zero? diffY) diffX
      (= diffX diffY) diffX
      :else nil)))

(defn can-move-piece?
  [board from to player]
  (let [origin (from board)
        target (to board)]
    (and origin
         target
         (= (:color origin) player)
         (= (distance from to)
            (:stack origin)))))

(defn move-piece
  [board from to]
  (let [getter (fn [key] (map #(get-in board [% key]) [from to]))
        stack (reduce + (getter :stack))
        dvonn (reduce + (getter :dvonn))]
    (merge board {from :empty
                  to {:color (get-in board [from :color])
                      :stack stack
                      :dvonn dvonn}})))

(defn alternate-player
  [player]
  (if (= player :black) :white :black))

(defn dvonn-phase
  [record]
  (let [pieces (take 3 record)]
    (reductions put-dvonn-piece (empty-board) pieces)))

(defn put-phase
  [record board]
  (loop [record (take 46 (drop 3 record))]
    (reductions (fn [board [to player]] (put-piece to player)) board
                (map vector board (repeat [:black :white])))))
