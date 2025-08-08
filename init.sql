CREATE DATABASE IF NOT EXISTS keycloak;
CREATE DATABASE IF NOT EXISTS easybuggy;
GRANT ALL ON keycloak.* TO 'admin'@'%';
GRANT ALL ON easybuggy.* TO 'admin'@'%';
