\c typing;

create table drills (
  id serial primary key,
  text text,
  timestamp timestamp default current_timestamp);
