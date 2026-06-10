# 全局 Reqflow MCP 技能安装执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-008-全局ReqflowMCP技能安装/requirement.md`
- MCP Key 控制器：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- MCP Key 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java`
- MCP Key 创建结果 DTO：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java`
- MCP 服务 skill 内容参考：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- 接口契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 模块 harness：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：补 `ReqMcpKeyControllerTest`，断言配置接口返回 `codexGlobalSkillPackage`，包含全局 skill 名称、作用域、文件清单、`SKILL.md` 内容和跨平台安装说明，覆盖 AC-BE-001、AC-BE-003、AC-BE-004。
2. TDD Red：补 `ReqMcpUserKeyServiceImplTest`，断言创建 Key 返回同样的全局 skill 包字段，且包内容和安装说明不包含明文 Key，覆盖 AC-BE-002。
3. TDD Green：新增共享模板类，集中生成 `reqflow-mcp` 全局 skill 包和符合 Codex skill 规范的 `SKILL.md`。
4. TDD Green：在 MCP 管理配置接口和 Key 创建/重置结果中返回全局 skill 包字段。
5. 清理：确认 `docs/specs/active` 下旧空目录已删除，覆盖 AC-BE-005。
6. 文档：更新接口契约和模块 harness，说明全局 skill 安装字段和使用边界。
7. 验证：运行相关单测、模块测试、admin 打包、文档检查、harness 检查和空白检查，覆盖 AC-BE-006。
8. 执行报告：记录修改、验证、接口影响和未涉及数据库的说明。

## 验证计划

- L2 指定测试：`mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 模块测试：`mvn -pl ruoyi-requirement -am test`
- L1 打包：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档：`sh scripts/check-docs.sh`
- Harness 当前阶段：`sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-008-全局ReqflowMCP技能安装`
- 空白检查：`git diff --check`

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 配置接口全局 skill 包字段 | `ReqMcpKeyControllerTest` |
| AC-BE-002 | Key 创建/重置全局 skill 包字段 | `ReqMcpUserKeyServiceImplTest` |
| AC-BE-003 | SKILL.md frontmatter 和触发条件 | `ReqMcpKeyControllerTest` |
| AC-BE-004 | reqflow MCP tool 顺序 | `ReqMcpKeyControllerTest` |
| AC-BE-005 | active 空目录清理 | `find docs/specs/active -maxdepth 2 -print` |
| AC-BE-006 | 回归验证 | Maven 测试、admin 打包、文档和 harness 检查 |
