(ns typing-ex.plot)

(defn solve
  "連立方程式 ax + by = c, dx + ey = f を解く。"
  [[a b c] [d e f]]
  ;; (println a)
  ;; (println b)
  ;; (println c)
  ;; (println d)
  ;; (println e)
  ;; (println f)
  (let [denom (- (* b d) (* a e))]
    [(/ (- (* b f) (* c e)) denom)
     (/ (- (* c d) (* a f)) denom)]))

(comment
  (solve [1 2 3] [4 5 6])
  :rcf)

(defn lsm
  "least square sum method"
  [x y]
  (let [n   (count x)
        sx  (reduce + x)
        sy  (reduce + y)
        sxy (reduce + (map * x y))
        sx2 (reduce + (map * x x))]
    ;; (println x)
    ;; (println y)
    (solve [sx2 sx (- sxy)] [sx n (- sy)])))

(defn- frame [w h]
  [:svg {:width w :height h :viewBox (str "0 0 " w " " h)}
   [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
   [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]
   [:line {:x1 0 :y1 (- h 110) :x2 w :y2 (- h 110) :stroke "red"}]
   [:line {:x1 0 :y1 (- h 70) :x2 w :y2 (- h 70) :stroke "blue"}]
   [:line {:x1 0 :y1 (- h 40) :x2 w :y2 (- h 40) :stroke "green"}]])

(defn bar-chart [w h data]
  (let [n  (count data)
        dx (* 1.0 (/ w (count data)))]
    (into
     (frame w h)
     (for [[x y] (map list (range n) (map :pt data))]
       ;; FIXME DRY!
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "green"}]))))

(defn scatter
  "w: width
   h: height
   data: [1 2 3...]"
  [w h data]
  (let [n  (count data)
        dx (* 1.0 (/ w (count data)))
        xs (map #(* dx %) (range n))
        ys (map #(- h 10 %) data)
        [a b] (lsm xs ys)
        f #(+ (* (- a) %) (- b))]
    ;; (println "y = " a "x+" b)
    (conj
     (into
      (frame w h)
      (for [[x y] (map list xs ys)]
        [:circle
         {:cx x :cy y :r 2 :fill "green"}]))
     [:line {:x1 0 :y1 (f 0)
             :x2 w :y2 (f w)
             :stroke "yellow"
             :stroke-width 10}])))
