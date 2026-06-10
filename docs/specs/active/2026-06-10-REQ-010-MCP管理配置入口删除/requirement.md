# MCP管理配置入口删除后端需求说明

## 背景

MCP 管理页不再需要常驻展示 MCP 地址、请求头、Codex 配置、全局 Skill 包和 Codex 安装包。用户进一步确认要直接删除没用的接口和相关功能，因此后端应移除专门给页面配置区使用的 `/requirement/mcp/key/config` 接口，并收敛创建/重置响应，避免继续暴露独立 Codex 配置和全局 Skill 包字段。

## 目标

- 删除 `/requirement/mcp/key/config` 接口。
- 创建和重置人员 MCP Key 的返回结果只保留一次性明文 Key、Key 记录摘要和 `codexSetupPackage`。
- 删除创建结果中的 `mcpAddress`、`headerName`、`codexConfig` 和 `codexGlobalSkillPackage` 独立字段。
- 保留 `codexSetupPackage` 作为创建/重置后复制给 Codex 的唯一安装材料。
- 更新后端 harness 契约，避免后续依赖旧配置接口。

## 范围

本次包含：

- 调整 `ReqMcpKeyController`，删除配置接口和仅服务于配置接口的独立全局 Skill 返回。
- 调整 `ReqMcpUserKeyCreateResult` 和 `ReqMcpUserKeyServiceImpl` 的创建结果结构。
- 更新相关单元测试和契约文档。

本次不包含：

- 修改 `req_mcp_user_key` 表结构。
- 修改 `/requirement/mcp` 协议入口或 MCP tool 列表。
- 修改菜单 SQL、权限点或角色授权。

## 影响范围

- 接口/API：是，删除 `GET /requirement/mcp/key/config`，创建/重置响应字段收敛。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：是，需同步前端 companion 仓库删除配置区。
- 导出/异步/任务：否。

## 契约与数据口径

- 删除接口：`GET /requirement/mcp/key/config`。
- 创建接口：`POST /requirement/mcp/key`，返回 `key`、`plainKey`、`codexSetupPackage`。
- 重置接口：`POST /requirement/mcp/key/{keyId}/regenerate`，返回 `key`、`plainKey`、`codexSetupPackage`。
- `codexSetupPackage` 不包含人员明文 Key 或一次性 `actionToken`；明文 Key 只通过 `plainKey` 字段出现一次。
- 数据粒度：一次创建或重置响应代表一次明文 Key 发放结果。

## 验收标准

- AC-BE-001：`ReqMcpKeyController` 不再暴露 `/config` 映射。
- AC-BE-002：创建 Key 返回结果不再包含 `mcpAddress`、`headerName`、`codexConfig`、`codexGlobalSkillPackage` 独立字段。
- AC-BE-003：创建和重置结果仍返回 `codexSetupPackage`，且安装包不包含人员明文 Key。
- AC-BE-004：后端 harness 文档同步删除旧配置接口契约。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除`
- 关联分支：`feature/REQ-20260610-010-mcp-key-config-cleanup`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/REQ-20260610-010-mcp-key-config-cleanup

## 约束与假设

- `reqflow.mcp.public-url` 仍用于创建/重置时生成 `codexSetupPackage` 内的 MCP 地址。
- 直接删除接口意味着旧前端必须同步更新；本需求 companion 前端会一并删除调用。
