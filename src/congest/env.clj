(ns congest.env
  (:require [dotenv :refer [app-env] :as e]
            [malli.core :as m]))

(def ENV [:map [:PORT :re #"^\d+$"]])

(defn get-env [] (let [v (map (fn [[k v]] [(keyword k) v]) (e/env))] (into (sorted-map) v)))

(defn validate-env []
  (let [env (get-env) valid (m/validate ENV env)]
    (if valid
      (assoc env :PORT (Integer/parseInt (:PORT env)))
      (do
        (println "Invalid env")
        (cond (not (= app-env "development")) (System/exit 1))))))

(def env (validate-env))
(println (:PORT env))
