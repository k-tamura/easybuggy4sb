[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub release](https://img.shields.io/github/release/k-tamura/easybuggy4sb.svg)](https://github.com/k-tamura/easybuggy4sb/releases/latest)

EasyBuggy Boot :four_leaf_clover:
=

EasyBuggy Bootは、Spring Bootで開発されたEasyBuggyのクローンです。[EasyBuggy](https://github.com/k-tamura/easybuggy)は、[メモリリーク、デッドロック、JVMクラッシュ、SQLインジェクションなど](https://github.com/k-tamura/easybuggy4sb/wiki)、バグや脆弱性の動作を理解するためにつくられたバグだらけのWebアプリケーションです。

![EasyBuggyBootGo](https://github.com/user-attachments/assets/70ce9fa2-7bc0-45a8-b554-4d75d0ab69b0)

:clock4: クイックスタート (Docker Compose)
-
Keycloak、MySQL、攻撃者のアプリと連携して起動する場合

    $ echo HOST=192.168.1.17 > .env # localhost以外でEasyBuggy Bootを実行する場合 (例: 192.168.1.17)
    $ echo TZ=Asia/Tokyo >> .env # コンテナーの時刻のずれを無くすために追加
    $ docker compose up

以下にアクセス

    http://192.168.1.17

:clock4: クイックスタート
-
EasyBuggy Boot単独で起動する場合（`docker compose up`より攻撃できる脆弱性が減ります）

    $ mvn spring-boot:run

( または[JVMオプション](https://github.com/k-tamura/easybuggy4sb/blob/master/pom.xml#L148)付きで ``` java -jar ROOT.war ``` か任意のサーブレットコンテナに ROOT.war をデプロイ。 )

以下にアクセス

    http://localhost
  
停止するには:

  <kbd>CTRL</kbd>+<kbd>C</kbd>をクリック
  

:clock4: 詳細は
-
   
[wikiページ](https://github.com/k-tamura/easybuggy4sb/wiki)を参照下さい。

:clock4: デモ
-

EasyBuggyを起動して、無限ループ、LDAPインジェクション、UnsatisfiedLinkError、BufferOverflowException、デッドロック、メモリリーク、JVMクラッシュの順で実行しています。

![demo](https://github.com/k-tamura/test/blob/master/demo_ebsb_ja.gif)
