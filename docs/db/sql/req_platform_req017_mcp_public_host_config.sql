-- REQ-017 MCP服务地址系统参数：管理员在系统管理/参数设置中维护 IP:端口
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
    '仅填写对外可访问的 IP:端口，例如 10.0.0.12:8080；后端自动拼接 /requirement/mcp'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'reqflow.mcp.public-host'
);
