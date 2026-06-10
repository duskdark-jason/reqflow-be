# 项目页签化与统一需求流转平台UI 后端执行报告

## 执行摘要

已新增统一动作 token 模型和服务，项目初始化上下文会为每个项目分支返回 `initInstruction`，复制内容包含简短提示词、目标 MCP 方法和加密唯一 `actionToken`。索引导入新增 `actionToken + remoteUrl` 解析路径，并保留旧 `mcpKey + remoteUrl` 兼容。后端用户可见品牌已调整为“统一需求流转平台”。

本次补充增加需求提交收束点：需求新增和修改时，后端按所选 `projectId + variantId` 校验分支归属、分支模块知识和该真实分支的仓库索引覆盖；未初始化完成的项目分支即使绕过前端也不能写入 `req_demand`。

- 分支：`feature/REQ-20260610-003-project-tabs-ui`
- 已合入：`feature/REQ-20260610-004-mcp-public-url-config` 的后端配置提交，当前无冲突
- 提交：`e516a08`（feat: 增加项目初始化动作token）、`7820ec4`（feat: 收束需求提交分支初始化校验）
- Review 阶段：未授权，未写 Review 报告，未将 spec 切换为 complete

## 修改内容

- `ReqActionToken`、`ReqActionInstruction`、`IReqActionTokenService`、`ReqActionTokenServiceImpl`、`ReqActionTokenMapper` 和 Mapper XML：新增动作 token 数据模型、生成、哈希落库和解析能力。
- `ReqProjectInitVariantItem`、`ReqProjectInitServiceImpl`：项目初始化上下文的分支行返回 `initInstruction`，同时保留 `mcpKey` 兼容字段。
- `ReqRepositoryIndexImportRequest`、`ReqRepositoryIndexServiceImpl`：索引导入支持 `actionToken`，校验动作类型和目标方法后解析项目、分支与仓库。
- `ReqActionTokenServiceImplTest`、`ReqProjectInitServiceImplTest`、`ReqRepositoryIndexServiceImplTest`：覆盖 token 生成解析、指令字段和 actionToken 索引导入。
- `ReqDemandServiceImpl`、`ReqDemandServiceImplTest`：新增需求提交分支初始化完成校验，覆盖未初始化分支拒绝和已初始化分支允许保存。
- `sql/req_platform_req003_action_token.sql`、`sql/req_platform_schema.sql`：新增 `req_action_token` 建表结构。
- `sql/req_platform_req003_brand_cleanup.sql`：提供可执行的用户可见若依数据清理 SQL。
- `application.yml`、`RuoYiApplication.java`、`SwaggerConfig.java`、`banner.txt`：调整后端可见系统名称、启动输出、Swagger 标题和 banner。
- `docs/ai-harness/**`、`docs/db/**`、`docs/domains/**`：同步动作 token、初始化指令、索引导入、需求提交收束和品牌边界契约。

## 模块知识库沉淀

- 影响模块：需求管理/项目管理、需求管理/项目接入中心、需求管理/分支知识库详情、需求管理/需求列表、需求管理/MCP 管理。
- 模块知识库动作：更新。
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`。
- 无需更新原因：不适用。

## 验收覆盖

| 验收项 | 结果 |
|---|---|
| AC-BE-001 | 通过。项目初始化上下文每个分支返回 `initInstruction`，并保留 `mcpKey` 兼容字段。 |
| AC-BE-002 | 通过。指令内容包含触发提示词、`targetMethod` 和唯一 `actionToken`；服务端只保存 token 哈希和前缀。 |
| AC-BE-003 | 通过。`req_action_token` 统一支持 `project_init`、`requirement_plan`、`requirement_develop` 动作类型。 |
| AC-BE-004 | 通过。人员 `X-MCP-Key` 仍负责认证和权限，`actionToken` 仅负责动作上下文定位，接口契约已明确二者不能互相替代。 |
| AC-BE-005 | 通过。后端用户可见系统名称、启动输出、Swagger、banner 和清理 SQL 已调整为统一需求流转平台语义。 |
| AC-BE-006 | 通过。后端 harness 和领域文档已同步指令 token、品牌清理边界、`actionToken + remoteUrl` 优先路径和旧 `mcpKey` 兼容策略。 |
| AC-BE-007 | 通过。新增/修改需求会拒绝未初始化完成的项目分支；服务测试覆盖拒绝和允许两条路径。 |

## 验证记录

| 层级 | 命令或检查 | 结果 |
|---|---|---|
| L0 | `git diff --check` | 通过；仅有 CRLF/LF 换行提示，无空白错误。 |
| L0 | `sh scripts/check-docs.sh` | 通过。 |
| L0 | `sh scripts/check-harness.sh init` | 通过。 |
| L2 | `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过；25 个测试，0 失败。 |
| L2 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过；3 个测试，0 失败。 |
| L2 | `mvn -pl ruoyi-requirement -am test` | 通过；51 个测试，0 失败。 |
| L2 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过；admin 可打包。 |

## 运行态证据

- 执行目录：后端仓库根目录
- 启动命令：本轮未启动完整后端服务，采用 service 测试和 admin 打包覆盖 token、索引解析和配置装配。
- 检查命令：`mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`
- 错误摘要：上述命令均通过；未出现编译、测试或打包错误。
- 当前执行 agent 环境：本机 Maven/JDK 环境；未连接测试库执行 REST/MCP 真实调用，本轮不宣称跨端联调完成。

## 后续风险

- 需要在目标数据库执行 `sql/req_platform_req003_action_token.sql`，既有环境如需清理可见若依数据再执行 `sql/req_platform_req003_brand_cleanup.sql`。
- `actionToken` 是动作上下文，不替代人员 `X-MCP-Key` 鉴权；后续 MCP 客户端文档需要继续区分两类 token。
- 本次没有做 Review 阶段；如要进入 complete，需要用户授权 Review Agent 产出 `review-report.md` 后再运行完成态 harness。
