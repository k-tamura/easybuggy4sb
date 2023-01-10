[![Build Status](https://travis-ci.org/k-tamura/easybuggy4sb.svg?branch=master)](https://travis-ci.org/k-tamura/easybuggy4sb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release](https://img.shields.io/github/release/k-tamura/easybuggy4sb.svg)](https://github.com/k-tamura/easybuggy4sb/releases/latest)

# EasyBuggy Boot :four_leaf_clover:

This is a clone of EasyBuggy built on Spring Boot. [EasyBuggy](https://github.com/k-tamura/easybuggy) is a broken web application in order to understand behavior of bugs and vulnerabilities, for example, [memory leak, deadlock, JVM crash, SQL injection and so on](https://github.com/k-tamura/easybuggy4sb/wiki).

![logo](https://raw.githubusercontent.com/wiki/k-tamura/easybuggy/images/mov_ebsb.gif)

:clock4: Quick Start (Docker Compose) with Keycloak, MySQL, Attacker's app
-

    $ echo HOST=192.168.1.17 > .env # if you run EasyBuggy Boot not on localhost (e.g. 192.168.1.17)
    $ docker compose up

Access to

    http://192.168.1.17:8080

:clock4: Quick Start
-

    $ mvn spring-boot:run

( or ``` java -jar ROOT.war ``` or deploy ROOT.war on your servlet container with [the JVM options](https://github.com/k-tamura/easybuggy4sb/blob/master/pom.xml#L148). )

:warning: **Java 7 or 8 is needed. Doesn't work with Java 9 or later.**

:clock4: Quick Start (Docker)
-

    $ docker build . -t easybuggy4sb       # Build container image
    $ docker run -p 8080:8080 easybuggy4sb # Start easybuggy

Access to

    http://localhost:8080

#### To stop:

  Use <kbd>CTRL</kbd>+<kbd>C</kbd>

    
:clock4: For more detail
-
   
See [the wiki page](https://github.com/k-tamura/easybuggy4sb/wiki).

:clock4: Demo
-

This demo shows: Start up -> Infinite Loop -> LDAP Injection -> UnsatisfiedLinkError -> BufferOverflowException -> Deadlock -> Memory Leak -> JVM Crash (Shut down)

![demo](https://github.com/k-tamura/test/blob/master/demo_ebsb.gif)
