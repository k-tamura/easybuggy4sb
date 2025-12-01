alter table forum MODIFY COLUMN message varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL;
create table forum (id INT AUTO_INCREMENT PRIMARY KEY, time timestamp, username varchar(30), picture varchar(200), message varchar(1000), isadmin varchar(5), file_name varchar(255), file_data LONGBLOB);
