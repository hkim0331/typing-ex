(ns typing-ex.plot
  #_(:require [taoensso.timbre :as timbre]))

(defn- frame [w h]
  [:svg {:width w :height h :viewBox (str "0 0 " w " " h)}
   [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
   [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]
   [:line {:x1 0 :y1 (- h 110) :x2 w :y2 (- h 110) :stroke "red"}]
   [:line {:x1 0 :y1 (- h 70) :x2 w :y2 (- h 70) :stroke "blue"}]
   [:line {:x1 0 :y1 (- h 40) :x2 w :y2 (- h 40) :stroke "green"}]])

(defn bar-chart [w h data]
  (let [n  (count data)
        dx (/ w (count data))]
    ;;(timbre/info "bar-chart" (take-last 3 data))
    (into
     (frame w h)
     (for [[x y] (map list (range n) (map :pt data))]
       ;; FIXME DRY!
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "green"}]))))

(defn scatter [w h data]
  (let [n  (count data)
        dx (/ w (count data))]
    (into
     (frame w h)
     (for [[x y] (map list (range n) (map :pt data))]
       ;; FIXME DRY!
       [:circle
        {:cx (* dx x) :cy (- h 10 y) :r 2 :fill "green"}]))))
