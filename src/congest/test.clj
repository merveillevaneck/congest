(ns congest.test
  (:require [congest.service :as s]
            [congest.schema :as is]
            [congest.middleware :as mw]
            [congest.gest :as gest]
            [congest.routes :as routes]))

;; TODO write some actual tests here

;; TESTING <== its working!!!!!!
(s/start-service s/mock-service)
(s/stop-service (:id s/mock-service))
(s/stop-all)

(r/register-service s/mock-service)
(r/deregister-service s/mock-service)
;; END TESTING