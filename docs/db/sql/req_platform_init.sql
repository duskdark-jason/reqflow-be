-- ReqFlow clean initial deployment script.
-- Run after docs/db/sql/ry_20260417.sql and docs/db/sql/quartz.sql.
-- This file contains ReqFlow business tables, menus, permissions, roles and initial release settings.

-- -----------------------------------------------------------------------------
-- ReqFlow business tables
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS req_project (
  project_id BIGINT NOT NULL AUTO_INCREMENT,
  project_name VARCHAR(100) NOT NULL,
  project_code VARCHAR(64) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  workspace_agents_template_version VARCHAR(32) DEFAULT 'v1',
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (project_id),
  UNIQUE KEY uk_req_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求平台项目';

CREATE TABLE IF NOT EXISTS req_repository (
  repo_id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  repo_name VARCHAR(100) NOT NULL,
  repo_type VARCHAR(20) NOT NULL,
  repo_url VARCHAR(500) NOT NULL,
  local_path_hint VARCHAR(300) DEFAULT NULL,
  default_branch VARCHAR(100) NOT NULL DEFAULT 'main',
  harness_status VARCHAR(32) NOT NULL DEFAULT 'uninitialized',
  harness_commit VARCHAR(100) DEFAULT NULL,
  last_indexed_at DATETIME DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (repo_id),
  KEY idx_req_repo_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求平台代码仓库';

CREATE TABLE IF NOT EXISTS req_variant (
  variant_id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  variant_name VARCHAR(100) NOT NULL,
  variant_code VARCHAR(64) NOT NULL,
  customer_name VARCHAR(100) DEFAULT NULL,
  scope_type VARCHAR(32) NOT NULL DEFAULT 'mainline',
  baseline_branch VARCHAR(100) NOT NULL DEFAULT 'main',
  branch_policy VARCHAR(1000) DEFAULT NULL,
  mcp_key VARCHAR(128) DEFAULT NULL,
  description VARCHAR(500) DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (variant_id),
  UNIQUE KEY uk_req_variant_code (project_id, variant_code),
  UNIQUE KEY uk_req_variant_mcp_key (mcp_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求平台项目分支';

CREATE TABLE IF NOT EXISTS req_mcp_user_key (
  key_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  key_name VARCHAR(100) NOT NULL,
  key_prefix VARCHAR(32) NOT NULL,
  plain_key VARCHAR(128) DEFAULT NULL,
  key_hash CHAR(64) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  last_used_time DATETIME DEFAULT NULL,
  last_used_ip VARCHAR(128) DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (key_id),
  UNIQUE KEY uk_req_mcp_user_key_hash (key_hash),
  KEY idx_req_mcp_user_key_user (user_id),
  KEY idx_req_mcp_user_key_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员MCP访问Key';

CREATE TABLE IF NOT EXISTS req_module (
  module_id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  variant_id BIGINT DEFAULT NULL,
  parent_id BIGINT NOT NULL DEFAULT 0,
  module_name VARCHAR(100) NOT NULL,
  module_code VARCHAR(64) NOT NULL,
  module_type VARCHAR(20) NOT NULL DEFAULT 'module',
  repo_scope VARCHAR(20) NOT NULL DEFAULT 'both',
  description VARCHAR(500) DEFAULT NULL,
  order_num INT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (module_id),
  UNIQUE KEY uk_req_module_code (project_id, variant_id, module_code),
  KEY idx_req_module_project_variant (project_id, variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求平台模块功能点';

CREATE TABLE IF NOT EXISTS req_demand (
  demand_id BIGINT NOT NULL AUTO_INCREMENT,
  demand_no VARCHAR(64) NOT NULL,
  title VARCHAR(200) NOT NULL,
  demand_type VARCHAR(32) NOT NULL,
  demand_source VARCHAR(64) NOT NULL DEFAULT 'BUSINESS',
  project_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  module_id BIGINT DEFAULT NULL,
  feature_id BIGINT DEFAULT NULL,
  business_background TEXT,
  expected_result TEXT,
  impact_page VARCHAR(1000) DEFAULT NULL,
  impact_api VARCHAR(1000) DEFAULT NULL,
  impact_data VARCHAR(1000) DEFAULT NULL,
  impact_permission VARCHAR(1000) DEFAULT NULL,
  impact_export_or_async VARCHAR(1000) DEFAULT NULL,
  acceptance_text TEXT,
  attachments TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  creator_id BIGINT NOT NULL,
  developer_user_id BIGINT DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (demand_id),
  UNIQUE KEY uk_req_demand_no (demand_no),
  KEY idx_req_demand_project (project_id),
  KEY idx_req_demand_variant (variant_id),
  KEY idx_req_demand_developer (developer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求';

CREATE TABLE IF NOT EXISTS req_package_version (
  package_id BIGINT NOT NULL AUTO_INCREMENT,
  demand_id BIGINT NOT NULL,
  artifact_type VARCHAR(32) NOT NULL,
  version_no INT NOT NULL,
  content MEDIUMTEXT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  version_note VARCHAR(500) DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (package_id),
  UNIQUE KEY uk_req_package_version (demand_id, artifact_type, version_no),
  KEY idx_req_package_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求执行包版本';

CREATE TABLE IF NOT EXISTS req_memory_index (
  memory_id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  repo_id BIGINT NOT NULL,
  variant_id BIGINT DEFAULT NULL,
  doc_type VARCHAR(32) NOT NULL,
  doc_path VARCHAR(500) NOT NULL,
  doc_title VARCHAR(200) DEFAULT NULL,
  branch_name VARCHAR(100) DEFAULT NULL,
  commit_hash VARCHAR(100) DEFAULT NULL,
  checksum VARCHAR(128) DEFAULT NULL,
  tags VARCHAR(500) DEFAULT NULL,
  summary VARCHAR(1000) DEFAULT NULL,
  indexed_at DATETIME DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (memory_id),
  KEY idx_req_memory_project (project_id),
  KEY idx_req_memory_repo (repo_id),
  KEY idx_req_memory_project_variant (project_id, variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目记忆索引';

CREATE TABLE IF NOT EXISTS req_repository_index_batch (
  batch_id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  repo_id BIGINT NOT NULL,
  repo_type VARCHAR(20) DEFAULT NULL,
  remote_url VARCHAR(500) NOT NULL,
  branch_name VARCHAR(100) NOT NULL,
  commit_hash VARCHAR(100) NOT NULL,
  index_version VARCHAR(32) NOT NULL,
  source_type VARCHAR(20) NOT NULL DEFAULT 'mcp',
  module_count INT NOT NULL DEFAULT 0,
  page_count INT NOT NULL DEFAULT 0,
  api_count INT NOT NULL DEFAULT 0,
  table_count INT NOT NULL DEFAULT 0,
  permission_count INT NOT NULL DEFAULT 0,
  document_count INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'imported',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (batch_id),
  KEY idx_req_index_batch_project (project_id),
  KEY idx_req_index_batch_repo (repo_id),
  KEY idx_req_index_batch_commit (repo_id, branch_name, commit_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库索引批次';

CREATE TABLE IF NOT EXISTS req_index_module (
  index_module_id BIGINT NOT NULL AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  repo_id BIGINT NOT NULL,
  variant_id BIGINT DEFAULT NULL,
  parent_code VARCHAR(64) DEFAULT NULL,
  module_code VARCHAR(64) NOT NULL,
  module_name VARCHAR(100) NOT NULL,
  module_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS',
  repo_scope VARCHAR(20) NOT NULL DEFAULT 'ALL',
  relative_path VARCHAR(500) DEFAULT NULL,
  source_ref VARCHAR(500) DEFAULT NULL,
  summary VARCHAR(1000) DEFAULT NULL,
  order_num INT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (index_module_id),
  KEY idx_req_index_module_project (project_id),
  KEY idx_req_index_module_repo (repo_id),
  KEY idx_req_index_module_code (project_id, module_code),
  KEY idx_req_index_module_variant (project_id, variant_id, module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库索引模块知识';

CREATE TABLE IF NOT EXISTS req_impact_item (
  impact_id BIGINT NOT NULL AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  repo_id BIGINT NOT NULL,
  variant_id BIGINT DEFAULT NULL,
  branch_name VARCHAR(100) DEFAULT NULL,
  module_code VARCHAR(64) DEFAULT NULL,
  module_id BIGINT DEFAULT NULL,
  item_type VARCHAR(20) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  item_key VARCHAR(300) DEFAULT NULL,
  relative_path VARCHAR(500) DEFAULT NULL,
  http_method VARCHAR(20) DEFAULT NULL,
  api_path VARCHAR(300) DEFAULT NULL,
  permission_key VARCHAR(200) DEFAULT NULL,
  table_name VARCHAR(100) DEFAULT NULL,
  summary VARCHAR(1000) DEFAULT NULL,
  tags VARCHAR(500) DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (impact_id),
  KEY idx_req_impact_project_module (project_id, module_code),
  KEY idx_req_impact_variant_branch (variant_id, branch_name),
  KEY idx_req_impact_repo (repo_id),
  KEY idx_req_impact_type (item_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块影响面索引';

CREATE TABLE IF NOT EXISTS req_action_token (
  token_id BIGINT NOT NULL AUTO_INCREMENT,
  action_type VARCHAR(64) NOT NULL,
  token_prefix VARCHAR(64) NOT NULL,
  token_hash CHAR(64) NOT NULL,
  target_method VARCHAR(100) NOT NULL,
  project_id BIGINT DEFAULT NULL,
  variant_id BIGINT DEFAULT NULL,
  demand_id BIGINT DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  expire_time DATETIME DEFAULT NULL,
  last_used_time DATETIME DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (token_id),
  UNIQUE KEY uk_req_action_token_hash (token_hash),
  KEY idx_req_action_token_context (action_type, project_id, variant_id, demand_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP动作Token表';

CREATE TABLE IF NOT EXISTS req_activity_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  project_id BIGINT DEFAULT NULL,
  demand_id BIGINT DEFAULT NULL,
  event_type VARCHAR(64) NOT NULL,
  client_type VARCHAR(20) NOT NULL DEFAULT 'web',
  summary VARCHAR(500) DEFAULT NULL,
  metadata_json TEXT,
  event_time DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_req_activity_user (user_id),
  KEY idx_req_activity_project (project_id),
  KEY idx_req_activity_demand (demand_id),
  KEY idx_req_activity_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求平台业务事件';

-- -----------------------------------------------------------------------------
-- ReqFlow menus and permissions
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求管理', 0, 20, 'requirement', NULL, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'requirement' AND menu_type = 'M');

SET @req_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'requirement' AND menu_type = 'M' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '项目管理', @req_parent_id, 1, 'project', 'requirement/project/index', 1, 0, 'C', '0', '0', 'req:project:list', 'tree', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'project');

SET @req_project_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'project' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '项目查询', @req_project_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:project:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:project:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '项目新增', @req_project_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:project:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:project:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '项目修改', @req_project_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:project:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:project:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '项目删除', @req_project_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:project:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:project:remove');

UPDATE sys_menu
SET visible = '1', status = '1'
WHERE parent_id = @req_parent_id
  AND path IN ('repository', 'variant', 'module');

UPDATE sys_menu
SET status = '1'
WHERE parent_id IN (
    SELECT menu_id FROM (
        SELECT menu_id
        FROM sys_menu
        WHERE parent_id = @req_parent_id
          AND path IN ('repository', 'variant', 'module')
    ) hidden_requirement_menu
);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '索引查询', @req_project_id, 5, '#', '', 1, 0, 'F', '0', '0', 'req:index:list', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:index:list');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '索引导入', @req_project_id, 6, '#', '', 1, 0, 'F', '0', '0', 'req:index:import', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_project_id AND perms = 'req:index:import');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求列表', @req_parent_id, 2, 'demand', 'requirement/demand/index', 1, 0, 'C', '0', '0', 'req:demand:list', 'form', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'demand');

SET @req_demand_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'demand' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求查询', @req_demand_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:demand:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_demand_id AND perms = 'req:demand:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求新增', @req_demand_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:demand:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_demand_id AND perms = 'req:demand:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求修改', @req_demand_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:demand:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_demand_id AND perms = 'req:demand:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求删除', @req_demand_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:demand:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_demand_id AND perms = 'req:demand:remove');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求执行包', @req_parent_id, 3, 'package', 'requirement/package/index', 1, 0, 'C', '0', '0', 'req:package:list', 'clipboard', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'package');

SET @req_package_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'package' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '执行包保存', @req_package_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:package:save', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_package_id AND perms = 'req:package:save');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP管理', @req_parent_id, 80, 'mcp-key', 'requirement/mcpKey/index', 1, 0, 'C', '0', '0', 'req:mcp:key:list', 'lock', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'mcp-key');

SET @req_mcp_key_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'mcp-key' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key查询', @req_mcp_key_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key新增', @req_mcp_key_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key删除', @req_mcp_key_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:remove');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '使用统计', @req_parent_id, 90, 'req-statistics', 'requirement/statistics/index', 1, 0, 'C', '0', '0', 'req:stats:view', 'chart', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'req-statistics');

-- -----------------------------------------------------------------------------
-- ReqFlow initial release settings
-- -----------------------------------------------------------------------------

-- Public MCP host configuration. Administrators fill only host:port in sys_config;
-- the backend derives scheme, context-path and /requirement/mcp from the current request.
INSERT INTO sys_config (
    config_name,
    config_key,
    config_value,
    config_type,
    create_by,
    create_time,
    remark
)
SELECT
    '需求平台-MCP服务IP端口',
    'reqflow.mcp.public-host',
    '',
    'Y',
    'admin',
    NOW(),
    '仅填写对外可访问的 IP:端口，例如 10.0.0.12:8080；后端自动拼接 context-path 和 /requirement/mcp'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'reqflow.mcp.public-host'
);

-- Remove template brand leftovers from the RuoYi base data for a clean first release.
UPDATE sys_dept
SET dept_name = '统一需求流转平台', leader = '平台管理员', email = 'admin@reqflow.local'
WHERE dept_id = 100 AND dept_name LIKE '%若依%';

UPDATE sys_dept
SET leader = '平台管理员', email = 'admin@reqflow.local'
WHERE leader = '若依' OR email IN ('ry@qq.com', 'ry@163.com');

UPDATE sys_user
SET nick_name = '平台管理员', email = 'admin@reqflow.local'
WHERE user_id = 1 AND nick_name = '若依';

UPDATE sys_user
SET nick_name = '测试人员', email = 'tester@reqflow.local'
WHERE user_id = 2 AND nick_name = '若依';

DELETE FROM sys_menu
WHERE menu_name = '若依官网' OR path IN ('http://ruoyi.vip', 'http://www.ruoyi.vip');

DELETE FROM sys_notice
WHERE notice_title LIKE '%若依%' OR notice_content LIKE '%ruoyi.vip%' OR notice_content LIKE '%RuoYi%';

-- Remove the old edit permission because MCP keys are created, viewed and deleted, not edited in place.
SET @req_mcp_key_edit_menu_id = (
    SELECT menu_id FROM sys_menu WHERE perms = 'req:mcp:key:edit' LIMIT 1
);
DELETE FROM sys_role_menu WHERE menu_id = @req_mcp_key_edit_menu_id;
DELETE FROM sys_menu WHERE menu_id = @req_mcp_key_edit_menu_id;

-- Initial role boundaries for requirement users and developers.
INSERT INTO sys_role(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '需求人员', 'requirement_user', 10, 1, 1, 1, '0', '0', 'admin', NOW(), '需求人员：仅首页、需求列表和使用统计'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'requirement_user');

UPDATE sys_role
SET role_name = '需求人员',
    role_sort = 10,
    status = '0',
    del_flag = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '需求人员：仅首页、需求列表和使用统计'
WHERE role_key = 'requirement_user';

INSERT INTO sys_role(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '开发人员', 'requirement_developer', 20, 1, 1, 1, '0', '0', 'admin', NOW(), '开发人员：首页、需求列表、MCP管理和使用统计'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'requirement_developer');

UPDATE sys_role
SET role_name = '开发人员',
    role_sort = 20,
    status = '0',
    del_flag = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = '开发人员：首页、需求列表、MCP管理和使用统计'
WHERE role_key = 'requirement_developer';

SET @requirement_user_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'requirement_user' AND del_flag = '0' LIMIT 1);
SET @requirement_developer_role_id = (SELECT role_id FROM sys_role WHERE role_key = 'requirement_developer' AND del_flag = '0' LIMIT 1);

DELETE FROM sys_role_menu WHERE role_id = @requirement_user_role_id;
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT @requirement_user_role_id, menu_id
FROM sys_menu
WHERE status = '0'
  AND (
    (parent_id = 0 AND path = 'requirement' AND menu_type = 'M')
    OR perms IN (
      'req:demand:list',
      'req:demand:query',
      'req:demand:add',
      'req:demand:edit',
      'req:stats:view'
    )
  );

DELETE FROM sys_role_menu WHERE role_id = @requirement_developer_role_id;
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT @requirement_developer_role_id, menu_id
FROM sys_menu
WHERE status = '0'
  AND (
    (parent_id = 0 AND path = 'requirement' AND menu_type = 'M')
    OR perms IN (
      'req:demand:list',
      'req:demand:query',
      'req:demand:edit',
      'req:mcp:key:list',
      'req:mcp:key:query',
      'req:mcp:key:add',
      'req:mcp:key:remove',
      'req:package:save',
      'req:stats:view'
    )
  );

-- Admin role_key=admin remains the RuoYi super administrator and is not narrowed here.
