(defproject mr/db "0.1.0-SNAPSHOT"
  :description "JDBC database component"
  :url "http://github.com/mrroman/db"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]]

  :profiles {:dev {:dependencies [[org.hsqldb/hsqldb "2.3.3"]]}})
