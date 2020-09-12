(ns cledgers-fulcro.mutations-server
  (:require [clojure.pprint :as pp]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as jdbc-sql]
            [mount.core :as mount]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.utils.utils :as utils]))


(pathom-connect/defmutation add-transaction [env params]
  {::pathom-connect/sym 'cledgers-fulcro.mutations-client/add-transaction}
  (let [#_ (println "hereeeeeeeeeeeeeeeee")
        #_ (pp/pprint {:muts-server--add-xaction {:params params}})
        params-renamed-kvs (map (fn [[k v]]
                                  [(utils/de-namespace-kw k)
                                   v])
                                params)
        params-renamed (into {} params-renamed-kvs)
        ledger-id (-> params-renamed :ledger second)
        payee-id (-> params-renamed :payee second)
        params-renamed (-> params-renamed
                           ;; TODO - fix date
                           (assoc :date (java.time.LocalDate/of 2020 9 1))
                           (assoc :uuid (-> params-renamed :id :id str))
                           ;; TODO - fix created_by_id
                           (assoc :created_by_id 1)
                           (dissoc :id)
                           (dissoc :payee)
                           (dissoc :ledger)
                           (assoc :payee_id payee-id)
                           (assoc :ledger_id ledger-id))
        #_ (pp/pprint {:mutations {:add-transaction {;; :db-ds (:db-datasource env)
                                                    :params-renamed params-renamed}}})
        insert-res (jdbc-sql/insert! (:db-datasource env)
                                     :xaction
                                     params-renamed)
        #_ (pp/pprint
           {:mutations-server
            {:add-transaction { ;; :env env
                               :params params
                               :params-renamed params-renamed
                               :insert-res insert-res}}})

        ]
    {:cledgers-fulcro.entities.transaction/id 99}
    ))


(def mutations [add-transaction])
#_(mount/defstate mutations
  :start [add-transaction])


(comment

  (keyword :testing/thing)
  (name :testing/thing)
  (utils/de-namespace-kw :testing/thing)

  )
