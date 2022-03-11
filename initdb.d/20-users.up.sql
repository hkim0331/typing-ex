\c typing;

create table users (
  id serial primary key,
  sid varchar(8) unique,
  nick varchar(8) unique,
  password text,
  timestamp timestamp default current_timestamp);
