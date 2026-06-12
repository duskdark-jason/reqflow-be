-- REQ-016 平台角色权限初始化/升级脚本
-- 角色边界：
-- 1. 管理员：role_key = 'admin'，沿用 RuoYi 超级管理员全部权限。
-- 2. 需求人员：首页为前端公共路由，菜单只分配需求列表和使用统计。
-- 3. 开发人员：首页为前端公共路由，菜单分配需求列表、MCP 管理和使用统计；隐藏分配 req:package:save 供 MCP 回写资料。

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

-- 需求人员角色权限
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

-- 开发人员角色权限
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

-- 超级管理员说明：role_key = 'admin' 是 RuoYi 超级管理员，权限服务自动视为全部功能。
