(defproject mrroman/mr.db "0.1.0-SNAPSHOT"
  :description "JDBC database component"
  :url "http://github.com/mrroman/mr.db"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [hikari-cp "1.5.0"]
                 [prismatic/schema "1.0.4"]]

  :profiles {:dev {:dependencies [[org.hsqldb/hsqldb "2.3.3"]]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]]}})
