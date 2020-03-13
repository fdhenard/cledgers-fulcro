(ns app.math
  (:require #?@(:cljs [["big.js" :as Big]])
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [cognitect.transit :as transit]
            [ghostwheel.core :refer [>defn =>]]))

(>defn bigdecimal?
    [value]
  [any? => boolean?]
  #?(:clj (instance? java.math.BigDecimal value)
     :cljs (transit/bigdec? value)))

(declare bigdecimal)

(spec/def ::bigdecimal
  (spec/with-gen bigdecimal? #(spec/gen #{(bigdecimal "11.35")
                                          (bigdecimal "5.00")
                                          (bigdecimal "42.11")})))

(>defn bigdec->str [bd]
  [(spec/nilable ::bigdecimal) => string?]
  (if-not bd
    ""
    #?(:cljs (or (some-> bd .-rep) "0")
       :clj (str bd))))

(defn strip-zeroes [s]
  #?(:clj  s
     :cljs (-> s
             (str/replace #"^0+([1-9].*)$" "$1")
             (str/replace #"^0*([.].*)$" "0$1"))))

(>defn bigdecimal [input]
  [any? => ::bigdecimal]
  (if (bigdecimal? input)
    input
    (let [input (if (seq (str input))
                  input
                  "0")]
      #?(:clj (java.math.BigDecimal. (.toString input))
         :cljs (transit/bigdec (strip-zeroes (.toString input)))))))
