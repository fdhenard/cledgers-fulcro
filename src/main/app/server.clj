(ns app.server
  (:require [taoensso.timbre :as log]
            [org.httpkit.server :as http]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [com.fulcrologic.fulcro.server.api-middleware :as server]
            [app.parser :refer [api-parser]]))

(def ^:private not-found-handler
  (fn [req]
    {:status 404
     :headers {"Content-type" "text/plain"}
     :body "Not Found"}))

(def middleware
  (-> not-found-handler
      (server/wrap-api {:uri "/api"
                        :parser api-parser})
      (server/wrap-transit-params)
      (server/wrap-transit-response)
      (wrap-resource "public")
      wrap-content-type))

(defonce stop-fn (atom nil))

(defn start []
  (reset! stop-fn (http/run-server middleware {:port 3000}))
  (log/info "started"))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
