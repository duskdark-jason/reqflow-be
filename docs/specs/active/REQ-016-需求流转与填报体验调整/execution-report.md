# 需求流转与填报体验调整执行报告

## 执行结论

- 状态：执行完成
- 分支：feature/req-016-demand-flow-ux
- commit：本分支提交记录为准

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 新增需求覆盖客户端编号/创建人，生成 `REQ-001` 风格编号，默认 `draft`，限制非草稿和非创建人修改。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransition.java` | 调整主状态流转为提需、资料生成、确认、开发、验收、办结。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqDemandService.java` | 增加需求 MCP 编排指令服务方法。 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqDemandController.java` | 新增 `/requirement/demand/{demandId}/plan-instruction`，编辑时注入当前用户 ID。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 支持 `actionToken` 解析需求上下文，允许 MCP 回写需求说明和执行计划。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqDemandMapper.java`、`ReqDemandMapper.xml` | 编号统计改为全量需求计数，不按日期生成。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImplTest.java`、`ReqDemandStatusTransitionTest.java`、`McpServiceTest.java` | 覆盖编号、创建人、草稿编辑、状态主路径、MCP actionToken 回写。 |
| `docs/ai-harness/modules/requirement-platform.md`、`docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md` | 同步 API、模块、状态和编号语义。 |

## 模块知识库沉淀

- 影响模块：需求管理/需求接口、需求管理/需求执行包、MCP动作Token、需求状态流转
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：无
- SQL 脚本路径：无
- 数据库文档路径：docs/db/table-dictionary.md、docs/db/relationship.md
- 数据库变更说明：无
- 无需更新原因：不修改表结构或 SQL，仅更新状态枚举和编号语义文档。

## 代码注释处理

- 注释动作：新增必要注释
- 注释文件：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java`
- 处理说明：MCP 编排指令中保留一处短说明，强调 `actionToken` 只识别需求上下文，不替代人员 `X-MCP-Key` 鉴权。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-005 | `sh scripts/check-docs.sh` | 通过 |
| L1 | AC-001~AC-004 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过 |
| L2 | AC-001~AC-004 | `mvn -pl ruoyi-requirement -am test` | 通过，75 个测试通过。 |
| L3 | AC-004 | 登录后调用 `/dev-api/requirement/demand/{id}/plan-instruction` | 通过，接口返回 `code=200`，指令包含 `save_requirement_package`、`save_development_plan` 和需求 ID。 |
| L4（可选） | AC-001~AC-004 | 真实新增/状态流转端到端写操作 | 未执行；为避免污染本地已有业务数据，本次使用单测覆盖写入语义。 |

## 运行态证据

- 执行目录：当前子仓库根目录
- 启动命令：复用本机已运行 RuoYi 后端服务，前端 dev server 通过 `/dev-api` 代理调用。
- profile/env/mode：本地开发环境。
- 检查命令：`mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`、`git diff --check`
- 原始错误摘要：无后端编译或测试失败；曾因 MCP actionToken 未接入包保存工具触发失败用例，已实现并复验通过。
- screenshot/trace 路径：前端 companion 联调截图 `../reqflow-ui/docs/specs/active/REQ-016-需求流转与填报体验调整/artifacts/target-req016-detail.png`
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：本地或测试环境

## 验收覆盖

| 验收 ID | 执行结果 | 证据 |
|---|---|---|
| AC-001 | 已完成 | 单测覆盖 `REQ-003`、`REQ-004`、`REQ-007` 风格编号，不含日期。 |
| AC-002 | 已完成 | 单测覆盖默认 `draft`、创建人来自当前用户、客户端创建人被覆盖。 |
| AC-003 | 已完成 | 状态机单测覆盖新主路径和兼容路径。 |
| AC-004 | 已完成 | 单测和接口冒烟覆盖 MCP 编排指令及 `actionToken` 回写。 |
| AC-005 | 已完成 | API、模块、表字典和关系文档已同步，`check-docs` 通过。 |

## 计划偏差

- MCP 编排指令没有修改 `ReqActionTokenServiceImpl` 的通用模板，而是在需求服务中生成面向需求说明和执行计划的专用指令文本。
- 同步扩展 MCP 包保存工具的 `actionToken` 解析能力，让审批人员复制指令后可以不手填 `demandId`。

## Review 返修记录

- 无需返修；占位 RF-001 已由真实复核报告替换，不存在遗留返修项。

## 风险与后续

- 新编号基于当前需求总数递增；如果线上已经存在旧日期编号，新建数据会从当前总数后继续生成 `REQ-###`，历史编号不迁移。
- 角色级按钮权限仍沿用现有 `req:demand:*`，未在后端新增审批人员/开发人员细分权限。
