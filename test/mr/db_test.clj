(ns mr.db-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [mr.db :refer :all]
            [schema.test :as schema.test]))

(use-fixtures :once schema.test/validate-schemas)

;; test data

(def test-db
  (let [n (atom 0)]
    (fn []
      {:adapter "hsqldb"
       :url (str "jdbc:hsqldb:mem:testdb" (swap! n inc))})))

(def ^{:private true} migr1 {1 #(jdbc/execute! %
                                               ["CREATE TABLE vals (value integer)"])})

(def ^{:private true} migr2 (conj migr1 {2 #(jdbc/insert! % :vals {:value 1})}))


;; macro with component

(defmacro with-component [[component constructor] & body]
  "Similar to with-open but it starts component, runs the body and stops component"
  `(let [~component (component/start ~constructor)]
    ~@body
    (component/stop ~component)))

(deftest new-database-test
  (testing "Set up clean database connection with migration"
    (with-component [c (new-database (test-db) {})]
      (is (= 0 (jdbc/query (:spec c)
                           "SELECT version FROM db_versions"
                           :row-fn :version :result-set-fn first)))))
  (testing "Set up database with one migration"
    (with-component [c (new-database (test-db) migr1)]
      (is (= 1 (jdbc/query (:spec c)
                           "SELECT version FROM db_versions"
                           :row-fn :version :result-set-fn first)))
      (is (= 0 (jdbc/query (:spec c)
                           "SELECT COUNT(*) n FROM vals"
                           :row-fn :n :result-set-fn first)))))
  (testing "Update already migrated database"
    (let [db (test-db)]
      (with-component [c (new-database db migr1)])
      (with-component [c (new-database db migr2)]
        (is (= 2 (jdbc/query (:spec c)
                             "SELECT version FROM db_versions"
                             :row-fn :version :result-set-fn first)))
        (is (= 1 (jdbc/query (:spec c)
                             "SELECT COUNT(*) n FROM vals"
                             :row-fn :n :result-set-fn first)))))))

(deftest jdbc-wrappers-test
  (testing "query"
    (with-component [c (new-database (test-db) migr1)]
      (is (= 0 (query c ["SELECT COUNT(*) n FROM vals"] :row-fn :n :result-set-fn first)))))
  (testing "insert!"
    (with-component [c (new-database (test-db) migr1)]
      (insert! c :vals {:value 2})
      (is (= 2 (query c ["SELECT value FROM vals"] :row-fn :value :result-set-fn first)))))
  (testing "update!"
    (with-component [c (new-database (test-db) migr2)]
      (update! c :vals {:value 2} ["value = ?" 1])
      (is (= 2 (query c ["SELECT value FROM vals"] :row-fn :value :result-set-fn first)))))
  (testing "delete!"
    (with-component [c (new-database (test-db) migr2)]
      (delete! c :vals ["value = ?" 1])
      (is (= 0 (query c ["SELECT COUNT(*) n FROM vals"] :row-fn :n :result-set-fn first))))))
