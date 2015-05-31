(ns dvonnalyzer.parser
  (:require [clojure.string :as str]))

(defn- extract-moves
  "Returns sequence of moves as pair <number, move>."
  [game-file]
  (let [terms (-> game-file
                  (str/split #"\]")
                  (last)
                  (str/trim)
                  (str/split #" "))
                  unnecessary-number? #(re-find #"\d+\." %)]
    (map vector (iterate inc 1) (remove unnecessary-number? terms))))

(defn- extract-metadata
  "Returns game metadata as map."
  [game-file]
  (->> game-file
       (re-seq #"(\w+) \[(.+?)\]")
       (map #(vector (-> (second %) str/lower-case keyword)
                     (last %)))
       flatten
       (apply hash-map)))

(defn- phase-pred
  [[idx mv]]
  (let [dvonn-count 3
        normal-count 46]
    (cond
      (<= idx dvonn-count) :dvonn
      (<= idx (+ dvonn-count normal-count)) :normal
      :else :move)))

(defn- split-to-phases
  [moves]
  (->> moves
       (group-by phase-pred)
       (merge {:dvonn [] :normal [] :move []})))

(defn parse-file
  [record]
  (let [metadata (extract-metadata record)
        moves (extract-moves record)
        moves-by-phase (split-to-phases moves)]
    {:metadata metadata
     :moves moves
     :moves-by-phase moves-by-phase}))
