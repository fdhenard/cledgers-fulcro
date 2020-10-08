(ns cledgers-fulcro.ui.core
  (:require [cljs.pprint :as pp]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [cognitect.transit :as xsit]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.mutations :as muts]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            ["react-number-format" :as NumberFormat]
            [tick.alpha.api :as tick]
            [cljc.java-time.month-day :as month-day]
            [cljc.java-time.year :as year]
            [cljc.java-time.month :as month]
            [cledgers-fulcro.math :as math]
            [cledgers-fulcro.utils.utils :as utils]
            [cledgers-fulcro.mutations-client :as muts-client]
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

(defsc TransactionListItem
  [this {:cledgers-fulcro.models.transaction/keys [id
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
  (let [#_ (pp/pprint {:TransactionListItem {:date date}})]
   (dom/tr {:key id}
           (dom/td (-> date edn/read-string str))
           (dom/td (ui-transaction-list-item-payee payee))
           (dom/td (ui-transaction-list-item-ledger ledger))
           (dom/td description)
           (dom/td (math/bigdec->str amount))
           (dom/td "something"))))

(def ui-transaction (comp/factory TransactionListItem {:keyfn :cledgers-fulcro.models.transaction/id}))

(def ui-number-format (interop/react-factory NumberFormat))

(defsc EditableMoneyInput
  [this {:keys [value on-change]}]
  {:initLocalState (fn [this props] {:editing? false})}
  (let [{:keys [editing?]} (comp/get-state this)
        attrs {:thousandSeparator true
               :decimalScale 2
               :prefix "$"
               :value (math/bigdec->str value)
               :onValueChange (fn [value]
                                (when on-change
                                  (let [str-value (.-value value)]
                                    (on-change (math/bigdecimal str-value)))))}]
    (ui-number-format attrs)))

(def ui-editable-money-input (comp/factory EditableMoneyInput))

(defsc LocalDateInput
  [this {:keys [value on-change]}]
  (let [#_ (pp/pprint {:LocalDateInput {:value value}})
        month (-> value tick/month month/get-value)
        day-of-month (-> value month-day/get-day-of-month)
        year (-> value tick/year year/get-value)]
    (dom/span
     (dom/input {:type :text
                 :size 2
                 :value (str month)
                 :onChange
                 (fn [evt]
                   (when on-change
                     (let [month-int-value (-> evt evt/target-value js/parseInt)
                           new-date (tick/new-date year month-int-value day-of-month)]
                      (on-change (pr-str new-date)))))})
     (dom/span "/")
     (dom/input {:type :text
                 :size 2
                 :value (str day-of-month)
                 :onChange
                 (fn [evt]
                   (when on-change
                     (let [day-int-value (-> evt evt/target-value js/parseInt)
                           new-date (tick/new-date year month day-int-value)]
                       (on-change (pr-str new-date)))))})
     (dom/span "/")
     (dom/input {:type :text
                 :size 4
                 :value (str year)
                 :onChange
                 (fn [evt]
                   (when on-change
                     (let [year-int-value (-> evt evt/target-value js/parseInt)
                           new-date (tick/new-date year-int-value month day-of-month)]
                       (on-change (pr-str new-date)))))}))))

(def ui-local-date-input (comp/factory LocalDateInput))


(defsc NewTransactionRow [this {:new-transaction/keys [id payee description amount ledger
                                                       date date-previous] :as props}]
  {:query [:new-transaction/id
           :new-transaction/payee
           :new-transaction/ledger
           :new-transaction/description
           :new-transaction/amount
           [:cledgers-fulcro.models.ledger/id '_]
           [:cledgers-fulcro.models.payee/id '_]
           :new-transaction/date
           :new-transaction/date-previous]
   :ident (fn [] [:new-transaction/id (:new-transaction/id props)])
   :initial-state (fn [params]
                    (utils/new-xaction))}
  (let [ledgers (-> props :cledgers-fulcro.models.ledger/id vals)
        payees (-> props :cledgers-fulcro.models.payee/id vals)
        date (or (edn/read-string date)
                 (edn/read-string date-previous)
                 (tick/today))
        #_ (pp/pprint {:NewTransactionRow {:date date
                                          ;; :month (tick/month date)
                                          }})]
   (dom/tr
    (dom/td (ui-local-date-input {:value date
                                  :on-change
                                  (fn [value]
                                    #_(js/console.log "local date input val = " value)
                                    (muts/set-value! this :new-transaction/date value))}))
    (dom/td (typeahead/ui-typeahead-component
             {:query-func (fn [q-str callback]
                            (let [payee-name-starts-with?
                                  (fn [payee q-str]
                                    (let [#_ (pp/pprint {:payee payee
                                                                 :q-str q-str})
                                          payee-name (:cledgers-fulcro.models.payee/name payee)]
                                      (str/starts-with? payee-name q-str)))
                                  matches (filter #(payee-name-starts-with? % q-str) payees)]
                              (callback matches)))
              :item->text :cledgers-fulcro.models.payee/name
              :item->id :cledgers-fulcro.models.payee/id
              :onChange (fn [value]
                          (let [pathom-val [:cledgers-fulcro.models.payee/id (:id value)]]
                           (muts/set-value! this :new-transaction/payee pathom-val)))}))
    (dom/td (typeahead/ui-typeahead-component
             {:query-func (fn [q-str callback]
                            (let [ledger-name-starts-with?
                                  (fn [ledger q-str]
                                    (let [ledger-name (:cledgers-fulcro.models.ledger/name ledger)]
                                      (str/starts-with? ledger-name q-str)))
                                  matches (filter #(ledger-name-starts-with? % q-str) ledgers)]
                              (callback matches)))
              :item->text :cledgers-fulcro.models.ledger/name
              :item->id :cledgers-fulcro.models.ledger/id
              :onChange (fn [value]
                          (let [#_ (pp/pprint {:ledger-on-change-value value})
                                pathom-val [:cledgers-fulcro.models.ledger/id (:id value)]
                                _ (muts/set-value! this :new-transaction/ledger pathom-val)]))}))
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
    (dom/td (dom/button
             {:onClick
              (fn [evt]
                (comp/transact!
                 this
                 [(muts-client/add-transaction
                   #:cledgers-fulcro.models.transaction{:id id
                                                        :payee payee
                                                        :description description
                                                        :amount amount
                                                        :ledger ledger
                                                        :date (pr-str date)})]))}
             "add")))))

(comment

  (def today (tick/today))

  today
  ;; => #time/date "2020-10-02"
  (.-repr today)
  ;; => nil
  (js-keys today)

  (.toString today)
  ;; => "2020-10-02"
  (.toJSON today)
  ;; => "2020-10-02"
  (.format today)
  ;; => #object[E NullPointerException: formatter must not be null]
  (type today)
  ;; => #object[LocalDate]
  (def writer (xsit/writer :json))
  (xsit/write writer today)
  ;; => #object[Error Error: Cannot write LocalDate]

  (str today)
  ;; => "2020-10-02"

  (require '[clojure.edn :as edn])
  (pr-str today)
  (-> today
      pr-str
      edn/read-string
      type)


  )




(def ui-new-transaction-row (comp/factory NewTransactionRow))

(defsc TransactionList [this {:transaction-list/keys [transactions new-transaction] :as props}]
  {:query [{:transaction-list/new-transaction (comp/get-query NewTransactionRow)}
           {:transaction-list/transactions (comp/get-query TransactionListItem)}]
   :ident (fn [this props] [:component/id ::transaction-list])
   :initial-state
   (fn [this props]
     {:transaction-list/new-transaction (comp/get-initial-state NewTransactionRow)})}
  (let [#_ (pp/pprint {:props-in-trans-list props})]
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
   :initial-state (fn [this] {:root/transaction-list (comp/get-initial-state TransactionList)})
   }
  (dom/div
   (ui-transaction-list transaction-list)))
