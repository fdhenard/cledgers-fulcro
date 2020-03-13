(ns app.ui.bulma-typeahead
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(defn cls-set->str [class-set]
  (let [names (->> class-set
                   (remove nil?)
                   (map name)
                   (interpose " "))]
    (apply str names)))

(defsc TypeaheadTextbox
  [this {:keys [value] :as props}]
  {:initLocalState (fn [this props]
                     {:change-count 0
                      :is-loading? false
                      ;;:inital-value ""
                      })}
  (let [{:keys [is-loading?]} (comp/get-state this)
        value (or value "")
        _ (cljs.pprint/pprint {:value value})]
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
                   #_(println "something" x)
                   )
       })))))

(def ui-typeahead-textbox (comp/factory TypeaheadTextbox))

(defsc TypeaheadComponent
  [this props]
  {:initLocalState (fn [this props]
                     {:dropdown-expanded? false
                      :textbox-value ""})}
  (let [{:keys [dropdown-expanded?
                textbox-value]} (comp/get-state this)]
    (dom/div
     {:className (cls-set->str #{:dropdown (when dropdown-expanded? :is-active)})}
     (dom/div
      :.dropdown-trigger
      (ui-typeahead-textbox {:value textbox-value}))
     (dom/div
      :.dropdown-menu
      {:id "dropdown-menu"
       :role "menu"}
      (dom/div
       :.dropdown-content
       (dom/a
        :.dropdown-item
        {:href "#"
         ;; :on-click (fn [evt] ...)
         }
        "testing"))))))

(def ui-typeahead-component (comp/factory TypeaheadComponent))
