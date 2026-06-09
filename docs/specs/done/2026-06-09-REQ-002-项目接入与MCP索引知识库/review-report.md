# 项目接入与 MCP 索引知识库后端 Review 报告

## Review 结论

- 结论：有条件通过
- Review Agent：Codex Review Agent
- Review 时间：2026-06-09

## 审查输入

- `requirement.md`
- `plan.md`
- `execution-report.md`
- 代码 diff
- 静态代码审查输出

## 问题清单

以下问题清单保留首次 Review 发现；最终处理状态以“复审记录”和“最终结论”为准。

| 严重级别 | 文件 | 问题 | 风险 | 建议 |
|---|---|---|---|---|
| 阻断 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java:20`、`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java:106` | MCP 统一入口仍只校验 `req:package:save`，但 `publish_repository_index` 会写入索引批次、模块知识、影响面和活动日志，没有校验 `req:index:import`。 | 拥有 Agent 资料保存权限的用户可绕过索引导入权限发布仓库索引，不满足 AC-BE-005 的权限控制要求。 | 增加 tool 级权限校验；`publish_repository_index` 必须要求 `req:index:import`，原有执行资料类工具仍使用 `req:package:save`。补充无权限调用返回错误的测试。 |
| 阻断 | `ruoyi-requirement/src/main/resources/mapper/requirement/ReqImpactItemMapper.xml:51`、`ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqImpactSuggestQuery.java:7`、`sql/req_platform_schema.sql:202` | 影响面推荐没有实际使用 `variantId`，`req_impact_item` 也没有客户线或基线分支字段；同时 `selectLatestImpactItems` 只是按 `impact_id desc` 返回历史记录，没有限定所选客户基线或最新索引批次。 | 不同地区客户线、不同分支、旧 commit 的页面和接口会混入推荐结果，需求创建时无法可信地索引到“所选功能模块会影响的页面、接口等信息”，不满足 AC-BE-003、AC-BE-004。 | 在索引影响面数据中沉淀客户线或从批次/模块关系可追溯到客户基线；推荐查询必须消费 `variantId`，按目标仓库、基线分支和最新批次收敛，并对同一资源去重。补充多批次、多客户线测试。 |
| 重要 | `docs/specs/active/2026-06-09-REQ-002-项目接入与MCP索引知识库/execution-report.md:31` | L3 MCP/REST 冒烟和 L4 跨端联调未执行。 | Controller、权限、MCP JSON-RPC 调用和前端联动仍缺少运行态证据，不能进入完成态。 | 返修后启动后端或测试环境，至少验证 MCP `publish_repository_index` 权限、索引批次查询、影响面推荐接口；跨端联调可作为完成前补验。 |

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-BE-001 | 提供 `publish_repository_index` MCP tool | `McpService.java` 新增 tool 和导入调用 | 单元测试记录通过；Review 静态确认 tool 存在 | 通过，但受 RF-001 权限问题阻断 |
| AC-BE-002 | 拒绝个人本机绝对路径 | `ReqRepositoryIndexServiceImpl.java` 路径校验；单元测试覆盖 Linux 用户目录形式 | 单元测试记录通过 | 有条件通过，建议补 Windows/macOS 变体和嵌套字段测试 |
| AC-BE-003 | 按仓库、分支、commit 记录批次并更新模块知识库 | 新增三张索引表和导入服务 | 单元测试记录通过 | 失败，推荐链路未按最新批次或基线分支使用批次版本 |
| AC-BE-004 | 按项目和模块返回影响面推荐 | `ReqIndexController.suggest`、`selectLatestImpactItems` | 单元测试仅覆盖分组 | 失败，未消费客户线/基线，且历史批次混入 |
| AC-BE-005 | 权限控制、参数校验和审计事件 | REST Controller 有 `req:index:*`，Service 有审计事件 | 单元测试覆盖审计调用 | 失败，MCP 入口可绕过 `req:index:import` |
| AC-BE-006 | 后端 harness 文档同步 | API、DB、领域文档已更新 | 执行报告记录 L0 通过 | 通过 |

## 验收复核

- MCP tool 发布入口：失败，缺少 tool 级 `req:index:import` 权限。
- 本机绝对路径边界：有条件通过，核心校验存在但建议扩展测试。
- 客户基线与最新索引批次：失败，推荐查询未按客户线、分支和最新批次收敛。
- 文档同步：通过。
- 运行态证据：未验证。

## 返修交接清单

| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |
|---|---|---|---|---|---|
| RF-001 | 阻断 | AC-BE-005 | MCP `publish_repository_index` 通过 `req:package:save` 即可调用。 | 为 MCP tools 增加按 tool name 的权限校验，`publish_repository_index` 必须校验 `req:index:import`；无权限时返回 MCP error，不写数据库。 | 增加/更新 MCP 单元测试覆盖有权限和无权限；运行 `mvn -pl ruoyi-requirement -am test`。 |
| RF-002 | 阻断 | AC-BE-003, AC-BE-004 | 推荐查询不区分客户线/基线分支，也不限定最新索引批次。 | 补齐影响面与客户线或基线分支的关联，推荐接口消费 `variantId`，按目标基线最新批次返回并去重；同步 SQL、DTO、文档和前端契约。 | 增加多客户线、多批次推荐测试；运行 `mvn -pl ruoyi-requirement -am test` 和 `mvn -pl ruoyi-admin -am -DskipTests package`。 |
| RF-003 | 重要 | AC-BE-001, AC-BE-004, AC-BE-005 | 缺少运行态 MCP/REST/权限冒烟。 | 返修后补充 L3 验证记录；如当前 agent 环境无法启动，记录启动命令、profile/env、检查命令和错误摘要。 | 运行后端并验证 MCP tool、`/requirement/index/batch/list`、`/requirement/index/impact/suggest` 权限和响应；更新 `execution-report.md`。 |

## 复审记录

| 修复 ID | 执行处理结果 | 复审结论 | 复审证据 |
|---|---|---|---|
| RF-001 | 已修复：MCP tool 级权限校验已补齐，`publish_repository_index` 使用 `req:index:import`，执行资料和 harness 登记使用 `req:package:save`。 | 通过 | `McpServiceTest` 覆盖有权限、无权限；`mvn -pl ruoyi-requirement -am test` 13 个测试通过 |
| RF-002 | 已修复：推荐链路消费 `variantId`，按客户基线分支和最新 imported 批次返回并去重；SQL、DTO、DDL 和文档已同步。 | 通过 | `ReqRepositoryIndexServiceImplTest` 覆盖客户基线推荐；`mvn -pl ruoyi-admin -am -DskipTests package` 通过 |
| RF-003 | 已处理：后端 jar 在 18080 启动成功，目标 REST/MCP 路由均到达安全过滤链并返回未登录 401。 | 有条件通过 | 当前执行 agent 未持有登录 token，业务态响应需在具备测试账号/token 的环境补验 |

## 最终结论

有条件通过。阻断项 RF-001、RF-002 已修复并有单元测试、编译和文档证据；RF-003 已补充应用启动和安全链路冒烟。剩余条件是登录态 MCP/REST 业务响应和跨端推荐内容需在具备测试账号/token 的环境继续补验。

## 关闭备注

用户已决定结束当前版本。当前版本按 MCP 索引和知识库闭环归档；后台项目管理菜单完整初始化体验未达到最初设想，应作为下一版独立需求处理。

## Review 分级说明

- 阻断：需求不可用、数据错误、安全/权限风险、迁移风险、缺少关键验收证据、无证据环境结论。
- 重要：契约不一致、关键测试缺口、计划承诺的运行态或增强验证未完成且影响真实流程。
- 一般：命名、文档清晰度、低风险维护建议。
