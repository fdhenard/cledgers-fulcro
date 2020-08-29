(ns cledgers-fulcro.server
  (:require [clojure.pprint :as pp]
            [mount.core :as mount]
            [taoensso.timbre :as log]
            [org.httpkit.server :as http]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [com.fulcrologic.fulcro.server.api-middleware :as server]
            [cledgers-fulcro.parser :refer [api-parser]]))

(def ^:private not-found-handler
  (fn [req]
    (let [#_ (pp/pprint {:request req})]
     {:status 404
      :headers {"Content-type" "text/plain"}
      :body "Not Found"})))

(def middleware
  (-> not-found-handler
      (server/wrap-api {:uri "/api"
                        :parser api-parser})
      (server/wrap-transit-params)
      (server/wrap-transit-response)
      (wrap-resource "public")
      wrap-content-type))

(mount/defstate server
  :start (http/run-server middleware {:port 3000})
  ;; the result of run-server is the stop function
  :stop (server))
