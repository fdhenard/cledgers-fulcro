(ns cledgers-fulcro.resolvers
  (:require [clojure.pprint :as pp]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set]
            [tick.alpha.api :as tick]
            [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.db.core :as db]))

#_(def ledgers-table
  {1 #:cledgers-fulcro.models.ledger{:id 1 :name "ledger 1"}
   2 #:cledgers-fulcro.models.ledger{:id 2 :name "ledger 2"}
   3 #:cledgers-fulcro.models.ledger{:id 3 :name "ledger 3"}})

#_(def payees-table
  {1 #:cledgers-fulcro.models.payee{:id 1 :name "payee 1"}
   2 #:cledgers-fulcro.models.payee{:id 2 :name "payee 2"}
   3 #:cledgers-fulcro.models.payee{:id 3 :name "payee 3"}})

#_(def transactions-table
  {1 #:cledgers-fulcro.models.transaction{:id 1
                                          :date "2020"
                                          :payee {:cledgers-fulcro.models.payee/id 1}
                                          :ledger {:cledgers-fulcro.models.ledger/id 1}
                                          :description "soem descrip"
                                          :amount "1111.11"}})

(pathom-connect/defresolver ledger-resolver [env {:cledgers-fulcro.models.ledger/keys [id]}]
  {::pathom-connect/input #{:cledgers-fulcro.models.ledger/id}
   ::pathom-connect/output [:cledgers-fulcro.models.ledger/name]}
  (let [result (jdbc/execute! (:db-datasource env)
                              ["select * from ledger where id = ?" id]
                              db/JDBC_QUERY_OPTS)
        #_ (pp/pprint {:ledger-resolver {:result result}})]
    (first result)))


(pathom-connect/defresolver all-ledgers-resolver [env {:cledgers-fulcro.models.ledger/keys [id]}]
  {::pathom-connect/output [{:all-ledgers [:cledgers-fulcro.models.ledger/id
                                           :cledgers-fulcro.models.ledger/name]}]}
  (let [result (jdbc/execute! (:db-datasource env)
                              ["select * from ledger"]
                              db/JDBC_QUERY_OPTS)
        #_ (pp/pprint {:all-ledgers-resolver {:result result}})]
   {:all-ledgers result}))

(pathom-connect/defresolver payee-resolver [env {:cledgers-fulcro.models.payee/keys [id]}]
  {::pathom-connect/input #{:cledgers-fulcro.models.payee/id}
   ::pathom-connect/output [:cledgers-fulcro.models.payee/name]}
  (let [result (jdbc/execute! (:db-datasource env)
                              ["select * from payee where id = ?" id]
                              db/JDBC_QUERY_OPTS)
        #_ (pp/pprint {:payee-resolver {:result result}})]
    (first result)))

(pathom-connect/defresolver all-payees-resolver [env {:cledgers-fulcro.models.payee/keys [id]}]
  {::pathom-connect/output [{:all-payees [:cledgers-fulcro.models.payee/id
                                          :cledgers-fulcro.models.payee/name]}]}
  (let [result (jdbc/execute! (:db-datasource env)
                              ["select * from payee"]
                              db/JDBC_QUERY_OPTS)
        #_ (clojure.pprint/pprint result)]
   {:all-payees result}))


#_(pathom-connect/defresolver ledgers-q-resolver [env {:keys [query] :as params}]
  {::pathom-connect/input #{:query}
   ::pathom-connect/output [{:q-results [:cledgers-fulcro.models.ledger/id]}]}
  (let [_ (clojure.pprint/pprint {:params params})
        result (jdbc/execute! db/data-source
                              ["select id from ledger where name like '?%'" query]
                              db/JDBC_QUERY_OPTS)]
    {:q-results result}
    #_{:q-results (mapv
                #(hash-map :cledgers-fulcro.models.ledger/id %)
                (keys ledgers-table))}))

#_(pathom-connect/defresolver answer-plus-one-resolver [env {:keys [input] :as params}]
  {::pathom-connect/input #{:input}
   ::pathom-connect/output [:answer-plus-one]}
  (let [_ (clojure.pprint/pprint {:params params})]
    {:answer-plus-one (inc input)}))

(defn db-xaction->fulcro-xaction [db-xaction]
  (let [date-tagged (-> db-xaction
                        :cledgers-fulcro.models.transaction/date
                        str
                        tick/date
                        pr-str)
        #_ (pp/pprint {:all-transactions-resolver {:date-as-str date-as-str}})]
    (-> db-xaction
        (assoc :cledgers-fulcro.models.transaction/payee
               {:cledgers-fulcro.models.payee/id
                (:cledgers-fulcro.models.transaction/payee_id db-xaction)})
        (assoc :cledgers-fulcro.models.transaction/ledger
               {:cledgers-fulcro.models.ledger/id
                (:cledgers-fulcro.models.transaction/ledger_id db-xaction)})
        (assoc :cledgers-fulcro.models.transaction/date
               date-tagged))))

(pathom-connect/defresolver transaction-resolver [env {:cledgers-fulcro.models.transaction/keys [id]}]
  {::pathom-connect/input #{:cledgers-fulcro.models.transaction/id}
   ::pathom-connect/output [:cledgers-fulcro.models.transaction/date
                            :cledgers-fulcro.models.transaction/payee
                            :cledgers-fulcro.models.transaction/ledger
                            :cledgers-fulcro.models.transaction/description
                            :cledgers-fulcro.models.transaction/amount]}
  (let [result (-> (jdbc/execute! (:db-datasource env)
                                  ["select * from transaction where id = ?" id]
                                  db/JDBC_QUERY_OPTS)
                   first
                   db-xaction->fulcro-xaction)]
    result))

(pathom-connect/defresolver all-transactions-resolver [env {:cledgers-fulcro.models.transaction/keys [id]}]
  {::pathom-connect/output [{:all-transactions [:cledgers-fulcro.models.transaction/id]}]}
  (let [#_ (println "\ninvoking all-transactions-resolver. result:")
        #_ (pp/pprint {:ds (:db-datasource env)})
        result (->> (jdbc/execute! (:db-datasource env)
                               ["select * from xaction"]
                               db/JDBC_QUERY_OPTS)
                    (map db-xaction->fulcro-xaction))
        #_#_result (map db-xaction->fulcro-xaction result)
        #_ (clojure.pprint/pprint {:result result})]
   {:all-transactions result}))

(def resolvers [ledger-resolver
                all-ledgers-resolver
                ;; ledgers-q-resolver
                ;; answer-plus-one-resolver
                payee-resolver
                all-payees-resolver
                ;; transaction-resolver
                all-transactions-resolver
                ])



(comment

  (def an-xaction (->>
                   (jdbc/execute! db/data-source
                                  ["select * from xaction limit 1"]
                                  {:builder-fn next.jdbc.result-set/as-modified-maps
                                   :label-fn identity
                                   :qualifier-fn #(get db/QUALIFIER_MAPPING % %)})
                   first))

  an-xaction
  (def the-date (:cledgers-fulcro.models.transaction/date an-xaction))
  the-date
  (require '[tick.alpha.api :as tick])
  (str the-date)
  (-> the-date str tick/new-date)
  (-> the-date str tick/date)

  (println "hi")

  )
