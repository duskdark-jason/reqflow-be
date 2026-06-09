# 项目接入与 MCP 索引知识库后端执行报告

## 执行结论

- 状态：已完成
- 分支：feature/REQ-002-project-index-mcp
- commit：无，普通模式未提交

## 版本关闭说明

- 关闭时间：2026-06-09
- 用户接受：结束当前版本。
- 当前版本完成范围：MCP 索引推送、索引批次/模块/影响面知识库、客户基线推荐契约、基础项目接入中心入口。
- 未完成原始设想：后台项目管理菜单仍未形成“新增项目时一体化维护项目名称、前后端仓库、统一客户基线/分支、模块功能点初始化”的完整管理员工作流。当前实现只在项目接入中心补了仓库和客户基线维护入口，尚未做到项目管理菜单内的完整初始化体验。
- 后续建议：另起需求专门建设“项目管理初始化向导/菜单重构”，把项目基础信息、仓库、客户基线、模块功能点提取和初始化状态合并为管理员可连续操作的页面流程。

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `sql/req_platform_schema.sql` | 新增仓库索引批次、索引模块知识、模块影响面条目三张表 |
| `sql/req_platform_menu.sql` | 新增索引查询和索引导入权限 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/*` | 新增索引批次、模块知识和影响面领域对象 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/*` | 新增 MCP/REST 索引导入和影响面推荐 DTO |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/*` | 新增索引相关 Mapper 接口 |
| `ruoyi-requirement/src/main/resources/mapper/requirement/*` | 新增索引相关 MyBatis XML |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/*` | 新增索引导入和影响面推荐服务 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 增加 `publish_repository_index` MCP tool，并补齐 tool 级权限校验 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqIndexController.java` | 新增索引批次、模块知识、影响面推荐和备用导入接口 |
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | MCP Controller 粗授权改为执行资料保存或索引导入权限二选一，Service 继续做 tool 级细授权 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/**` | 新增索引导入、路径拒绝、推荐聚合和 MCP tool 单元测试 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 同步索引接口和 MCP tool 契约 |
| `docs/db/relationship.md` | 同步索引表关系、数据粒度和导入约束 |
| `docs/domains/requirement-platform/README.md` | 修正领域当前状态并补充项目索引闭环 |

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-BE-006 | `sh scripts/check-docs.sh` | 通过 |
| L0 | AC-BE-006 | `sh scripts/check-harness.sh complete --spec docs/specs/active/2026-06-09-REQ-002-项目接入与MCP索引知识库` | 通过 |
| L1 | AC-BE-001, AC-BE-004 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过 |
| L2 | AC-BE-001, AC-BE-002, AC-BE-003, AC-BE-004, AC-BE-005 | `mvn -pl ruoyi-requirement -am test` | 通过，13 个测试通过 |
| L3 | AC-BE-001, AC-BE-004, AC-BE-005 | `java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=druid,test --server.port=18080`；`curl -i -s http://localhost:18080/system/config/configKey/sys.index.skinName`；`curl -i -s 'http://localhost:18080/requirement/index/impact/suggest?projectId=1&variantId=1&moduleCode=demand'`；`curl -i -s -X POST http://localhost:18080/requirement/mcp ...` | 应用在 18080 启动成功；公开检查和目标接口均到达 Spring Security 并返回未登录 401 响应；无登录 token，未继续验证业务响应内容 |
| L4（可选） | AC-BE-004 | 与前端联调索引批次和影响面推荐 | 未执行；当前执行 agent 未持有可使用的登录态账号/token，避免对既有 8080 运行进程做状态性操作 |

## 运行态证据

- 执行目录：`reqflow-be` 仓库根目录
- 启动命令：`java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=druid,test --server.port=18080`
- profile/env/mode：`druid,test` profile，临时端口 `18080`
- 检查命令：`mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`、`curl -i -s http://localhost:18080/system/config/configKey/sys.index.skinName`、`curl -i -s 'http://localhost:18080/requirement/index/impact/suggest?projectId=1&variantId=1&moduleCode=demand'`、`curl -i -s -X POST http://localhost:18080/requirement/mcp ...`
- 原始错误摘要：按手册默认端口 8080 启动时端口已占用；改用 18080 后应用启动成功。未登录请求返回 RuoYi 认证失败 JSON，目标路由和安全过滤链可达；业务态响应需登录态补验。
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：具备测试账号/token 的本地或测试环境，继续执行 MCP/REST 登录态冒烟

## 计划偏差

- L3 已完成应用启动和未登录安全链路冒烟；登录态 MCP/REST 业务响应和 L4 跨端联调因当前执行 agent 未持有可使用的测试账号/token 未执行。

## Review 返修记录

| 修复 ID | 处理结果 | 说明 | 验证证据 |
|---|---|---|---|
| RF-001 | 已修复 | `publish_repository_index` 增加 `req:index:import` tool 级权限；执行资料保存和 harness 登记继续校验 `req:package:save`。 | `McpServiceTest` 新增无权限测试；`mvn -pl ruoyi-requirement -am test` 13 个测试通过 |
| RF-002 | 已修复 | 影响面和索引模块沉淀 `variantId`、`branchName`；推荐接口按 `variantId` 解析客户线基线分支，只取每个仓库最新 imported 批次并去重。 | `ReqRepositoryIndexServiceImplTest` 覆盖客户基线推荐；`mvn -pl ruoyi-admin -am -DskipTests package` 通过 |
| RF-003 | 已处理 | 补充 L3 启动和未登录安全链路冒烟；登录态业务响应需在具备测试账号/token 的环境补验。 | jar 在 18080 启动成功；目标 REST/MCP 路由返回 401 认证拦截 |

## 风险与后续

- MCP tool 当前不执行仓库扫描，只接收本地 agent 推送结果；真实索引器仍需后续接入。
- 当前执行 agent 未持有登录 token，未做登录态的索引导入和推荐响应断言；需要在确认测试账号后补一轮 L4。
