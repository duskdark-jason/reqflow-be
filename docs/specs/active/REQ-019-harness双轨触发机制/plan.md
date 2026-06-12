# harness双轨触发机制执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求平台 harness 模板、项目接入初始化流程、需求执行流程、前端 harness 同步
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 脚本测试先行：修改 `ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh`，新增 AC-004 的失败夹具。
2. 门禁实现：修改模板、当前后端和当前前端 `scripts/check-harness.sh`，校验 `search-map.md`、`searchMap` 入口、模块初始化占位、本地模式伪造 MCP 回写，以及需求设计确认前不得生成执行计划。
3. 模板源头：新增模板 `docs/ai-harness/search-map.md`，更新 `files.txt`、模板 `harness-index.json`、模板 README、流程规范、文档治理和 ai-harness 模板。
4. 触发机制：更新 `ReqflowCodexGlobalSkillTemplate`、`McpService` 的项目初始化 skill 文案和动态模块文档，补充双轨同构和短索引要求。
5. 当前项目同步：新增当前后端和前端 `docs/ai-harness/search-map.md`，更新当前 `harness-index.json`、流程文档、模块文档、模板文档和检查脚本。
6. 验证闭环：运行脚本测试、文档检查、harness init/complete 检查，并在执行报告记录结果。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/resources/harness-template/docs/ai-harness/search-map.md` | 模板功能导航 |
| 新增 | `docs/ai-harness/search-map.md` | 当前后端功能导航 |
| 修改 | `ruoyi-requirement/src/main/resources/harness-template/files.txt` | 下发功能导航模板 |
| 修改 | `ruoyi-requirement/src/main/resources/harness-template/docs/**` | 模板流程、触发机制、拆分规则 |
| 修改 | `ruoyi-requirement/src/main/resources/harness-template/scripts/check-harness.sh` | 模板门禁 |
| 修改 | `ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh` | 模板门禁测试 |
| 修改 | `scripts/check-harness.sh`、`scripts/test-check-harness.sh` | 当前仓库门禁同步 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP 下发说明与动态模块模板 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 全局 skill 触发和流程说明 |
| 修改 | `docs/ai-harness/**`、`docs/process/**` | 当前后端 harness 同步 |
| 修改 | `../reqflow-ui/docs/ai-harness/**`、`../reqflow-ui/docs/process/**`、`../reqflow-ui/scripts/**` | 当前前端 harness 同步 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，记录双轨 harness、短索引和模板源头维护要求。
- 新增 `docs/ai-harness/search-map.md` 作为低 token 导航。

## 代码注释计划

- `McpService` 已有模板下发注释；如新增非直观规则，在动态模板生成附近补充中文注释。
- `ReqflowCodexGlobalSkillTemplate` 为纯字符串模板，逻辑直接，通常无需新增注释。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh init`、`sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-019-harness双轨触发机制`
- L1 编译/构建：`mvn -pl ruoyi-requirement -am -DskipTests compile`
- L2 单元/契约：`sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh`、`sh scripts/test-check-harness.sh`、前端 `sh scripts/test-check-harness.sh`
- L3 运行态冒烟：不适用，本次不改运行态接口、页面、权限或配置。
- L4 跨端/端到端（可选）：不适用，本次为文档、模板和脚本治理。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-001 | 模板源头 | `rg "searchMap|search-map" ruoyi-requirement/src/main/resources/harness-template` |
| AC-002 | 流程文档 | `rg "本地 Harness 模式|MCP 接入模式|伪造 MCP 回写" docs ruoyi-requirement/src/main/resources/harness-template/docs` |
| AC-003 | 确认点门禁 | `sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh`、`sh scripts/test-check-harness.sh`、前端 `sh scripts/test-check-harness.sh` |
| AC-004 | 拆分规则 | `rg "拆分|决策|search-map" docs ruoyi-requirement/src/main/resources/harness-template/docs ../reqflow-ui/docs` |
| AC-005 | 脚本测试 | `sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh`、`sh scripts/test-check-harness.sh`、前端 `sh scripts/test-check-harness.sh` |
| AC-006 | 当前项目短索引 | `test -f docs/ai-harness/search-map.md && test -f ../reqflow-ui/docs/ai-harness/search-map.md` |

## 执行约束

- 本计划由 Execution Agent 基于最终 `requirement.md` 生成；用户已明确确认执行。
- 当前分支为 `docs/req-019-harness-dual-mode`，不得在 `main` 直接实现。
- 完成修改和验证后直接 commit，并在 `execution-report.md` 记录 commit、验证命令和结果。
- Execution Agent 不得自我 Review；执行完成后交给 Review Agent 或等价独立审查角色复核。
