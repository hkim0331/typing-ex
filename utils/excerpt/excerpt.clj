#!/usr/bin/env bb
;; 2021-06-06
;; excerpt text for typing-ex from given files

(require '[clojure.string :refer [replace split-lines starts-with?]])

(defn print-non-empty [line]
  (when-not (re-matches #"^\s*$" line)
    (println line)))

(defn excerpt [file-name]
  (let [in? (atom false)]
    (doseq [line (-> file-name slurp split-lines)]
      (when (or (starts-with? line "D:") (starts-with? line "S:"))
        (reset! in? true))
      (when (re-matches #"^\s*$" line)
        (reset! in? false))
      (when @in?
        (-> (replace line #"^.:" "")
            (replace #"^\s+" "")
            print-non-empty)))))

;;(excerpt "q.typ")
(doseq [typ *command-line-args*]
  (excerpt typ))
