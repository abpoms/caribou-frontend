(ns caribou.app.request)

(declare ^{:dynamic true} *request*)

(defn ring-request
  "Returns back the current ring request map"
  []
  *request*)

(defn wrap-request-map [handler]
  (fn [req]
    (binding [*request* req]
      (handler req))))
