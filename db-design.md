# データベース設計書

## 1. 目的と前提

この文書は `requirements.md` と `handout_app_spec.md` に基づき、ToDoアプリのデータベースを定義します。データベースは MySQL とし、アプリが使うテーブルは `todos` の1つです。画面の配置や操作の詳細は、この設計書の対象外です。

用語：テーブルは、同じ種類のデータを行と列で保存する表です。

## 2. テーブル一覧

| テーブル名 | 用途 |
|---|---|
| `todos` | やることと、その分類・期限・状態・日時を1件ずつ保存します。 |

## 3. `todos` テーブル

### 3.1 カラム定義

用語：カラムは表の列、NULL は「値が入っていない」状態です。主キーは各行を区別する列です。

| カラム | 型 | NULL | キー・初期値 | 内容・制約 |
|---|---|---:|---|---|
| `id` | `BIGINT` | 不可 | 主キー、自動採番 | 1件を識別する番号。利用者は入力しません。 |
| `title` | `VARCHAR(255)` | 不可 | — | やること。空白だけは入力チェックで拒否し、最大255文字です。 |
| `detail` | `VARCHAR(255)` | 可 | — | メモ。最大255文字です。未入力は `NULL` で保存します。 |
| `category` | `VARCHAR(255)` | 不可 | — | ジャンル。画面で指定された5種類から選びます。 |
| `priority` | `INT` | 不可 | 初期値 `2` | 優先度。`1`＝高、`2`＝中、`3`＝低。画面では未選択から始まり、保存前に選択します。 |
| `due_date` | `DATE` | 可 | — | 期限。未指定は `NULL` で保存します。 |
| `completed` | `BOOLEAN` | 不可 | 初期値 `FALSE`（未完了） | 完了状態。`TRUE`（`1`）＝完了、`FALSE`（`0`）＝未完了。新規登録時は未完了です。 |
| `created_at` | `DATETIME` | 不可 | 初期値 `CURRENT_TIMESTAMP` | 登録日時。保存時にDBが自動設定します。 |
| `updated_at` | `DATETIME` | 不可 | 初期値 `CURRENT_TIMESTAMP`、更新時に自動更新 | 最終更新日時。保存・更新時にDBが自動設定します。 |

用語：`VARCHAR(255)` は最大255文字の文字列、`INT` は整数、`BOOLEAN` は真（はい）または偽（いいえ）を表す型、`DATE` は日付、`DATETIME` は日付と時刻です。

### 3.2 制約と動作

- `id` は主キーとして自動採番します。
- `title`、`category`、`priority` は必須です。未入力や空白だけの `title` の拒否は、画面・アプリ側の入力チェックでも行います。
- `priority` は画面で `1`、`2`、`3` のいずれかを選んでから保存します。DBの初期値は仕様書どおり `2` です。`completed` は `0`（未完了）または `1`（完了）です。
- `category` は `VARCHAR(255)` とし、画面で定義済みの5候補から選択します。
- `detail` と `due_date` は任意です。未入力時は `NULL` とします。
- `completed` は新規登録時に `0` とします。完了日時は要件にないため保存しません。
- 登録日時と最終更新日時は利用者の入力対象にせず、MySQLが管理します。
- 削除は行自体を削除します。削除後に復元するための論理削除列は設けません。

## 4. 要件とカラムの対応

用語：CRUD は登録・読み取り・更新・削除のことです。

| 要件ID・内容 | 対応カラム | 設計上の対応 |
|---|---|---|
| F-01 一覧 | 全カラム | `todos` の行を読み取り、一覧に必要な値を取得します。 |
| F-02 登録 | `title`, `detail`, `category`, `priority`, `due_date`, `completed`, `created_at`, `updated_at`, `id` | 入力値を保存し、番号と日時は自動設定します。新規の `completed` は `0` です。 |
| F-03 編集 | `id`, `title`, `detail`, `category`, `priority`, `due_date`, `completed`, `updated_at` | `id` で対象行を特定して値を更新し、更新日時を自動更新します。 |
| F-04 削除 | `id` | `id` で対象行を特定し、行を削除します。 |
| F-05 名前検索 | `title` | 入力語を含む `title` を検索します（部分一致）。 |
| ジャンル絞り込み | `category` | 選択したジャンルと一致する値を検索します。 |
| F-06 期限順の並び替え | `due_date` | 期限昇順・降順で並べ替えます。未設定期限や同一期限内の順番は資料に指定がありません。 |
| 入力制限 | `title`, `detail`, `category`, `priority` | 文字数上限、必須、選択肢の制約に対応します。空白だけの `title` はアプリ側で拒否します。 |
| 完了状態の保存値 | `completed` | `1` を完了、`0` を未完了として保存します。 |
| 識別番号・登録日時・最終更新日時 | `id`, `created_at`, `updated_at` | いずれも入力欄に出さず、自動設定します。 |
| 操作記録（登録・編集・削除の種類と対象ID） | `id`（対象IDとして参照） | 操作記録には名前やメモを含めない要件です。テーブル1つの前提を守るため `todos` にログ列やログ用テーブルを追加しません。記録の保存先はDB外（アプリケーションログ等）として扱います。 |

## 5. CREATE TABLE 文

用語：DDL（データ定義文）は、テーブルの形を作るSQL文です。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

`CHECK` は、列に許可する値をDBにも確認させる制約です。ジャンルの5候補の選択、空白だけの文字列の拒否、優先度の選択必須はアプリ側でも確認します。

## 6. 資料に明記されていない点

- 期限が未設定の行を期限順のどこに置くか、同一期限の行をどう並べるかは資料に指定がありません。この設計では追加ルールを決めません。
- 操作記録の保存先の技術方式は資料に指定がありません。テーブルは `todos` だけとする前提に従い、DBテーブルとしては定義しません。




