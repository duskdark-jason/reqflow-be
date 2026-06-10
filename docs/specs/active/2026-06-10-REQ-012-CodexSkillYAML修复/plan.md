# Codex Skill YAML 修复执行计划

## 执行步骤

1. TDD Red：新增或扩展后端测试，断言 `reqflow-mcp` skill frontmatter 可解析且字段完整，覆盖 AC-BE-001、AC-BE-002。
2. TDD Red：扩展安装脚本测试，断言 shell/PowerShell 脚本内嵌 skill frontmatter 已加引号，覆盖 AC-BE-003。
3. Green：修改 `ReqflowCodexGlobalSkillTemplate`，让 frontmatter 使用安全 YAML 字符串，避免未转义 `: `。
4. 更新 harness：同步 `docs/ai-harness/modules/requirement-platform.md` 的安装脚本风险说明。
5. 写执行报告，记录 Red/Green、影响范围和验证命令。
6. 验证并提交任务分支。

## 分层验证

| 层级 | 命令 | 覆盖 |
|---|---|---|
| L1 | `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqflowCodexGlobalSkillTemplateTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 模板和安装脚本定向测试 |
| L1 | `mvn -pl ruoyi-requirement -am test` | 后端需求模块回归 |
| L0 | `sh scripts/check-docs.sh` | 文档结构 |
| L0 | `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-012-CodexSkillYAML修复` | active spec 和 harness |
| L0 | `git diff --check` | 空白检查 |

## 验收映射

| 验收 ID | 验证方式 |
|---|---|
| AC-BE-001 | `ReqflowCodexGlobalSkillTemplateTest` |
| AC-BE-002 | `ReqflowCodexGlobalSkillTemplateTest` |
| AC-BE-003 | `ReqCodexInstallControllerTest` |
| AC-BE-004 | Maven、文档、harness 和空白检查 |
