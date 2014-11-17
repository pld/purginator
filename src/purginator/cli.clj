(ns purginator.cli
  (:require [clojure.java.io :as jio]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]))

(def cli-opts
  [["-t" "--table TABLE" "DynamoDB table name"
    :parse-fn keyword]

   ["-k" "--primary-key KEY" "Primary key name"
    :parse-fn keyword]

   [nil  "--batch-size SIZE" "Writes per batch"
    :default 25
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 1 % 25) "Must be a number between 1 and 25"]]

   [nil  "--write-rate RATE" "Writes per second"
    :default 2000
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be a positive number"]]

   [nil  "--endpoint URL" "Local DynamoDB endpoint"
    :default "http://localhost:8000"
    :validate [(partial re-matches #"^http://.*:[0-9]+$") "Must be a valid HTTP URL"]]

   [nil  "--remote" "Execute using AWS environment variables against a remote DynamoDB instance"]

   [nil  "--access-key AWS-ID" "AWS Access Key ID"
    :default (get (System/getenv) "AWS_ACCESS_KEY_ID")
    :default-desc "AWS_ACCESS_KEY_ID"
    :validate [string? "Must be a string"]]

   [nil  "--secret-key AWS-SECRET" "AWS Secret Key"
    :default (get (System/getenv) "AWS_SECRET_ACCESS_KEY")
    :default-desc "AWS_SECRET_ACCESS_KEY"
    :validate [string? "Must be a string"]]

   ["-h" "--help"]])

(defn usage [summary]
  (->> ["Usage: purginator [options] action file"
        ""
        "Options:"
        summary
        ""
        "Actions:"
        "  put      Populate specified DynamoDB table with primary keys from file"
        "  delete   Remove items with primary keys listed in file from specified DynamoDB table"]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (println)
  (System/exit status))

(defn parse-opts [opts]
  (let [{:keys [arguments errors options summary]} (cli/parse-opts opts cli-opts)
        {:keys [table primary-key]} options]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 2) (exit 1 (usage summary))
      (nil? table) (exit 1 (error-msg ["--table is a required argument"]))
      (nil? primary-key) (exit 1 (error-msg ["--primary-key is a required argument"]))
      errors (exit 1 (error-msg errors)))
    (let [file (jio/file (second arguments))]
      (when-not (.exists file)
        (exit 1 (error-msg [(format "'%s' does not exist" file)])))
      (-> options
          (assoc ,,, :file file :action (-> arguments first keyword))
          (dissoc ,,, (when (:remote options) :endpoint))))))
