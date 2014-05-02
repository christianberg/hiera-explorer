(ns hiera-explorer.core
  (require [hiccup.page :refer [html5]]
           [hiera-explorer.yaml :as yaml]
           [clojure.string :as str]))

(def config-file "resources/example/hiera.yaml")

(def navbar
  [:div.navbar.navbar-default
   [:div.container
    [:div.navbar-header.navbar-brand
     "Hiera Explorer"]]])

(def solarized-colors
  ["b58900"
   "cb4b16"
   "dc322f"
   "d33682"
   "6c71c4"
   "268bd2"
   "2aa198"
   "859900"])

(def values
  {"::group" "dev"
   "::fqdn" "host1.example.com"})

(def level-styles
  (apply str
         (map-indexed
          (fn [index color]
            (format ".hier-level-%d { color: #%s; }" index color))
          solarized-colors)))

(defn hierarchy-view [levels]
  [:ul
   (map-indexed
    (fn [index level]
      [:li {:class (str "hier-level-" index)} level])
    levels)])

(defn replace-variables [levels value-map]
  (for [level levels]
    (str
     (str/replace level
                  #"%\{(.*?)\}"
                  (fn [[_ var-name]]
                    (get value-map var-name "[not set]")))
     ".yaml")))

(defn web [request]
  (let [conf (yaml/load-config config-file)
        levels (yaml/hierarchy conf)]
    {:status 200
     :body
     (html5
      [:head
       [:link
        {:rel "stylesheet"
         :href "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"}]
       [:style level-styles]]
      [:body
       navbar
       [:div.container
        [:div.row
         [:div.col-md-6
          (hierarchy-view levels)]
         [:div.col-md-6
          (hierarchy-view (replace-variables levels values))]]]])}))
