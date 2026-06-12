# harness双轨触发机制执行报告

## 执行结论

- 状态：已完成
- 分支：docs/req-019-harness-dual-mode
- commit：本分支最终提交，见 `git log -1 HEAD`
- 流程模式：平台自身建设模式（按本地 Harness 流程闭环）
- MCP 回写：未接入 MCP，本地文件闭环

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/resources/harness-template/**` | 新增 `search-map.md`、`local-harness-workflow.md`，更新模板下发清单、流程文档、可复制模板和检查脚本。 |
| `scripts/check-harness.sh`、`scripts/test-check-harness.sh` | 同步当前后端门禁，覆盖 search-map、localHarnessWorkflow、初始化占位、本地伪造回写和需求设计确认点。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 动态 harness-index 增加 `searchMap` 与 `localHarnessWorkflow`，项目初始化说明要求维护搜索导航。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java` | 全局 skill 增加无 MCP 本地 Harness 触发和执行规则。 |
| `docs/ai-harness/**`、`docs/process/**`、`docs/templates/**`、`docs/specs/**` | 当前后端 harness 同步双轨流程、搜索导航、确认点和模板规则。 |
| `../reqflow-ui/**` | 前端 companion harness 同步，由前端仓库 spec 记录。 |

## 模块知识库沉淀

- 影响模块：需求平台 harness 模板、项目接入初始化流程、需求执行流程、前端 harness 同步
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`
- 搜索导航更新：已更新 `docs/ai-harness/search-map.md`
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：无
- SQL 脚本路径：无
- 数据库文档路径：无
- 数据库变更说明：无
- 无需更新原因：本次只修改 harness 文档、模板、脚本和 Java 字符串模板，不涉及持久化结构、SQL、Mapper 或数据口径。

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：本次 Java 变更集中在字符串模板和已有注释附近，业务约束已经沉淀到 harness 文档和脚本测试；没有新增复杂业务分支、权限边界或数据口径逻辑。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh init` | 通过 |
| L0 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 前端 `sh scripts/check-docs.sh && sh scripts/check-harness.sh init` | 通过 |
| L1 | AC-001、AC-002 | `mvn -pl ruoyi-requirement -am -DskipTests compile` | 通过 |
| L2 | AC-003、AC-005 | `sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh` | 通过 |
| L2 | AC-003、AC-005 | `sh scripts/test-check-harness.sh` | 通过 |
| L2 | AC-003、AC-005 | 前端 `sh scripts/test-check-harness.sh` | 通过 |
| L2 | AC-001、AC-002、AC-003、AC-004、AC-006 | `rg "本地 Harness 模式|需求设计确认点|伪造 MCP 回写|search-map|localHarnessWorkflow|searchMap" ...` | 通过 |
| L2 | AC-002、AC-003 | 旧术语和降级冲突描述扫描 | 无命中 |
| L0 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-019-harness双轨触发机制` | 通过 |
| L0 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 前端 `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-019-harness双轨触发机制` | 通过 |
| L3 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 不适用 | 本次不改运行态接口、页面、权限或配置 |
| L4（可选） | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 不适用 | 本次不改跨端运行态流程 |

## 运行态证据

- 执行目录：当前后端子仓库根目录、companion 前端子仓库根目录
- 启动命令：未启动服务
- profile/env/mode：本地文档和脚本治理
- 检查命令：见“验证结果”
- 原始错误摘要：无
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：不需要，本次无运行态变更

## 计划偏差

- 原计划只包含后端；根据用户补充要求，扩展为后端模板、当前后端 harness 和前端 harness 同步。
- 本地建设确认点从文档规则强化为脚本门禁：所有 `planning` 状态下提前出现 `plan.md` 都会失败。
- 补充清理 harness 规范冲突：统一使用“MCP 接入模式”命名，普通无 Key/无 MCP 场景固定进入本地 Harness 模式；平台自身建设模式支持需求平台自身、平台类治理能力和明确拷贝平台建设版本的项目本地自举；脚本提示同步为本地 Harness/平台自身建设模式都不得伪造 MCP 回写。

## Review 返修记录

无。

## 风险与后续

- 无剩余阻断风险。
- 后续如继续拆分大模块文档，应同步维护 `search-map.md` 并补充脚本测试。
