;; SORRY BRO THIS FILE IS A MESS
(ns congest.routes
  (:require [compojure.core :refer [GET POST defroutes] :as comp]
            [org.httpkit.client :as client]
            [congest.service :as s]
            [congest.schema :as is]
            [congest.middleware :as mw]))


(def url "https://random-data-api.com/api/v3/projects/5eb844a0-736e-43a8-8359-41ac7ca7c0b9?api_key=CTMHkU8YolE7YfhdhVvK5g")

(defn home [_]
  (let [{:keys [status error body]}
        @(client/get url)]
    {:status status
     :body body
     :error error
     :headers {"content-type" "application/json"}}))

(defn deregister-service [body]
  (if (is/id-payload? body)
    (let [id (:id body)]
      (if (s/running? id)
        (do
          (s/stop-service id)
          {:status 200
           :body {:message 
                  (str "Service with id " id " started successfully!")}})
        {:status 404 :body {:message (str "Service with id " id " no found")}}))
    { :status 400 :body {:message "Invalid payload provided: Missing 'id'"} }
  ))

(defn register-service [service]
  (if (not (is/service? service))
    {:status 400 :body {:message "Invalid payload: missing or omitted fields" }}
    (let [result (s/start-service service)]
      {
       :status 200
       :body (if (coll? result)
               {:message "service started successfully" :data result}
               {:message result})
       }
      )))

(defn stop-all [_] (s/stop-all))

(defn service-status [_]
  {:status 200
   :body (s/get-service-status)})

(register-service {:body s/mock-service})
(defroutes app
  (GET "/" [] (mw/wrap home))
  (GET "/scheduled" [] (mw/wrap service-status))
  (POST "/register" [] (mw/wrap register-service))
  (POST "/deregister" [] (mw/wrap deregister-service))
  (POST "/stop-all" [] (mw/wrap stop-all)))
