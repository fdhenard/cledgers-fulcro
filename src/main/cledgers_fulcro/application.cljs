(ns cledgers-fulcro.application
  (:require [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.networking.http-remote :as http]
            [cledgers-fulcro.ui.core :as ui]))



(defonce app (app/fulcro-app
              {:remotes {:remote (http/fulcro-http-remote {})}
               ;; :client-did-mount (fn [app]
               ;;                     (df/load! app :all-transactions ui/TransactionListItem))
               }))



(comment

  (fc-app/current-state app)


  )
