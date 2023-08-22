(ns samflores.db.core
  (:require
   [io.pedestal.interceptor :as i]))

(defn make-db-interceptor [database]
  (i/interceptor
   {:name ::database-interceptor
    :enter #(update % :request assoc :database database)}))

