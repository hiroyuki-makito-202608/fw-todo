# fw-todo 開発環境メモ

このメモは、リポジトリ内の `pom.xml`、`docker-compose.yml`、アプリ設定、ソースコード、Git 履歴を確認してまとめたものです。インストール履歴に記録がない作業は、確認できた事実と区別して記載しています。

## 構成

| 区分 | 使用技術 | 用途・設定 |
|---|---|---|
| 言語 | Java 21 | `pom.xml` の `java.version` |
| Web フレームワーク | Spring Boot 4.0.9 / Spring MVC | Web アプリの起動、コントローラー、HTTP リクエスト処理 |
| テンプレート | Thymeleaf | サーバー側 HTML テンプレート |
| DB アクセス | MyBatis Spring Boot Starter 4.0.1 | Mapper インターフェースと XML (`src/main/resources/mapper/`) で SQL を実行 |
| データベース | MySQL 8.0 | Docker Compose で起動。DB 名 `todoapp`、ポート `3306` |
| JDBC ドライバー | MySQL Connector/J | Spring Boot アプリから MySQL へ接続 |
| ビルド | Apache Maven 3.10.0 (Maven Wrapper) | `mvnw` / `mvnw.cmd` から利用。初回実行時に Maven を取得 |
| テスト | JUnit 5、Spring Boot / MyBatis のテスト依存関係 | `src/test/java` に Spring コンテキスト起動テストあり |
| 補助ライブラリ | Lombok | 依存関係とコンパイル時アノテーション処理を設定済み。現在のソースでの利用は確認できません |

フロントエンドは Thymeleaf と通常の HTML/CSS で構成されています。Node.js、npm、JavaScript のビルドツールは現在の依存関係にはありません。

## 導入済みとして確認できるもの

- Maven プロジェクトと Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) を追加。
- `pom.xml` に Spring Boot、Thymeleaf、Spring Web MVC、MyBatis、MySQL ドライバー、Lombok、およびテスト依存関係を定義。
- `docker-compose.yml` に MySQL 8.0 サービス、データベース `todoapp`、ポート転送 `3306:3306`、永続ボリューム `fw-db-data` を定義。
- `src/main/resources/application.properties` に接続先 `jdbc:mysql://localhost:3306/todoapp`、`root` ユーザー、MyBatis Mapper の場所と underscore-to-camel-case 設定を記載。
- Git 履歴では `468cc40`（2026-11-05）「SpringBoot+MySQL環境作成」で、上記の基礎ファイルやアプリの初期ファイルを追加したことを確認。

Git のコミットから確認できるのはプロジェクト設定の追加までです。開発 PC に JDK や Docker をインストールした実作業の記録、実行したコマンドまではリポジトリに残っていないため、以下はこのプロジェクトを動かすための手順です。

## 初回セットアップと起動

前提として JDK 21 と Docker Desktop（Docker Compose を含む）が使える状態にします。Maven は Wrapper が取得するため、別途の Maven インストールは通常不要です。

1. リポジトリのルートで MySQL を起動します。

   ```powershell
   docker compose up -d db
   ```

2. `db-design.md` の `CREATE TABLE todos` 文を `todoapp` データベースに一度実行し、テーブルを作成します。現在、自動実行用の schema/migration SQL は登録されていません。例：

   ```powershell
   docker exec -it fw-mysql mysql -uroot -ppassword todoapp
   ```

   MySQL の `mysql>` プロンプトが表示されたら、`db-design.md` の `CREATE TABLE` 文を貼り付けて実行します。プロンプトを終了するには `exit` を入力します。

3. `fw-todo` リポジトリのルートで Spring Boot アプリ（todoapp）を起動します。

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   起動ログに `Started TodoappApplication` が表示されたら起動完了です。アプリを止めるときは、起動中の PowerShell で `Ctrl+C` を押します。

4. ブラウザーで `http://localhost:8080/todos` を開きます。

### IDE から実行する場合

IDE でプロジェクトを Maven プロジェクトとして開き、JDK 21 を選択します。MySQL コンテナを起動し、`todos` テーブルを作成した後、`src/main/java/com/example/todoapp/TodoappApplication.java` の `main` メソッドを実行します。IDE の実行構成で作業ディレクトリをリポジトリのルートにしてください。

初回の Maven Wrapper 実行では Maven 本体や依存ライブラリをダウンロードします。Java や Docker が PATH から見つからない場合は、それぞれのインストールと環境変数設定が必要です。

## よく使うコマンド

```powershell
# ビルド
.\mvnw.cmd clean package

# テスト
.\mvnw.cmd test

# アプリの停止後、MySQL コンテナを停止
docker compose down

# MySQL コンテナと DB データも削除（DB を初期化したい場合）
docker compose down -v
```

`docker compose down -v` は `fw-db-data` の保存データを削除します。通常の停止では `docker compose down` を使います。

## 設定上の注意

- DB 接続情報は `src/main/resources/application.properties` にあります。同ファイルは `.gitignore` 対象ですが、現状のローカル設定は `root` / `password` です。これはローカル開発用として扱い、本番や共有環境では環境変数などに置き換えてください。
- MySQL はポート `3306` をホストに公開します。同じポートを別サービスが使っている場合は起動に失敗するため、Compose と JDBC URL の両方を合わせて変更します。
- DB の永続化先は Docker の名前付きボリューム `fw-db-data` です。コンテナを再作成してもデータは残ります。
- 現在の MyBatis 設定では Mapper XML を `classpath:mapper/*.xml` から読み込み、DB の `due_date` 等のアンダースコア名を Java の camelCase 名に対応させます。
