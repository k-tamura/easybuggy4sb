create schema demo;
create table users (id varchar(10) primary key, name varchar(30), password varchar(30), secret varchar(100), ispublic varchar(5), phone varchar(20), mail varchar(100));
create table forum (time timestamp, username varchar(30), picture varchar(200), message varchar(1000));
