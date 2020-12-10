(ns clojello.core
  (:gen-class)
  (:use clojello.game
        [clojure.set :only (map-invert)]))

(defn get-index-array
  ([board locs] (get-index-array board locs []))
  ([board locs acc]
   (let [slocs (sort-by second locs)
         same-row? (fn [loc]
                     (= (second loc)
                        (second (first slocs))))]
     (if (empty? locs)
       acc
       (recur
        board
        (drop-while same-row? slocs)
        (conj acc
              (vec (take-while same-row? slocs))))))))

(defn get-array
  ([state] (get-array state {}))
  ([state exceptions]
   (let [board (state :board)
         locs (choices-of (map range (state :dimensions)))
         index-arr (get-index-array board locs)]
     (map (fn [row]
            (map (fn [loc]
                   (if (= (exceptions loc) nil)
                     (board loc)
                     (exceptions loc)))
                 row))
          index-arr))))

(defn string-array
  [array]
  (map (fn [row]
         (map (fn [cont]
                (cond
                  (= cont '.) " "
                  :else (str cont)))
              row))
       array))

(defn display-state
  [state]
  (map (fn [line] (apply println line))
       (string-array (get-array state))))

(defn alph-range [n]
  (let [abc "abcdefghijklmnopqrstuvwz"]
    (map (fn [i] (str (get abc i)))
         ; cast to str to get strings not characters
         ; (otherwise equality testing fails)
         (range n))))

(defn get-moves [state]
  (let [moves (all-moves state)]
    (pour-pairs-to-map
     {}
     (pair moves
           (alph-range
            (count moves))))))

(defn display-state-with-moves
  [state]
  (let [moves (get-moves state)]
    (doall (map (fn [line] (apply println line))
                (string-array
                 (get-array state moves))))))
; ^ doall required above because otherwise
;   the laziness of Clojure can stop it from printing.


(defn random-move
  [state moves]
  (second (first moves)))

(defn user-move
  [state moves]
  (let [in (read-line)]
    (if (contains? (map-invert moves) in)
      in
      (do (println "ERROR. Valid moves are: "
                   (vals moves))
        (recur state moves)))))

(defn state-after-move
  [decision-func state]
  (let [moves (get-moves state)
        move (decision-func state moves)
        move-loc ((map-invert moves) move)]
    (apply-move state move-loc)))

(defn end-print
  [state]
  (do
    (println "GAME OVER")
    (println "SCORE for"
             (state :players)
             "is"
             (score state) "respectively")
    (println "Final board:")
    (display-state-with-moves state)))

(defn state-print
  [state]
  (do
    (println "-------------------")
    (display-state-with-moves state)
    (println "-------------------")
    (println "Player" (first (state :players))
             "to play")))

(defn game-loop
  [state input-funcs]
  (if (ended? state)
    (end-print state)
    (do
      (state-print state)
      (let [next-state
            (state-after-move (first input-funcs)
                              state)]
        (recur next-state (cycle-left 1
                                      input-funcs))))))

(defn -main
  [& args]
  (game-loop
    start-state
    [user-move user-move]))
