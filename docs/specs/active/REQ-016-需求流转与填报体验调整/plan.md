# 需求流转与填报体验调整执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求管理/需求接口、需求管理/需求执行包、MCP动作Token、需求状态流转、角色菜单权限、系统MCP工具链
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 后端 TDD：先修改 `ReqDemandStatusTransitionTest` 和 `ReqDemandServiceImplTest`，让编号、默认状态、创建人覆盖和新状态主路径用例失败，覆盖 AC-001~AC-003。
2. 状态机实现：修改 `ReqDemandStatusTransition` 和 `ReqDemandServiceImpl`，实现不带日期编号、`draft` 默认状态和新流转，覆盖 AC-001~AC-003。
3. 生成需求设计指令：扩展 `IReqDemandService`、`ReqDemandController`、`ReqDemandServiceImpl`，提供生成需求设计指令查询接口，指令内容采用初始化指令风格，覆盖 AC-004、AC-006。
4. 执行任务指令与返修：新增执行任务指令接口，支持 `save_development_plan` 和 `upload_execution_report` 动作 token；增加 `review -> repairing -> review` 状态事件记录，覆盖 AC-007、AC-008。
5. Token 生命周期：统一 actionToken 24 小时有效期和一次性消费校验，覆盖 AC-009。
6. 角色权限 SQL：新增幂等角色授权脚本，覆盖需求人员、开发人员和管理员角色边界，补充脚本契约测试，覆盖 AC-010。
7. MCP 阶段拆分：收窄 `requirement_plan` actionToken 只允许 `save_requirement_package`，执行阶段生成 `save_development_plan` 和 `upload_execution_report` 两个一次性 actionToken，覆盖 AC-011、AC-012。
8. 资料包读取权限：需求详情嵌入读取允许 `req:demand:query`，独立资料包菜单仍保留 `req:package:list`，覆盖 AC-013。
9. 需求填报字段与上传限制：新增 `demand_source`、`attachments` 字段和专用上传接口，执行包上下文同步来源、背景和附件，覆盖 AC-014。
10. 需求上下文权限与 Controller 迁移：将需求管理 Controller 迁入 `ruoyi-requirement` 模块，需求表单所需项目、分支、模块和索引模块只读接口接受需求权限，覆盖 AC-015。
11. 删除与流程隔离：新增 `req:demand:remove` 管理员删除接口和按钮权限，服务层按角色拦截具体状态动作，覆盖 AC-016。
12. 文档同步：更新 API 契约、模块文档、表字典和关系说明，覆盖 AC-005、AC-010~AC-016。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransitionTest.java` | 状态机失败用例。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImplTest.java` | 编号、默认状态和创建人失败用例。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransition.java` | 新状态主路径。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 编号、创建人和状态实现。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqDemandService.java` | 指令查询服务方法。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/ReqDemandController.java` | 生成需求设计、执行任务指令查询接口和管理员删除接口。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 需求编排动作 token、执行开发动作 token 和返修事件记录。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java`、`ReqActionTokenMapper.xml` | actionToken 24 小时有效期、一次性消费和并发条件更新。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP 工具与 actionToken 阶段边界。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/ReqPackageController.java` | 需求详情嵌入资料包读取权限。 |
| 迁移 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/**` | 将需求管理 Controller 从 admin 模块迁移到需求模块，需求页面只读上下文接口按需求权限放行。 |
| 新增 | `docs/db/sql/req_platform_req016_role_permissions.sql` | 平台三类角色菜单权限。 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqPlatformRoleSqlTest.java` | 角色 SQL 契约测试。 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md` | 长期契约同步。 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，记录编号、创建人、状态流转、返修分支、MCP 指令、角色权限和 actionToken 生命周期。
- 更新 `docs/ai-harness/contracts/requirement-platform-api.md`，记录接口语义、状态流转、生成需求设计指令、执行任务指令、角色权限和 actionToken 一次性/有效期。
- 更新 `docs/db/table-dictionary.md` 和 `docs/db/relationship.md`，记录状态枚举、编号语义、执行包版本链、角色权限 SQL 和 `expire_time`/`last_used_time` 边界。

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
| AC-004 | 生成需求设计指令 | 单测或编译、前端调用冒烟 |
| AC-005 | 文档同步 | `sh scripts/check-docs.sh` |
| AC-006 | 生成需求设计指令 | 单测断言初始化式指令字段 |
| AC-007 | 执行任务指令 | 单测和接口冒烟 |
| AC-008 | 返修状态流转 | 状态事件单测、文档复核 |
| AC-009 | Token 生命周期 | `ReqActionTokenServiceImplTest`、mapper 条件更新复核 |
| AC-010 | 角色权限 SQL | `ReqPlatformRoleSqlTest`、SQL 脚本复核 |
| AC-011 | 计划阶段只生成需求设计 | `ReqDemandServiceImplTest`、`McpServiceTest` |
| AC-012 | 执行阶段生成计划和报告 | `ReqDemandServiceImplTest`、`McpServiceTest` |
| AC-013 | 详情嵌入资料读取权限 | Controller 权限复核、前端构建 |
| AC-014 | 来源必填、附件和 2MB 上传限制 | `ReqDemandServiceImplTest`、`ReqDemandSchemaSqlTest`、`ReqDemandControllerUploadTest`、`RequirementTemplateServiceTest` |
| AC-015 | 需求列表上下文只读接口权限 | Controller 权限复核、端到端账号冒烟 |
| AC-016 | 管理员删除和流程角色隔离 | `ReqDemandServiceImplTest`、`ReqPlatformRoleSqlTest` |

## 执行约束

- 本需求使用任务分支 `feature/req-016-demand-flow-ux`，不在 `main` 直接实现。
- 新增 `demand_source`、`attachments` 字段脚本和角色权限脚本均为幂等升级脚本，本轮不直接连接本机库执行。
- 完成修改和验证后直接 commit；merge、push、rebase 仍需用户确认。
