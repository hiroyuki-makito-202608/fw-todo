# サブタスク API / MCP 試験用バックアップ・復旧手順

## 目的と対象

この手順は、サブタスクの API / MCP 試験前に `todoapp` のデータを退避し、試験後に元の状態へ戻すためのものです。試験ではデータを削除・更新する操作があるため、必ず試験用または停止中の環境で実施してください。

対象は次のとおりです。

- MySQL データベース `todoapp` 内の全テーブルとデータ
- 添付ファイル本体を保存する `uploads` ディレクトリ

`todo_attachments` には添付ファイルのメタ情報だけが保存され、実ファイルは `uploads` にあります。SQLだけでは添付ファイルを復元できません。

## 重要な注意

- `todos` を全件削除すると、外部キーの `ON DELETE CASCADE` により `todo_attachments` と `todo_subtasks` も削除されます。
- `todo_categories` は `todos` から参照されるマスターデータです。全件削除試験ではカテゴリを削除しません。復旧SQLで元のカテゴリ表を戻す場合は、カテゴリを含む完全ダンプを使います。
- `TRUNCATE` は外部キー制約のため失敗する場合があり、試験中の操作として推奨しません。全削除はアプリ/API/MCPの試験対象に応じた `DELETE` 操作を使います。
- バックアップファイルには個人情報や業務情報が含まれる可能性があります。リポジトリへコミットせず、アクセス制御された保管場所に保管してください。
- 以下の PowerShell 例は `docker compose` の MySQL コンテナ `fw-mysql` と、環境変数 `MYSQL_ROOT_PASSWORD` を前提とします。

## 1. 試験前の準備とバックアップ

1. アプリからの書き込みを止めます。アプリを停止するか、全利用者に操作を止めてもらいます。試験対象以外の更新がバックアップに混ざらないようにします。
2. PowerShellでプロジェクトルート `C:\project\fw-todo` に移動します。
3. パスワードをコマンド履歴に直接書かず、環境変数から渡します。すでに設定済みなら再設定は不要です。

```powershell
$env:MYSQL_ROOT_PASSWORD = Read-Host 'MySQL root password' -AsSecureString |
    ForEach-Object { [System.Net.NetworkCredential]::new('', $_).Password }
```

4. 日時付きのバックアップディレクトリを作成します。

```powershell
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupDir = Join-Path (Get-Location) "backup/subtask-test-$stamp"
New-Item -ItemType Directory -Path $backupDir | Out-Null
```

5. データベース全体を構造・データ込みでダンプします。PowerShellではリダイレクトの文字コード差を避けるため、`docker exec -e MYSQL_PWD` と `--result-file` を使います。

```powershell
docker exec -e MYSQL_PWD="$env:MYSQL_ROOT_PASSWORD" fw-mysql `
    mysqldump -uroot --single-transaction --routines --triggers --events `
    --default-character-set=utf8mb4 --hex-blob --databases todoapp `
    --result-file=/tmp/todoapp-before-subtask-test.sql

docker cp fw-mysql:/tmp/todoapp-before-subtask-test.sql `
    (Join-Path $backupDir 'todoapp-before-subtask-test.sql')
```

6. SQLダンプが空でないこと、対象テーブル定義とデータが入っていることを確認します。**このコードブロックを実行し終えてから、次の7番のブロックを別に実行してください。**

```powershell
$dump = Join-Path $backupDir 'todoapp-before-subtask-test.sql'
Get-Item $dump | Select-Object FullName, Length, LastWriteTime
Select-String -Path $dump -Pattern 'CREATE TABLE.*todos|INSERT INTO.*todos|todo_subtasks|todo_attachments|todo_categories'
```

7. 添付ファイル本体も退避します。6番のコードブロックとは**別の実行**で、次を一行ずつ実行します。`uploads` がなければ「存在しない」と表示されます。存在する場合はバックアップへコピーされます。

```powershell
if (Test-Path -LiteralPath '.\uploads') {
    Copy-Item -LiteralPath '.\uploads' -Destination (Join-Path $backupDir 'uploads') -Recurse
    Write-Host 'uploads をバックアップしました。'
} else {
    Write-Host 'uploads ディレクトリはありません。コピーをスキップします。'
}
```

コピー後、存在する場合は次でバックアップ先を確認します。

```powershell
Get-ChildItem -Force -LiteralPath (Join-Path $backupDir 'uploads')
```

8. ダンプの保管先・サイズと、添付ファイルの有無を記録します。可能ならバックアップディレクトリをプロジェクト外の安全な場所にも複製します。

## 2. バックアップ内容の記録

試験開始前に次を記録します。これにより復旧後に件数を比較できます。

| テーブル | 記録する値 |
|---|---|
| `todos` | 全件数 |
| `todo_categories` | 全件数 |
| `todo_attachments` | 全件数 |
| `todo_subtasks` | 全件数 |

MySQLで確認するSQL:

```sql
SELECT 'todos' AS table_name, COUNT(*) AS row_count FROM todos
UNION ALL SELECT 'todo_categories', COUNT(*) FROM todo_categories
UNION ALL SELECT 'todo_attachments', COUNT(*) FROM todo_attachments
UNION ALL SELECT 'todo_subtasks', COUNT(*) FROM todo_subtasks;
```

必要に応じて、試験対象となるToDo ID、タイトル、サブタスク一覧も記録します。

## 3. 試験後の復旧

復旧はデータベース `todoapp` の全体をバックアップ時点へ戻します。試験中に作成されたデータもすべて消えるので、復旧前に必要な試験結果を記録してください。

1. アプリを停止し、DBへの接続と更新を止めます。
2. 正しいバックアップのパスを確認します。

```powershell
$dump = 'C:\project\fw-todo\backup\subtask-test-YYYYMMDD-HHMMSS\todoapp-before-subtask-test.sql'
Test-Path $dump
Get-Item $dump | Select-Object FullName, Length, LastWriteTime
```

3. `Test-Path` が `True` で、日時とファイルサイズが試験前バックアップと一致することを確認します。違う場合は次へ進まないでください。
4. ダンプに `todoapp` と4テーブルの定義・データが含まれていることを確認し、復旧対象のDB名が `todoapp` であることを再確認します。
5. 本復旧は既存の `todoapp` を削除して再作成してから実施します。これは `todoapp` の全データを置き換える操作です。接続先、バックアップの日時・サイズ、DB名を確認してから実行してください。

```powershell
docker exec -i -e MYSQL_PWD="$env:MYSQL_ROOT_PASSWORD" fw-mysql `
    mysql -uroot -e 'DROP DATABASE todoapp; CREATE DATABASE todoapp CHARACTER SET utf8mb4'

docker exec -i -e MYSQL_PWD="$env:MYSQL_ROOT_PASSWORD" fw-mysql `
    mysql -uroot < $dump
```

6. ダンプファイルをコンテナへコピーし、コンテナ内のシェルからMySQLへ読み込みます。

```powershell
docker cp $dump fw-mysql:/tmp/todoapp-before-subtask-test.sql
docker exec -e MYSQL_PWD="$env:MYSQL_ROOT_PASSWORD" fw-mysql `
    sh -c 'mysql -uroot < /tmp/todoapp-before-subtask-test.sql'
```

7. `uploads` をバックアップ版へ戻します。試験中に作られた添付ファイルとの混在を防ぐため、対象ディレクトリを確認してから置き換えます。既存の `uploads` を別名に退避してからバックアップ版をコピーします。

```powershell
$uploadBackup = Join-Path (Split-Path $dump) 'uploads'
if (Test-Path $uploadBackup) {
    if (Test-Path .\uploads) {
        Move-Item .\uploads ('.\uploads-after-test-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    }
    Copy-Item $uploadBackup .\uploads -Recurse
}
```

8. DBの4テーブル件数を試験前の記録と比較し、代表的なToDo、サブタスク、添付ファイルのメタ情報と実体を確認します。
9. 復旧確認後にアプリを起動します。

## 推奨するより安全な方法

可能なら、本番・開発用 `todoapp` から全件削除する代わりに、Docker Composeで試験専用のMySQLデータベースまたは別ボリュームを用意し、試験後にその試験環境だけ破棄してください。既存データを保持したまま試験でき、復旧作業のリスクを下げられます。

## 復旧チェックリスト

- [ ] アプリを停止してから復旧した
- [ ] 対象DB名が `todoapp` であることを確認した
- [ ] バックアップSQLの日時・サイズ・保管場所を確認した
- [ ] `todos` / `todo_categories` / `todo_attachments` / `todo_subtasks` の件数が試験前記録と一致した
- [ ] 添付ファイル本体を `uploads` から確認した
- [ ] 代表データを画面またはAPIで読み取れた
- [ ] 復旧完了後にアプリを起動した
- [ ] 一時的なパスワード環境変数を消した（`Remove-Item Env:MYSQL_ROOT_PASSWORD`）
