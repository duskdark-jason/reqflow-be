# MCP多客户端安装支持执行报告

## 执行结论

- 状态：已完成
- 分支：feature/req-020-mcp-multi-client-setup
- commit：待提交
- 流程模式：本地 Harness 模式
- MCP 回写：未接入 MCP，本地文件闭环

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ReqflowCodexSetupPackageTemplate.java` | 输出 `reqflow-mcp-multi-client-setup`，顶层 `installCommands[]` 使用 `--client all` / `-Client "all"` 统一安装指令，`clientInstructions[]` 保留单客户端高级配置。 |
| `ReqCodexInstallController.java` | 新增 `/requirement/codex/skill/SKILL.md` 匿名只读端点。 |
| `ReqflowCodexInstallScriptTemplate.java` | 将 `/requirement/codex/install.sh` 和 `install.ps1` 升级为通用安装脚本，支持 `--client all`/`-Client "all"` 一次覆盖六类客户端，也支持单客户端安装，并调用 `npx skills add` 安装全局 skill。 |
| `ReqflowCodexGlobalSkillTemplate.java` | 全局 skill 包说明改为通用 Agent Skill，并记录 `npx skills add` 覆盖客户端。 |
| `ReqflowCodexSetupPackageTemplateTest.java`、`ReqMcpUserKeyServiceImplTest.java`、`ReqCodexInstallControllerTest.java` | 增加多客户端、OpenCode、`npx skills add` 和无明文泄漏断言。 |
| `../reqflow-ui/src/views/requirement/mcpKey/index.vue` | 结果弹窗只展示统一安装指令，不再按客户端分组展示。 |
| `docs/ai-harness/**`、`../reqflow-ui/docs/ai-harness/**` | 同步多客户端安装包结构、OpenCode 配置和 CodeBuddy 口径。 |

## 模块知识库沉淀

- 影响模块：MCP Key 安装包、全局 skill 安装、MCP 管理页展示
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`
- 搜索导航更新：已更新前端 `docs/ai-harness/search-map.md`
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：无
- SQL 脚本路径：无
- 数据库文档路径：`docs/db/README.md`（仅确认无需更新）
- 数据库变更说明：无
- 无需更新原因：本次只修改安装包模板、展示和文档，不涉及持久化结构、SQL、Mapper 或数据口径。

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：模板结构由测试覆盖，复杂度不需要额外注释。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L2 | AC-001、AC-002、AC-003、AC-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqflowCodexSetupPackageTemplateTest,ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，15 个测试通过 |
| L2 | AC-003、AC-004 | 生成 `install.sh` 后执行 `bash -n` 和 `--client all` 假 `npx` 冒烟 | 通过，1 条统一命令触发 6 次 `npx skills add`，agent 为 codex、claude-code、trae、qoder、codebuddy、opencode，Codex/OpenCode 配置写入校验通过 |
| L1 | AC-005 | 前端 `npm run build:prod` | 通过，存在历史体积告警 |
| L0 | AC-006 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/done/REQ-020-MCP多客户端安装支持` | 通过 |
| L3 | AC-001 到 AC-006 | 不适用 | 未启动服务，未执行真实客户端安装 |
| L4 | AC-001 到 AC-006 | 不适用 | 不涉及真实跨端安装闭环 |

## 运行态证据

- 执行目录：当前后端子仓库根目录、companion 前端子仓库根目录
- 启动命令：未启动服务
- profile/env/mode：本地模板、单测和构建验证
- 检查命令：见“验证结果”
- 原始错误摘要：无
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：如需真实客户端安装，应在用户本机分别用 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 验证。

## 计划偏差

- 用户补充客户端名称以 CodeBuddy 为准，已保留 CodeBuddy 作为唯一对应客户端。
- 用户追加 OpenCode，已补充 OpenCode MCP 配置和 `npx skills add -a opencode`。
- 用户要求前端只展示统一指令，已把顶层安装命令调整为 `all` 入口，前端不再按工具分组。

## Review 返修记录

无。
