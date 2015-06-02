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
  ([board to] (assoc board to {:stack 1
                               :dvonn 1}))
  ([board to player] (put-dvonn-piece board to)))

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
  [board [from to] player]
  (let [origin (from board)
        target (to board)]
    (and origin
         target
         (= (:color origin) player)
         (= (distance from to)
            (:stack origin)))))

(defn move-piece
  ([board [from to]]
  (let [getter (fn [key] (map #(get-in board [% key] 0) [from to]))
        stack (reduce + (getter :stack))
        dvonn (reduce + (getter :dvonn))]
    (merge board {from :empty
                  to {:color (get-in board [from :color])
                      :stack stack
                      :dvonn dvonn}})))
  ([board [from to] player] (move-piece board [from to])))

(defn alternate-player
  [player]
  (if (= player :black) :white :black))

(defn- apply-phase
  "Produce a vector of boards for certain phase."
  [moves initial-board initial-player move-fn]
  (loop [remaining-moves moves
         player initial-player
         board initial-board
         acc []]
    (if-let [[move-number current-move] (first remaining-moves)]
      (let [new-board (move-fn board current-move player)]
        (recur (rest remaining-moves)
               (alternate-player player)
               new-board
               (conj acc {:board new-board
                          :number move-number
                          :move current-move
                          :player player})))
      acc)))

(defn- handle-special-moves
  [move-fn]
  (fn [board move player]
    (cond
     (= move :pass) board
     (= move :resign) board
     :else (move-fn board move player))))

(defn- apply-dvonn-phase
  [moves]
  (apply-phase moves
               (empty-board)
               :white
               (handle-special-moves put-dvonn-piece)))

(defn- complement-last-put
  "Last player doesn't have too much of a choice so his stone is put automatically."
  [board player]
  (let [move (-> (filter #(= (get board %) :empty) (keys board)) first)]
    (put-piece board move player)))

(defn- apply-put-phase
  [moves board player]
  (let [states (apply-phase moves
                            board
                            player
                            (handle-special-moves put-piece))
        last-state (last states)
        last-index (dec (count states))
        new-board (complement-last-put (:board last-state)
                                       (alternate-player (:player last-state)))]
    (assoc-in states [last-index :board] new-board)))

(defn- apply-move-phase
  [moves board player]
  (apply-phase moves
               board
               player
               (handle-special-moves move-piece)))

(defn apply-all-moves
  [moves-by-phase]
  (let [dvonn-phase (apply-dvonn-phase (:dvonn moves-by-phase))
        last-dvonn (last dvonn-phase)
        put-phase (apply-put-phase (:put moves-by-phase)
                                   (:board last-dvonn)
                                   (alternate-player (:player last-dvonn)))
        last-put (last put-phase)
        move-phase (apply-move-phase (:move moves-by-phase)
                                     (:board last-put)
                                     (alternate-player (:player last-put)))]
    {:dvonn dvonn-phase
     :put put-phase
     :move move-phase}))
