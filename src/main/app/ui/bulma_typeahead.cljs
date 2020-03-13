(ns app.ui.bulma-typeahead
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(defn cls-set->str [class-set]
  (let [names (->> class-set
                   (remove nil?)
                   (map name)
                   (interpose " "))]
    (apply str names)))

(defsc TypeaheadTextbox
  [this {:keys [set-value!
                query-func
                set-matches!] :as props}]
  {:initLocalState (fn [this props]
                     {:change-count 0
                      :is-loading? false})}
  (let [_ (println "rendering TypeaheadTextbox")
        {:keys [is-loading?]} (comp/get-state this)
        #_ (cljs.pprint/pprint {:value value})]
    (dom/div
     :.field
    (dom/div
     {:className (cls-set->str #{:control (when is-loading? :is-loading)})}
     (dom/input
      :.input
      {:type :text
       :placeholder "something"
       :value ""
       :onChange (fn [evt]
                   (let [#_ (println "old value:" @value-atom)
                         new-val (evt/target-value evt)
                         #_ (println "ta-textbox value = " new-val)]
                     (set-value! new-val)
                     (query-func new-val (fn [new-matches]
                                           (let [_ (println "new matches" new-matches)
                                                 new-matches (set new-matches)
                                                 _ (set-matches! new-matches)])))))})))))

(def ui-typeahead-textbox (comp/factory TypeaheadTextbox))

(defsc TypeaheadComponent
  [this {:keys [query-func] :as props}]
  {:initLocalState (fn [this props]
                     {:dropdown-expanded? true
                      :textbox-value ""
                      :matches #{}})}
  (let [_ (println "rendering TypeaheadComponent")
        #_ (cljs.pprint/pprint {:value value})
        {:keys [dropdown-expanded?
                textbox-value
                matches]} (comp/get-state this)
        set-matches! (fn [in] (comp/set-state! this {:matches in}))
        set-textbox-value! (fn [in] (comp/set-state! this {:textbox-value in}))]
    (dom/div
     {:className (cls-set->str #{:dropdown (when dropdown-expanded? :is-active)})}
     (dom/div
      :.dropdown-trigger
      (ui-typeahead-textbox {:set-matches! set-matches!
                             :set-value! set-textbox-value!
                             :query-func query-func}))
     (dom/div
      :.dropdown-menu
      {:id "dropdown-menu"
       :role "menu"}
      (dom/div
       :.dropdown-content
       (map
        #(dom/a
          :.dropdown-item
          {:href "#"
           ;; :on-click (fn [evt] ...)
           :key (:id %)
           }
          (:name %))
        matches))))))

(def ui-typeahead-component (comp/factory TypeaheadComponent))
