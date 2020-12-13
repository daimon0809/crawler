create table LINKS_TO_BE_PROCESSED
(
  link varchar(2000)
);

create table LINKS_ALREADY_PROCESSED
(
  link varchar(2000)
);

create table NEWS
(
  id          bigint primary key auto_increment,
  title       text,
  content     text,
  url         varchar(100),
  create_at   timestamp default now(),
  modified_at timestamp default now()
) DEFAULT CHARSET=uft8mb4;