(ns cledgers-fulcro.utils.utils)

(defn de-namespace-kw [kw]
  (-> kw name keyword))
