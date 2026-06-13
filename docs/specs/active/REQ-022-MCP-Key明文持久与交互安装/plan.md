# MCP Key明文持久与交互安装执行计划

## 计划

1. 后端测试先行：调整 `ReqMcpUserKeyServiceImplTest`、安装包模板测试和安装脚本控制器测试，覆盖明文持久、使用指令返回明文和交互选择脚本。覆盖 AC-001、AC-002、AC-003、AC-004。
2. 后端实现：为 `ReqMcpUserKey`、Mapper、Service 和 SQL 基线新增 `plain_key` 字段，保留哈希鉴权。覆盖 AC-001、AC-002。
3. 安装脚本：顶层统一命令去掉默认 `all` 参数，Shell 和 PowerShell 脚本在未传 client 时提示选择工具，保留自动化参数。覆盖 AC-003、AC-004。
4. 后端配置接口：新增管理员专用 MCP 请求地址配置 DTO、GET/PUT 接口和验证，拒绝完整 URL 或路径输入。覆盖 AC-010。
5. 前端 companion：移除 MCP 管理页明文 Key 和 Key 前缀展示，只用 `plainKey` 渲染统一安装命令；补充管理员 MCP 请求地址配置弹窗。覆盖 AC-005、AC-011。
6. 文档同步：更新 API 契约、模块知识库、数据库字典、关系说明和前端契约。覆盖 AC-006、AC-010、AC-011。
7. Harness 门禁修正：补充 `--spec` 只能指向 `docs/specs/active/` 的脚本约束、流程说明和测试。覆盖 AC-007。
8. Harness 模板同步：同步项目接入初始化下发模板中的流程说明、检查脚本和自测，并用 `get_harness_template` 单测锁定模板内容。覆盖 AC-008。
9. 归档收尾规范：同步 MCP 合并归档指令、全局 skill、本地 Harness 流程和模板，明确收到归档、办结或结束任务指令时先迁移 `active -> done` 再合并分支。覆盖 AC-009。
10. 多客户端安装结果修复：按官方文档校准 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 的安装边界；OpenCode/CodeBuddy 可解析 JSON 配置自动合并，Trae/Qoder 或无法自动合并的场景输出手工导入清单。覆盖 AC-012。
11. 合并归档验证接口：新增只读归档验证结果接口，复用办结前逐仓平台验证口径，供前端互斥展示确认归档完成。覆盖 AC-013。
12. 返修问题说明接口：新增专用返修提交接口，保存需求人返修问题说明并禁止普通状态接口直接进入返修。覆盖 AC-014。
13. 验证：运行后端目标测试、前端静态检查、构建、harness 校验和 diff 检查。覆盖 AC-001、AC-002、AC-003、AC-004、AC-005、AC-006、AC-007、AC-008、AC-009、AC-010、AC-011、AC-012、AC-013、AC-014。

## 分层验证

| 层级 | 覆盖验收 | 命令或方式 |
|---|---|---|
| L2 | AC-001、AC-002、AC-003、AC-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqflowCodexSetupPackageTemplateTest,ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L2 | AC-012 | `mvn -pl ruoyi-requirement -am -Dtest=ReqCodexInstallControllerTest,ReqflowCodexSetupPackageTemplateTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L2 | AC-010 | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L2 | AC-002、AC-005、AC-011 | companion 前端 `node scripts/test-mcp-install-dialog-unified.js` |
| L2 | AC-007 | `sh scripts/test-check-harness.sh` |
| L2 | AC-008 | `sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh`；`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L2 | AC-009、AC-013 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest,ReqflowCodexGlobalSkillTemplateTest,McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L2 | AC-014 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` |
| L1 | AC-005 | companion 前端 `npm run build:prod` |
| L0 | AC-006、AC-007、AC-008、AC-009、AC-010、AC-011、AC-013、AC-014 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-022-MCP-Key明文持久与交互安装` |

## 风险与处理

- 历史 Key 无法从哈希恢复明文：文档明确升级前旧数据需要重新生成 Key。
- 明文持久化提高泄漏风险：页面不展示明文/前缀/哈希，操作日志继续关闭创建响应保存，`toString()` 不输出 `plainKey`。
- `curl | bash` 交互读取 stdin 风险：Shell 脚本从 `/dev/tty` 读取选择；无交互终端时提示传 `--client`。
- MCP 请求地址填完整 URL 的风险：管理员配置接口只接受 host/port，完整地址由服务端拼接并回显。
- OpenCode 或 CodeBuddy 配置文件为 JSONC 且含注释时自动合并可能失败：脚本输出 `Manual MCP import required` 和片段路径，避免误报已安装。
- 合并归档验证读取失败或未通过：只读接口返回 `verified=false` 和原因，前端继续展示合并归档指令，状态流转接口仍负责最终兜底。
- 返修问题说明被绕过：普通状态接口拒绝 `review -> repairing`，必须通过专用 `/repair` 接口写入说明后再进入返修。
