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
