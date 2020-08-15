(ns cledgers-fulcro.mutations-server
  (:require [clojure.pprint :as pp]
            [com.wsscode.pathom.connect :as pathom-connect]))


(pathom-connect/defmutation add-transaction [env params]
     {::pathom-connect/sym 'cledgers-fulcro.mutations-client/add-transaction}
     (let [_ (pp/pprint {:mutations-server {;; :env env
                                            :params params}})]
       {:cledgers-fulcro.entities.transaction/id 99}))


(def mutations [add-transaction])
