insert into users (id, name, password, secret, ispublic, mail) values ('admin','admin','password','4839201746','false','admin@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('admin02','admin02','pas2w0rd','9203847561','false','admin02@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('admin03','admin03','pa33word','1073849203','false','admin03@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('admin04','admin04','pathwood','8392017465','false','admin04@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user00','Mark','password','2948375610','true','mark@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user01','David','pa32w0rd','6758493021','true','david@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user02','Peter','pa23word','3847561029','true','peter@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user03','James','patwired','9182736450','true','james@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user04','Benjamin','password','7362910845','true','benjamin@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user05','Eric','pas2w0rd','5029384716','true','eric@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user06','Sharon','pa3world','8493027165', 'true','sharon@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user07','Pamela','pathwood','6273849102','true''pamela@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user08','Jacqueline','password','1938472650','true','jacqueline@gmail.com');
insert into users (id, name, password, secret, ispublic, mail) values ('user09','Michelle','pas2w0rd','3849201763','true','michelle@gmail.com');

delete from forum;
insert into forum (time, username, picture, message, isadmin) values (CURRENT_TIMESTAMP,'admin','images/avatar_woman.png','Feel free to write any questions you may have.', 'true');
