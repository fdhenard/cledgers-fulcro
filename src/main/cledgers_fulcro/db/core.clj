(ns cledgers-fulcro.db.core
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set]
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

(def JDBC_QUERY_OPTS {:builder-fn next.jdbc.result-set/as-modified-maps
                      :label-fn identity
                      :qualifier-fn #(get QUALIFIER_MAPPING % %)})


(comment

  (jdbc/execute! data-source ["select * from xaction limit 5"])

  )
