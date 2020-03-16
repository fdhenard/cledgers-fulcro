(ns app.parser
  (:require [com.wsscode.pathom.core :as pathom]
            [com.wsscode.pathom.connect :as pathom-connect]
            [taoensso.timbre :as log]))

(def resolvers [])

(def pathom-parser
  (pathom/parser {::pathom/env {::pathom/reader [pathom/map-reader
                                                 pathom-connect/reader 2
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
