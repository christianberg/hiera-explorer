(ns hiera-explorer.yaml
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]))

(defn load-config [config-file]
  (yaml/parse-string
   (slurp config-file)))

(defn hierarchy [config]
  (config (keyword ":hierarchy")))

(defn expand-variables [string value-map]
  "Replaces patterns of the form %{variable-name} with values from the a map

If a pattern %{variable-name} exists in the string and their either is
no key of the same name in the map or the value for the key is nil or
the empty string, do not replace the pattern."
  (reduce (fn [string [variable-name value]]
            (if (or (nil? value) (= value ""))
              string
              (str/replace string
                           (str "%{" variable-name "}")
                           (str value))))
          string
          value-map))

(defn scope-vars [config]
  (into #{}
        (for [level (hierarchy config)
              match (re-seq #"%\{(.*?)\}" level)]
          (second match))))
