(ns purginator.core-test
  (:require [clojure.java.io :as jio]
            [clojure.test :refer :all]
            [purginator.core :as core]
            [taoensso.faraday :as faraday]))

(defn capture [requests options request]
  (swap! requests conj ,,, request))

(def skeleton-options
  {:action "put"
   :file "test-resources/keys.txt"
   :table "table" :primary-key "pkey"
   :write-rate 1000})

(deftest go-basic-test
  (testing "go function makes dynamo requests"
    (let [requests (atom [])]
      (with-redefs [faraday/batch-write-item (partial capture requests)]
        (core/go (assoc skeleton-options :batch-size 25))
        (is (= [{"table" {"put" [{"pkey" ["foo" "bar" "baz"]}]}}]
               @requests))))))

(deftest go-batch-test
  (testing "go function makes dynamo requests with correct batch size"
    (let [requests (atom [])]
      (with-redefs [faraday/batch-write-item (partial capture requests)]
        (core/go (assoc skeleton-options :batch-size 2))
        (is (= [{"table" {"put" [{"pkey" ["foo" "bar"]}]}}
                {"table" {"put" [{"pkey" ["baz"]}]}}]
               @requests))))))
