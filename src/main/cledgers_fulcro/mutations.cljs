(ns cledgers-fulcro.mutations
  (:require [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(defmutation add-transaction
  [{:keys [id description amount] :as mut-in}]
  (action [{:keys [state] :as action-in}]
    (let [#_ (cljs.pprint/pprint {:state @state})
          new-xaction-key [:cledgers-fulcro.models.transaction/id id]]
      (do
        (swap!
         state
         assoc-in
         new-xaction-key
         #:cledgers-fulcro.models.transaction{:id id
                                              :description description
                                              :amount amount})
        (swap!
         state
         update-in
         [:component/id :cledgers-fulcro.ui.core/transaction-list :transaction-list/transactions]
         #(conj % new-xaction-key)))))
  )
