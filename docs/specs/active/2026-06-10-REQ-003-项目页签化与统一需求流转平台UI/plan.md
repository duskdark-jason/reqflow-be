# 项目页签化与统一需求流转平台UI 后端执行计划

## 输入文件

- 需求说明：`requirement.md`
- 后端契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 数据库关系说明：`docs/db/relationship.md`
- 后端领域说明：`docs/domains/requirement-platform/README.md`
- 前端 companion：`../reqflow-ui/docs/specs/active/2026-06-10-REQ-003-项目页签化与统一需求流转平台UI`
- 目标客户与基线分支：通用 / main

## 方案选择

使用服务端可解析、可审计、可停用的统一动作 token 服务，先支持项目分支初始化动作，同时预留需求编排和开发执行动作类型。

本需求固定采用新增轻量 `req_action_token` 表：复制内容中的明文 token 使用安全随机数生成，服务端只保存 SHA-256 哈希、token 前缀、动作类型、项目、分支、需求、状态、过期时间和最近使用时间。这样能满足“加密唯一 token”、可复制指令、后续需求 token 和开发 token 同模型实现，以及停用和审计需求。

## 实施步骤

1. 失败用例先行：新增 `ReqActionTokenServiceImplTest`，先写失败测试，覆盖唯一 token、动作类型、项目分支上下文、停用 token、非法 token、需求 token 和开发 token 动作类型，覆盖 AC-BE-001、AC-BE-002、AC-BE-003、AC-BE-004。
2. Token 数据模型：新增 `ReqActionToken` 领域对象、`ReqActionInstruction` DTO、`ReqActionTokenMapper`、`ReqActionTokenService`、`ReqActionTokenServiceImpl` 和 SQL 迁移 `sql/req_platform_req003_action_token.sql`，覆盖 AC-BE-002、AC-BE-003。
3. 初始化指令 DTO：扩展 `ReqProjectInitVariantItem`，增加 `initInstruction` 或 `instructions` 字段，包含 `actionType`、`token`、`prompt`、`content`、`copyLabel`、`expireTime`，覆盖 AC-BE-001。
4. 项目初始化上下文集成：修改 `ReqProjectInitServiceImpl`，为每个项目分支生成或读取初始化动作 token，并构造复制指令内容，覆盖 AC-BE-001、AC-BE-002。
5. MCP 解析入口：扩展独立 token 解析服务并接入 `McpService` 需要的上下文解析点；`publish_repository_index` 保持 `mcpKey + remoteUrl` 兼容，并增加 `token + remoteUrl` 兼容，覆盖 AC-BE-003、AC-BE-004。
6. 需求提交收束：修改 `ReqDemandServiceImpl`，新增和修改需求时校验目标项目分支已初始化完成，覆盖 AC-BE-007。
7. 品牌清理：修改 `application.yml`、Swagger 配置、启动输出、banner、SQL 初始化数据中用户可见的若依品牌，保留底层包名和框架兼容项，覆盖 AC-BE-005。
8. Harness 更新：同步 `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/relationship.md`、`docs/domains/requirement-platform/README.md`，覆盖 AC-BE-006、AC-BE-007。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqActionToken.java` | 动作 token 领域对象 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqActionInstruction.java` | 前端复制指令响应 DTO |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitVariantItem.java` | 项目分支返回初始化指令字段 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 构造项目分支初始化指令 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqActionTokenService.java` | token 生成与解析接口 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | token 生成、哈希、解析和指令构造 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqActionTokenMapper.java` | token Mapper 接口 |
| 新增 | `ruoyi-requirement/src/main/resources/mapper/requirement/ReqActionTokenMapper.xml` | token 持久化映射 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java` | token 服务单元测试 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 项目初始化上下文返回指令字段测试 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java` | `token + remoteUrl` 索引导入兼容测试 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 需求提交分支初始化完成校验 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImplTest.java` | 需求提交分支初始化校验测试 |
| 新增 | `sql/req_platform_req003_action_token.sql` | token 表 SQL 迁移 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | token 上下文解析和兼容入口 |
| 修改 | `ruoyi-admin/src/main/resources/application.yml`、`ruoyi-admin/src/main/resources/banner.txt` | 系统名称和启动可见信息 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/core/config/SwaggerConfig.java` | Swagger 用户可见标题 |
| 修改 | `sql/req_platform_menu.sql`、`sql/ry_20260417.sql` | 用户可见菜单或初始化品牌文案，执行阶段谨慎处理历史基线 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 后端契约同步 |
| 修改 | `docs/db/relationship.md` | 数据库关系同步 |
| 修改 | `docs/domains/requirement-platform/README.md` | 领域说明同步 |

## 任务级执行清单

1. 在 `ReqActionTokenServiceImplTest` 写 `createsProjectInitInstructionWithUniqueTokenAndHashOnlyStorage`，期望生成 `project_init` 指令、明文 token 以 `reqflow_action_` 开头、Mapper 保存哈希和前缀。
2. 运行 `mvn -pl ruoyi-requirement -Dtest=ReqActionTokenServiceImplTest test`，确认因类或方法不存在失败。
3. 实现 `ReqActionToken`、`ReqActionInstruction`、Mapper、Service 和 SQL，使第 1 个测试通过。
4. 在 `ReqActionTokenServiceImplTest` 写 `rejectsDisabledOrUnknownToken` 和 `supportsRequirementAndDevelopActionTypes`，确认服务能解析合法 token 且拒绝异常 token。
5. 运行同一测试类，确认新测试先失败再实现通过。
6. 在 `ReqProjectInitServiceImplTest` 写项目分支返回 `initInstruction` 的失败测试，断言 `content` 同时包含简短提示词和 token，且 `mcpKey` 仍存在。
7. 修改 `ReqProjectInitVariantItem` 和 `ReqProjectInitServiceImpl` 注入 token 服务，构造初始化指令并保持 `mcpKey` 兼容。
8. 在 `ReqRepositoryIndexServiceImplTest` 写 `importsByActionTokenAndRemoteUrl`，断言 token 解析出的项目和分支可替代 `mcpKey` 定位上下文。
9. 修改 `ReqRepositoryIndexImportRequest` 和 `ReqRepositoryIndexServiceImpl`，增加 `actionToken` 入参解析，保留旧 `mcpKey` 路径。
10. 在 `ReqDemandServiceImplTest` 写未初始化分支拒绝提交和已初始化分支允许提交测试，确认服务层不依赖前端过滤。
11. 修改 `ReqDemandServiceImpl`，按所选 `projectId + variantId` 校验分支归属、分支模块知识和真实分支仓库索引覆盖。
12. 清理用户可见品牌：更新 `application.yml` 的系统名称、Swagger 标题、启动输出、banner 和 SQL 中面向新安装环境的若依菜单或示例组织文案。
13. 更新后端 harness 和领域文档，明确 token 不替代人员 Key，`X-MCP-Key` 仍负责认证。
14. 运行 L2、L1、L0 验证，并按运行态条件执行 L3 冒烟。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`；完成态使用 `sh scripts/check-harness.sh complete --spec docs/specs/active/2026-06-10-REQ-003-项目页签化与统一需求流转平台UI`。
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`。
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`，重点覆盖 token 生成解析、项目初始化上下文字段和 MCP 兼容。
- L3 运行态冒烟：按 `docs/runbooks/local-run.md` 启动后端，调用项目初始化详情和 MCP 入口，验证 token 指令字段、权限边界和品牌可见输出。
- L4 跨端/端到端：前端联调项目维护页、初始化指令复制、索引导入兼容和首页看板；如当前环境无法联调，在执行报告记录启动命令、错误摘要和后续补验方式。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 初始化指令 DTO、项目初始化上下文集成 | 单元测试、接口冒烟 |
| AC-BE-002 | Token 数据模型、项目初始化上下文集成 | 单元测试、接口冒烟 |
| AC-BE-003 | Token 数据模型、MCP 解析入口、契约文档 | 单元测试、文档检查 |
| AC-BE-004 | MCP 解析入口 | 单元测试、权限冒烟 |
| AC-BE-005 | 品牌清理 | 文案搜索、启动输出检查 |
| AC-BE-006 | Harness 更新 | `sh scripts/check-docs.sh` 和 harness 完成态检查 |
| AC-BE-007 | 需求提交收束 | `ReqDemandServiceImplTest`、接口冒烟 |

## 执行约束

- 本计划获认可后仍只代表计划阶段完成；开始实现必须另有明确执行授权。
- Execution Agent 必须从 `main` 创建 ASCII 任务分支，建议 `feature/REQ-20260610-003-project-tabs-ui`，不得直接在 `main` 实现。
- 新增 token 表时必须提供 SQL 迁移和 Mapper 测试，不得只在 DTO 中拼假 token。
- token 不能替代 `X-MCP-Key` 人员鉴权，也不能绕过 `req:*` 权限校验。
- 用户可见品牌清理不能扩大成 Java 包名迁移或大规模脚手架重命名。
- Execution Agent 不得自我 Review；进入 Review 必须有明确 Review 授权或独立 Review 请求。
