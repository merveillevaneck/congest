(ns congest.routes
  (:require [compojure.core :refer [GET POST defroutes] :as comp]
            [congest.gest :as g]
            [clojure.pprint :as pp]
            [org.httpkit.client :as client]
            [jsonista.core :as json]))


(defn parse [body] (json/read-value body json/keyword-keys-object-mapper))

(def url "https://random-data-api.com/api/v3/projects/5eb844a0-736e-43a8-8359-41ac7ca7c0b9?api_key=CTMHkU8YolE7YfhdhVvK5g")

(defn home [req]
  (let [{:keys [status error body]} @(client/get url)]
    {:status status :body body :error error :headers {"content-type" "application/json"}}))

(home {:body nil})

(defn greet [req]
  (let [{:keys [body]} req bod (parse body)]
    (pp/pprint bod)
    {:status 200
     :body {:message "Schedule received"}})
  ;; (g/schedule-greet)
  )
(defn release [_] (g/release-all) {:status 200 :body "messages stopped"})

(greet {:body nil})
(release {:body nil})
(defroutes app
  (GET "/" [] home)
  (GET "/schedule" [] greet)
  (POST "/schedule" [] greet)
  (GET "/stop" [] release))
