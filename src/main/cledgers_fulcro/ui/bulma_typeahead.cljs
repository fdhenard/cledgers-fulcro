(ns cledgers-fulcro.ui.bulma-typeahead
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
                get-value
                query-func
                set-matches!] :as props}]
  {:initLocalState (fn [this props]
                     {:change-count 0
                      :is-loading? false})}
  (let [_ (println "rendering TypeaheadTextbox")
        {:keys [is-loading?]} (comp/get-state this)
        value (get-value)
        #_ (cljs.pprint/pprint {:value value})]
    (dom/div
     :.field
    (dom/div
     {:className (cls-set->str #{:control (when is-loading? :is-loading)})}
     (dom/input
      :.input
      {:type :text
       :placeholder "something"
       :value value
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
  [this {:keys [query-func
                item->text
                item->id
                onChange] :as props}]
  {:initLocalState (fn [this props]
                     {;; :dropdown-expanded? true
                      :should-expand-dropdown? false
                      :textbox-value ""
                      :matches #{}
                      :selection-value ""})}
  (let [_ (println "rendering TypeaheadComponent")
        #_ (cljs.pprint/pprint {:value value})
        {:keys [should-expand-dropdown?
                textbox-value
                matches
                selection-value]} (comp/get-state this)
        set-matches! (fn [in] (comp/set-state! this {:matches in}))
        set-textbox-value! (fn [in] (comp/set-state! this {:textbox-value in}))
        get-textbox-value (fn [] (comp/get-state this :textbox-value))
        should-expand-dropdown? (not= textbox-value selection-value)]
    (dom/div
     {:className (cls-set->str #{:dropdown (when should-expand-dropdown? :is-active)})}
     (dom/div
      :.dropdown-trigger
      (ui-typeahead-textbox {:set-matches! set-matches!
                             :set-value! set-textbox-value!
                             :get-value get-textbox-value
                             :query-func query-func}))
     (dom/div
      :.dropdown-menu
      {:id "dropdown-menu"
       :role "menu"}
      (dom/div
       :.dropdown-content
       (map
        (fn [item]
          (let [#_ (cljs.pprint/pprint item)
                text (item->text item)
                id (item->id item)]
           (dom/a
            :.dropdown-item
            {:href "#"
             ;; :on-click (fn [evt] ...)
             :key id
             :onClick (fn [evt]
                        #_(println "hi")
                        (comp/set-state! this {:textbox-value text
                                               :selection-value text})
                        (onChange {:value text
                                   :is-new false
                                   :id id}))
             }
            text)))
        matches))))))

(def ui-typeahead-component (comp/factory TypeaheadComponent))
