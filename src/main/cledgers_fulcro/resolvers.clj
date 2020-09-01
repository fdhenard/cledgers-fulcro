(ns cledgers-fulcro.resolvers
  (:require [clojure.pprint :as pp]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set]
            [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.db.core :as db]))

(def ledgers-table
  {1 #:cledgers-fulcro.models.ledger{:id 1 :name "ledger 1"}
   2 #:cledgers-fulcro.models.ledger{:id 2 :name "ledger 2"}
   3 #:cledgers-fulcro.models.ledger{:id 3 :name "ledger 3"}})

(def payees-table
  {1 #:cledgers-fulcro.models.payee{:id 1 :name "payee 1"}
   2 #:cledgers-fulcro.models.payee{:id 2 :name "payee 2"}
   3 #:cledgers-fulcro.models.payee{:id 3 :name "payee 3"}})

(def transactions-table
  {1 #:cledgers-fulcro.models.transaction{:id 1
                                          :date "2020"
                                          :payee {:cledgers-fulcro.models.payee/id 1}
                                          :ledger {:cledgers-fulcro.models.ledger/id 1}
                                          :description "soem descrip"
                                          :amount "1111.11"}})

#_(pathom-connect/defresolver ledger-resolver [env {:cledgers-fulcro.models.ledger/keys [id]}]
  {::pathom-connect/input #{:cledgers-fulcro.models.ledger/id}
   ::pathom-connect/output [:cledgers-fulcro.models.ledger/name]}
  (let [result (get ledgers-table id)]
    result))


(pathom-connect/defresolver all-ledgers-resolver [env {:cledgers-fulcro.models.ledger/keys [id]}]
  {::pathom-connect/output [{:all-ledgers [:cledgers-fulcro.models.ledger/id
                                           :cledgers-fulcro.models.ledger/name]}]}
  (let [#_ (clojure.pprint/pprint {:id-in id})
        ;; result (mapv
        ;;           #(hash-map :cledgers-fulcro.models.ledger/id %)
        ;;           (keys ledgers-table))
        result (jdbc/execute! db/data-source
                              ["select * from ledger"]
                              {:builder-fn next.jdbc.result-set/as-modified-maps
                               :label-fn identity
                               :qualifier-fn #(get db/QUALIFIER_MAPPING % %)})
        _ (pp/pprint {:all-ledgers-resolver {:result result}})]
   {:all-ledgers result}))

#_(pathom-connect/defresolver payee-resolver [env {:cledgers-fulcro.models.payee/keys [id]}]
  {::pathom-connect/input #{:cledgers-fulcro.models.payee/id}
   ::pathom-connect/output [:cledgers-fulcro.models.payee/name]}
  (get payees-table id))

(pathom-connect/defresolver all-payees-resolver [env {:cledgers-fulcro.models.payee/keys [id]}]
  {::pathom-connect/output [{:all-payees [:cledgers-fulcro.models.payee/id
                                          :cledgers-fulcro.models.payee/name]}]}
  (let [#_ (println "invoking all-payees-resolver")
        ;; result (mapv
        ;;          #(hash-map :cledgers-fulcro.models.payee/id %)
        ;;          (keys payees-table))
        result (jdbc/execute! db/data-source
                              ["select * from payee"]
                              {:builder-fn next.jdbc.result-set/as-modified-maps
                               :label-fn identity
                               :qualifier-fn #(get db/QUALIFIER_MAPPING % %)})
        #_ (clojure.pprint/pprint result)]
   {:all-payees result}))


(pathom-connect/defresolver ledgers-q-resolver [env {:keys [query] :as params}]
  {::pathom-connect/input #{:query}
   ::pathom-connect/output [{:q-results [:cledgers-fulcro.models.ledger/id]}]}
  (let [_ (clojure.pprint/pprint {:params params})]
   {:q-results (mapv
                #(hash-map :cledgers-fulcro.models.ledger/id %)
                (keys ledgers-table))}))

#_(pathom-connect/defresolver answer-plus-one-resolver [env {:keys [input] :as params}]
  {::pathom-connect/input #{:input}
   ::pathom-connect/output [:answer-plus-one]}
  (let [_ (clojure.pprint/pprint {:params params})]
    {:answer-plus-one (inc input)}))

;; (pathom-connect/defresolver transaction-resolver [env {:cledgers-fulcro.models.transaction/keys [id]}]
;;   {::pathom-connect/input #{:cledgers-fulcro.models.transaction/id}
;;    ::pathom-connect/output [:cledgers-fulcro.models.transaction/date
;;                             :cledgers-fulcro.models.transaction/payee
;;                             :cledgers-fulcro.models.transaction/ledger
;;                             :cledgers-fulcro.models.transaction/description
;;                             :cledgers-fulcro.models.transaction/amount]}
;;   (get transactions-table id))

(pathom-connect/defresolver all-transactions-resolver [env {:cledgers-fulcro.models.transaction/keys [id]}]
  {::pathom-connect/output [{:all-transactions [:cledgers-fulcro.models.transaction/id]}]}
  (let [#_ (println "\ninvoking all-transactions-resolver. result:")
        result (jdbc/execute! db/data-source
                 ["select * from xaction"]
                 {:builder-fn next.jdbc.result-set/as-modified-maps
                  :label-fn identity
                  :qualifier-fn #(get db/QUALIFIER_MAPPING % %)})
        result (map (fn [x]
                      (let [date-as-str (str (:cledgers-fulcro.models.transaction/date x))
                            #_ (pp/pprint {:all-transactions-resolver {:date-as-str date-as-str}})]
                       (-> x
                           (assoc :cledgers-fulcro.models.transaction/payee
                                  {:cledgers-fulcro.models.payee/id
                                   (:cledgers-fulcro.models.transaction/payee_id x)})
                           (assoc :cledgers-fulcro.models.transaction/ledger
                                  {:cledgers-fulcro.models.ledger/id
                                   (:cledgers-fulcro.models.transaction/ledger_id x)})
                           (assoc :cledgers-fulcro.models.transaction/date
                                  (str (:cledgers-fulcro.models.transaction/date x))))))
                    result)
        ;; result (mapv
        ;;         #(hash-map :cledgers-fulcro.models.transaction/id %)
        ;;         (keys transactions-table))
        #_ (clojure.pprint/pprint result)]
   {:all-transactions result}))

(def resolvers [;; ledger-resolver
                all-ledgers-resolver
                ledgers-q-resolver
                ;; answer-plus-one-resolver
                ;; payee-resolver
                all-payees-resolver
                ;; transaction-resolver
                all-transactions-resolver
                ])



(comment

  (jdbc/execute! db/data-source
                 ["select * from xaction"]
                 {:builder-fn next.jdbc.result-set/as-modified-maps
                  :label-fn identity
                  :qualifier-fn #(get db/QUALIFIER_MAPPING % %)})

  )
