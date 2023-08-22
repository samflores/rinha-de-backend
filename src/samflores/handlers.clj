(ns samflores.handlers
  (:require
   [samflores.model.people :as people]
   [io.pedestal.http.body-params :refer [json-parser]]))

(defn make-handler [handler-name]
  (ns-resolve (find-ns 'samflores.handlers) (symbol handler-name)))

(defn find-people
  [{:keys [database query-params]}]
  (let [search-term (:t query-params)
        code (if search-term 200 400)
        body (when search-term (people/search database search-term))]
    {:status code
     :body body}))

(defn create-person
  [{:keys [database] :as request}]
  (let [json-params (-> request json-parser :json-params)
        creation-response (people/create database json-params)
        code (if (contains? creation-response :error) 400 201)]
    {:status code
     :body creation-response}))

(defn show-person
  [{database :database
    {person-id :person-id} :path-params}]
  {:status 200
   :body (people/find database person-id)})
