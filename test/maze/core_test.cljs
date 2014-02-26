(ns maze.core-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require cemerick.cljs.test
            [maze.core :as core]))

(deftest test-neighbors
  (testing "returns all neighbors for a location"
    (is (= #{[2 1] [3 2] [2 3] [1 2]}
           (core/neighbors [2 2])))
    (is (= #{[0 -1] [1 0] [0 1] [-1 0]}
           (core/neighbors [0 0])))))

(deftest test-unvisited-neighbors
  (testing "returns a set of neighbors within bounds of maze"
    (is (= #{[1 0] [0 1]}
           (core/unvisited-neighbors [0 0] {:visited #{}
                                            :size 5})))
    (is (= #{[4 3] [3 4]}
           (core/unvisited-neighbors [4 4] {:visited #{}
                                            :size 5}))))
  (testing "returns all unvisited neighbors"
    (is (= #{[2 3] [1 2]}
           (core/unvisited-neighbors [2 2] {:visited #{[2 1] [3 2]}
                                            :size 5})))))

(defn dumb-next-location [location {:keys [visited size]}]
  (cond
    (= [0 0] location) (if (visited [1 0]) nil [1 0])
    (= [1 0] location) (if (visited [1 1]) nil [1 1])
    (= [1 1] location) (if (visited [0 1]) nil [0 1])
    (= [0 1] location) nil))

(deftest test-generate-maze
  (testing "contains the correct set of walls"
    (is (= #{#{[0 0] [0 1]}}
           (:walls
             (core/generate-maze {:size 2
                                  :next-location-fn dumb-next-location}))))))

(deftest test-solved?
  (testing "returns true if location is in bottom-right corner"
    (is (core/solved? [1 1] {:size 2})))
  (testing "returns false if location is not bottom-right corner"
    (not (core/solved? [0 1] {:size 2}))))

(deftest test-all-walls
  (testing "returns all walls for specified maze size"
    (is (= #{#{[0 0] [0 1]} #{[0 0] [1 0]} #{[1 0] [1 1]} #{[1 1] [0 1]}}
           (core/all-walls 2)))))

(deftest test-all-walls-without-doors
  (testing "returns all the walls when there are no doors"
    (is (= (core/all-walls 2)
           (core/all-walls-without-doors 2 #{}))))
  (testing "returns all walls for the maze with doors removed"
    (is (= #{#{[0 0] [0 1]}}
           (core/all-walls-without-doors 2 #{#{[0 0] [1 0]}
                                             #{[1 0] [1 1]}
                                             #{[1 1] [0 1]}})))))

(deftest test-reachable-neighbors
  (testing "returns the set of neighbors that are within the maze, unvisited
           and not blocked by walls"
    (is (= #{[1 0]} (core/reachable-neighbors [0 0] {:visited #{}
                                                     :walls #{#{[0 0] [0 1]}}
                                                     :size 2})))
    (is (= #{} (core/reachable-neighbors [0 0] {:visited #{}
                                                :walls #{#{[0 0] [0 1]} #{[0 0] [1 0]}}
                                                :size 2}))))
  (testing "returns unvisited neighbors when there are no walls"
    (is (= (core/unvisited-neighbors [0 0] {:visited #{[1 0]}
                                            :size 2})
           (core/reachable-neighbors [0 0] {:visited #{[1 0]}
                                            :size 2})))))

(deftest test-solve-maze
  (testing "it finds a path from top-left to bottom-right"
    (is (= [[0 0] [1 0] [1 1]]
           (:path
             (core/solve-maze {:walls #{#{[0 0] [0 1]}}
                               :size 2}))))))
