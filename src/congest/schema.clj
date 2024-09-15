(ns congest.schema
  (:require [malli.core :as m]))

(def REQUEST [:map [:body {:optional true} [:map]]])

(def ID_PAYLOAD [:map [:id :string]])
(def SERVICE [:map
              [:id :string]
              [:url :string]
              [:interval :int]
              [:recur {:optional true} :boolean]
              [:method :string]])

(defn id-payload? [body] (m/validate ID_PAYLOAD body))

(defn request? [req] (m/validate REQUEST req))

(defn service? [service] (m/validate SERVICE service))