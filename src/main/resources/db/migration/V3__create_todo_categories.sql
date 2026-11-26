-- 1) Create the category master table.
CREATE TABLE IF NOT EXISTS todo_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_todo_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Import categories already used by existing todos.
INSERT INTO todo_categories (name)
SELECT DISTINCT TRIM(category)
FROM todos
WHERE category IS NOT NULL AND TRIM(category) <> ''
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3) Add a nullable reference, populate it, then enforce the relationship.
ALTER TABLE todos ADD COLUMN category_id BIGINT NULL AFTER category;

UPDATE todos t
JOIN todo_categories c ON c.name = TRIM(t.category)
SET t.category_id = c.id;

ALTER TABLE todos
    ADD CONSTRAINT fk_todos_category
        FOREIGN KEY (category_id) REFERENCES todo_categories(id)
        ON UPDATE CASCADE;

-- After application code has been switched to category_id, enforce NOT NULL and
-- remove the legacy string column:
-- ALTER TABLE todos MODIFY category_id BIGINT NOT NULL;
-- ALTER TABLE todos DROP COLUMN category;
