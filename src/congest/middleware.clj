(ns congest.middleware
  (:require [congest.schema :as is]
            [jsonista.core :as json]))

;;  Will validate a request, parse the body if it exists
;; and return it. If the body isnt present, returns nil.
(defn parse-body [req]
  (try
    (if (and (is/request? req) (some? (:body req)))
      (json/read-value (:body req) json/keyword-keys-object-mapper)
      nil)
    (catch Exception _ nil)))

;; just stringified the body, assuming all keys are keywords.
(defn stringify [body]
  (json/write-value-as-string body json/keyword-keys-object-mapper))

;; pre-stringifies the response of a handler by
;; checking if it is a collection first
(defn handle-response [response]
  (println "here")
  (let [resbody (:body response)
        has-body (some? resbody)
        parse? (and (coll? resbody) has-body)]
    (if parse?
      (assoc response :body (stringify resbody))
      response)))

;; Wraps a handler in parse-body and stringify.
;; Processes the request body into a keywords-map.
;; processes the response of handler into a an http
;; readable respnse.
(defn wrap [handler]
  (fn [req]
    (handle-response
     (handler (parse-body (:body req))))))