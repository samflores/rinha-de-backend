(ns samflores.jetty
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.interceptor.chain :as chain]
   [clojure.data.json :as json])
  (:import (java.nio ByteBuffer)
           (java.net InetSocketAddress)
           (org.eclipse.jetty.server Request Server)
           (org.eclipse.jetty.server.handler AbstractHandler)))

(defn direct-jetty-provider
  [service-map]
  (let [interceptors (::http/interceptors service-map)]
    (assoc service-map
           ::handler
           (proxy [AbstractHandler] []
             (handle [target ^Request base-request servlet-req servlet-resp]
               (let [resp (.getResponse base-request)
                     initial-context {:request {:query-string     (.getQueryString base-request)
                                                :request-method   (keyword (.toLowerCase (.getMethod base-request)))
                                                :body             (.getInputStream base-request)
                                                :path-info        (.getRequestURI base-request)
                                                :async-supported? (.isAsyncSupported base-request)}}
                     resp-ctx (chain/execute initial-context interceptors)]
                 (.setContentType resp "application/json")
                 (.setStatus resp (get-in resp-ctx [:response :status]))
                 (.sendContent (.getHttpOutput resp)
                               (ByteBuffer/wrap (.getBytes ^String (json/write-str (get-in resp-ctx [:response :body])) "UTF-8")))
                 (.setHandled base-request true)))))))

(defn jetty-server-fn
  [service-map server-opts]
  (let [handler (::handler service-map)
        {:keys [host port join?]
         :or {host "127.0.0.1"
              port 8080
              join? false}} server-opts
        addr (InetSocketAddress. ^String host ^int port)
        server (doto (Server. addr)
                 (.setHandler handler))]
    {:server server
     :start-fn (fn []
                 (.start server)
                 (when join? (.join server))
                 server)
     :stop-fn (fn []
                (.stop server)
                server)}))

