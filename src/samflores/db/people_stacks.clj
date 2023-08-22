(ns samflores.db.people-stacks
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "samflores/db/sql/people_stacks.sql")
