(ns samflores.db.people
  (:require [hugsql.core :as hugsql]))

(declare create-people-table)
(declare insert-person)
(declare all-people)
(declare search-for)
(declare person-by-id)

(hugsql/def-db-fns "samflores/db/sql/people.sql")
