(ns typing-ex.boundary.exam-mode
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]))

(defprotocol Exam-Mode
  (exam-mode [db])
  (toggle-exam-mode [db]))

(extend-protocol Exam-Mode
  duct.database.sql.Boundary
  (exam-mode [db]
    (let [ret (first
               (sql/query
                (ds-opt db)
                ["select exam from exam_mode"]))]
      (println "ret " (str ret))
      ret)) ;; (:exam ret) で true/false を返すことができない。
            ;; 返るのは {:exam true/false}
  ;;
  ;; FIXME:これができてない。
  (toggle-exam-mode [db]
    (let [{mode :exam} (exam-mode db)
          _ (println "mode " mode)
          _ (jdbc/execute! (ds-opt db)
                           ["update exam_mode set exam=NOT ?" mode])]
      {:ret 1})))