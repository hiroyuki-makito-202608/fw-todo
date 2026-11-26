# fw-todo 説明書

## 1. アプリケーション概要

fw-todo は、やることを登録・整理し、進み具合を管理するWebアプリケーションです。
Web画面に加えて、REST APIとMCPインターフェースからも操作できます。

## 2. 起動方法

### 必要なもの

- Docker Desktop（Docker Composeを含む）
- 初回起動時のインターネット接続（Dockerイメージの取得・ビルドに使用）

### Docker Composeで起動する

1. Docker Desktopを起動します。
2. PowerShellでプロジェクトのフォルダーに移動します。

```powershell
cd C:\project\fw-todo
```

3. 環境設定ファイルを作成します。

```powershell
Copy-Item .env.example .env
notepad .env
```

`.env` を開き、`MYSQL_ROOT_PASSWORD=` の右側にMySQLのrootパスワードを設定して保存します。

4. アプリとMySQLをビルド・起動します。

```powershell
docker compose up -d --build
```

5. 起動状態を確認します。

```powershell
docker compose ps
```

MySQLが `healthy`、アプリが `Up` になったら、ブラウザーで次を開きます。

```text
http://localhost:8080/todos
```

起動ログを確認する場合:

```powershell
docker compose logs -f app
```

ログ表示を終了するには `Ctrl+C` を押します。コンテナは停止しません。

### 停止・再起動

```powershell
# 停止（DBデータは名前付きボリュームに保持）
docker compose down

# 再起動
docker compose up -d
```

`docker compose down -v` はDBデータを含むボリュームを削除します。通常の停止では使用しないでください。

### データベースの初期化

アプリ起動時にテーブルは自動作成されません。初めてDBを作る環境では、まずMySQLだけを起動します。

```powershell
docker compose up -d db
```

MySQL Workbenchなどで `localhost:3306` のデータベース `todoapp` に接続し、次の基本テーブルを作成します。既存DBには実行しないでください。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

続けて、次のSQLを番号順に同じデータベースで実行してください。

1. `src/main/resources/db/migration/V2__create_todo_attachments.sql`
2. `src/main/resources/db/migration/V3__create_todo_categories.sql`
3. `src/main/resources/db/migration/V4__expand_todo_category_name.sql`
4. `src/main/resources/db/migration/V5__normalize_todo_status.sql`
5. `src/main/resources/db/migration/V6__create_todo_subtasks.sql`

既存DBには適用済みのSQLを再実行しないでください。特にV3は既存の `todos` テーブルへ列と外部キーを追加します。DB変更前にはバックアップを取得してください。

## 3. 機能一覧

### ToDo管理

- ToDoの新規登録、確認、編集、削除
- タイトル、メモ、ジャンル、優先度、期限の管理
- タイトル・ジャンルでの絞り込みと期限順の並び替え
- 状態の管理: 未着手、進行中、完了

### カンバンボード

- 未着手・進行中・完了の3列表示
- カードのドラッグ＆ドロップによる状態変更
- サブタスクがあるToDoカードに完了数と進捗バーを表示

### サブタスク

- 1件のToDoにつき最大10件
- サブタスクの追加、タイトル変更、完了状態変更、表示順変更、削除
- ToDoごとのサブタスク完了率をカンバンカードに表示

### 添付ファイル

- ToDoへのファイル添付、ダウンロード、削除
- アップロード上限は1ファイル10 MB
- 保存時のファイル名をUUIDへ変換

### ジャンルとカレンダー

- ジャンルをDBのマスターテーブルで管理
- 月表示カレンダーで期限と祝日を確認
- 祝日は外部データから取得（取得できない場合は画面に通知）

### REST API

- ToDoの一覧・詳細取得、登録、更新、削除
- ToDo状態の更新
- サブタスクの一覧取得、追加、更新、削除
- 祝日一覧の取得

APIの基本URLは `http://localhost:8080/api` です。書き込みAPIではCSRFトークンが必要です。

### MCP

MCPサーバーはStreamable HTTPを使用し、接続先は次のとおりです。

```text
http://localhost:8080/mcp
```

ToDoの一覧・詳細・追加・更新・削除・完了・状態変更、サブタスクの一覧・追加・更新・削除、空き日の検索をMCPツールから実行できます。

## 4. 主なポートと保存先

| 用途 | 接続先 |
|---|---|
| Webアプリ | `http://localhost:8080` |
| REST API | `http://localhost:8080/api` |
| MCP | `http://localhost:8080/mcp` |
| MySQL | `localhost:3306` |
| DB永続化 | Dockerボリューム `fw-todo_fw-db-data`（Composeのプロジェクト名により名称が変わる場合があります） |
| 添付ファイル | アプリの `uploads` ディレクトリ |

## 5. 技術構成

- Java 21
- Spring Boot 4.0.9
- Thymeleaf
- MyBatis
- MySQL 8
- Spring AI MCP Server
