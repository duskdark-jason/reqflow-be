# Codex 安装指令包执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-009-Codex安装指令包/requirement.md`
- MCP Key 控制器：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- MCP Key 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java`
- MCP Key 创建结果 DTO：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java`
- 已有 skill 模板：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java`
- 接口契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 模块 harness：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：补 `ReqMcpKeyControllerTest`，断言配置接口返回 `codexSetupPackage`，包含安装包名称、作用域、MCP server、Codex 配置模板、skill 包、短提示词和 server metadata，覆盖 AC-BE-001 到 AC-BE-004。
2. TDD Red：补 `ReqMcpUserKeyServiceImplTest`，断言创建 Key 返回安装指令包，且包内容不包含明文 Key，覆盖 AC-BE-005。
3. TDD Green：新增模板类，集中生成 `reqflow-codex-setup` 安装包和 server metadata，并复用已有 `codexGlobalSkillPackage`。
4. TDD Green：在 MCP 管理配置接口和 Key 创建/重置结果中返回 `codexSetupPackage`。
5. 文档：更新接口契约和模块 harness，说明安装指令包字段、安装边界和安全约束。
6. 验证：运行相关单测、模块测试、admin 打包、文档检查、harness 检查和空白检查，覆盖 AC-BE-006。
7. 执行报告：记录修改、验证、接口影响和未涉及数据库的说明。

## 验证计划

- L2 指定测试：`mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 模块测试：`mvn -pl ruoyi-requirement -am test`
- L1 打包：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档：`sh scripts/check-docs.sh`
- Harness 当前阶段：`sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-009-Codex安装指令包`
- 空白检查：`git diff --check`

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 配置接口安装包字段 | `ReqMcpKeyControllerTest` |
| AC-BE-002 | MCP server 配置摘要 | `ReqMcpKeyControllerTest` |
| AC-BE-003 | 可复制短提示词 | `ReqMcpKeyControllerTest` |
| AC-BE-004 | server metadata 内容 | `ReqMcpKeyControllerTest` |
| AC-BE-005 | Key 创建/重置安装包字段 | `ReqMcpUserKeyServiceImplTest` |
| AC-BE-006 | 回归验证 | Maven 测试、admin 打包、文档和 harness 检查 |
