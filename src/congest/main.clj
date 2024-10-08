(ns congest.main
  (:require [org.httpkit.server :as http]
            [congest.env :refer [env]]
            [congest.routes :as r]
            [clojure.pprint :as pp]))


(def PORT (:PORT env))

(def *server (atom nil))

(defn start [] (if (nil? @*server)
                 (reset! *server (http/run-server r/app {:port PORT}))
                 (println " Server on port " PORT " already started... ")))

(defn stop []
  (if (not (nil? @*server))
    (@*server)
    (println " No server running on port " PORT))
  (reset! *server nil))

(println @*server)

(defn -main [& args]
  (pp/pprint (str "Starting server with options " args "...."))
  (start))

