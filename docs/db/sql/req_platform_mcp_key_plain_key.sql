-- MCP Key 明文展示升级脚本：为已有库补充人员 Key 明文字段。
SET @reqflow_plain_key_exists := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'req_mcp_user_key'
    AND COLUMN_NAME = 'plain_key'
);

SET @reqflow_plain_key_sql := IF(
  @reqflow_plain_key_exists = 0,
  'ALTER TABLE req_mcp_user_key ADD COLUMN plain_key VARCHAR(128) DEFAULT NULL COMMENT ''明文Key'' AFTER key_prefix',
  'SELECT ''req_mcp_user_key.plain_key already exists'' AS message'
);

PREPARE reqflow_plain_key_stmt FROM @reqflow_plain_key_sql;
EXECUTE reqflow_plain_key_stmt;
DEALLOCATE PREPARE reqflow_plain_key_stmt;
