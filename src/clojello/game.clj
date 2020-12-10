(ns clojello.game
  (:use [clojure.set :only (union)]))

(defn choices-of
  ([choice-lists]
   (choices-of
    (rest choice-lists)
    (first choice-lists)
    '() '(())))
  ([choice-lists choices-left acc prev-acc]
     (if (empty? choices-left)
       (if (empty? choice-lists)
         acc
         (recur (rest choice-lists) (first choice-lists)
           '() acc))
       (recur
         choice-lists
         (rest choices-left)
         (concat acc
                 (map (fn [t]
                        (conj t
                              (first choices-left)))
                      prev-acc))
         prev-acc))))

(defn pair
  ([lista listb] (pair lista listb '()))
  ([lista listb acc]
  (if (empty? lista)
    acc
    (recur (rest lista) (rest listb)
      (conj acc (list (first lista) (first listb)))))))

(defn pour-pairs-to-map
  [m pairs]
  (if (empty? pairs)
    m
    (let [pair (first pairs)]
      (pour-pairs-to-map (assoc m
                                (first pair)
                                (second pair))
                         (rest pairs)))))

(defn board
  ([xi-sizes]
   (pour-pairs-to-map
    {}
    (pair (choices-of
           (map (fn [is] (range is))
                xi-sizes))
          (repeat (reduce * xi-sizes) '.))))
  ([xs ys] (board [xs ys]))
  ([] (board 8 8)))

(defn new-state
  ([] (new-state [8 8] ['X 'O]))
  ([dimension-sizes players]
    {:board (board dimension-sizes)
     :players players
     :dimensions dimension-sizes
     :ended false}))

(defn ended? [state] (state :ended))

(defn place-starting-pieces
  [state]
  (let [mids (map (fn [xsize]
                    (let [mid (int (/ xsize 2))]
                      [(dec mid) mid]))
                  (state :dimensions))]
    (assoc state :board
           (pour-pairs-to-map
            (state :board)
            (map (fn [loc]
                   (list loc
                         ((state :players)
                          (mod (reduce + loc)
                               (count (state :players))))))
                 (choices-of mids))))))

(def start-state (place-starting-pieces (new-state)))

(defn v+
  ([vec1] vec1)
  ([vec1 vec2 & args]
   (let [res (map (partial reduce +)
                  (pair (reverse vec1)
                        (reverse vec2)))]
     (if (empty? args)
       res
       (apply v+ res args)))))

(defn in-range-f [a b] (fn [n] (and (<= a n) (> b n))))
(defn andf [a b] (and a b))
;^ required because the and macro can't be applied
(defn out-of-bounds?
  [state loc]
  (reduce andf
          (map (fn [p] (not (apply (first p) (rest p))))
               (pair (map
                      (fn [dim] (in-range-f 0 dim))
                      (state :dimensions))
                     loc))))

(defn crawler
  [state loc delta continue? success? return-func out-func acc]
  (if (out-of-bounds? state loc)
    (out-func state)
    (let [cell-cont ((state :board) loc)]
      (if (continue? cell-cont)
        (recur state (v+ loc delta) delta
          continue? success? return-func out-func
          (conj acc loc))
        (if (and (> (count acc) 0) (success? cell-cont))
          (return-func state loc acc)
          (out-func state))))))

(defn not-all-zero? [d]
  (not (= 0 (reduce + (map (fn [x] (* x x)) d)))))
(defn move-dirs [state]
  (filter not-all-zero?
   (choices-of (repeat (count (state :dimensions))
                       '(-1 0 1)))))

(defn equalsf [val] (fn [input] (= input val)))
(defn moves-crawler
  [state loc]
  (apply union
    (map (fn [delta]
           (crawler
            state (v+ loc delta) delta
            (equalsf ((state :players) 1))
            (equalsf '.)
            (fn [state loc acc] #{loc})
            (fn [state] #{})
            #{}))
         (move-dirs state))))

(defn all-moves
  [state]
  (apply union
    (map (fn [loc-cont]
           (moves-crawler state (first loc-cont)))
         (filter
          (fn [key-val]
            (= (second key-val)
               ((state :players) 0)))
          (state :board)))))

(defn move-applier
  [state loc dirs]
  (if (empty? dirs)
    state
    (recur
      (crawler state (v+ loc (first dirs)) (first dirs)
               (equalsf ((state :players) 1))
               (equalsf ((state :players) 0))
               (fn [state loc acc]
                 (assoc
                  state :board
                  (pour-pairs-to-map
                   (state :board)
                   (pair acc (repeat (count acc)
                                     ((state :players) 0))))))
               (fn [state] state)
               #{})
      loc
      (rest dirs))))

(defn cycle-left
  [n l]
  (let [N (count l)
        n (mod n N)]
    (reverse (concat
              (reverse (take n l))
              (reverse (drop n l))))))

(defn turn-transition
  [state]
  (assoc state
         :players (vec (cycle-left 1
                                   (state :players)))))
(defn place-piece
  [state loc]
  (assoc state :board
         (assoc (state :board)
                loc
                (first (state :players)))))

(defn apply-move
  [state loc]
  (let [candidate-state
        (turn-transition
         (move-applier (place-piece state loc)
                       loc
                       (move-dirs state)))]
    (if (> (count (all-moves candidate-state)) 0)
      candidate-state
      (if (> (count (all-moves (turn-transition
                                candidate-state)))
             0)
        (turn-transition candidate-state)
        (assoc candidate-state
               :ended true)))))

(defn score
  [state]
  (map (fn [player]
         (reduce
          (fn [n board-loc]
            (if (= (second board-loc)
                   player)
              (+ n 1)
              n))
          0
          (state :board)))
       (state :players)))
