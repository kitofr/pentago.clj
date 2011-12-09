(ns pentago-web.views.welcome
  (:require [pentago-web.views.common :as common]
            [pentago-web.models.game :as game]
            [pentago-web.responses :as resp]
            [noir.session :as session]
            [noir.content.getting-started]
            [clj-json [core :as json]])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]))

(defn json-data [rq]
  (:json-data (:params rq)))

(defpage [:post "/game"] { }
         (let [id (str (java.util.UUID/randomUUID))]
          (session/put! id game/starting-board)
            (resp/created { :links { :self (str "game/" id) } } )))

(defpage "/game/:id" {game-id :id}
         (resp/ok { :board (session/get game-id) }))

(defpage [:put "/game/:id/move"] { game-id :id }
         (let [rq (json-data (req/ring-request))
               player (:player rq)
               space (:space rq)]
           (session/put! game-id (game/move player space (session/get game-id))) 
           (resp/accepted { :board (session/get game-id ) } )))
