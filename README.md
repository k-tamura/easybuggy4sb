[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release](https://img.shields.io/github/release/k-tamura/easybuggy4sb.svg)](https://github.com/k-tamura/easybuggy4sb/releases/latest)

# EasyBuggy Boot :four_leaf_clover:

This is a clone of EasyBuggy built on Spring Boot. [EasyBuggy](https://github.com/k-tamura/easybuggy) is a broken web application in order to understand behavior of bugs and vulnerabilities, for example, [memory leak, deadlock, JVM crash, SQL injection and so on](https://github.com/k-tamura/easybuggy4sb/wiki).

![EasyBuggyBootGo](https://github.com/user-attachments/assets/70ce9fa2-7bc0-45a8-b554-4d75d0ab69b0)

:clock4: Quick Start (Docker Compose) with Keycloak, MySQL, Attacker's app
-
When running EasyBuggy Boot with Keycloak, MySQL, and an attacker's application

    $ echo HOST=192.168.1.17 > .env # if you run EasyBuggy Boot not on localhost (e.g. 192.168.1.17)
    $ echo TZ=America/New_York >> .env # Added to eliminate container time skew
    $ docker compose up

Access to

    http://192.168.1.17

:clock4: Quick Start
-
When running EasyBuggy Boot alone (fewer vulnerabilities than `docker compose up`)

    $ mvn spring-boot:run

( or ``` java -jar ROOT.war ``` or deploy ROOT.war on your servlet container with [the JVM options](https://github.com/k-tamura/easybuggy4sb/blob/master/pom.xml#L148). )

:warning: **Java 7 or 8 is needed. Doesn't work with Java 9 or later.**

Access to

    http://localhost

#### To stop:

  Use <kbd>CTRL</kbd>+<kbd>C</kbd>

    
:clock4: For more detail
-
   
See [the wiki page](https://github.com/k-tamura/easybuggy4sb/wiki).

:clock4: Environment
-

When you start with Docker Compose, the following containers will be started:

<img width="1140" height="948" alt="Env_en" src="https://github.com/user-attachments/assets/a1aec1c4-fd4d-448b-aee0-bf5d057556fc" />
