-- :name create-people-stacks-table
-- :command :execute
-- :result :raw
-- :doc Create people_stacks table
create table if not exists people_stacks(
  person_id uuid references people(id),
  stack_id integer references stacks(id),
  primary key (person_id, stack_id)
);

-- :name insert-person-stack :?
WITH insertion AS (
  INSERT INTO people_stacks (person_id, stack_id)
  VALUES :t*:cols
  ON CONFLICT (person_id, stack_id) DO NOTHING
  RETURNING person_id, stack_id
)
SELECT * FROM insertion
UNION
SELECT * FROM people_stacks
  WHERE (person_id, stack_id) = any (array[:t*:cols]);

