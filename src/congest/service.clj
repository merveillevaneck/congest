(ns congest.service
  (:require [org.httpkit.client :as c]
            [congest.gest :refer [create-scheduler at-every]]))

(def ^{:private true} *services (atom (sorted-map)))

(def test-url "https://random-data-api.com/api/v3/projects/5eb844a0-736e-43a8-8359-41ac7ca7c0b9?api_key=CTMHkU8YolE7YfhdhVvK5g")

(def mock-service {:id "panda army"
                   :interval 1000
                   :method "get"
                   :url test-url})

(def callable {:get c/get
               :post c/post
               :delete c/delete
               :put c/put})

(defn gcall [method url]
  (let [m (keyword method) c (get callable m)]
    (tap> @(c url))))
(defn invoke [id method url]
  (let [result (gcall (keyword method) url)]
    (println (:status result))
    (swap! *services dissoc id)))

(println @((get callable (keyword :get)) test-url))

(println (gcall :get test-url))


(defn running? [id] (not (nil? (clojure.core/get @*services id))))
(defn get-service [id] (get @*services id))

(defn remove-service [id]
  (cond (running? id)
        (swap! *services dissoc id)))
(defn save-service [service]
  (let [{:keys [id]} service]
    (swap! *services assoc id service)))


(defn start-service [options]
  (tap> (str "Starting service " (:id options) "..."))
  (let [{:keys [id interval recur method url]} options]
    (if (not (running? id))
      (let [scheduler (create-scheduler
                       (if recur :at-every :after))
            stop (scheduler interval #(invoke id method url))]
        (save-service (assoc options :stop! stop)))
      (println "Service with id" id "already running")))
  (tap> (str "Starting service " (:id options) "...")))

(defn stop-service [id]
  (tap> (str "Stopping " id "..."))
  (if (running? id)
    (let [service (get-service id)
          stop (get service :stop!)]
      (cond (not (nil? stop))
            (stop))
      (remove-service id))
    (println "No running service for id" id))
  (tap> (str "Stopped " id "!")))

(defn stop-item [v]
  (tap> (str "Stopping " (:id v) "..."))
  (let [stop (:stop! v)]
    (cond (not (nil? stop))
          (stop)))
  (tap> (str "Stopped " (:id v) "!")))
(defn remove-all []
  (tap> "Removing all services from state...")
  (reset! *services {})
  (tap> "Removed all services from state!"))
(defn stop-all []
  (tap> "Stopping all services....")
  (map (fn [[_ v]]
         (stop-item v))
       @*services)
  (tap> "Services stopped!")
  (remove-all))

(start-service mock-service)
(stop-service (:id mock-service))
(println @*services)

(map (fn [[k v]] (tap> (str k v))) @*services)


