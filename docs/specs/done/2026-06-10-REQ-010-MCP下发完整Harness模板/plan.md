# MCP下发完整Harness模板执行计划

## 实施步骤

1. TDD Red：扩展 `McpServiceTest#getHarnessTemplateToolReturnsWorkspaceAndRepositoryInstructions`，断言 MCP 返回完整流程文档、模板文档、测试脚本、自动 Review 规则和初始化索引状态，覆盖 AC-001、AC-002、AC-004。
2. 将 workspace 根目录完整 `harness-template/` 复制到 `ruoyi-requirement/src/main/resources/harness-template/`，并维护 `files.txt` 文件清单，覆盖 AC-003。
3. 调整 `McpService#getHarnessTemplate` 的文件生成逻辑，从 classpath 模板资源读取 `docs/**` 和 `scripts/**`，同时保留项目、仓库、分支上下文，覆盖 AC-001、AC-003。
4. 对 `AGENTS.md`、workspace `AGENTS.md`、`harness-index.json` 和非模板文档做初始化处理，避免目标仓库 init 校验失败，覆盖 AC-004。
5. 保持 `get_harness_template` 原有 `req:project:query` 权限校验并运行既有权限失败测试，覆盖 AC-005。
6. 更新需求平台长期 harness 文档和接口契约说明。
6. 运行后端文档检查、harness init、目标测试和相关 MCP 测试。

## 影响文件

| 动作 | 文件 | 原因 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP 从完整模板资源生成初始化包 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 补充完整模板返回断言 |
| 新增 | `ruoyi-requirement/src/main/resources/harness-template/**` | 存储可随后端发布的 harness 模板 |
| 更新 | `docs/ai-harness/modules/requirement-platform.md` | 沉淀 MCP 项目接入初始化规则 |
| 更新 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP 返回契约 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh init`。
- L2 单元/契约：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`。
- L2 定点回归：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest#getHarnessTemplateToolReturnsWorkspaceAndRepositoryInstructions -Dsurefire.failIfNoSpecifiedTests=false test`。
- L3/L4：本次只调整 MCP 返回包生成逻辑，不启动服务；如后续接入真实 MCP 客户端，再补 HTTP/MCP 冒烟。
