(ns cledgers-fulcro.config.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [cprop.source]
            [cprop.core :as cprop]
            [mount.core :as mount]))

(def ^:private dev-local-config-file
  (let [dev-local-config-path (str (System/getProperty "user.home") "/dev/dev-local-config-files/cledgers-fulcro.edn")]
    (io/as-file dev-local-config-path)))

(defn is-running-local? []
  (.exists dev-local-config-file))

(defn- get-config []
  (let [dev-local-config (if (not (.exists dev-local-config-file))
                           {}
                           (or (edn/read-string (slurp dev-local-config-file)) {}))
        config-result (cprop/load-config :resource "config.edn"
                                         :merge [dev-local-config
                                                 (cprop.source/from-system-props)
                                                 (cprop.source/from-env)])]
    config-result))

#_(def config (get-config))

#_(clojure.pprint/pprint {:config config})

(mount/defstate config :start (get-config))
