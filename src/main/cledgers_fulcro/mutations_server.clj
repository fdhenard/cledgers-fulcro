(ns cledgers-fulcro.mutations-server
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as jdbc-sql]
            [mount.core :as mount]
            [time-literals.read-write]
            [com.wsscode.pathom.connect :as pathom-connect]
            [cledgers-fulcro.utils.utils :as utils]))


(pathom-connect/defmutation add-transaction [env params]
  {::pathom-connect/sym 'cledgers-fulcro.mutations-client/add-transaction}
  (let [params-renamed-kvs (map (fn [[k v]]
                                  [(utils/de-namespace-kw k)
                                   v])
                                params)
        params-renamed (into {} params-renamed-kvs)
        #_ (pp/pprint {:muts-server--add-xaction {:params params
                                                 :params-renamed-kvs params-renamed-kvs
                                                 :params-renamed params-renamed}})
        ledger-id (-> params-renamed :ledger second)
        payee-id (-> params-renamed :payee second)
        date (->> params-renamed
                  :date
                  (edn/read-string {:readers time-literals.read-write/tags}))
        params-renamed (-> params-renamed
                           (assoc :date date)
                           (assoc :uuid (-> params-renamed :id :id str))
                           ;; TODO - fix created_by_id
                           (assoc :created_by_id 1)
                           (dissoc :id)
                           (dissoc :payee)
                           (dissoc :ledger)
                           (assoc :payee_id payee-id)
                           (assoc :ledger_id ledger-id))
        #_ (pp/pprint {:mutations {:add-transaction {;; :db-ds (:db-datasource env)
                                                    :params-renamed params-renamed
                                                    :date-in (:date params)
                                                    :date-updated date}}})
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


  (require '[tick.alpha.api :as tick])
  (require '[time-literals.read-write])
  (def today (tick/today))
  today
  (def today-str (pr-str today))
  today-str
  (def today-obj (edn/read-string {:readers time-literals.read-write/tags} today-str))
  today-obj
  (type today-obj)

  )
