(ns congest.routes
  (:require [compojure.core :refer [GET POST defroutes] :as comp]
            [congest.gest :as g]
            [clojure.pprint :as pp]
            [org.httpkit.client :as client]))

(def url "https://random-data-api.com/api/v3/projects/5eb844a0-736e-43a8-8359-41ac7ca7c0b9?api_key=CTMHkU8YolE7YfhdhVvK5g")

(defn home [req] (client/get url {:headers {"Content-Type" "application/json"}}
                             (fn [res] (pp/pprint res))) {:status 200
                                                          :body " Hello, World! "})
(defn greet [_] (g/schedule-greet))

(defn end [_] (g/release) {:status 200 :body "messages stopped"})

(defroutes app
  (GET "/" [] home)
  (GET "/schedule" [] greet)
  (POST "/schedule" [] greet)
  (GET "/stop" [] end))
