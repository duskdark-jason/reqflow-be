-- ReqFlow initial release settings.
-- Run after ry_20260417.sql, quartz.sql, req_platform_schema.sql and req_platform_menu.sql.

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
