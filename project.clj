(defproject hiera-explorer "0.2.0-SNAPSHOT"
  :description "A web app to visualize Hiera configuration data"
  :url "http://github.com/christianberg/hiera-explorer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [hiccup "1.0.5"]
                 [circleci/clj-yaml "0.5.2"]
                 [me.raynes/fs "1.4.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :main hiera-explorer.main
  :aot :all
  :ring {:handler hiera-explorer.core/web})
