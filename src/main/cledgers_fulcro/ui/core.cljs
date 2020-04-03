(ns cledgers-fulcro.ui.core
  (:require ["react-number-format" :as NumberFormat]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.mutations :as muts]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [cledgers-fulcro.math :as math]
            [cledgers-fulcro.mutations :as api]
            [cledgers-fulcro.ui.bulma-typeahead :as typeahead]))

(defsc TransactionListItemPayee
  [this {:cledgers-fulcro.models.payee/keys [id name] :as props}]
  {:query [:cledgers-fulcro.models.payee/id
           :cledgers-fulcro.models.payee/name]
   :ident :cledgers-fulcro.models.payee/id}
  name)

(def ui-transaction-list-item-payee (comp/factory TransactionListItemPayee))

(defsc TransactionListItemLedger
  [this {:cledgers-fulcro.models.ledger/keys [id name] :as props}]
  {:query [:cledgers-fulcro.models.ledger/id
           :cledgers-fulcro.models.ledger/name]
   :ident :cledgers-fulcro.models.ledger/id}
  (let [#_ (println "rendering TransactionListItemLedger")]
   name))

(def ui-transaction-list-item-ledger (comp/factory TransactionListItemLedger))

(defsc TransactionListItem [this {:cledgers-fulcro.models.transaction/keys [id
                                                                           date
                                                                           payee
                                                                           ledger
                                                                           description
                                                                           amount] :as props}]
  {:query [:cledgers-fulcro.models.transaction/id
           :cledgers-fulcro.models.transaction/date
           {:cledgers-fulcro.models.transaction/payee (comp/get-query TransactionListItemPayee)}
           {:cledgers-fulcro.models.transaction/ledger (comp/get-query TransactionListItemLedger)}
           :cledgers-fulcro.models.transaction/description
           :cledgers-fulcro.models.transaction/amount]
   :ident (fn [] [:cledgers-fulcro.models.transaction/id (:cledgers-fulcro.models.transaction/id props)])}
  (dom/tr
     (dom/td date)
     (dom/td (ui-transaction-list-item-payee payee))
     (dom/td (ui-transaction-list-item-ledger ledger))
     (dom/td description)
     (dom/td amount)
     (dom/td "something")))

(def ui-transaction (comp/factory TransactionListItem {:keyfn :cledgers-fulcro.models.transaction/id}))

(def ui-number-format (interop/react-factory NumberFormat))

(defsc EditableMoneyInput
  [this {:keys [value onChange]}]
  {:initLocalState (fn [this props] {:editing? false})}
  (let [{:keys [editing?]} (comp/get-state this)
        attrs {:thousandSeparator true
               :decimalScale 2
               :prefix "$"
               :value (math/bigdec->str value)
               :onValueChange (fn [value]
                                (let [str-value (.-value value)]
                                  (when onChange
                                    (onChange (math/bigdecimal str-value)))))}]
    (ui-number-format attrs)))

(def ui-editable-money-input (comp/factory EditableMoneyInput))

(defn get-payees! [q-str callback]
  (let [#_ (println "q-str = " q-str)]
   (callback [{:id 1
               :name "payee 1"}
              {:id 2
               :name "payee 3"}])))

(defn get-ledgers! [q-str callback]
  (let [#_ (println "q-str = " q-str)]
   (callback [{:id 1
               :name "ledger 1"}
              {:id 2
               :name "ledger 4"}])))

(defsc NewTransactionRow [this {:new-transaction/keys [id payee description amount ledger] :as props}]
  {:query [:new-transaction/id
           :new-transaction/payee
           :new-transaction/ledger
           :new-transaction/description
           :new-transaction/amount]
   :ident (fn [] [:new-transaction/id (:new-transaction/id props)])
   :initial-state (fn [params]
                    #_(cljs.pprint/pprint props)
                    {:new-transaction/id ::new-transaction
                     :new-transaction/payee nil
                     :new-transaction/ledger nil
                     :new-transaction/description ""
                     :new-transaction/amount ""
                     })}
  (let [#_ (cljs.pprint/pprint {:props-in-new-trans-row props})]
   (dom/tr
    (dom/td (dom/input {:type :text
                        :size 2
                        :value "1"})
            (dom/span "/")
            (dom/input {:type :text
                        :size 2
                        :value "1"})
            (dom/span "/")
            (dom/input {:type :text
                        :size 4
                        :value "2002"}))
    (dom/td (typeahead/ui-typeahead-component
             {:query-func get-payees!
              :item->text :name
              :onChange (fn [value]
                          (muts/set-value! this :new-transaction/payee value))}))
    (dom/td (typeahead/ui-typeahead-component
             {:query-func get-ledgers!
              :item->text :name
              :onChange (fn [value]
                          (muts/set-value! this :new-transaction/ledger value))}))
    (dom/td (dom/input {:type :text
                        :value description
                        :onChange
                        (fn [evt]
                          (muts/set-value! this :new-transaction/description (evt/target-value evt)))}))
    (dom/td (ui-editable-money-input {:value amount
                                      :onChange
                                      (fn [value]
                                        #_(js/console.log "value = " value)
                                        (muts/set-value! this :new-transaction/amount value))}))
    (dom/td (dom/button "what")))))

(def ui-new-transaction-row (comp/factory NewTransactionRow))

(defsc TransactionList [this {:transaction-list/keys [transactions new-transaction] :as props}]
  {:query [{:transaction-list/new-transaction (comp/get-query NewTransactionRow)}
           {:transaction-list/transactions (comp/get-query TransactionListItem)}]
   :ident (fn [this props] [:component/id ::transaction-list])
   :initial-state
   (fn [this props]
     {:transaction-list/new-transaction (comp/get-initial-state NewTransactionRow)})}
  (let [#_ (cljs.pprint/pprint {:props-in-trans-list props})]
   (dom/div
    (dom/table
     :.table
     (dom/thead
      (dom/tr
       (dom/th "date")
       (dom/th "payee")
       (dom/th "ledger")
       (dom/th "desc")
       (dom/th "amount")
       (dom/th "controls")))
     (dom/tbody
      (ui-new-transaction-row new-transaction)
      (map ui-transaction transactions))))))

(def ui-transaction-list (comp/factory TransactionList))

(defsc Root
  [this {:root/keys [transaction-list]}]
  {:query [{:root/transaction-list (comp/get-query TransactionList)}]
   :initial-state (fn [this] {:root/transaction-list (comp/get-initial-state TransactionList)})}
  (dom/div
   (ui-transaction-list transaction-list)))
