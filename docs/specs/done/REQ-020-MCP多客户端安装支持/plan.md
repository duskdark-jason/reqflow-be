# MCP多客户端安装支持执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- companion 前端文档：`../reqflow-ui/docs/ai-harness/modules/requirement-platform.md`
- 影响模块：MCP Key 安装包、全局 skill 安装、MCP 管理页展示
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 资料确认：查找并引用 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 和 `npx skills add` 的公开文档，确定 MCP 配置和 skill 安装方式。
2. 测试先行：补充后端安装包模板、服务创建结果和安装脚本控制器测试，覆盖 AC-001 到 AC-004。
3. 后端实现：扩展 `ReqflowCodexSetupPackageTemplate` 的多客户端结构，新增 `clientInstructions` 和 `supportedClients`，新增 `SKILL.md` 下载端点。
4. 前端实现：调整 `mcpKey/index.vue` 弹窗，按客户端展示 MCP 命令、配置片段和全局 skill 安装命令。
5. 文档同步：更新后端模块文档、后端接口契约、前端模块文档、前端 UI 契约和搜索导航。
6. 验证闭环：运行后端目标测试、前端生产构建和 harness 文档检查。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexSetupPackageTemplate.java` | 多客户端安装包模板 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 全局 Agent Skill 安装说明 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexInstallScriptTemplate.java` | Codex 便捷脚本默认 skill 路径 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/ReqCodexInstallController.java` | 新增 `SKILL.md` 内容端点 |
| 新增/修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/**` | 多客户端安装包测试 |
| 修改 | `../reqflow-ui/src/views/requirement/mcpKey/index.vue` | MCP Key 结果弹窗分组展示 |
| 修改 | `docs/ai-harness/**`、`../reqflow-ui/docs/ai-harness/**` | harness 文档同步 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md` 和 `docs/ai-harness/contracts/requirement-platform-api.md`。
- 更新前端 `docs/ai-harness/modules/requirement-platform.md` 和 `docs/ai-harness/contracts/requirement-platform-ui.md`。

## 代码注释计划

- 本次以模板结构和测试表达约束，暂无需要新增复杂代码注释的逻辑。

## 验证计划

- L0 文档/规范：后端和前端分别运行 `sh scripts/check-docs.sh`，必要时运行 `sh scripts/check-harness.sh complete --spec ...`。
- L1/L2 后端：`mvn -pl ruoyi-requirement -am -Dtest=ReqflowCodexSetupPackageTemplateTest,ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`。
- L1 前端：`npm run build:prod`。
- L3 运行态冒烟：不执行，本次未启动后端服务，也未进行真实客户端安装。
- L4 跨端/端到端：不执行，本次不覆盖真实客户端安装和登录态。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-001 | 后端模板 | 后端目标测试、`rg "codebuddy|opencode"` |
| AC-002 | 后端模板 | 后端目标测试、文档检查 |
| AC-003 | 后端模板 | 后端目标测试 |
| AC-004 | 后端模板 | 后端目标测试 |
| AC-005 | 前端实现 | `npm run build:prod` |
| AC-006 | 文档同步 | `sh scripts/check-docs.sh`、harness complete 检查 |

## 执行约束

- 当前分支为 `feature/req-020-mcp-multi-client-setup`。
- 不修改数据库和权限。
- 不把真实人员 Key 或 actionToken 写入模板、测试或文档。
