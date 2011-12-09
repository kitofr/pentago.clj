(ns pentago-web.responses
  (:require [noir.response :as response]
            [clj-json [core :as json]]))

(defn ok [content]
  (merge {:status 200}
         (response/json content)))

(defn created [content]
  (merge {:status 201}
         (response/json content)))

(defn accepted [content]
  (merge {:status 202}
         (response/json content)))
