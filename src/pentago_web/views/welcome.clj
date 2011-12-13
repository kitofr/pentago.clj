(ns pentago-web.views.welcome
  (:require [pentago-web.views.common :as common]
            [pentago-web.models.game :as game]
            [pentago-web.responses :as resp]
            [noir.request :as req]
            [noir.session :as session]
            [noir.content.getting-started]
            [clj-json [core :as json]])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]))

(defn json-data [rq]
  (:json-data (:params rq)))

(defn to-int [x]
  (Integer/parseInt x))

(defn store! [id board players]
  (session/put! id { :board board :players players}))

(defn next-player [id] (first (next (cycle ((session/get id) :players)))))
(defn board [id] ((session/get id) :board))
(defn players [id] ((session/get id) :players))

(defn game-state [id]
  { :game {:board (board id) 
       :next  (next-player id)
       :players (players id) } } )

(defpage [:post "/game"] { }
         (let [id (str (java.util.UUID/randomUUID))
               rq (json-data (req/ring-request))
               p1 (:player1 rq)
               p2 (:player2 rq)]
          (store! id game/starting-board [p1 p2])
            (resp/created (merge { :links { :self (str "game/" id) } } 
                            (game-state id)))))

(defpage "/game/:id" {game-id :id}
         (resp/ok (game-state game-id)))

(defpage [:put "/game/:id/:space"] { game-id :id space :space }
         (let [rq (json-data (req/ring-request))
               space (to-int space)
               player (:player rq)
               board (board game-id)]
           (if (game/available? space board)
             (do 
               (store! game-id (game/move player space board) (players game-id)) 
               (resp/accepted (merge { :links { :self (str "game/" game-id) 
                                                :turn (str "game/" game-id "/turn/{corner}")
                                                :pass (str "game/" game-id "/pass") } } 
                                      (game-state game-id ))))
             (resp/conflict (game-state game-id)))))

(defpage [:put "/game/:id/turn"] { game-id :id }
         (let [board (session/get game-id)
               rq (json-data (req/ring-request))
               corner (to-int (:corner rq))
               dir (to-int (:dir rq))]
           (session/put! game-id (game/restore-board (game/turn corner dir board)))
           (resp/accepted { :board (session/get game-id) })))
