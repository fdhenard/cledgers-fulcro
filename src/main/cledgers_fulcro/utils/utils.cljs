(ns cledgers-fulcro.utils.utils
  (:require [cledgers-fulcro.math :as math]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defn new-xaction []
  {:new-transaction/id (tempid/tempid)
   :new-transaction/payee nil
   :new-transaction/ledger nil
   :new-transaction/description ""
   :new-transaction/amount (math/bigdecimal "")
   :new-transaction/date nil
   :new-transaction/date-previous nil})
