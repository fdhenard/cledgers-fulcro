(ns cledgers-fulcro.parser
  (:require [clojure.pprint :as pp]
            [taoensso.timbre :as log]
            [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.resolvers]
            [cledgers-fulcro.mutations-server]))

#_(def resolvers [cledgers-fulcro.resolvers/resolvers])

(def pathom-parser
  (pathom/parser {::pathom/env {::pathom/reader [pathom/map-reader
                                                 pathom-connect/reader2
                                                 pathom-connect/ident-reader
                                                 pathom-connect/index-reader]
                                ::pathom-connect/mutation-join-globals [:tempids]}
                  ::pathom/mutate pathom-connect/mutate
                  ::pathom/plugins [(pathom-connect/connect-plugin
                                     {::pathom-connect/register #_resolvers
                                      [cledgers-fulcro.resolvers/resolvers
                                       cledgers-fulcro.mutations-server/mutations]})
                                    pathom/error-handler-plugin]}))


(defn api-parser [query]
  (log/info (with-out-str (pp/pprint {:parser {:query query}})))
  (pathom-parser {} query))


(comment

  (let [res (api-parser [{:all-ledgers [:cledgers-fulcro.models.ledger/id :cledgers-fulcro.models.ledger/name]}])
        #_ (println)
        #_ (clojure.pprint/pprint res)])

  (let [res (api-parser [{[:query "hi"] [{:q-results [:cledgers-fulcro.models.ledger/id]}]}])
        #_ (println)
        #_ (clojure.pprint/pprint res)])

  (let [res (api-parser [{[:input 2] [:answer-plus-one]}])
        #_ (println)
        #_ (clojure.pprint/pprint res)])

  (let [res (api-parser [{:all-transactions [:cledgers-fulcro.models.transaction/id
                                             :cledgers-fulcro.models.transaction/description
                                             {:cledgers-fulcro.models.transaction/ledger
                                              [:cledgers-fulcro.models.ledger/name]}]}])
        #_ (println)
        #_ (clojure.pprint/pprint res)])

  (let [res (api-parser [{:all-payees [:cledgers-fulcro.models.payee/id
                                       :cledgers-fulcro.models.payee/name]}])
        #_ (println)
        #_ (clojure.pprint/pprint res)])

  )
