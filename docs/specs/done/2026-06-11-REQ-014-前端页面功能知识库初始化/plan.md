# 前端页面功能知识库初始化执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 目标客户与基线分支：通用 / main
- 影响模块：需求管理、项目接入初始化、模块知识库
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 测试先行：补充 MCP 模板、tool schema 和索引导入的失败测试，覆盖 AC-BE-001 到 AC-BE-004。
2. 模板与 skill：调整 `McpService` 和 `ReqflowCodexGlobalSkillTemplate` 文本，使初始化明确按前端页面功能沉淀模块知识库。
3. 默认模块文档：把 `get_harness_template` 生成的非模板模块文档改为页面功能索引骨架。
4. 契约与 harness：更新接口契约和后端模块文档，记录新口径和风险。
5. 验证与报告：运行指定测试、文档检查、harness 检查和必要打包，回填执行报告。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 调整 MCP 下发模板、skill 资源和 tool schema 描述。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 同步全局 `reqflow-mcp` skill 的页面功能分析规则。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 覆盖模板和 schema 输出。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java` | 覆盖页面功能粒度索引导入。 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP 初始化和索引导入口径。 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步模块 harness 规则和验证建议。 |
| 新增 | `docs/specs/done/2026-06-11-REQ-014-前端页面功能知识库初始化/*` | 本需求过程文档。 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，把 MCP 项目接入初始化的模块知识库口径改为“前端页面业务功能优先”。
- 更新 `docs/ai-harness/contracts/requirement-platform-api.md`，明确 `modules` 的数据粒度和 `get_harness_template` 下发文件要求。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec docs/specs/done/2026-06-11-REQ-014-前端页面功能知识库初始化`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -Dtest=McpServiceTest,ReqRepositoryIndexServiceImplTest test`
- L3 运行态冒烟：本次不改 HTTP Controller、权限和运行态路径；以 MCP service 单测覆盖工具 schema 和 tool call 结构，打包覆盖集成编译。
- L4 跨端/端到端：不适用，本次不改前端页面，真实接入项目端到端需后续用项目初始化指令在目标 workspace 人工或自动执行。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 默认模块文档 | `McpServiceTest#getHarnessTemplateToolReturnsWorkspaceAndRepositoryFiles` |
| AC-BE-002 | skill 文本 | `McpServiceTest#readsReqflowProjectInitSkillResource` |
| AC-BE-003 | tool schema | `McpServiceTest#toolListExposesRepositoryIndexPublisherSchema` |
| AC-BE-004 | 索引导入 | `ReqRepositoryIndexServiceImplTest` 新增页面功能索引导入断言 |
| AC-BE-005 | 文档同步 | `sh scripts/check-docs.sh` 和 harness complete 检查 |

## 执行约束

- 不改数据库结构，不改 MCP tool 名称，不改 actionToken 作为 `arguments.actionToken` 的约定。
- 不改 reqflow-ui 业务页面；仅在后端契约中引用前端页面分析口径。
- 任务分支完成验证后直接 commit；merge、push、rebase 仍需用户确认。
