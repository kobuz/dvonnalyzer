(ns dvonnalyzer.parser
  (:require [clojure.string :refer [split trim lower-case]]))

(defn- make-keyword-move
  [move]
  (cond
   (= (count move) 2) (keyword move)
   (= move "pass") :pass
   (= move "resign") :resign
   :else (vec (map keyword (map #(apply str %) (partition 2 move))))))

(defn- extract-moves
  "Returns sequence of moves as pair <number, move>."
  [game-file]
  (let [terms (-> game-file
                  (split #"\]")
                  last
                  trim
                  (split #" "))
                  unnecessary-number? #(re-find #"\d+\." %)
        final-terms (remove unnecessary-number? terms)
        keyword-moves (map make-keyword-move final-terms)]
    (map vector (iterate inc 1) keyword-moves)))

(defn- extract-metadata
  "Returns game metadata as map."
  [game-file]
  (->> game-file
       (re-seq #"(\w+) \[(.+?)\]")
       (map #(vector (-> (second %) lower-case keyword)
                     (last %)))
       flatten
       (apply hash-map)))

(defn- phase-pred
  [[idx mv]]
  (let [dvonn-count 3
        put-count 45]
    (cond
      (<= idx dvonn-count) :dvonn
      (<= idx (+ dvonn-count put-count)) :put
      :else :move)))

(defn- split-to-phases
  [moves]
  (->> moves
       (group-by phase-pred)
       (merge {:dvonn [] :put [] :move []})))

(defn parse-file
  [record]
  (let [metadata (extract-metadata record)
        moves (extract-moves record)
        moves-by-phase (split-to-phases moves)]
    {:metadata metadata
     :moves moves
     :moves-by-phase moves-by-phase}))
