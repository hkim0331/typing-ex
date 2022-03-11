#!/usr/bin/env bb
;; 2021-06-06
;; synopsis:
;;  produce split every n lines into out/<n.txt>
;; usage:
;;  bb partition.clj <n> <in-file.txt>
;; example: (after)
;;  bb partition.clj 4 out.txt

(require '[clojure.string :refer [split-lines]])

(def num (atom 0))

(defn name []
  (swap! num inc)
  (str "out/" @num ".txt"))

(let [[n file] *command-line-args*
      text (partition (Integer. n) (split-lines (slurp file)))]
  (reset! num 0)
  (doseq [s text]
    (spit (name) (apply str (interpose "\n" s)))))
