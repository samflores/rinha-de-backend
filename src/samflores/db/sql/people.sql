-- :name create-people-table
-- :command :execute
-- :result :raw
-- :doc Create people table
create table if not exists people (
  id         uuid DEFAULT uuid_generate_v4(),
  nickname   varchar(32) not null unique,
  name       varchar(100) not null,
  born_on    date not null,
  primary key (id)
)

-- :name insert-person :<!
insert into people (nickname, name, born_on)
values (:nickname, :name, :born_on::date)
returning id

-- :name person-by-id :? :1
select
  p.id,
  p.nickname,
  p.name,
  p.born_on,
  array_agg(s.name) as "stacks"
from people p
left join people_stacks ps
  on ps.person_id = p.id
left join stacks s
  on s.id = ps.stack_id
where p.id = :id::uuid
group by
  p.id

-- :name search-for :?
select * 
from (
  select
    p.id,
    p.nickname,
    p.name,
    p.born_on,
    array_agg(s.name) as "stacks"
  from people p
  left join people_stacks ps
    on ps.person_id = p.id
  left join stacks s
    on s.id = ps.stack_id
  group by
    p.id
) as all_people
where all_people.name || ' ' || all_people.nickname || ' ' || array_to_string(all_people.stacks, ' ') ilike :term

-- :name all-people :?
select
  p.id,
  p.nickname,
  p.name,
  p.born_on,
  array_agg(s.name) as "stacks"
from people p
left join people_stacks ps
  on ps.person_id = p.id
left join stacks s
  on s.id = ps.stack_id
group by
  p.id
