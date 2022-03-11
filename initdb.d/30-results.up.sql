\c typing;

create table results (
  id serial primary key,
  users_nick varchar(8), -- reference するのは変更しないキー
  pt int default 0,
  timestamp timestamp default current_timestamp);
