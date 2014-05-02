(ns hiera-explorer.yaml
  (:require [clj-yaml.core :as yaml]))

(defn load-config [config-file]
  (yaml/parse-string
   (slurp config-file)))

(defn hierarchy [config]
  (config (keyword ":hierarchy")))

(defn scope-vars [config]
  (into #{}
        (for [level (hierarchy config)
              match (re-seq #"%\{(.*?)\}" level)]
          (second match))))
