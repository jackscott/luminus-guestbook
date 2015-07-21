(ns luminus-guestbook.routes.home
  (:require [luminus-guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [luminus-guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
   "home.html"
   (merge {:messages (db/run db/get-messages)}
          (select-keys flash [:name :message :errors]))))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" request (home-page request))
  (POST "/" request (save-message! requesst))
  (GET "/about" [] (about-page)))

(defn validate-message [params]
  (first
   (b/validate params
               :name v/required
               :messages [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (redirect "/")
        (assoc :flash (assoc params :errors errors)))
    (docs/docs.md (db/run
                    db/save-message!
                    (assoc params :timestamp (java.util.Date.)))
                  (redirect "/"))))
