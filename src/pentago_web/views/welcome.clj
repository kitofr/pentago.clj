(ns pentago-web.views.welcome
  (:require [pentago-web.views.common :as common]
            [pentago-web.models.game :as game]
            [pentago-web.responses :as resp]
            [noir.request :as req]
            [noir.session :as session]
            [noir.content.getting-started]
            [clj-json [core :as json]])
  (:use [noir.core :only [defpage]]))

(defn json-data [rq]
  (:json-data (:params rq)))

(defn to-int [x]
  (Integer/parseInt x))

(defn store! [id board players]
  (session/put! id { :board board :players players}))

(defn board [id] ((session/get id) :board))
(defn players [id] ((session/get id) :players))
(defn in-turn [id] (first (cycle (players id))))

(defn game-state [id]
  { :game {:board (board id) 
           :in-turn  (in-turn id)
           :players (players id) } } )

(defn swap-players [id]
  (vec (take 2 (next (cycle (players id))))))

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

(defn is-playing? [player id]
  (= player (in-turn id)))

(defpage [:put "/game/:id/set"] { game-id :id }
         (let [rq (json-data (req/ring-request))
               space (:space rq)
               player (:player rq)
               board (board game-id)]
           (cond 
             (not (is-playing? player game-id)) (resp/forbidden (game-state game-id))
             (not (game/available? space board)) (resp/conflict (game-state game-id))
             true 
             (do 
               (store! game-id (game/move player space board) (players game-id)) 
               (resp/accepted (merge { :links { :self (str "game/" game-id) 
                                               :turn (str "game/" game-id "/turn/{corner}")
                                               :pass (str "game/" game-id "/pass") } } 
                                     (game-state game-id )))))))

(defpage [:post "/game/:id/pass"] { game-id :id }
         (let [rq (json-data (req/ring-request))
               player (:player rq)]
           (if (not (is-playing? player game-id)) 
             (resp/forbidden (game-state game-id))
             (do
               (store! game-id (board game-id) (swap-players game-id))
               (resp/accepted (game-state game-id))))))

(defpage [:put "/game/:id/turn"] { game-id :id }
         (let [board (board game-id)
               rq (json-data (req/ring-request))
               player (:player rq)
               corner (:corner rq)
               dir (:dir rq)]
           (if (not (is-playing? player game-id))
             (resp/forbidden (game-state game-id))
             (do
               (store! game-id (game/restore-board (game/turn corner dir board)) (swap-players game-id))
               (resp/accepted (game-state game-id))))))
