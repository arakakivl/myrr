(ns hello-world.api.core
  (:require [hello-world.api.handlers.resource1-handler :refer :all]
            [hello-world.api.handlers.resource2-handler :refer :all]
            [hello-world.api.middlewares.api-middlewares :refer :all]))

;; Valid HTTP methods for this API:
(def valid-http-methods 
  [:get :post :put :delete :patch])

(def router-handler-map
  {"api/v1" {:get (fn [req] {:status 200 :body "Ok"})
             :children {"resource1" {:get resource1-get-handler :post resource1-post-handler}
                        "resource2" {:get resource2-get-handler}}}})

(defn base-handler [request]
  (let [request-method (:request-method request)
        target-endpoint (:uri request)]
    (if-let [endpoint-handler (get-in router-handler-map [target-endpoint request-method])]
      (endpoint-handler request)
      {:status 404 :body "Not found."})))

(def app
  (-> base-handler
      (test-middleware)))
