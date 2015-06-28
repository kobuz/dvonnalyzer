(ns dvonnalyzer.game)

(def blank :blank)

(defn blank?
  ([p] (= p blank))
  ([board p] (blank? (get board p))))

(defn abs [n] (if (pos? n) n (- n)))

(defn char-range
  "Return range of characters including end."
  [start end]
  (map char (range (.charCodeAt start) (inc (.charCodeAt end)))))

(defn empty-board
  "Generate empty board as hashmap with values of blank."
  []
  (let [rect-grid (for [x (char-range "a" "k") y (range 1 6)]
                    (keyword (str x y)))
        dvonn-grid (remove #{:a4 :a5 :b5 :j1 :k1 :k2} rect-grid)]
    (zipmap dvonn-grid (repeat blank))))

(defn make-move
  [& {:keys [stack dvonn color]
      :or {stack 1 dvonn 0}}]
  (let [move {:stack stack :dvonn dvonn}]
    (if (nil? color)
      move
      (assoc move :color color))))

(defn can-put-piece?
  [board to]
  (blank? board to))

(defn put-dvonn-piece
  ([board to] (assoc board to (make-move :stack 1 :dvonn 1)))
  ([board to player] (put-dvonn-piece board to)))

(defn put-piece
  [board to player]
  (assoc board to (make-move :color player :stack 1)))

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
  (let [origin (get board from)
        target (get board to)]
    (and origin
         target
         (= (:color origin) player)
         (= (distance from to)
            (:stack origin)))))

(declare drop-dead-spots)

(defn move-piece
  ([board [from to]]
    (let [sum (fn [key] (->> [from to]
                             (map #(get-in board [% key] 0))
                             (reduce +)))]
      (drop-dead-spots
       (merge board {from blank
                     to (make-move :color (get-in board [from :color])
                                   :stack (sum :stack)
                                   :dvonn (sum :dvonn))}))))
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
  (let [move (-> (filter #(= (get board %) blank) (keys board)) first)]
    (put-piece board move player)))

(defn find-dvonn
  [board]
  (for [[coord stack] board
    :when (> (get stack :dvonn 0) 0)]
    coord))

(defn coord->vec
  "Change symbolic coordinates to vector of ints, eg. \"b5\" -> [1, 4]"
  [s]
  (let [to-int #(-> s name % .charCodeAt)]
    (vector (- (to-int first) (.charCodeAt "a"))
            (- (to-int second) (.charCodeAt "1")))))

(defn vec->coord
  [v]
  (keyword (apply str
                  (map #(String/fromCharCode %) [(+ (first v) (.charCodeAt "a"))
                                                 (+ (second v) (.charCodeAt "1"))]))))

(defn neighbours
  "Get non-empty adjacent positions."
  [board from]
  (let [curr (coord->vec from)
        moves [[0 1]
               [0 -1]
               [1 0]
               [-1 0]
               [1 1]
               [-1 -1]]]
    (->> (map #(map + curr %) moves)
         (map vec->coord)
         (filterv #(not= (get board % blank) blank)))))

(defn find-accessible
  "Find spots accessible from the source with simplified DFS."
  [board from]
  (loop [visited (hash-set from)
         queue (list from)]
    (if-let [node (first queue)]
      (let [nei (neighbours board node)
            new-nei (remove visited nei)]
        (recur (into #{} (concat visited new-nei))
               (concat (rest queue) new-nei)))
      (vec visited))))

(defn- drop-dead-spots
  "Remove spots that have no connection to dvonn pieces."
  [board]
  (let [dvonn-pieces (find-dvonn board)
        accessible (set (mapcat (partial find-accessible board) dvonn-pieces))]
    (merge (empty-board) (filter #(accessible (first %)) board))))

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
