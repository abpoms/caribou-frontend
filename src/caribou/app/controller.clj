(ns caribou.app.controller
  (:require [clojure.java.io :as io]
            [caribou.util :as util]))

(defn get-controller-action
  "Find the function corresponding to the given controller namespace and
   its name in that namespace"
  [controller-ns controller-key action-key]
  (if (and controller-key action-key)
    (let [full-ns-name (str controller-ns "." controller-key)
          full-ns (symbol full-ns-name)]
      (try
        (do
          (require :reload full-ns)
          (ns-resolve full-ns (symbol action-key)))
        (catch Exception e (println "Cannot load namespace" full-ns-name (str e)))))))

(def content-map
  {:json "application/json"
   :iframe/json "text/plain"})

(defn render
  "Render the template corresponding to this page and return a proper response."
  ([content-type params]
     (render (assoc params :content-type (content-type content-map))))
  ([params]
    (let [template (:template params)]
      {:status (or (:status params) 200)
       :session (:session params)
       :body (template params)
       :headers {"Content-Type" (or (:content-type params) "text/html")}})))

(defn redirect
  "Return a response corresponding to a redirect triggered in the user's browser."
  ([url]
     (redirect url {}))
  ([url params]
     (let [headers (merge (:headers params) {"Location" url})]
       (merge params {:status 302 :headers headers}))))

(defn cookie
  "Get the value from the given cookie."
  [request key]
  (if-let [cookies (:cookies request)]
    (if-let [cookie (cookies (name key))]
      (:value cookie))))
