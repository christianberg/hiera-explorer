(defproject hiera-explorer "0.2.7"
  :description "A web app to visualize Hiera configuration data"
  :url "http://github.com/christianberg/hiera-explorer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.5.0"]
                 [hiccup "1.0.5"]
                 [circleci/clj-yaml "0.5.5"]
                 [me.raynes/fs "1.4.6"]
                 [prone "1.1.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :main hiera-explorer.main
  :aot :all
  :ring {:handler hiera-explorer.core/web})
