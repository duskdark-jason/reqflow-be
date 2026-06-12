# 需求流转与填报体验调整执行报告

## 执行结论

- 状态：执行完成
- 分支：feature/req-016-demand-flow-ux
- commit：本分支提交记录为准

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 新增需求覆盖客户端编号/创建人，生成 `REQ-001` 风格编号，默认 `draft`，限制非草稿和非创建人修改；生成初始化式 MCP 编排指令、执行开发指令并记录返修事件；按角色隔离状态动作并支持管理员删除清理。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqDemand.java`、`ReqDemandMapper.xml` | 增加指定开发人员字段回显、开发人员候选查询和非管理员参与人列表过滤。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandStatusTransition.java` | 调整主状态流转为提需、资料生成、确认、开发、验收、办结。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqDemandService.java` | 增加需求 MCP 编排指令、执行开发指令、开发人员候选和参与人校验服务方法。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/ReqDemandController.java` | 新增 `/requirement/demand/developer-options`、`/requirement/demand/{demandId}/plan-instruction`、`/requirement/demand/{demandId}/develop-instruction`、`/requirement/demand/upload` 和管理员删除接口，编辑时注入当前用户 ID，上传单文件限制 2MB。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/**` | 将需求管理 Controller 从 admin 模块迁移到需求模块；项目、分支、模块和索引模块只读上下文接口允许需求权限访问，管理类写接口仍保持原权限。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 支持 `actionToken` 解析需求上下文，允许 MCP 回写需求说明和执行计划。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java`、`ReqActionTokenMapper.xml` | actionToken 生成后写入 24 小时过期时间，成功解析后写入 `last_used_time`，过期、已使用或并发重复消费时拒绝。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqDemandMapper.java`、`ReqDemandMapper.xml` | 编号统计改为全量需求计数，不按日期生成，并读写需求来源和附件字段。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/**`、`templates/requirement/**` | 执行包上下文增加需求来源、纯文本业务背景和附件，JSON 模板对文本引号做转义。 |
| `docs/db/sql/req_platform_req016_demand_form_fields.sql` | 新增需求来源和附件字段幂等升级脚本。 |
| `docs/db/sql/req_platform_req017_demand_developer_lock.sql` | 新增指定开发人员字段和索引幂等升级脚本。 |
| `ruoyi-requirement/src/test/java/**` | 覆盖编号、创建人、草稿编辑、状态主路径、MCP actionToken 回写、需求来源必填、上传 2MB 限制、模板文本转义、角色动作隔离和管理员删除权限 SQL。 |
| `docs/ai-harness/modules/requirement-platform.md`、`docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md` | 同步 API、模块、状态和编号语义。 |

## 模块知识库沉淀

- 影响模块：需求管理/需求接口、需求管理/需求执行包、MCP动作Token、需求状态流转、返修版本记录、actionToken 生命周期、需求来源和附件上传、参与人锁定
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：是
- SQL 脚本路径：docs/db/sql/req_platform_req016_demand_form_fields.sql、docs/db/sql/req_platform_req017_demand_developer_lock.sql
- 数据库文档路径：docs/db/table-dictionary.md、docs/db/relationship.md
- 数据库变更说明：新增 `req_demand.demand_source`、`req_demand.attachments` 和 `req_demand.developer_user_id`；历史来源默认 `BUSINESS`，历史需求允许指定开发人员为空，新建和草稿编辑由服务层强制指定。
- 无需更新原因：不适用。

## 代码注释处理

- 注释动作：新增必要注释
- 注释文件：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java`
- 处理说明：MCP 编排指令中保留一处短说明，强调 `actionToken` 只识别需求上下文，不替代人员 `X-MCP-Key` 鉴权。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-005 | `sh scripts/check-docs.sh` | 通过 |
| L1 | AC-001~AC-017 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，Controller 迁移后 admin 整包编译成功。 |
| L1 | AC-013、AC-015、AC-017 | 在前端 companion 执行 `npm run build:prod` | 通过，仅保留既有资产体积 warning。 |
| L2 | AC-001~AC-017 | `mvn -pl ruoyi-requirement -am test` | 通过，108 个测试覆盖编号、创建人、状态、指令、返修事件、来源必填、上传 2MB 限制、角色动作隔离、删除链路、参与人锁定和 SQL 权限。 |
| L2 | AC-017 | 连接本机 `ry-vue` 执行 `docs/db/sql/req_platform_req017_demand_developer_lock.sql` 并查询字段/索引 | 通过，`req_demand.developer_user_id` 和 `idx_req_demand_developer` 已存在。 |
| L3 | AC-004、AC-006、AC-007、AC-009、AC-017 | 使用 `xqr/123456` 和 `yfr/123456` 调用需求接口流转 | 通过，xqr 创建 draft 并指定 yfr；yfr 提交前不可见，提交后可见；xqr 生成计划/开发指令被拒绝，yfr 可生成计划和开发指令，指令包含一次性 24 小时 token 规则。 |
| L4（可选） | AC-013、AC-015、AC-017 | 打开前端 companion `http://127.0.0.1:1024/requirement/demand` 和详情页 | 通过，xqr 需求列表展示开发人员 `研发人员（yfr）`，详情内嵌 Agent 交接资料包并展示当前需求标题和文档页签，xqr 不展示开发指令按钮。 |

## 运行态证据

- 执行目录：当前子仓库根目录
- 启动命令：复用本机已运行 RuoYi 后端服务，前端 dev server 通过 `/dev-api` 代理调用。
- profile/env/mode：本地开发环境。
- 检查命令：`mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`、前端 companion `npm run build:prod`、`git diff --check`
- 原始错误摘要：无后端编译或测试失败；曾因 MCP actionToken 未接入包保存工具、执行开发指令未实现、token 未限制重复使用触发失败用例，已实现并复验通过。
- 运行态 trace：`xqr/yfr` 接口流转测试生成本地需求 `REQ-001`，状态推进到 `developing`；前端 companion 详情页可见 Agent 交接资料包。
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：本地或测试环境

## 验收覆盖

| 验收 ID | 执行结果 | 证据 |
|---|---|---|
| AC-001 | 已完成 | 单测覆盖 `REQ-003`、`REQ-004`、`REQ-007` 风格编号，不含日期。 |
| AC-002 | 已完成 | 单测覆盖默认 `draft`、创建人来自当前用户、客户端创建人被覆盖。 |
| AC-003 | 已完成 | 状态机单测覆盖新主路径和兼容路径。 |
| AC-004 | 已完成 | 单测和 `yfr` 接口冒烟覆盖 MCP 编排指令及 `actionToken` 回写，`xqr` 获取指令被拒绝。 |
| AC-005 | 已完成 | API、模块、表字典和关系文档已同步，`check-docs` 通过。 |
| AC-006 | 已完成 | 单测断言指令包含 `reqflow-mcp`、`mcpServer`、`mcpTool`、`toolName`、`arguments.actionToken` 和非 `X-MCP-Key` 说明。 |
| AC-007 | 已完成 | 新增执行开发指令接口，单测和 `yfr` 接口冒烟断言目标工具包含 `reqflow.save_development_plan` 与 `reqflow.upload_execution_report`。 |
| AC-008 | 已完成 | 单测覆盖 `review -> repairing` 记录 `demand_repairing` 事件，返修回到验收记录 `demand_repair_submitted`。 |
| AC-009 | 已完成 | `ReqActionTokenServiceImplTest` 覆盖 24 小时 `expireTime`、过期拒绝、已使用拒绝和并发条件更新失败拒绝。 |
| AC-010 | 已完成 | `ReqPlatformRoleSqlTest` 覆盖需求人员、开发人员和管理员角色授权脚本。 |
| AC-011 | 已完成 | `ReqDemandServiceImplTest` 和 `McpServiceTest` 覆盖计划阶段只回写需求设计。 |
| AC-012 | 已完成 | `ReqDemandServiceImplTest` 和 `McpServiceTest` 覆盖执行阶段计划和执行报告两个 token。 |
| AC-013 | 已完成 | `ReqPackageController` 权限允许 `req:demand:query` 读取当前需求资料包。 |
| AC-014 | 已完成 | `ReqDemandServiceImplTest` 覆盖来源必填，`ReqDemandSchemaSqlTest` 覆盖字段脚本，`ReqDemandControllerUploadTest` 覆盖 2MB 上传限制，`RequirementTemplateServiceTest` 覆盖文本转义。 |
| AC-015 | 已完成 | `ReqProjectController`、`ReqVariantController`、`ReqModuleController` 和 `ReqIndexController` 的只读上下文接口接受需求权限；`xqr` 账号进入需求列表接口无权限不足；前端首页快捷入口按权限过滤。 |
| AC-016 | 已完成 | `ReqDemandServiceImplTest` 覆盖需求人员/开发人员状态动作互斥、管理员合法流转和删除关联数据；`ReqPlatformRoleSqlTest` 覆盖角色脚本不分配删除权限。 |
| AC-017 | 已完成 | `ReqDemandServiceImplTest` 覆盖未指定开发人员拒绝、指定开发人员可执行开发动作、非参与人拒绝读取；`xqr/yfr` 真实接口流转验证提交前后可见性、指令权限和同一开发人员进入开发中。 |

## 计划偏差

- MCP 编排指令没有修改 `ReqActionTokenServiceImpl` 的通用模板，而是在需求服务中生成面向需求说明和执行计划的专用指令文本。
- 同步扩展 MCP 包保存工具的 `actionToken` 解析能力，让开发人员复制指令后可以不手填 `demandId`。
- 根据用户返修反馈，额外增加初始化式指令字段、执行开发指令、返修状态事件和 actionToken 生命周期限制。

## Review 返修记录

| 修复 ID | 处理结果 | 说明 | 验证证据 |
|---|---|---|---|
| RF-002 | 已修复 | 已补充初始化式 MCP 指令字段、执行开发指令、返修状态事件和版本链文档。 | `mvn -pl ruoyi-requirement -am test` 通过；接口冒烟通过；`check-docs` 通过 |
| RF-003 | 已修复 | 已补充通用 actionToken 24 小时有效期、一次性消费校验、并发条件更新、指令文案和文档。 | `ReqActionTokenServiceImplTest` 通过；`mvn -pl ruoyi-requirement -am test` 通过 |

## 风险与后续

- 新编号基于当前需求总数递增；如果线上已经存在旧日期编号，新建数据会从当前总数后继续生成 `REQ-###`，历史编号不迁移。
- 状态推进接口仍沿用 `req:demand:edit` 和状态机约束；前端按角色过滤具体流程按钮。
- 新增管理员删除需求权限 `req:demand:remove`，删除时清理资料包版本和动作 token；需求人员和开发人员角色脚本不分配删除权限。
- 本轮已在本机 `ry-vue` 执行 `docs/db/sql/req_platform_req017_demand_developer_lock.sql`；部署其他环境时仍需按顺序执行 `docs/db/sql/req_platform_req016_demand_form_fields.sql` 和 `docs/db/sql/req_platform_req017_demand_developer_lock.sql`。
- 返修历史复用 `req_package_version` 追加版本，不新增返修表；如后续需要对每轮返修单独命名，可再扩展版本元数据。
