(ns cledgers-fulcro.mutations-client
  (:require [cljs.pprint :as pp]
            #_[com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [cledgers-fulcro.utils.utils :as utils]))

(defn add-xaction-func [state-map new-xaction]
  (let [#_ (pp/pprint {:add-xaction-func {:new-xaction new-xaction}})
        new-xaction-id (:cledgers-fulcro.models.transaction/id new-xaction)
        #_ (pp/pprint {:add-xaction-func {:new-xaction-id new-xaction-id}})
        new-xaction-key
        [:cledgers-fulcro.models.transaction/id new-xaction-id]
        date-previous (:cledgers-fulcro.models.transaction/date new-xaction)
        new-new-xaction (utils/new-xaction date-previous)
        new-new-xaction-id (:new-transaction/id new-new-xaction)]
    (-> state-map
        (assoc-in new-xaction-key new-xaction)
        (update-in [:component/id
                    :cledgers-fulcro.ui.core/transaction-list
                    :transaction-list/transactions]
                   #(conj % new-xaction-key))
        (update-in [:new-transaction/id] dissoc new-xaction-id)
        (assoc-in [:new-transaction/id new-new-xaction-id] new-new-xaction)
        (assoc-in [:component/id
                   :cledgers-fulcro.ui.core/transaction-list
                   :transaction-list/new-transaction]
                  [:new-transaction/id new-new-xaction-id]))))

(defmutation add-transaction
    [{:keys [id] :as mut-in}]
  (action [{:keys [state] :as action-in}]
    (let [#_ (pp/pprint {:muts-client--add-xaction
                         { ;; :state @state
                          :xaction-in mut-in}})
          _ (swap! state add-xaction-func mut-in)]))
  (remote [env] true))
