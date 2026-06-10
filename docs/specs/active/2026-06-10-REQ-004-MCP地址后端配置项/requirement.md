# MCP地址后端配置项需求说明

## 背景

MCP Key 管理页面会通过 `/requirement/mcp/key/config` 返回 MCP 地址和 Codex 配置模板。当前地址按请求头自动推导，如果前端或代理请求 Host 为 `localhost:1024`，页面会展示：

```json
{
  "url": "http://localhost:1024/requirement/mcp"
}
```

该地址不一定是用户实际应配置到 Codex 的后端访问地址，需要提供后端配置项进行显式覆盖。

## 目标

- 后端提供可配置的 MCP 对外访问完整地址。
- 配置为空时保留现有自动推导能力。
- 配置地址同时影响 `mcpAddress` 和 Codex 配置模板中的 `url`。
- 文档记录配置项名称、优先级和适用场景。

## 非目标

- 不调整前端页面布局和交互。
- 不改变 MCP Key 鉴权、权限点或数据库结构。
- 不新增环境专属配置文件。

## 验收标准

- AC-BE-001：当 `reqflow.mcp.public-url` 有值时，`/requirement/mcp/key/config` 返回的 MCP 地址必须优先使用该配置，不受 `Host` 或 `X-Forwarded-Host` 影响。
- AC-BE-002：当 `reqflow.mcp.public-url` 为空时，服务端继续按 `X-Forwarded-Proto`、`X-Forwarded-Host`、`Host`、`serverName/serverPort` 和 `context-path` 推导地址。
- AC-BE-003：返回给页面的 `mcpAddress` 与 `codexConfigTemplate.url` 使用同一地址来源，避免展示和模板不一致。
- AC-BE-004：`application.yml` 与 API 契约文档记录配置项名称、填写完整地址的要求和为空时的兜底规则。
