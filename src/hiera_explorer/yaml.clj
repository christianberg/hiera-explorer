(ns hiera-explorer.yaml
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [me.raynes.fs :as fs]))

(defn load-config [config-file]
  (->
   (yaml/parse-string
    (slurp config-file))
   (assoc ::config-file (fs/file config-file))))

(defn hierarchy [config]
  (map-indexed
   (fn [index level] {:definition level :index index})
   (config (keyword ":hierarchy"))))

(defn dirty? [string]
  (not (re-seq #"^[a-zA-Z0-9._-]*$" string)))

(defn expand-variables [string value-map]
  "Replaces patterns of the form %{variable-name} with values from the a map

If a pattern %{variable-name} exists in the string and their either is
no key of the same name in the map or the value for the key is nil or
the empty string, do not replace the pattern."
  (reduce (fn [string [variable-name value]]
            (if (or (nil? value)
                    (= value "")
                    (dirty? (str value)))
              string
              (str/replace string
                           (str "%{" variable-name "}")
                           (str value))))
          string
          value-map))

(defn expand-hierarchy [levels value-map]
  (for [level levels]
    (let [expanded-string (str (expand-variables (:definition level) value-map)
                               ".yaml")
          fully-expanded? (not (re-seq #"%\{" expanded-string))]
      (assoc level
        :expanded expanded-string
        :fully-expanded? fully-expanded?))))

(defn scope-vars [config]
  (into #{}
        (for [{level :definition} (hierarchy config)
              match (re-seq #"%\{(.*?)\}" level)]
          (second match))))

(defn data-dir [config explicit-data-dir]
  (let [data-dir (or explicit-data-dir
                     (-> config
                         (get (keyword ":yaml") {})
                         (get (keyword ":datadir")))
                     "")]
    (if (fs/absolute? data-dir)
      (fs/file data-dir)
      (fs/file (fs/parent (::config-file config)) data-dir))))

(defn find-data-files [levels data-dir]
  (for [level levels]
    (or (when (:fully-expanded? level)
          (let [full-path (fs/file data-dir (:expanded level))]
            (when (fs/exists? full-path)
              (assoc level :data-file full-path))))
        level)))

(defn load-and-parse-data-files [levels]
  (for [{:keys [data-file] :as level} levels]
    (if data-file
      (let [raw-content (slurp data-file)]
        (assoc
            level
          :raw-content raw-content
          :parsed-content (yaml/parse-string raw-content :keywords false)))
      level)))
