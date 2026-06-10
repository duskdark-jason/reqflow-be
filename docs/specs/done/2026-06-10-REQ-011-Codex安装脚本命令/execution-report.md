# Codex 安装脚本命令后端执行报告

## 执行结论

已完成后端侧安装脚本命令能力：

- `codexSetupPackage` 新增 `installScripts` 和 `installCommands`，提供 macOS/Linux 与 Windows PowerShell 两个平台命令模板。
- 命令模板使用 `${REQFLOW_MCP_KEY}` 占位符，不在安装包内写入人员明文 Key 或 `actionToken`。
- 新增匿名安装脚本端点 `GET /requirement/codex/install.sh` 和 `GET /requirement/codex/install.ps1`。
- 安装脚本写入 Codex MCP 配置和全局 `reqflow-mcp/SKILL.md`，并提示重启或刷新 Codex；不自动调用 reqflow MCP tool。

提交：`0789972 feat: 提供Codex安装脚本命令`。

## 修改文件

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexSetupPackageTemplate.java` | 安装包增加脚本 URL 与多平台命令模板。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexInstallScriptTemplate.java` | 新增 shell/PowerShell 安装脚本模板。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 暴露 skill 内容供安装脚本复用。 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqCodexInstallController.java` | 新增安装脚本下载端点。 |
| `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` | 放行安装脚本匿名 GET 访问。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 覆盖安装命令模板和明文 Key 边界。 |
| `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqCodexInstallControllerTest.java` | 覆盖 shell/PowerShell 安装脚本内容。 |
| `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md` | 同步接口契约和模块知识库。 |

## 验收覆盖

| 验收 ID | 覆盖结果 |
|---|---|
| AC-BE-001 | 已通过 `ReqMcpUserKeyServiceImplTest.createsRandomKeyAndStoresOnlyHashAndPrefix` 覆盖。 |
| AC-BE-002 | 已通过安装命令模板断言不含明文 Key 和 `actionToken` 覆盖。 |
| AC-BE-003 | 已通过 `ReqCodexInstallControllerTest.shellInstallScriptWritesMcpConfigAndGlobalSkill` 覆盖。 |
| AC-BE-004 | 已通过 `ReqCodexInstallControllerTest.powershellInstallScriptWritesMcpConfigAndGlobalSkill` 覆盖。 |
| AC-BE-005 | 已通过 Maven 测试、admin 打包、文档检查、harness 检查和空白检查覆盖。 |

## 接口、数据库、权限和页面影响

- 接口/API：是，新增安装脚本端点，创建/重置响应的 `codexSetupPackage` 新增 `installScripts` 和 `installCommands`。
- 数据库/SQL：否。
- 权限：是，安装脚本端点匿名可读；MCP Key 管理权限不变。
- 页面：是，前端 companion 仓库同步展示安装命令。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | Red 阶段失败于缺少 `installCommands`；Green 后通过，10 tests。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，62 tests。 |
| `mvn -pl ruoyi-admin -am -DskipTests package` | 通过。 |
| `sh scripts/check-docs.sh` | 通过，文档检查通过。 |
| `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-011-Codex安装脚本命令` | 通过，Harness 检查通过（init 模式）。 |
| `git diff --check` | 通过，退出码 0；存在既有 CRLF 提示，不影响本次检查。 |

## Review 返修记录

- 未进入 Review。
