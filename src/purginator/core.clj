(ns purginator.core
  (:require [clojure.java.io :as jio]
            [clojure.tools.logging :as log]
            [purginator.cli :as cli]
            [taoensso.faraday :as faraday])
  (:import [java.util Date]
           [org.apache.commons.math3.stat.descriptive SummaryStatistics])
  (:gen-class))

(defn request [table primary-key action ks]
  {table {action [{primary-key (vec ks)}]}})

(defn now []
  (System/currentTimeMillis))

(defn log-start [action min-duration]
  (log/infof "Starting batch %s, minimum write duration %.2f ms"
             action min-duration))

(defn log-progress [total stats]
  (log/infof "total count: %d, mean duration: %.2f ms"
             total (.getMean stats)))

(defn log-finish [total stats]
  (log/infof "total count: %d, min duration: %.2f ms, mean duration: %.2f ms, max duration: %.2f ms"
             total (.getMin stats) (.getMean stats) (.getMax stats)))

(defn go [{:keys [action batch-size file table primary-key write-rate] :as options}]
  (with-open [f (jio/reader file)]
    (let [min-duration (double (/ 1000 (/ write-rate batch-size)))
          cnt (atom 0)
          stats (SummaryStatistics.)
          faraday-options (select-keys options [:access-key :endpoint :secret-key])]
      (log-start action min-duration)
      (doseq [ks (partition batch-size batch-size '() (line-seq f))]
        (let [start (now)
              _ (faraday/batch-write-item
                  faraday-options (request table primary-key action ks))
              _ (when (zero? (mod (swap! cnt + ,,, (count ks)) (* write-rate 5)))
                  (log-progress @cnt (.getMean stats)))
              duration (- (now) start)]
          (.addValue stats duration)
          (Thread/sleep (max 0 (- min-duration duration)))))
      (log-finish @cnt stats))))

(defn -main [& args]
  (let [{:keys [action] :as options} (cli/parse-opts args)]
    (go options)))
