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

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓库管理', @req_parent_id, 2, 'repository', 'requirement/repository/index', 1, 0, 'C', '0', '0', 'req:repo:list', 'code', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'repository');

SET @req_repo_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'repository' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓库查询', @req_repo_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:repo:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_repo_id AND perms = 'req:repo:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓库新增', @req_repo_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:repo:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_repo_id AND perms = 'req:repo:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓库修改', @req_repo_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:repo:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_repo_id AND perms = 'req:repo:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓库删除', @req_repo_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:repo:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_repo_id AND perms = 'req:repo:remove');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '客户定制线', @req_parent_id, 3, 'variant', 'requirement/variant/index', 1, 0, 'C', '0', '0', 'req:variant:list', 'peoples', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'variant');

SET @req_variant_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'variant' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '定制线查询', @req_variant_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:variant:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_variant_id AND perms = 'req:variant:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '定制线新增', @req_variant_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:variant:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_variant_id AND perms = 'req:variant:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '定制线修改', @req_variant_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:variant:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_variant_id AND perms = 'req:variant:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '定制线删除', @req_variant_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:variant:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_variant_id AND perms = 'req:variant:remove');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '模块功能点', @req_parent_id, 4, 'module', 'requirement/module/index', 1, 0, 'C', '0', '0', 'req:module:list', 'tree-table', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'module');

SET @req_module_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'module' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '模块查询', @req_module_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:module:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_module_id AND perms = 'req:module:query');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '模块新增', @req_module_id, 2, '#', '', 1, 0, 'F', '0', '0', 'req:module:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_module_id AND perms = 'req:module:add');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '模块修改', @req_module_id, 3, '#', '', 1, 0, 'F', '0', '0', 'req:module:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_module_id AND perms = 'req:module:edit');
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '模块删除', @req_module_id, 4, '#', '', 1, 0, 'F', '0', '0', 'req:module:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_module_id AND perms = 'req:module:remove');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '需求列表', @req_parent_id, 5, 'demand', 'requirement/demand/index', 1, 0, 'C', '0', '0', 'req:demand:list', 'form', 'admin', NOW()
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
SELECT '需求执行包', @req_parent_id, 6, 'package', 'requirement/package/index', 1, 0, 'C', '0', '0', 'req:package:list', 'clipboard', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'package');

SET @req_package_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'package' LIMIT 1);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '执行包保存', @req_package_id, 1, '#', '', 1, 0, 'F', '0', '0', 'req:package:save', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_package_id AND perms = 'req:package:save');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '使用统计', @req_parent_id, 90, 'req-statistics', 'requirement/statistics/index', 1, 0, 'C', '0', '0', 'req:stats:view', 'chart', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @req_parent_id AND path = 'req-statistics');
