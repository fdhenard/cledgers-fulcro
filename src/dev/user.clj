(ns user
  (:require [mount.core :as mount]
            [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs
                                                               refresh]]
            [cledgers-fulcro.db.core :as db]
            [cledgers-fulcro.server :as server]))

;; Ensure we only refresh the source we care about. This is important
;; because `resources` is on our classpath and we don't want to
;; accidentally pull source from there when cljs builds cache files there.
(set-refresh-dirs "src/dev" "src/main")

(defn start []
  (mount/start))

(defn restart
  "Stop the server, reload all source code, then restart the server.
  
  See documentation of tools.namespace.repl for more information"
  []
  (mount/stop)
  (refresh :after 'user/start))

(comment

  ;; These are here so we can run them from the editor with kb shortcuts.
  ;; See IntelliJ's "send top form to REPL" in keymap settings.

  (start)

  (restart)

  )
