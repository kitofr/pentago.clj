(ns pentago-web.views.welcome
  (:require [pentago-web.views.common :as common]
            [noir.response :as response]
            [noir.session :as session]
            [noir.content.getting-started]
            [pentago-web.models.game :as game])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]))

(defn created [content]
  (merge {:status 201}
         (response/json content)))

(defpage [:post "/game"] { }
         (let [id (str (java.util.UUID/randomUUID))]
          (session/put! id game/starting-board)
            (created { :links { :self (str "game/" id) } } )))

(defpage "/game/:id" {game-id :id}
         (response/json { :board (session/get game-id) }))

