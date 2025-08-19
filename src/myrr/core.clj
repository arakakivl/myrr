(ns final.core
  (:gen-class))

(defn- build-full-path
  [base-path path]
  (clojure.string/replace (str "/" base-path "/" path) #"//" "/"))

(defn- retrieve-flat-routes
  "Returns a flat map of endpoints in which every map key represents
  a route and every value represents a map of HTTP methods to handlers."
  [base-path base-routes]
  (reduce (fn [result [path routes]]
            (let [final-path (build-full-path base-path path)
                  route-methods (dissoc routes :children)
                  updated-result (if (empty? route-methods) result (assoc result final-path route-methods))]
              (into updated-result (hash-set (retrieve-flat-routes final-path (get routes :children {}))))))
          {} base-routes))

(defn routing-handler
  "Returns a base handler that redirects requests to endpoint handlers."
  [routes-map]
  (fn [req]
    (let [request-method (:request-method req)
          target-endpoint (:uri req)]
      (if-let [endpoint-handler (get-in (retrieve-flat-routes routes-map) [target-endpoint request-method])]
        (endpoint-handler)
        {:status 404 :body "Not found."}))))
