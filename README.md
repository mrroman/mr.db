# mrroman/mr.db

A Clojure Stuart Sierra's component for access database. It uses a connection
pool and supports simple migration engine.

## Usage

Create component:

```clojure
(mr.db/new-database {:adapter "hsqldb"
                     :url "jdbc:hsqldb:mem:testdb"}
                    {1 #(jdbc/execute! % ["CREATE TABLE data(value TEXT)"])})
```

Component will contain `clojure.java.jdbc` compatible db-spec assigned to :spec keyword.
You can also use wrapped `clojure.java.jdbc` functions like `query`, `execute!`, `insert!`,
`update!` or `delete!` that support using component directly. They are located in the `mr.db` namespace.

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
