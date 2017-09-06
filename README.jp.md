[![Build Status](https://travis-ci.org/k-tamura/easybuggy4sb.svg?branch=master)](https://travis-ci.org/k-tamura/easybuggy4sb)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release](https://img.shields.io/github/release/k-tamura/easybuggy4sb.svg)](https://github.com/k-tamura/easybuggy4sb/releases/latest)

EasyBuggy Boot
=

EasyBuggy Bootは、Spring Bootで開発されたEasyBuggyのクローンです。[EasyBuggy](https://github.com/k-tamura/easybuggy)は、[メモリリーク、デッドロック、JVMクラッシュ、SQLインジェクションなど](https://github.com/k-tamura/easybuggy4sb/wiki)、バグや脆弱性の動作を理解するためにつくられたバグだらけのWebアプリケーションです。

![logo](https://github.com/k-tamura/easybuggy4sb/blob/master/src/main/webapp/images/easybuggy.png)

クイックスタート
-

    $ mvn spring-boot:run

( または[JVMオプション](https://github.com/k-tamura/easybuggy4sb/blob/master/pom.xml#L148)付きで ``` java -jar ROOT.war ``` か任意のサーブレットコンテナに ROOT.war をデプロイ。 )

以下にアクセス:

    http://localhost:8080


停止するには:

  <kbd>CTRL</kbd>+<kbd>C</kbd>をクリック
  

詳細は
-
   
[wikiページ](https://github.com/k-tamura/easybuggy4sb/wiki)を参照下さい。
