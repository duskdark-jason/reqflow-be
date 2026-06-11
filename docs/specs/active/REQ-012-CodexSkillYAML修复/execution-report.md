# Codex Skill YAML 修复执行报告

## 执行摘要

已修复平台生成的 `reqflow-mcp/SKILL.md` 模板 frontmatter。`name` 和 `description` 现在使用双引号包裹，`description` 改为只描述触发条件，并移除未转义的 `: ` 片段，避免 Codex 启动扫描 skill 时因 YAML 解析失败而跳过该 skill。

## 修改内容

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 修复全局 `reqflow-mcp` skill 模板 frontmatter，生成合法 YAML。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplateTest.java` | 新增模板回归测试，使用 SnakeYAML 解析 frontmatter，并校验关键触发词。 |
| `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqCodexInstallControllerTest.java` | 扩展 shell/PowerShell 安装脚本测试，校验脚本内嵌的 skill frontmatter 使用加引号格式。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 同步安装包测试断言，校验新的加引号 skill 名称。 |
| `docs/ai-harness/modules/requirement-platform.md` | 补充全局 skill 模板 frontmatter 必须保持合法 YAML 的维护约束。 |

## TDD 记录

- Red：先新增 `ReqflowCodexGlobalSkillTemplateTest`，运行定向 Maven 测试，SnakeYAML 报错 `mapping values are not allowed here`，复现 `description` 中 `mcpServer: reqflow` 导致 frontmatter 无法解析的问题。
- Green：修复模板后重新运行定向测试，`ReqflowCodexGlobalSkillTemplateTest` 和 `ReqCodexInstallControllerTest` 均通过。
- 回归：运行 `mvn -pl ruoyi-requirement -am test` 时发现旧断言仍期待 `name: reqflow-mcp`，已收窄为新的合法 YAML 输出 `name: "reqflow-mcp"`，重新运行后通过。

## 影响范围

- 接口/API：接口路径和字段不变；`/requirement/codex/install.sh`、`/requirement/codex/install.ps1` 返回的内嵌 skill 文本变更。
- 数据库/SQL：无影响。
- 权限：无影响。
- 页面展示：无直接影响。
- MCP 行为：不自动调用任何 MCP tool；仅修复 Codex 可识别的全局 skill 模板。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqflowCodexGlobalSkillTemplateTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，3 个测试无失败。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，63 个测试无失败。 |
| `sh scripts/check-docs.sh && sh scripts/check-harness.sh init --spec docs/specs/active/REQ-012-CodexSkillYAML修复 && git diff --check` | 通过，文档检查通过，Harness 检查通过（init 模式），空白检查无输出。 |
