ALTER TABLE req_variant ADD COLUMN mcp_key VARCHAR(128) DEFAULT NULL COMMENT 'MCP项目分支识别key' AFTER branch_policy;

UPDATE req_variant v
JOIN req_project p ON p.project_id = v.project_id
SET v.mcp_key = concat(
  upper(replace(replace(replace(p.project_code, '-', '_'), '/', '_'), ' ', '_')),
  ':',
  upper(replace(replace(replace(v.variant_code, '-', '_'), '/', '_'), ' ', '_'))
)
WHERE v.mcp_key IS NULL OR v.mcp_key = '';

CREATE UNIQUE INDEX uk_req_variant_mcp_key ON req_variant (mcp_key);

SET @req_parent_id = (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'requirement' AND menu_type = 'M' LIMIT 1);

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

UPDATE sys_menu
SET order_num = 2
WHERE parent_id = @req_parent_id AND path = 'demand';

UPDATE sys_menu
SET order_num = 3
WHERE parent_id = @req_parent_id AND path = 'package';
