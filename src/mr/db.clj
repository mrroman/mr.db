(ns mr.db
  (:require [com.stuartsierra.component :as component]
            [jdbc.pool.c3p0 :as pool]
            [clojure.java.jdbc :as jdbc]))

;; migration

(defn- has-table [db table-name]
  (= (jdbc/query db
                  [(str "SELECT COUNT(*) n FROM information_schema.tables "
                        "WHERE UCASE(table_name) = UCASE(?)")
                   table-name]
                  :row-fn :n :result-set-fn first)
      1))

(defn- create-versions [db]
  (jdbc/execute! db ["CREATE TABLE db_versions (version INTEGER)"])
  (jdbc/insert! db :db_versions {:version 0}))

(defn- query-version [db]
  (jdbc/query db
              "SELECT version FROM db_versions"
              :row-fn :version :result-set-fn first))

(defn- update-version [db version]
  (jdbc/update! db :db_versions {:version version} []))

(defn- migrate-from [db old-ver migrations]
  (let [new-ver (inc old-ver)]
    ((get migrations new-ver) db)
    (update-version db new-ver)
    new-ver))

(defn- migrate [db migrations]
  (let [was-migrated (has-table db "db_versions")
        version (if was-migrated
                  (query-version db)
                  (do
                    (create-versions db)
                    (query-version db)))]
    (loop [old-ver version]
      (if-not (contains? migrations (inc old-ver))
        old-ver
        (recur (migrate-from db old-ver migrations))))))

;; wrappers

(defn query [db & args]
  "See documentation of clojure.java.jdbc/query"
  (apply jdbc/query (cons (:spec db) args)))

(defn execute! [db & args]
  "See documentation of clojure.java.jdbc/execute!"
  (apply jdbc/execute! (cons (:spec db) args)))

(defn insert! [db & args]
  "See documentation of clojure.java.jdbc/insert!"
  (apply jdbc/insert! (cons (:spec db) args)))

(defn update! [db & args]
  "See documentation of clojure.java.jdbc/update!"
  (apply jdbc/update! (cons (:spec db) args)))

(defn delete! [db & args]
  "See documentation of clojure.java.jdbc/delete!"
  (apply jdbc/delete! (cons (:spec db) args)))

;; database component

(defrecord Database [db-spec migrations]
  component/Lifecycle

  (start [this]
    (let [spec (pool/make-datasource-spec db-spec)]
      (migrate spec migrations)
      (assoc this :spec spec)))
  (stop [this]
    (when (:spec this)
      (.close (:spec this))
      (assoc this :spec nil))))

(defn new-database [db-spec migrations]
  (->Database db-spec migrations))
