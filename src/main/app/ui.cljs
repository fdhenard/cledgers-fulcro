(ns app.ui
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [app.mutations :as api]))

(defsc Person [this {:person/keys [id name age] :as props} {:keys [onDelete]}]
  {:query [:person/id :person/name :person/age]
   :ident (fn [] [:person/id (:person/id props)])
   :initial-state (fn [{:keys [id name age] :as params}]
                    {:person/id id
                     :person/name name
                     :person/age age})}
  (dom/li
   (dom/h5 (str name " (age: " age ")")
           (dom/button {:onClick #(onDelete id)} "X"))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:list/keys [id label people] :as props}]
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

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:keys [friends enemies]}]
  {:query [{:friends (comp/get-query PersonList)}
           {:enemies (comp/get-query PersonList)}]
   :initial-state (fn [params]
                    {:friends (comp/get-initial-state PersonList {:id :friends
                                                                  :label "Friends"})
                     :enemies (comp/get-initial-state PersonList {:id :enemies
                                                                  :label "Enemies"})})}
  (dom/div
   (ui-person-list friends)
   (ui-person-list enemies)))
