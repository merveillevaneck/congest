(ns congest.gest
  (:require [overtone.at-at :as at]))

(def pool (at/mk-pool))

(defn schedule-greet [] (at/every 1000 #(println "hello") pool))
(defn release [] (at/stop-and-reset-pool! pool))


(schedule-greet)
(release)