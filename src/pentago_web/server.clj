(ns pentago-web.server
  (:require [noir.server :as server]
            [clj-json [core :as json]]))

(server/load-views "src/pentago_web/views/")

(defn backbone [handler]
  (fn [req]
    (let [neue (if (= "application/json" (get-in req [:headers "content-type"]))
              (update-in req [:params] assoc :json-data (json/parse-string (slurp (:body req)) true))
              req)]
      (handler neue))))

(server/add-middleware backbone)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'pentago-web})))

