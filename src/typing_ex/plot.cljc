(ns typing-ex.plot)

(defn- frame [w h]
 [:svg {:width w :height h :viewBox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]
      [:line {:x1 0 :y1 (- h 110) :x2 w :y2 (- h 110) :stroke "red"}]
      [:line {:x1 0 :y1 (- h 70) :x2 w :y2 (- h 70) :stroke "blue"}]
      [:line {:x1 0 :y1 (- h 40) :x2 w :y2 (- h 40) :stroke "green"}]])

;; plot は適当な名前ではない。bar-chart?
(defn plot [w h data]
  (let [n  (count data)
        dx (/ w (count data))]
    (into
     (frame w h)
     (for [[x y] (map list (range n) (map :pt data))]
       ;; scatter と以下が異なるだけ。DRY!
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "green"}]))))

(defn scatter [w h data]
  (let [n  (count data)
        dx (/ w (count data))]
    (into
     (frame w h)
     (for [[x y] (map list (range n) (map :pt data))]
       [:circle
        {:cx (* dx x) :cy (- h 10 y) :r 2 :fill "green"}]))))
