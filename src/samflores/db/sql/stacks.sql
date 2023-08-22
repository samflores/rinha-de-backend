-- :name create-stacks-table
-- :command :execute
-- :result :raw
-- :doc Create stacks table
create table if not exists stacks (
  id         serial primary key,
  name       varchar(32) not null
);
create unique index if not exists stacks_lower_name_key on stacks (lower(name));

-- :name insert-stacks :?
WITH insertion AS (
  INSERT INTO stacks (name)
  VALUES :t*:names
  ON CONFLICT (lower(name)) DO NOTHING
  RETURNING id, name
)
SELECT * FROM insertion
UNION
SELECT * FROM stacks
  WHERE lower(name) ilike any (array(select unnest(array[:t*:names])));

-- :name stack-by-id :? :1
select *
from stacks
where id = :id

-- :name stacks-by-name :?
select id, name
from stacks
where lower(name) = lower(:name)
