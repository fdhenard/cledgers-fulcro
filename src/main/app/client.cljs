(ns app.client
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [app.ui.core :as ui]
            [app.application :refer [app]]))

;; (defonce app (app/fulcro-app))

;; (defsc Person [this {:person/keys [name age]}]
;;   (dom/div
;;    (dom/p "Name: " name)
;;    (dom/p "Age: " age)))

;; (def ui-person (comp/factory Person))

;; (defsc Root [this props]
;;   (dom/div
;;    (ui-person {:person/name "Joe" :person/age 22})))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function.  See shadow-cljs.edn
  `:init-fn` in the modules of the main build."
  []
  (app/mount! app ui/Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source.
  See shadow-cljs.edn"
  []
  ;; re-mounting will call forced UI refresh, update intervals, etc.
  (app/mount! app ui/Root "app")
  (js/console.log "Hot reload"))
