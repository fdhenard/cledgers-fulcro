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

(defsc Transaction [this {:transaction/keys [id date payee ledger description amount] :as props}]
  {:query [:transaction/id :transaction/date :transaction/payee :transaction/ledger :transaction/description :transaction/amount]
   :ident (fn [] [:transaction/id (:transaction/id props)])
   :initial-state (fn [{:keys [id date payee ledger description amount] :as params}]
                    {:transaction/id id
                     :transaction/date date
                     :transaction/payee payee
                     :transaction/ledger ledger
                     :transaction/description description
                     :transaction/amount amount})}
  (dom/tr
     (dom/td date)
     (dom/td payee)
     (dom/td ledger)
     (dom/td description)
     (dom/td amount)
     (dom/td "something")))

(def ui-transaction (comp/factory Transaction {:keyfn :transaction/id}))

(defsc Root #_[this {:keys [friends enemies]}]
  [this {:root/keys [transactions]}]
  ;; {:query [{:friends (comp/get-query PersonList)}
  ;;          {:enemies (comp/get-query PersonList)}]
  ;;  :initial-state (fn [params]
  ;;                   {:friends (comp/get-initial-state PersonList {:id :friends
  ;;                                                                 :label "Friends"})
  ;;                    :enemies (comp/get-initial-state PersonList {:id :enemies
  ;;                                                                 :label "Enemies"})})}
  {:query [{:root/transactions (comp/get-query Transaction)}]
   :initial-state
   (fn [params]
     {:root/transactions
      [(comp/get-initial-state Transaction {:id 1
                                            :date "2020"
                                            :payee "test payee"
                                            :ledger "test ledger"
                                            :description "soem descrip"
                                            :amount "1111.11"})]})}
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
    (map ui-transaction transactions)))
   #_(ui-transaction-list transactions)
   #_(ui-person-list friends)
   #_(ui-person-list enemies)))
