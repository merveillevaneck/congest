(ns congest.gest)


(defn after [delay task]
  (let [condition (promise)]
    (future
      (cond (not (= :stop (deref condition delay :timeout)))
            (task)))
    (fn []
      (deliver condition :stop))))

(defn at-every [interval task]
  (let [condition (promise)]
    (future
      (loop []
        (task)
        (when-not (= :stop (deref condition interval :timeout))
          (recur))))
    (fn [] (deliver condition :stop))))

(def schedule {:at-every at-every
               :after after})

(defn create-scheduler [type] ((keyword type) schedule))