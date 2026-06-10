# MCP人员Key管理需求说明

## 背景

当前需求平台已经存在 `/requirement/mcp` 接口和项目分支级 `req_variant.mcp_key`。分支级 Key 用于索引工具识别项目分支，不是人员访问凭据。用户需要新增一个后台 MCP 管理菜单，让具备管理权限的人员可以为具体系统用户生成 MCP 访问 Key，并在页面上看到 MCP 地址和 Codex 配置所需信息。

## 目标

- 新增 MCP 管理菜单，需求人员不具备该菜单和接口权限。
- 管理人员可以为一个启用状态的系统用户创建唯一随机 MCP Key。
- MCP Key 与人员绑定，后续 MCP 调用按该人员的系统权限执行。
- 页面展示 MCP 地址、鉴权 Header 和 Codex 配置提示，用户创建 Key 后即可复制到 Codex 配置。
- MCP Key 不复用项目分支 `mcpKey`，避免“人员访问凭据”和“项目分支识别 Key”混用。

## 范围

本次包含：

- 新增人员 MCP Key 表、领域对象、Mapper、Service 和 Controller。
- 新增 MCP Key 鉴权逻辑，让 `/requirement/mcp` 支持 `X-MCP-Key` 访问。
- 新增菜单 SQL 和后端权限点：`req:mcp:key:list`、`req:mcp:key:query`、`req:mcp:key:add`、`req:mcp:key:edit`、`req:mcp:key:remove`。
- 更新后端契约、数据库关系和验证说明涉及的长期上下文。

本次不包含：

- 不新增独立 OAuth、PAT 管理体系或第三方登录。
- 不让 MCP Key 执行 Git、shell、文件系统写入或大模型调用。
- 不改变项目分支 `req_variant.mcp_key` 的索引用途。
- 不给需求人员角色自动分配 MCP 管理权限。

## 影响范围

- 接口/API：是，新增 `/requirement/mcp/key/**` 管理接口，并调整 `/requirement/mcp` 鉴权入口。
- 数据库/SQL：是，新增 `req_mcp_user_key` 表和菜单迁移 SQL。
- 权限/菜单：是，新增 MCP 管理菜单和 `req:mcp:key:*` 权限点。
- 页面/交互：是，前端 companion 新增 MCP 管理页。
- 导出/异步/任务：否。

## 契约与数据口径

### 数据表

`req_mcp_user_key` 一行代表一个人员 MCP 访问 Key：

- `key_id`：主键。
- `user_id`：绑定的 `sys_user.user_id`。
- `key_name`：Key 名称，用于区分用途。
- `key_prefix`：明文 Key 前缀，用于列表识别。
- `key_hash`：明文 Key 的 SHA-256 哈希，唯一。
- `status`：`0` 正常，`1` 停用。
- `last_used_time`、`last_used_ip`：最近使用记录。
- `create_by/create_time/update_by/update_time/remark`：沿用 RuoYi 审计字段。

明文 Key 只在创建或重新生成成功时返回一次；列表不返回完整明文。

### 管理接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/mcp/key/list` | GET | `req:mcp:key:list` | 分页查询人员 MCP Key |
| `/requirement/mcp/key/{keyId}` | GET | `req:mcp:key:query` | 查询单条 Key 管理信息，不返回完整明文 |
| `/requirement/mcp/key` | POST | `req:mcp:key:add` | 为用户创建随机唯一 Key，返回一次性明文 |
| `/requirement/mcp/key` | PUT | `req:mcp:key:edit` | 修改 Key 名称、状态或备注 |
| `/requirement/mcp/key/{keyIds}` | DELETE | `req:mcp:key:remove` | 删除或撤销 Key |
| `/requirement/mcp/key/{keyId}/regenerate` | POST | `req:mcp:key:edit` | 重新生成 Key，返回一次性明文 |
| `/requirement/mcp/key/config` | GET | `req:mcp:key:list` | 返回 MCP 地址、Header 名称和配置提示 |

### MCP 调用鉴权

- `/requirement/mcp` 支持原有登录态调用，也支持请求头 `X-MCP-Key: <plainKey>`。
- 服务端按 Key 哈希查找启用状态 Key，加载绑定用户和该用户当前菜单权限，构造 MCP 调用的安全上下文。
- 如果绑定用户停用、Key 停用、Key 不存在或用户没有任一 MCP 访问权限，则拒绝调用。
- 进入 `McpService` 后继续沿用现有 tool name 细粒度权限校验，例如 `publish_repository_index` 仍需要 `req:index:import`。

## 验收标准

- AC-BE-001：执行菜单 SQL 后，系统存在 MCP 管理菜单和 `req:mcp:key:*` 权限点；未分配该权限的需求人员看不到菜单且不能调用管理接口。
- AC-BE-002：创建 MCP Key 时生成随机唯一明文 Key，数据库只保存哈希和前缀，响应只在创建或重置时返回明文。
- AC-BE-003：同一个用户可拥有多个 Key；停用或删除 Key 后不能再通过该 Key 调用 MCP。
- AC-BE-004：使用 `X-MCP-Key` 调用 `/requirement/mcp` 时，服务端按 Key 绑定用户的现有权限执行，权限不足时返回错误。
- AC-BE-005：MCP Key 管理和 MCP 鉴权相关接口、表关系、安全边界已同步到 `docs/ai-harness` 和 `docs/db`。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/2026-06-10-REQ-002-MCP人员Key管理`
- 关联分支：`feature/REQ-20260610-002-branch-module-depth`

## 客户与分支

- 目标客户：通用
- 基线分支：`feature/REQ-20260610-002-branch-module-depth`
- 任务分支：未创建，本次计划阶段沿用当前分支记录。

## 约束与假设

- 需求人员没有 MCP 管理权限通过 RuoYi 菜单权限实现，不依赖尚未定义的专用角色 key。
- MCP 管理页可以选择系统用户；服务端应拒绝为停用用户生成可用 Key。
- 绑定用户的权限变化即时影响 MCP Key 可用能力，不把权限快照固化到 Key 表。
