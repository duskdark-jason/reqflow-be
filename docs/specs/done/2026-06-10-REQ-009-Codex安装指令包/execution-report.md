# Codex 安装指令包执行报告

## 执行结论

已完成后端侧 Codex 安装指令包返回：

- `/requirement/mcp/key/config` 返回 `codexSetupPackage`。
- 创建或重置人员 MCP Key 的响应返回同一份 `codexSetupPackage`。
- `codexSetupPackage` 包含 MCP server 摘要、Codex 配置模板、全局 skill 包、安装短提示词和 MCP server metadata。
- 安装包使用占位 Key，不包含人员明文 `plainKey` 或一次性 `actionToken`。
- 保留 `codexGlobalSkillPackage` 兼容字段。

提交：`da6d77c feat: 提供codex安装指令包`。

## 修改文件

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexSetupPackageTemplate.java` | 新增 `reqflow-codex-setup` 安装指令包和 server metadata 模板。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 创建/重置 Key 结果增加 `codexSetupPackage`。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | 创建/重置 Key 时附带安装指令包。 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | MCP 管理配置接口附带安装指令包，并复用统一 Codex 配置模板。 |
| `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 覆盖配置接口安装指令包字段和内容。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 覆盖 Key 创建结果安装指令包字段和不包含明文 Key。 |
| `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md` | 同步接口契约和模块风险说明。 |

## 验收覆盖

| 验收 ID | 覆盖结果 |
|---|---|
| AC-BE-001 | 已通过 `ReqMcpKeyControllerTest.configReturnsCodexSetupPackage` 覆盖。 |
| AC-BE-002 | 已通过 MCP server 摘要字段断言覆盖。 |
| AC-BE-003 | 已通过安装短提示词断言覆盖。 |
| AC-BE-004 | 已通过 server metadata 工具组断言覆盖。 |
| AC-BE-005 | 已通过 `ReqMcpUserKeyServiceImplTest.createsRandomKeyAndStoresOnlyHashAndPrefix` 覆盖。 |
| AC-BE-006 | 已通过 Maven 测试、admin 打包、文档检查、harness 检查和空白检查覆盖。 |

## 接口、数据库、权限和页面影响

- 接口/API：是，`/requirement/mcp/key/config`、创建 Key、重置 Key 响应新增 `codexSetupPackage`。
- 数据库/SQL：否；未新增或修改 `sql/`、`docs/db/` 文件。
- 权限：否，沿用 `req:mcp:key:*`。
- 页面：是，前端 companion 仓库同步展示和复制 Codex 安装包。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，14 tests。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，62 tests。 |
| `mvn -pl ruoyi-admin -am -DskipTests package` | 通过。 |
| `sh scripts/check-docs.sh` | 通过。 |
| `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-009-Codex安装指令包` | 通过。 |
| `git diff --check` | 通过，无输出。 |

## Review 返修记录

- 未进入 Review。
