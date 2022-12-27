insert into users (id, name, password, secret, ispublic) values ('admin','admin','password','1000000000','false');
insert into users (id, name, password, secret, ispublic) values ('admin02','admin02','pas2w0rd','2000000000','false');
insert into users (id, name, password, secret, ispublic) values ('admin03','admin03','pa33word','3000000000','false');
insert into users (id, name, password, secret, ispublic) values ('admin04','admin04','pathwood','4000000000','false');
insert into users (id, name, password, secret, ispublic) values ('user00','Mark','password','0000000000','true');
insert into users (id, name, password, secret, ispublic) values ('user01','David','pa32w0rd','1111111111','true');
insert into users (id, name, password, secret, ispublic) values ('user02','Peter','pa23word','2222222222','true');
insert into users (id, name, password, secret, ispublic) values ('user03','James','patwired','3333333333','true');
insert into users (id, name, password, secret, ispublic) values ('user04','Benjamin','password','4444444444','true');
insert into users (id, name, password, secret, ispublic) values ('user05','Eric','pas2w0rd','5555555555','true');
insert into users (id, name, password, secret, ispublic) values ('user06','Sharon','pa3world','6666666666', 'true');
insert into users (id, name, password, secret, ispublic) values ('user07','Pamela','pathwood','7777777777','true');
insert into users (id, name, password, secret, ispublic) values ('user08','Jacqueline','password','8888888888','true');
insert into users (id, name, password, secret, ispublic) values ('user09','Michelle','pas2w0rd','9999999999','true');

delete from forum;
insert into forum (time, username, picture, message) values (CURRENT_TIMESTAMP,'admin','images/avatar_man.png','Feel free to write any questions you may have.');
insert into forum (time, username, picture, message) values (CURRENT_TIMESTAMP,'Ichiro Suzuki','images/avatar_man.png','I am using PDO after migrating away from the mysql library. What do I use in place of the old real_escape_string function?');
insert into forum (time, username, picture, message) values (CURRENT_TIMESTAMP,'Rekca 2Ta','http://localhost:9999/attacker_app/img/avatar_woman.png','>>#2<br>PDO offers an alternative designed to replace mysql_escape_string() with the PDO::quote() method.');
