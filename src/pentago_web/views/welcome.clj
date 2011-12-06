(ns pentago-web.views.welcome
  (:require [pentago-web.views.common :as common]
            [noir.response :as response]
            [noir.session :as session]
            [noir.request :as req]
            [noir.server :as server]
            [noir.content.getting-started]
            [clj-json [core :as json]]
            [pentago-web.models.game :as game])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]))

(defn ok [content]
  (merge {:status 200}
         (response/json content)))

(defn created [content]
  (merge {:status 201}
         (response/json content)))

(defn accepted [content]
  (merge {:status 202}
         (response/json content)))

(defn backbone [handler]
  (fn [req]
    (let [neue (if (= "application/json" (get-in req [:headers "content-type"]))
              (update-in req [:params] assoc :json-data (json/parse-string (slurp (:body req)) true))
              req)]
      (handler neue))))

(server/add-middleware backbone)

(defn json-data [rq]
  (:json-data (:params rq)))

(defpage [:post "/game"] { }
         (let [id (str (java.util.UUID/randomUUID))]
          (session/put! id game/starting-board)
            (created { :links { :self (str "game/" id) } } )))

(defpage "/game/:id" {game-id :id}
         (ok { :board (session/get game-id) }))

(defpage [:put "/game/:id/move"] { game-id :id }
         (let [rq (json-data (req/ring-request))
               player (:player rq)
               space (:space rq)]
           (session/put! game-id (game/move player space (session/get game-id))) 
           (accepted { :board (session/get game-id ) } )))
