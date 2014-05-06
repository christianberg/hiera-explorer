(ns hiera-explorer.main
  (:require [hiera-explorer.core :refer [web]]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(def port (if-let [port-string (System/getenv "PORT")]
            (Integer/parseInt port-string)
            3000))

(defn -main []
  (jetty/run-jetty web {:port port}))
