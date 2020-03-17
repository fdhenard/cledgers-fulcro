(ns app.resolvers
  (:require [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]))

(def ledgers-table
  {1 {:app.models.ledger/id 1 :app.models.ledger/name "ledger 1"}
   2 {:app.models.ledger/id 2 :app.models.ledger/name "ledger 2"}
   3 {:app.models.ledger/id 3 :app.models.ledger/name "ledger 3"}})

(def payees-table
  {1 {:payee/id 1 :payee/name "payee 1"}
   2 {:payee/id 2 :payee/name "payee 2"}
   3 {:payee/id 3 :payee/name "payee 3"}})

(pathom-connect/defresolver ledger-resolver [env {:app.models.ledger/keys [id]}]
  {::pathom-connect/input #{:app.models.ledger/id}
   ::pathom-connect/output [:app.models.ledger/name]}
  (let [result (get ledgers-table id)]
    result))


#_(pathom-connect/defresolver ledger-q-resolver [env {:keys [query]}]
  {::pathom-connect/input #{:query}
   ::pathom-connect/output [{:root/query-ledgers [:app.models.ledger/id
                                                  :app.models.ledger/name]}]}
  (let [_ (clojure.pprint/pprint {:env env
                                  :query query})]
    (vals ledgers-table)))

(pathom-connect/defresolver all-ledgers-resolver [env {:app.models.ledger/keys [id]}]
  {::pathom-connect/output [{:all-ledgers [:app.models.ledger/id]}]}
  {:all-ledgers (mapv
                 #(hash-map :app.models.ledger/id %)
                 (keys ledgers-table))})

(def resolvers [ledger-resolver all-ledgers-resolver])
