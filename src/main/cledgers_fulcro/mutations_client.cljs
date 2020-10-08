(ns cledgers-fulcro.mutations-client
  (:require [cljs.pprint :as pp]
            #_[com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [cledgers-fulcro.utils.utils :as utils]))

(defmutation add-transaction
    [{:keys [id] :as mut-in}]
  (action [{:keys [state] :as action-in}]
    (let [#_ (pp/pprint {:muts-client--add-xaction
                         { ;; :state @state
                          :xaction-in mut-in}})
          new-xaction-key [:cledgers-fulcro.models.transaction/id id]
          _ (swap! state assoc-in new-xaction-key mut-in)
          _ (swap!
             state
             update-in
             [:component/id
              :cledgers-fulcro.ui.core/transaction-list
              :transaction-list/transactions]
             #(conj % new-xaction-key))
          date-previous (:cledgers-fulcro.models.transaction/date mut-in)
          new-new-xaction (utils/new-xaction date-previous)
          new-new-xaction-id (:new-transaction/id new-new-xaction)
          _ (swap!
             state
             assoc-in
             [:new-transaction/id new-new-xaction-id]
             new-new-xaction)
          _ (swap!
             state
             assoc-in
             [:component/id
              :cledgers-fulcro.ui.core/transaction-list
              :transaction-list/new-transaction]
             [:new-transaction/id new-new-xaction-id])]))
  (remote [env] true))
