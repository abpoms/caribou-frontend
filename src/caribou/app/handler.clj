(ns caribou.app.handler
  (:use caribou.debug
        [ring.middleware.content-type :only (wrap-content-type)]
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.resource :only (wrap-resource)]
        [ring.middleware.file-info :only (wrap-file-info)]
        [ring.middleware.head :only (wrap-head)]
        [ring.middleware.json-params :only (wrap-json-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.session :only (wrap-session)]
        [ring.util.response :only (resource-response file-response)])
  (:require [caribou.util :as util]
            [caribou.config :as core-config]
            [caribou.model :as core-model]
            [caribou.db :as core-db]
            [caribou.app.halo :as halo]
            [caribou.app.i18n :as i18n]
            [caribou.app.middleware :as middleware]
            [caribou.app.pages :as pages]
            [caribou.app.error :as error]
            [caribou.app.request :as request]
            [caribou.app.routing :as routing]
            [caribou.app.template :as template]
            [caribou.app.util :as app-util]))

(declare reset-handler)

(defn use-public-wrapper
  [handler public-dir]
  (if public-dir
    (fn [request] ((wrap-resource handler public-dir) request))
    (fn [request] (handler request))))

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn static-handler
  [options response-fn]
  (fn [request]
    (let [options (merge {:root "public"} options)
          file-path (-> request :route-params :*)]
      (response-fn file-path options))))

(defn static
  [route-key response-fn]
  (fn [path & [options]]
    (routing/add-route
     route-key
     :get
     (add-wildcard path)
     (static-handler options response-fn))))

(def files (static :--ASSETS file-response))
(def resources (static :--RESOURCES resource-response))

(defn wrap-request-response-cycle
  [handler]
  (fn [request]
    (log/debug request :REQUEST)
    (let [response (handler request)]
      (log/debug response :RESPONSE)
      response)))

(defn init-routes
  []
  (middleware/add-custom-middleware middleware/wrap-xhr-request)
  (let [routes (routing/routes-in-order @routing/routes)]
    (routing/add-head-routes)))

(defn handler
  []
  (-> (routing/router @routing/routes)
      (middleware/wrap-custom-middleware)
      (wrap-file-info)
      (wrap-head)))

