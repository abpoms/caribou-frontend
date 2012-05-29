(ns caribou.app.controller
  (:use [cheshire.core :only (generate-string)])
  (:require [clojure.java.io :as io]
            [caribou.util :as util]))

(defn get-controller-action
  [controller-ns controller-key action-key]
  ;FIXME is string concatenation idomatic?
  (if (and controller-key action-key)
    (let [full-ns-name (str controller-ns "." controller-key)
          full-ns (symbol full-ns-name)]
      (try
        (do
          (require :reload full-ns)
          (ns-resolve full-ns (symbol action-key)))
        (catch Exception e (println "Cannot load namespace " full-ns-name))))))


(defn render
  [params & [^String response-type]]
    (let [template (params :template)
          response { :status (or (:status params) 200)
                     :session (:session params)
                     :body (template params)
                     :headers { "Content-Type" "text/html" }}]

      (condp = response-type
        :json (assoc response :body (generate-string (template params))
                              :headers { "Content-Type" "application/json" })
        response)))
