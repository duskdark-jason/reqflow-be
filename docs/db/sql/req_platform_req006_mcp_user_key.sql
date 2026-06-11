CREATE TABLE IF NOT EXISTS req_mcp_user_key (
  key_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  key_name VARCHAR(100) NOT NULL,
  key_prefix VARCHAR(32) NOT NULL,
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

SET @req_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'requirement' AND menu_type = 'M' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP管理', @req_parent_id, 80, 'mcp-key', 'requirement/mcpKey/index', 1, 0, 'C', '0', '0', 'req:mcp:key:list', 'lock', 'admin', NOW()
WHERE @req_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'mcp-key');

SET @req_mcp_key_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'mcp-key' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key查询', @req_mcp_key_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:query', '#', 'admin', NOW()
WHERE @req_mcp_key_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key新增', @req_mcp_key_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:add', '#', 'admin', NOW()
WHERE @req_mcp_key_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key修改', @req_mcp_key_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:edit', '#', 'admin', NOW()
WHERE @req_mcp_key_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'MCP Key删除', @req_mcp_key_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:mcp:key:remove', '#', 'admin', NOW()
WHERE @req_mcp_key_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_mcp_key_id AND perms = 'req:mcp:key:remove');
