(ns congest.service
  (:require [org.httpkit.client :as c]
            [congest.gest :refer [at-every after]])) ;; the task scheduler API

;; the context of running services
(def ^{:private true} *services (atom (sorted-map)))

;; just return the private thang for now
;; will update the status reports to only
;; include specific info later
(defn get-service-status [] @*services)

(def test-url "https://random-data-api.com/api/v3/projects/5eb844a0-736e-43a8-8359-41ac7ca7c0b9?api_key=CTMHkU8YolE7YfhdhVvK5g")

(def mock-service {:id "panda army"
                   :interval 1000
                   :method "get"
                   :url test-url
                   :recur true})

(def callable {:get c/get
               :post c/post
               :delete c/delete
               :put c/put})

(defn running? [id] (contains? @*services id))

;; Responsible for writing a service map to the services context
(defn save-service [service]
  (let [{:keys [id]} service]
    (swap! *services assoc id service)))

;; Responsible for removing a service map from the services context
(defn remove-service [id]
  (cond (running? id)
        (swap! *services dissoc id)))

;; do i even need to say it?
(defn get-service [id] (get @*services id))

;; Returns if the service with key id is recurring
;; A server is considered non-recurring if: it doesnt exist the :recur
;; key is set to false
(defn recurring? [id]
  (if (running? id)
    (let  [service (get-service id)]
      (:recur service))
    false))

;; This is just a handler to help with calling the correct
;; http method when making outgoing requests
;; FIXME - add in an optional payload etc.
(defn gcall [method url]
  (println "fetching")
  (let [m (keyword method)
        c (get callable m)
        result @(c url)]
    (println result)
    (println "done fetching")))

;; Explicitly runs after invoke was called. only responsible
;; for clearning up non-recurring services. All recurring services
;; are indefinite by default (for now).
;; FIXME - add in conditional checks for num of iter on a service
;; (not implemented yet)
(defn cleanup [service]
  (if  (recurring? (:id service))
    nil
    (cond (contains? service :stop!)
          ((:stop! service)))))

;; Responsible for handling webhook invocation from the
;; scheduler. Only reference during task creation (once).
;; Uses the services context to get required context on
;; invocation (method, url, payload etc).
;; FIXME - add in optional passing of payload
(defn invoke [id]
  (if (not (running? id))
    nil
    (let [{:keys [method url] :as service} (get-service id)]
      (println (gcall (keyword method) url))
      (cleanup service))))


;; Creates a stop callback. Used primarily upon
;; registering a new service (once).
;; all services are deregistered (removed) when stopped.
(defn create-stop [id interval recur]
  (let [scheduler (if recur at-every after)
        stop (scheduler interval #(invoke id))]
    (fn []
      (println "stopping" id)
      (stop)
      (remove-service id))))

;; Responsible for registering a new service.
;; All services already present will be skipped.
;; Registering a service schedules a task (recurring or not)
;; immediately, and saves the service to the context with a stop handler.
(defn start-service [options]
  (tap> (str "Starting service " (:id options) "..."))
  (let [{:keys [id interval recur]} options]
    (if (not (running? id))
      (save-service
       (assoc
        options
        :stop! (create-stop id interval recur)))
      (str "Service with id " id " already running")))
  (tap> (str "Started service " (:id options) "!")))

;; Helper function used to stop a service if its context is known.
(defn stop! [service]
  (println "attempting to stop" (:id service))
  (let [stop (:stop! service)]
    (cond (some? stop)
          (stop)))
  (println "continuing..."))

;; Finds the stop function of a service if it exists
;; in context and invokes it. All stopped services are
;; implicitly removed by the stop invocation.
(defn stop-service [id]
  (if (running? id)
    (stop! (get-service id))
    (println "No running service for id" id)))

;; Responsible for stopping all services.
;; This implicitly removes all services from
;; services context as well AND resets the
;; services context.
(defn stop-all []
  (tap> "Removing all services from state...")
  (let [_ (map (fn [[_ service]]
                 (println "item: v")
                 (stop! service)
                 @*services))])
  (reset! *services {})
  (tap> "Removed all services from state!"))