(ns hiera-explorer.yaml-test
  (:require [clojure.test :refer :all]
            [hiera-explorer.yaml :refer :all]))

(def config-file "resources/example/hiera.yaml")

(deftest config
  (let  [conf (load-config config-file)]
    (testing "load hiera.yaml config file"
      (is (contains? conf (keyword ":backends")))
      (is (contains? conf (keyword ":yaml")))
      (is (contains? conf (keyword ":hierarchy"))))
    (testing "load hierarchy from config"
      (is (= (first (hierarchy conf))
             "%{::fqdn}"))
      (is (= (last (hierarchy conf))
             "common")))
    (testing "finds the scope variables"
      (is (= (scope-vars conf)
             #{"::fqdn" "::vertical" "::group" "::server_type"})))))
