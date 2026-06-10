# 全局 Reqflow MCP 技能安装执行报告

## 执行结论

已完成后端侧全局 Codex skill 包返回：

- `/requirement/mcp/key/config` 返回 `codexGlobalSkillPackage`。
- 创建或重置人员 MCP Key 的响应返回同一份 `codexGlobalSkillPackage`。
- 全局 skill 包名为 `reqflow-mcp`，作用域为 `global`，包含符合 Codex skill 规范的 `reqflow-mcp/SKILL.md`。
- skill 包使用跨平台安装说明，由 Codex 按自身当前 skill 规范决定全局 skills 目录和写入方式，不绑定 macOS/Linux shell 命令。
- 已清理本地 `docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复` 空目录；当前 active 仅保留 `.gitkeep` 和 REQ-008 目录。

提交：`0e21aee feat: 提供全局 reqflow MCP skill 包`

## 修改文件

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 新增全局 `reqflow-mcp` skill 包模板。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 创建/重置 Key 结果增加 `codexGlobalSkillPackage`。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | 创建/重置 Key 时附带全局 skill 包。 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | MCP 管理配置接口附带全局 skill 包。 |
| `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 覆盖配置接口全局 skill 包字段和内容。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 覆盖 Key 创建结果全局 skill 包字段和不包含明文 Key。 |
| `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md` | 同步接口契约和模块风险说明。 |

## 验收覆盖

| 验收 ID | 覆盖结果 |
|---|---|
| AC-BE-001 | 已通过 `ReqMcpKeyControllerTest.configReturnsCrossPlatformGlobalSkillPackage` 覆盖。 |
| AC-BE-002 | 已通过 `ReqMcpUserKeyServiceImplTest.createsRandomKeyAndStoresOnlyHashAndPrefix` 覆盖。 |
| AC-BE-003 | 已通过 `SKILL.md` frontmatter 和触发条件断言覆盖。 |
| AC-BE-004 | 已通过 reqflow MCP tool 名称断言覆盖。 |
| AC-BE-005 | 已通过 `find docs/specs/active -maxdepth 2 -print` 确认旧空目录已清理。 |
| AC-BE-006 | 已通过 Maven 测试、admin 打包、文档检查、harness 检查和空白检查覆盖。 |

## 接口、数据库、权限和页面影响

- 接口/API：是，`/requirement/mcp/key/config`、创建 Key、重置 Key 响应新增 `codexGlobalSkillPackage`。
- 数据库/SQL：否；未新增或修改 `sql/`、`docs/db/` 文件。
- 权限：否，沿用 `req:mcp:key:*`。
- 页面：是，前端 companion 仓库同步展示和复制全局 Skill 包。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，13 tests。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，62 tests。 |
| `mvn -pl ruoyi-admin -am -DskipTests package` | 通过。 |
| `sh scripts/check-docs.sh` | 通过。 |
| `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-008-全局ReqflowMCP技能安装` | 通过。 |
| `git diff --check` | 通过，无输出。 |

## Review 返修记录

- 无返修项。
