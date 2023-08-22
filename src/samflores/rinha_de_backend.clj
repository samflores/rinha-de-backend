(ns samflores.rinha-de-backend
  (:require
   [integrant.core :as ig]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as router]
   [jdbc.pool.c3p0    :as pool]
   [samflores.db.core :refer [make-db-interceptor]]
   [samflores.db.people :refer [create-people-table]]
   [samflores.db.people-stacks :refer [create-people-stacks-table]]
   [samflores.db.stacks :refer [create-stacks-table]]
   [samflores.handlers :as handlers]
   [samflores.jetty :as jetty])
  (:gen-class))

(def ^:private config
  (ig/read-string (slurp "src/config.edn")))

(defmethod ig/init-key :adapter/http [_ {:keys [port routes db]}]
  (let [database-interceptor (make-db-interceptor db)]
    (->
     {:env :prod
      ::http/port port
      ; ::http/type :jetty
      ::http/type jetty/jetty-server-fn
      ::http/chain-provider jetty/direct-jetty-provider
      ::http/join? true
      ::http/interceptors [router/query-params
                           (router/router routes)
                           database-interceptor]}
     ; http/default-interceptors
     ; (update ::http/interceptors conj database-interceptor)
     ; (update ::http/interceptors conj (body-params/body-params))
     ; (update ::http/interceptors conj http/json-body)
     http/create-server
     http/start)))

(defmethod ig/halt-key! :adapter/http [_ {stop ::http/stop-fn}]
  (stop))

(defmethod ig/init-key :database/pool [_ spec]
  (let [db (pool/make-datasource-spec spec)]
    (doto db
      (create-people-table)
      (create-stacks-table)
      (create-people-stacks-table))))

(defmethod ig/init-key :handler/routes [_ {:keys [routes]}]
  (->>
   routes
   (map #(update-in % [2] handlers/make-handler))
   set
   router/expand-routes))

; (def system
;   (ig/init config))

(defn -main [& _]
  (ig/init config))
