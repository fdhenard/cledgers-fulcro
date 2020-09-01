(ns cledgers-fulcro.db.core
  (:require [next.jdbc :as jdbc]
            [mount.core :as mount]
            [cledgers-fulcro.config.core :as config]))


(mount/defstate data-source
  :start (let [{:keys [type name host username]} (:db config/config)]
           (jdbc/get-datasource
            {:dbtype type
             :dbname name
             :host host
             :user username})))

(def QUALIFIER_MAPPING {"xaction" "cledgers-fulcro.models.transaction"
                        "ledger" "cledgers-fulcro.models.ledger"
                        "payee" "cledgers-fulcro.models.payee"})


(comment

  (jdbc/execute! data-source ["select * from xaction limit 5"])

  )
