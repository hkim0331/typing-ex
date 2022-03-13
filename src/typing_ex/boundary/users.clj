(ns typing-ex.boundary.users
  (:require
   [typing-ex.boundary.utils :refer [ds ds-opt]]
   #_[next.jdbc :refer [with-transaction] :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]
   [taoensso.timbre :as timbre]))

(defprotocol Users
  (insert-user [db user])
  (find-user-by-nick [db nick])
  (update-user [db m id])
  #_(delete-user-by-id [db id]))

(extend-protocol Users
  duct.database.sql.Boundary
  (insert-user [db user]
    (try
      (let [ret (sql/insert! (ds db) :users user)]
        ;; FIXME: debug も println も表示されない。
        (timbre/info "insert-user" ret)
        true)
      (catch Exception e
        (timbre/info (.getMessage e))
        false)))

  (find-user-by-nick [db nick]
    (let [ret (sql/query
               (ds-opt db)
               ["select * from users where nick=?" nick])]
      ;; not found?
      (-> ret first)))

  (update-user [db m id]
    (let [ret (sql/update!
               (ds db)
               :users m {:id id})]
      (timbre/info "update-user returned" ret)
      (= 1 (:next.jdbc/update-count ret)))))

    ;; FIXME, まだ作ってない。削除のキーは id でいいか？
  ;;(delete-user-by-id [db id] nil)))
