(ns congest.middleware
  (:require [congest.schema :as is]
            [jsonista.core :as json]))

(defn parse-body [req]
  (try
    (if (and (is/request? req) (some? (:body req)))
      (json/read-value (:body req) json/keyword-keys-object-mapper)
      nil)
    (catch Exception _ nil)))

(defn stringify [body]
  (json/write-value-as-string body json/keyword-keys-object-mapper))

(defn handle-response [response]
  (println "here")
  (let [resbody (:body response)
        has-body (some? resbody)
        parse? (and (coll? resbody) has-body)]
    (if parse?
      (assoc response :body (stringify resbody))
      response)))

(defn wrap [handler]
  (fn [req]
    (handle-response
     (handler (parse-body (:body req))))))