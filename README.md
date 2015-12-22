# mrroman/mr.db

A Clojure Stuart Sierra's component for access database. It uses a connection
pool and supports simple migration engine.

## Usage

Create new component

```clojure
(mr.db/new-database {:adapter "hsqldb"
                     :url "jdbc:hsqldb:mem:testdb"}
                    {1 #(jdbc/execute! ["CREATE TABLE data(value TEXT)"])})
```

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
