# MCP地址后端配置项执行报告

## 执行摘要

已为 MCP Key 管理配置接口增加后端配置项 `reqflow.mcp.public-url`：

- 配置有值时，`/requirement/mcp/key/config` 返回的 `mcpAddress` 优先使用配置值。
- Codex 配置模板中的 `url` 与 `mcpAddress` 使用同一地址值。
- 配置为空时，继续按 `X-Forwarded-Proto`、`X-Forwarded-Host`、`Host` 和 `context-path` 推导地址。
- 已同步 `application.yml` 和后端 API 契约文档。

## 文件变更

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | 增加 `reqflow.mcp.public-url` 配置读取、地址归一化和单次地址复用 |
| 修改 | `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 增加配置优先、请求头兜底和配置模板一致性测试 |
| 修改 | `ruoyi-admin/src/main/resources/application.yml` | 增加 MCP 对外访问完整地址默认配置项 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 记录配置项名称、完整地址格式和兜底推导规则 |
| 新增 | `docs/specs/active/2026-06-10-REQ-004-MCP地址后端配置项/` | 记录本需求的元信息、需求、计划、执行和 Review |

## 验收覆盖

- AC-BE-001：通过。`mcpAddressUsesConfiguredPublicUrlBeforeRequestHost` 覆盖 `reqflow.mcp.public-url` 优先于 `X-Forwarded-Host`，并验证尾部 `/` 会归一化。
- AC-BE-002：通过。`mcpAddressFallsBackToForwardedHostWhenPublicUrlIsBlank` 覆盖配置为空时继续使用转发请求头和 `context-path` 推导地址。
- AC-BE-003：通过。`configReturnsTheSameConfiguredAddressInAddressAndTemplate` 覆盖 `mcpAddress` 和 `codexConfigTemplate.url` 使用同一配置地址。
- AC-BE-004：通过。`application.yml` 已增加 `reqflow.mcp.public-url`，后端 API 契约已记录配置优先级和部署建议。

## 验证命令

| 层级 | 命令 | 结果 |
|---|---|---|
| TDD Red | `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 按预期失败：生产代码尚无 `mcpPublicUrl` 字段，2 个新增测试报错 |
| TDD Green | `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，4 tests，0 failures，0 errors |
| L1 标准命令 | 在临时 clean worktree 指向当前提交后执行 `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，`ruoyi-admin` 打包成功 |
| L1 脏工作区观察 | `mvn -pl ruoyi-admin -am -DskipTests package` | 当前主工作区曾被无关 `ruoyi-requirement` 测试源改动挡住，未作为本需求验证依据 |
| L1 生产打包 | `mvn -pl ruoyi-admin -am -Dmaven.test.skip=true package` | 通过，`ruoyi-admin` 打包成功 |
| L0 | `sh scripts/check-docs.sh` | 通过，输出“文档检查通过” |
| 空白检查 | `git diff --check` | 通过，无输出 |

## 提交记录

- 提交：4b26c51 `feat: 增加 MCP 地址后端配置项`

## 未执行项

- L3 运行态未执行：本次没有启动后端服务，也没有用登录态实际访问 `/requirement/mcp/key/config`。本轮已用 Controller 层单元测试覆盖地址生成和返回模板一致性。

## 工作区注意事项

- 当前工作区存在无关的 `docs/specs/active/2026-06-10-REQ-003-项目页签化与统一需求流转平台UI/` 未跟踪目录。
- 当前工作区存在无关的 `ruoyi-requirement` 动作 token 相关代码、测试和 SQL 改动，未纳入本次提交。
