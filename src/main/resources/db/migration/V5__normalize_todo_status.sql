UPDATE todos
SET status = CASE WHEN completed = TRUE THEN 'COMPLETED' ELSE 'TODO' END
WHERE status IS NULL OR status NOT IN ('TODO', 'IN_PROGRESS', 'COMPLETED')
   OR (completed = TRUE AND status <> 'COMPLETED');
