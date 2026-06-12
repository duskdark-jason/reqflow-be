-- REQ-016 需求填报字段升级脚本
-- 新增需求来源必填字段和附件路径字段；历史数据默认归类为业务提报。

SET @has_demand_source = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'req_demand'
    AND column_name = 'demand_source'
);

SET @add_demand_source_sql = IF(
  @has_demand_source = 0,
  'ALTER TABLE req_demand ADD COLUMN demand_source VARCHAR(64) NOT NULL DEFAULT ''BUSINESS'' COMMENT ''需求来源'' AFTER demand_type',
  'SELECT 1'
);

PREPARE add_demand_source_stmt FROM @add_demand_source_sql;
EXECUTE add_demand_source_stmt;
DEALLOCATE PREPARE add_demand_source_stmt;

SET @has_attachments = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'req_demand'
    AND column_name = 'attachments'
);

SET @add_attachments_sql = IF(
  @has_attachments = 0,
  'ALTER TABLE req_demand ADD COLUMN attachments TEXT NULL COMMENT ''需求附件路径，多个文件逗号分隔'' AFTER acceptance_text',
  'SELECT 1'
);

PREPARE add_attachments_stmt FROM @add_attachments_sql;
EXECUTE add_attachments_stmt;
DEALLOCATE PREPARE add_attachments_stmt;
