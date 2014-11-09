(ns dvonnalyzer.game-test
  (:require [clojure.test :refer :all]
            [dvonnalyzer.game :as game]))

(deftest test-game
  (testing "char range"
    (is (= (game/char-range \a \d) [\a \b \c \d])))

  (testing "empty board"
    (let [board (game/empty-board)]
      (is (true? (contains? board :a1)))
      (is (false? (contains? board :a4)))
      (is (true? (contains? board :d1)))
      (is (true? (contains? board :d5)))
      (is (false? (contains? board :k2))))

  (testing "can put piece"
    (testing "on empty board"
      (let [board (game/empty-board)]
        (is (true? (game/can-put-piece? board :a1)))
        (is (true? (game/can-put-piece? board :b1)))
        (is (false? (game/can-put-piece? board :a4)))))
    (testing "on partially filled board"
      (let [board (game/put-piece (game/empty-board) :b1 :black)]
        (is (true? (game/can-put-piece? board :a1)))
        (is (false? (game/can-put-piece? board :b1)))))))

  (testing "put dvonn piece"
    (let [board (game/empty-board)]
      (is (= (:a1 (game/put-dvonn-piece board :a1))
             {:stack 1 :dvonn 1}))))

  (testing "put piece"
    (let [board (-> (game/empty-board)
                    (game/put-piece :a1 :black))]
      (is (= (:a1 board)
             {:color :black :stack 1 :dvonn 0}))))

  (testing "distance"
    (is (= (game/distance :a1 :a1) 0))
    (is (= (game/distance :a1 :a3) 2))
    (is (= (game/distance :a1 :e1) 4))
    (is (= (game/distance :a1 :c3) 2))
    (is (= (game/distance :a1 :b3) nil)))

  (testing "can move piece"
    (let [board (-> (game/empty-board)
                    (game/put-dvonn-piece :c2)
                    (game/put-piece :c1 :black)
                    (game/put-piece :b1 :white))]
      (is (true? (game/can-move-piece? board :c1 :c2 :black)))
      (is (false? (game/can-move-piece? board :c1 :c2 :white)))
      (is (false? (game/can-move-piece? board :c1 :c3 :black)))
      (is (true? (game/can-move-piece? board :c1 :b1 :black)))
      (is (true? (game/can-move-piece? board :c1 :d1 :black)))))

  (testing "move piece"
    (let [board (-> (game/empty-board)
                    (game/put-dvonn-piece :c2)
                    (game/put-piece :c1 :black)
                    (game/put-piece :c3 :white))]
      (testing "over dvonn piece"
        (let [board (game/move-piece board :c3 :c2)]
          (is (= (:c3 board) :empty))
          (is (= (:c2 board)
                 {:color :white
                  :stack 2
                  :dvonn 1}))))
      (testing "over opponents piece"
        (let [board (game/move-piece board :c3 :c1)]
          (is (= (:c3 board) :empty))
          (is (= (:c1 board)
                 {:color :white
                  :stack 2
                  :dvonn 0})))))))
