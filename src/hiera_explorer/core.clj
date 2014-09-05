(ns hiera-explorer.core
  (require [hiccup.page :as h]
           [hiccup.form :as f]
           [hiera-explorer.yaml :as yaml]
           [clojure.string :as str]
           [clojure.pprint :refer [pprint]]
           [ring.middleware.params :refer [wrap-params]]
           [prone.middleware :as prone]))

(def config-file (or (System/getenv "HIERA_CONFIG")
                     "resources/example/hiera.yaml"))

(def data-dir-override (System/getenv "HIERA_DATADIR"))

(def navbar
  [:div.navbar.navbar-default
   [:div.container
    [:div.navbar-header.navbar-brand
     "Hiera Explorer"]]])

(def solarized-colors
  ["b58900"
   "6c71c4"
   "cb4b16"
   "268bd2"
   "dc322f"
   "d33682"
   "859900"
   "2aa198"])

(def level-styles
  (apply str
         (map-indexed
          (fn [index color]
            (format ".hier-level-%d { color: #%s; }\n" index color))
          solarized-colors)))

(defn expanded-class [{:keys [index data-file]}]
  (if data-file
    (str "hier-level-" index)
    "text-muted"))

(defn raw-class [{:keys [index]}]
  (str "hier-level-" index))

(defn hierarchy-view [hierarchy name-key class-fn]
  [:ul
   (for [level hierarchy]
     [:li {:class (class-fn level)} (name-key level)])])

(defn var-form [variable-map]
  (f/form-to {:class "form-horizontal"}
             [:get "/"]
             (for [[name value] (sort variable-map)]
               [(if (yaml/dirty? value)
                  :div.form-group.has-error
                  :div.form-group)
                (f/label {:class "col-md-2 control-label"} name name)
                [:div.col-md-10
                 (f/text-field {:class "form-control"} name value)
                 (when (yaml/dirty? value)
                   [:span.help-block
                    "Only the following characters are allowd: a-z A-Z 0-9 _ -"])]])
             [:div.form-group
              [:div.col-md-offset-2.col-md-10
               (f/submit-button {:class "btn btn-primary"} "Submit")]]))

(defn make-panel [panel-title panel-id title-class tabs]
  [:div.panel.panel-default
   [:div.panel-heading
    [:a {:href (str "#" panel-id)
         :data-toggle "collapse"}
     [:div.row
      [:div.col-md-12
       [:h3.panel-title
        {:class title-class}
        [:span.caret] " "
        panel-title
        [:small.pull-right "Click to expand"]]]]]]
   [:div.panel-collapse.collapse
    {:id panel-id}
    [:div.panel-body
     [:ul.nav.nav-tabs
      (for [tab tabs]
        [(if (:active tab) :li.active :li)
         [:a {:href (str "#" (str panel-id \- (:id tab))) :data-toggle "tab"}
          (:title tab)]])]
     [:div.tab-content
      (for [tab tabs]
        [(if (:active tab) :div.tab-pane.active :div.tab-pane)
         {:id (str panel-id \- (:id tab))}
         (:content tab)])]]]])

(defn show-merged-data [data]
  (make-panel "Merged Data"
              "datafile-panel-merged"
              ""
              [{:id "table"
                :title "Table"
                :active true
                :content [:table.table.table-striped
                          [:thead
                           [:tr [:th "Key"] [:th "Value"] [:th "Source"]]]
                          [:tbody
                           (for [[k v] (sort data)]
                             [:tr {:class (str "hier-level-" (:index v))}
                              [:td (str k)]
                              [:td (str (:value v))]
                              [:td (str (:source v))]])]]}]))

(defn show-data-files [hierarchy]
  (for [{:keys [expanded raw-content parsed-content index] :as level} hierarchy
        :when raw-content]
    (make-panel expanded
                (str "datafile-panel-" index)
                (expanded-class level)
                [{:id "table"
                  :title "Table"
                  :active true
                  :content [:table.table.table-striped
                            [:thead
                             [:tr [:th "Key"] [:th "Value"]]]
                            [:tbody
                             (for [[k v] (sort parsed-content)]
                               [:tr [:td (str k)] [:td (str v)]])]]}
                 {:id "raw"
                  :title "Raw"
                  :content [:pre raw-content]}])))

(defn get-handler [& {:keys [config-file hiera-data-dir]}]
  (fn [request]
    (let [conf (yaml/load-config config-file)
          data-dir (yaml/data-dir conf hiera-data-dir)
          variable-names (yaml/scope-vars conf)
          variable-map (merge
                        (apply hash-map (interleave variable-names (repeat "")))
                        (select-keys (:query-params request) variable-names))
          hierarchy (-> conf
                        yaml/hierarchy
                        (yaml/expand-hierarchy variable-map)
                        (yaml/find-data-files data-dir)
                        yaml/load-and-parse-data-files)]
      {:status 200
       :body
       (h/html5
        [:head
         [:title "Hiera Explorer"]
         (h/include-css
          "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css")
         (h/include-js
          "https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"
          "//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js")
         [:style level-styles]]
        [:body
         navbar
         [:div.container
          [:div.panel.panel-default
           [:div.panel-heading
            [:div.row
             [:div.col-md-12
              [:h3.panel-title "Scope Variables"]]]]
           [:div.panel-body
            (var-form variable-map)]]
          [:div.panel.panel-default
           [:div.panel-heading
            [:div.row
             [:div.col-md-12
              [:h3.panel-title "Hierarchy"]]]]
           [:div.panel-body
            [:div.row
             [:div.col-md-6
              [:h4 "Definition"]
              (hierarchy-view hierarchy :definition raw-class)]
             [:div.col-md-6
              [:h4 "Expanded"]
              (hierarchy-view hierarchy :expanded expanded-class)]]]]
          (show-merged-data (yaml/merge-data-files (reverse hierarchy)))
          (show-data-files hierarchy)]])})))

(def web (-> (get-handler :config-file config-file
                          :hiera-data-dir data-dir-override)
             wrap-params
             prone/wrap-exceptions))
