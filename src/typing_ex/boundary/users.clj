(ns typing-ex.boundary.users
  (:require
   [typing-ex.boundary.utils :refer [ds-opt]]
   #_[next.jdbc :refer [with-transaction] :as jdbc]
   [next.jdbc.sql :as sql]
   [duct.database.sql]
   [taoensso.timbre :as timbre]))

(defprotocol Users
  (insert-user [db user])
  (find-user-by-login [db login])
  (update-user [db m id])
  #_(delete-user-by-id [db id]))

(extend-protocol Users
  duct.database.sql.Boundary
  (insert-user [db user]
    (try
      (let [ret (sql/insert! (ds-opt db) :users user)]
        ;; FIXME: debug も println も表示されない。
        (timbre/info "insert-user" ret)
        true)
      (catch Exception e
        (timbre/info (.getMessage e))
        false)))

  (find-user-by-login [db login]
    (let [ret (sql/query
               (ds-opt db)
               ["select * from users where login=?" login])]
      ;; not found?
      (-> ret first)))

  (update-user [db m id]
    (let [ret (sql/update!
               (ds-opt db)
               :users m {:id id})]
      (timbre/info "update-user returned" ret)
      (= 1 (:next.jdbc/update-count ret)))))

    ;; FIXME, まだ作ってない。削除のキーは id でいいか？
  ;;(delete-user-by-id [db id] nil)))
