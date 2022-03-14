(ns typing-ex.middleware
  (:require
   #_[ataraxy.core :as ataraxy]
   [ataraxy.response :as response]
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth.backends.session :refer [session-backend]]
   [buddy.auth.middleware :refer [wrap-authorization wrap-authentication]]
   [integrant.core :as ig]
   [ring.middleware.cors :refer [wrap-cors]]
   #_[ring.middleware.params :refer [wrap-params]]
   [taoensso.timbre :as timbre]))

(defn unauthorized-handler
  [request _]
  (if (authenticated? request)
    (throw-unauthorized) ;{:status 403 :body "error"}
    [::response/found  "/login"]))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defmethod ig/init-key :typing-ex.middleware/auth [_ _]
  (fn [handler]
    (-> handler
        (restrict {:handler authenticated?})
        (wrap-authorization  auth-backend)
        (wrap-authentication auth-backend))))

(defmethod ig/init-key :typing-ex.middleware/cors [_ _]
  (fn [handler]
    (-> handler
        (wrap-cors :access-control-allow-origin [#".*"]
                   :access-control-akkiw-methods #{:get :post :delete}))))

(defn probe [handler keys]
  (fn [req]
    (timbre/debug (select-keys req keys))
    (handler req)))

(defmethod ig/init-key :typing-ex.middleware/probe [_ _]
  (fn [handler]
    (-> handler
        (probe [:anti-forgery-token :session]))))
