(ns pentago-web.models.game)

(def players (cycle ["X" "O"]))

(def starting-board
  [[1 2 3 4 5 6]
   [7 8 9 10 11 12]
   [13 14 15 16 17 18]
   [19 20 21 22 23 24]
   [25 26 27 28 29 30]
   [31 32 33 34 35 36]])

(defn available? [space board]
  (some (fn [brick] (some #(= %1 space) brick)) board))

(defn move [player space board]
  (letfn [(move-in-row
            ([accum row]
             (cond
               (empty? row) accum
               (= space (first row)) (concat accum (cons player (rest row)))
               true (recur (concat accum (list (first row))) (rest row))))
             ([row] (move-in-row '() row)))]
            (map move-in-row board)))

(defn space->string [space]
  (if (number? space)
    (if (< 9 space)
      (str " " space " ")
      (str "  " space " "))
    (str "  " space " ")))

(defn row->string [row] (apply str (interpose "|" (map space->string row))))
(defn board->string [board]
  (apply str (interpose "\n----+----+----+----+----+----\n" (map row->string board))))

(defn print-board [board]
  (do
    (newline)
    (newline)
    (print (board->string board))
    (newline)
    (newline)))

(defn full? [board]
  (not (some number? (mapcat identity board))))

(defn get-row [index board]
  (nth board index))

(defn get-column [index board]
  (map #(nth %1 index) board))

(defn five-in-a-row? [sq]
  (or
    (apply = (rest sq))
    (apply = (rest (reverse sq)))))

(def indexes '(0 1 2 3 4 5))

(defn get-indexes [sq board]
  (map #(get-row (first %1) (get-column (second %1) board)) sq))

(map five-in-a-row? 
     (get-indexes [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5]] starting-board))

;(let bind2nd f y = fun x -> f x y)

(defn winner? [board]
  (or (some #(= true %1) (map five-in-a-row? (map #(get-row %1 board) indexes)))
      (some #(= true %1) (map five-in-a-row? (map #(get-column %1 board) indexes)))
      (five-in-a-row? (get-indexes [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5]] board))
      (five-in-a-row? (get-indexes [[0 5] [1 4] [2 3] [3 2] [4 1] [5 0]] board))
      (apply = (get-indexes [[1 0] [2 1] [3 2] [4 3] [5 4]] board))
      (apply = (get-indexes [[0 1] [1 2] [2 3] [3 4] [4 5]] board))
      (apply = (get-indexes [[0 4] [1 3] [2 2] [3 1] [4 0]] board))
      (apply = (get-indexes [[1 5] [2 4] [3 3] [4 2] [5 1]] board))))

(defn get-corner [corner board] 
  (cond
    (= 1 corner) 
      (get-indexes [[0 0] [0 1] [0 2] 
                    [1 0] [1 1] [1 2]
                    [2 0] [2 1] [2 2]] board)
    (= 2 corner)
      (get-indexes [[0 3] [0 4] [0 5] 
                    [1 3] [1 4] [1 5]
                    [2 3] [2 4] [2 5]] board)
    (= 3 corner) 
      (get-indexes [[3 0] [3 1] [3 2] 
                    [4 0] [4 1] [4 2]
                    [5 0] [5 1] [5 2]] board)
    (= 4 corner) 
      (get-indexes [[3 3] [3 4] [3 5] 
                    [4 3] [4 4] [4 5]
                    [5 3] [5 4] [5 5]] board)))

(defn turn-corner [corner dir]
  (let [[a b c d e f g h i] corner] dir
    (if (= 0 dir)
      (list g d a h e b i f c)
      (list c f i b e h a d g))))

(defn list->board [sq]
  (loop [coll sq i 0 acc [[][][][][][]]]
    (if (empty? coll)
      acc
      (recur (drop 6 coll) (inc i) (assoc acc i (vec (take 6 coll)))))))

(defn connect-corners [c1 c2]
  (loop [c1 c1 c2 c2 acc '()]
    (if (empty? c1)
      acc
      (recur (drop 3 c1) (drop 3 c2) (concat acc (concat (take 3 c1) (take 3 c2)))))))


(defn turn [corner dir board]
  (cond 
    (= 1 corner)
    (list->board 
      (concat 
        (connect-corners (turn-corner (get-corner 1 board) dir) 
                         (get-corner 2 board))
        (connect-corners (get-corner 3 board) 
                         (get-corner 4 board))))
    (= 2 corner)
    (list->board 
      (concat 
        (connect-corners (get-corner 1 board) 
                         (turn-corner (get-corner 2 board) dir))
        (connect-corners (get-corner 3 board) 
                         (get-corner 4 board))))
    (= 3 corner)
    (list->board
      (concat
        (connect-corners (get-corner 1 board)
                         (get-corner 2 board))
        (connect-corners (turn-corner (get-corner 3 board) dir)
                         (get-corner 4 board))))
    (= 4 corner)
    (list->board
      (concat
        (connect-corners (get-corner 1 board)
                         (get-corner 2 board))
        (connect-corners (get-corner 3 board)
                         (turn-corner (get-corner 4 board) dir))))))


(defn restore-board [board]
  (loop [sq (flatten board) i 0 acc '()]
    (if (empty? sq)
      (list->board (reverse acc))
      (recur (rest sq) (inc i) (cons (if (number? (first sq)) (inc i) (first sq)) acc)))))

;; (defn play [board players]
;;   (do
;;     (print-board board)
;;     (let [player (first players)]
;;       (cond
;;         (winner? board) (print (str "*** Player: " (first (next players)) " Wins! ***\n"))
;;         (full? board) (print "It's a Draw!\n")
;;         true (do
;;                (print (str "Select a square, " player ": "))
;;                (flush)
;;                (let [square (read)]
;;                  (if (and square (available? square board))
;;                    (do
;;                      (let [b (move player square board)]
;;                        (print-board b)
;;                        (print "Select a corner to turn: ")
;;                        (flush)
;;                        (let [corner (read)]
;;                          (print "Direction? (0 = CW, 1 = CCW) ")
;;                          (flush)
;;                          (let [dir (read)]
;;                            (recur (restore-board (turn corner dir b)) (next players))))))
;;                    (recur board players))))))))
;; 
;; 
;; (play starting-board players)
