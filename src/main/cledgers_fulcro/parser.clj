(ns cledgers-fulcro.parser
  (:require [taoensso.timbre :as log]
            [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.resolvers]))

(def resolvers [cledgers-fulcro.resolvers/resolvers])

(def pathom-parser
  (pathom/parser {::pathom/env {::pathom/reader [pathom/map-reader
                                                 pathom-connect/reader2
                                                 pathom-connect/ident-reader
                                                 pathom-connect/index-reader]
                                ::pathom-connect/mutation-join-globals [:tempids]}
                  ::pathom/mutate pathom-connect/mutate
                  ::pathom/plugins [(pathom-connect/connect-plugin
                                     {::pathom-connect/register resolvers})
                                    pathom/error-handler-plugin]}))


(defn api-parser [query]
  (log/info "Process" query)
  (pathom-parser {} query))


(comment

  (let [res (api-parser [{:all-ledgers [:cledgers-fulcro.models.ledger/id :cledgers-fulcro.models.ledger/name]}])
        _ (println)
        _ (clojure.pprint/pprint res)])

  (let [res (api-parser [{[:query "hi"] [{:q-results [:cledgers-fulcro.models.ledger/id]}]}])
        _ (println)
        _ (clojure.pprint/pprint res)])

  (let [res (api-parser [{[:input 2] [:answer-plus-one]}])
        _ (println)
        _ (clojure.pprint/pprint res)])

  )
