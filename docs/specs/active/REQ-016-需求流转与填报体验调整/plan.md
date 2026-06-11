# 需求流转与填报体验调整执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求管理/需求接口、需求管理/需求执行包、MCP动作Token、需求状态流转
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 后端 TDD：先修改 `ReqDemandStatusTransitionTest` 和 `ReqDemandServiceImplTest`，让编号、默认状态、创建人覆盖和新状态主路径用例失败，覆盖 AC-001~AC-003。
2. 状态机实现：修改 `ReqDemandStatusTransition` 和 `ReqDemandServiceImpl`，实现不带日期编号、`draft` 默认状态和新流转，覆盖 AC-001~AC-003。
3. MCP 编排指令：扩展 `IReqDemandService`、`ReqDemandController`、`ReqDemandServiceImpl`，提供需求编排指令查询接口，指令内容采用初始化指令风格，覆盖 AC-004、AC-006。
4. 执行开发指令与返修：新增执行开发指令接口，支持 `upload_execution_report` 动作 token；增加 `review -> repairing -> review` 状态事件记录，覆盖 AC-007、AC-008。
5. Token 生命周期：统一 actionToken 24 小时有效期和一次性消费校验，覆盖 AC-009。
6. 文档同步：更新 API 契约、模块文档、表字典和关系说明，覆盖 AC-005。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransitionTest.java` | 状态机失败用例。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImplTest.java` | 编号、默认状态和创建人失败用例。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransition.java` | 新状态主路径。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 编号、创建人和状态实现。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqDemandService.java` | 指令查询服务方法。 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqDemandController.java` | 需求编排和执行开发指令查询接口。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 需求编排动作 token、执行开发动作 token 和返修事件记录。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java`、`ReqActionTokenMapper.xml` | actionToken 24 小时有效期、一次性消费和并发条件更新。 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md` | 长期契约同步。 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，记录编号、创建人、状态流转、返修分支、MCP 指令和 actionToken 生命周期。
- 更新 `docs/ai-harness/contracts/requirement-platform-api.md`，记录接口语义、状态流转、MCP 编排指令、执行开发指令和 actionToken 一次性/有效期。
- 更新 `docs/db/table-dictionary.md` 和 `docs/db/relationship.md`，记录状态枚举、编号语义、执行包版本链和 `expire_time`/`last_used_time` 边界，无 SQL 脚本。

## 代码注释计划

- 在状态机或需求编排指令生成处补充简短注释，说明兼容旧状态和动作 token 不替代人员 MCP Key。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：如本机后端可启动，验证需求接口；否则记录当前执行环境限制。
- L4 跨端/端到端（可选）：本次不强制，前端联调补验。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-001 | 后端 TDD、状态机实现 | `mvn -pl ruoyi-requirement -am test` |
| AC-002 | 后端 TDD、状态机实现 | `mvn -pl ruoyi-requirement -am test` |
| AC-003 | 后端 TDD、状态机实现 | `mvn -pl ruoyi-requirement -am test` |
| AC-004 | MCP 编排指令 | 单测或编译、前端调用冒烟 |
| AC-005 | 文档同步 | `sh scripts/check-docs.sh` |
| AC-006 | MCP 编排指令 | 单测断言初始化式指令字段 |
| AC-007 | 执行开发指令 | 单测和接口冒烟 |
| AC-008 | 返修状态流转 | 状态事件单测、文档复核 |
| AC-009 | Token 生命周期 | `ReqActionTokenServiceImplTest`、mapper 条件更新复核 |

## 执行约束

- 本需求使用任务分支 `feature/req-016-demand-flow-ux`，不在 `main` 直接实现。
- 不新增数据库表字段，不执行 SQL。
- 完成修改和验证后直接 commit；merge、push、rebase 仍需用户确认。
