(ns pentago-web.responses
  (:require [noir.response :as response]
            [clj-json [core :as json]]))

(defn status-response [code content]
  (merge {:status code}
         (response/json content)))

;OKs
(defn ok [content] (status-response 200 content))
(defn created [content] (status-response 201 content))
(defn accepted [content] (status-response 202 content))


;ERRORS
(defn forbidden [content] (status-response 403 content))
(defn bad-request [content] (status-response 404 content))
(defn not-acceptable [content] (status-response 406 content))
(defn conflict [content] (status-response 409 content))

