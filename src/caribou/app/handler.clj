(ns caribou.app.handler
  (:use
        [compojure.core :only (routes)]
        caribou.debug
        [ring.middleware.file :only (wrap-file)])
  (:require
        [caribou.util :as util]
        [compojure.handler :as compojure-handler]
        [caribou.config :as core-config]
        [caribou.model :as core-model]
        [caribou.db :as core-db]
        [caribou.app.halo :as halo]
        [caribou.app.pages :as pages]
        [caribou.app.request :as request]
        [caribou.app.routing :as routing]
        [caribou.app.template :as template]
        [caribou.app.util :as app-util]))

(declare reset-handler)

(defn use-public-wrapper
  [handler]
  (if-let [public-dir (@core-config/app :public-dir)]
    (fn [request] ((wrap-file handler public-dir) request))
    (fn [request] (handler request))))

(defn _dynamic-handler
  "calls the dynamic route generation functions and returns a composite handler"
  []
  (log :handler "Creating handler.")
  (core-model/init)
  (template/init)
  (pages/create-page-routes)
  (halo/init reset-handler)
  (-> (apply routes (vals @routing/caribou-routes))
      (use-public-wrapper)
      (core-db/wrap-db @core-config/db)
      (compojure-handler/api)))

(def dynamic-handler (app-util/memoize-visible-atom _dynamic-handler))

(defn gen-handler
  "Returns a function that calls our memoized handler on every request"
  []
  (fn [request]
    ((dynamic-handler) request)))

(defn reset-handler 
  "clears the memoize atom in the metadata for dynamic-handler, which causes it to 'un-memoize'"
  []
  (log :handler "Resetting Handler")
  (routing/clear-routes)
  (app-util/memoize-reset dynamic-handler))