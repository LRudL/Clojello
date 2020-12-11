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
  (let [abc "abcdefghijklmnpqrstuvwz123456789"]
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

(defn user-choice
  [choices]
  (let [in (read-line)]
    (if (contains? choices in)
      in
      (do (println "INVALID INPUT. Valid inputs are: "
                   (vec choices))
        (recur choices)))))

(defn user-move
  [state moves]
  (user-choice (set (concat '("quit" "undo") (vals moves)))))
; ^ allow Q and U (quit game and undo move)

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
    (apply println (repeat (second (state :dimensions)) \-))
    (display-state-with-moves state)
    (apply println (repeat (second (state :dimensions)) \-))
    (println "Player" (first (state :players))
             "to play")))

(defn game-loop
  [state input-funcs]
  (if (ended? state)
    (end-print state)
    (do
      (state-print state)
      (let [moves (get-moves state)
            in (str ((first input-funcs) state moves))]
        (cond
          (= in "quit") (println "Game was quit.")
          (= in "undo") (let [h (state :history)
                              hlen (count h)]
                          (cond
                            (= hlen 0) (recur state input-funcs)
                            :else (recur
                                    (first h)
                                    (cycle-left -1 input-funcs))))
          :else
          (let [next-state
                (apply-move state
                            ((map-invert moves) in))]
            (recur next-state (cycle-left 1 input-funcs))))))))

(def board-size-choices (set (map str (range 4 21))))

(defn get-user-choices
  ([choices] (get-user-choices choices []))
  ([choices acc]
   (if (empty? choices)
     acc
     (let [choice-name (first (first choices))
           options (second (first choices))]
       (do
         (println "Enter" choice-name)
         (recur
           (rest choices)
           (conj acc (user-choice options))))))))

(defn game-config
  []
  (do
    (println "--CLOJELLO--")
    (println "Press enter for default game,"
             "enter anything else to customise.")
    (println
     "Type undo/quit to undo a move / quit the game.")
    (if (= (read-line) "")
      (game-loop start-state [user-move user-move])
      (let [options (get-user-choices
                     [["board height" board-size-choices]
                      ["board width" board-size-choices]
                      ["number of players" #{"2" "3"}]])]
        (game-loop (place-starting-pieces
                    (apply new-state (map read-string options)))
          [user-move user-move])))))

(defn -main
  [& args]
  (game-config))
