(ns app.ui
  (:require ["react-number-format" :as NumberFormat]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.mutations :as muts]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [app.math :as math]
            [app.mutations :as api]))

(defsc TransactionListItemPayee
  [this {:payee/keys [id name] :as props}]
  {:query [:payee/id :payee/name]
   :ident (fn [] [:payee/id (:payee/id props)])
   :initial-state (fn [{:keys [id name] :as params}]
                    {:payee/id id
                     :payee/name name})}
  name)

(def ui-transaction-list-item-payee (comp/factory TransactionListItemPayee))

(defsc TransactionListItem [this {:transaction/keys [id date payee ledger description amount] :as props}]
  {:query [:transaction/id :transaction/date :transaction/payee :transaction/ledger :transaction/description :transaction/amount]
   :ident (fn [] [:transaction/id (:transaction/id props)])
   :initial-state (fn [{:keys [id date payee ledger description amount] :as params}]
                    {:transaction/id id
                     :transaction/date date
                     :transaction/payee (comp/get-initial-state TransactionListItemPayee payee)
                     :transaction/ledger ledger
                     :transaction/description description
                     :transaction/amount amount})}
  (dom/tr
     (dom/td date)
     (dom/td (ui-transaction-list-item-payee payee))
     (dom/td ledger)
     (dom/td description)
     (dom/td amount)
     (dom/td "something")))

(def ui-transaction (comp/factory TransactionListItem {:keyfn :transaction/id}))

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

(defsc NewTransactionRow [this {:new-transaction/keys [id description amount] :as props}]
  {:query [:new-transaction/id :new-transaction/description :new-transaction/amount]
   :ident (fn [] [:new-transaction/id (:new-transaction/id props)])
   :initial-state (fn [params]
                    #_(cljs.pprint/pprint props)
                    {:new-transaction/id ::new-transaction :new-transaction/description "" :new-transaction/amount ""})}
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
    (dom/td "new payee")
    (dom/td "new ledger")
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
   :initial-state (fn [this props]
                    {:transaction-list/transactions [(comp/get-initial-state TransactionListItem {:id 1
                                                                                          :date "2020"
                                                                                          ;; :payee "test payee"
                                                                                          :payee {:id 1
                                                                                                  :name "test payee"}
                                                                                          :ledger "test ledger"
                                                                                          :description "soem descrip"
                                                                                                  :amount "1111.11"})]
                     :transaction-list/new-transaction (comp/get-initial-state NewTransactionRow)})}
  (let [#_ (cljs.pprint/pprint {:props-in-trans-list props})]
   (dom/div
    (dom/table :.table
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
