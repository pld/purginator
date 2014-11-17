(defproject purginator "1.0.0"
  :description "The Purginator, a DynamoDB tool"
  :url "http://github.com/intentmedia/purginator"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main purginator.core
  :dependencies [[com.taoensso/faraday "1.5.0"
                  :exclusions [org.clojure/clojure]]
                 [org.apache.commons/commons-math3 "3.3"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]])
