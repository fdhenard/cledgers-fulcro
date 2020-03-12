(ns app.ui
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [app.mutations :as api]))

#_(defsc Person [this {:person/keys [id name age] :as props} {:keys [onDelete]}]
  {:query [:person/id :person/name :person/age]
   :ident (fn [] [:person/id (:person/id props)])
   :initial-state (fn [{:keys [id name age] :as params}]
                    {:person/id id
                     :person/name name
                     :person/age age})}
  (dom/li
   (dom/h5 (str name " (age: " age ")")
           (dom/button {:onClick #(onDelete id)} "X"))))

#_(def ui-person (comp/factory Person {:keyfn :person/id}))

#_(defsc PersonList [this {:list/keys [id label people] :as props}]
  {:query [:list/id :list/label {:list/people (comp/get-query Person)}]
   :ident (fn [] [:list/id (:list/id props)])
   :initial-state
   (fn [{:keys [id label]}]
     {:list/id id
      :list/label label
      :list/people (case label
                     "Friends"
                     [(comp/get-initial-state Person {:id 1 :name "Sally" :age 32})
                      (comp/get-initial-state Person {:id 2 :name "Joe" :age 22})]
                     "Enemies"
                     [(comp/get-initial-state Person {:id 3 :name "Fred" :age 11})
                      (comp/get-initial-state Person {:id 4 :name "Bobby" :age 55})]
                     (throw (js/Error. (str "invalid label " label))))})}
  (let [delete-person
        (fn [person-id]
          (println label "asked to delete person" person-id)
          (comp/transact! this [(api/delete-person {:list/id id :person/id person-id})]))]
   (dom/div
    (dom/h4 label)
    (dom/ul
     (map #(ui-person (comp/computed % {:onDelete delete-person})) people)))))

#_(def ui-person-list (comp/factory PersonList))

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
                        :value description}))
    (dom/td (dom/input {:type :text
                        :value amount}))
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
