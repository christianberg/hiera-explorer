(ns hiera-explorer.yaml-test
  (:require [clojure.test :refer :all]
            [hiera-explorer.yaml :refer :all]))

(def config-file "resources/example/hiera.yaml")

(deftest variable-expansion
  (testing "replace all variables in string"
    (is (= (expand-variables "%{foo}/%{bar}" {"foo" "one" "bar" "two"})
           "one/two")))
  (testing "do not replace variables that are not in value-map"
    (is (= (expand-variables "%{foo}/%{unknown}" {"foo" "one"})
           "one/%{unknown}")))
  (testing "do not replace variables that are set to the empty string"
    (is (= (expand-variables "%{foo}/%{unset}" {"foo" "one" "unset" ""})
           "one/%{unset}")))
  (testing "do not replace variables that are set to nil"
    (is (= (expand-variables "%{foo}/%{unset}" {"foo" "one" "unset" nil})
           "one/%{unset}")))
  (testing "do replace variables that are set to false"
    (is (= (expand-variables "%{foo}/%{bar}" {"foo" "one" "bar" false})
           "one/false"))))

(deftest config
  (let [conf (load-config config-file)]
    (testing "load hiera.yaml config file"
      (is (contains? conf (keyword ":backends")))
      (is (contains? conf (keyword ":yaml")))
      (is (contains? conf (keyword ":hierarchy"))))
    (testing "load hierarchy from config"
      (is (= (first (hierarchy conf))
             "%{::fqdn}"))
      (is (= (last (hierarchy conf))
             "global")))
    (testing "finds the scope variables"
      (is (= (scope-vars conf)
             #{"::fqdn" "::foo" "::bar"})))))
