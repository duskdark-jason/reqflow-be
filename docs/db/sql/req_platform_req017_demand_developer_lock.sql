-- REQ-017 需求指定开发人员与参与人锁定升级脚本
-- 新增 req_demand.developer_user_id，用于锁定需求创建人和指定开发人员之间的协作范围。

SET @has_developer_user_id = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'req_demand'
    AND column_name = 'developer_user_id'
);

SET @add_developer_user_id_sql = IF(
  @has_developer_user_id = 0,
  'ALTER TABLE req_demand ADD COLUMN developer_user_id BIGINT NULL COMMENT ''指定开发人员用户ID'' AFTER creator_id',
  'SELECT 1'
);

PREPARE add_developer_user_id_stmt FROM @add_developer_user_id_sql;
EXECUTE add_developer_user_id_stmt;
DEALLOCATE PREPARE add_developer_user_id_stmt;

SET @has_developer_user_id_index = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'req_demand'
    AND index_name = 'idx_req_demand_developer'
);

SET @add_developer_user_id_index_sql = IF(
  @has_developer_user_id_index = 0,
  'ALTER TABLE req_demand ADD INDEX idx_req_demand_developer (developer_user_id)',
  'SELECT 1'
);

PREPARE add_developer_user_id_index_stmt FROM @add_developer_user_id_index_sql;
EXECUTE add_developer_user_id_index_stmt;
DEALLOCATE PREPARE add_developer_user_id_index_stmt;
