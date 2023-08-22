(ns samflores.db.stacks
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "samflores/db/sql/stacks.sql")
