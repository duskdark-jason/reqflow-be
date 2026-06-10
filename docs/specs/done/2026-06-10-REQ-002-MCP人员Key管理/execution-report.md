# MCP人员Key管理执行报告

## 执行摘要

已按计划实现后端 MCP 人员 Key 管理能力：

- 新增 `req_mcp_user_key` 表、迁移 SQL、领域对象、Mapper、Service 和单元测试。
- 新增 `/requirement/mcp/key/**` 管理接口，支持列表、详情、配置、用户选项、创建、修改、重置和删除。
- 调整 `/requirement/mcp`，支持登录态或请求头 `X-MCP-Key` 鉴权，并继续复用 `McpService` 内部 tool 级权限校验。
- 新增 MCP 管理菜单 SQL 和 `req:mcp:key:list/query/add/edit/remove` 权限点。
- Review 返修后，创建/重置接口不再把明文 Key 响应写入操作日志，更新接口拒绝换绑用户，绑定用户删除后 Key 鉴权失效。
- 同步后端 API 契约、数据库关系和安全边界文档。

已获得用户授权自动 Review 和通过后办结，本报告同步最终返修与验证证据。

## 文件变更

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqMcpUserKey.java` | 人员 MCP Key 领域对象，`keyHash` 不参与 JSON 输出 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 创建/重置后的一次性明文响应 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserOption.java` | MCP Key 可绑定用户选项，只返回用户基础标识 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqMcpUserKeyMapper.java` | MCP Key Mapper 接口 |
| 新增 | `ruoyi-requirement/src/main/resources/mapper/requirement/ReqMcpUserKeyMapper.xml` | MCP Key MyBatis SQL |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqMcpUserKeyService.java` | MCP Key 服务接口 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | Key 生成、哈希、重置、停用校验和鉴权 |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | MCP Key 管理接口 |
| 新增 | `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 校验创建/重置接口不保存明文响应日志 |
| 修改 | `ruoyi-admin/pom.xml` | 增加 admin 模块测试依赖 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | 接入 `X-MCP-Key`，并做 MCP 粗权限校验 |
| 修改 | `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` | 允许 `/requirement/mcp` 进入 Controller 内部鉴权 |
| 修改 | `sql/req_platform_schema.sql`、`sql/req_platform_menu.sql` | 新增表和菜单权限 |
| 新增 | `sql/req_platform_req006_mcp_user_key.sql` | MCP 人员 Key 迁移脚本 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | Key 生成、哈希和鉴权测试 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/relationship.md` | 同步契约和表关系 |

## 验收覆盖

- AC-BE-001：已在 `req_platform_menu.sql` 和 `req_platform_req006_mcp_user_key.sql` 增加 MCP 管理菜单及 `req:mcp:key:*` 权限点；管理接口均使用权限注解；本地 `ry-vue` 迁移后 `sys_menu` 存在 5 条 MCP 菜单/按钮记录。
- AC-BE-002：`ReqMcpUserKeyServiceImplTest.createsRandomKeyAndStoresOnlyHashAndPrefix` 覆盖随机明文、哈希入库、前缀展示和一次性响应；`ReqMcpKeyControllerTest` 覆盖创建/重置接口不保存明文响应日志。
- AC-BE-003：服务支持同一用户多 Key，停用 Key 在 `authenticate` 中拒绝；删除接口物理删除 Key；更新接口禁止换绑用户；绑定用户停用或删除后拒绝鉴权。
- AC-BE-004：`/requirement/mcp` 支持 `X-MCP-Key` 构造绑定用户登录上下文，粗权限和 `McpService` 细权限共同生效。
- AC-BE-005：已更新 `docs/ai-harness/contracts/requirement-platform-api.md` 和 `docs/db/relationship.md`。

## 验证命令

| 层级 | 命令 | 结果 |
|---|---|---|
| TDD Red | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 返修前按预期失败：删除用户仍可认证、更新接口可换绑用户 |
| TDD Red | `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 返修前按预期失败：创建/重置接口 `@Log.isSaveResponseData` 为 `true` |
| TDD Green | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，8 tests，0 failures，0 errors |
| TDD Green | `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，1 test，0 failures，0 errors |
| L2 | `mvn -pl ruoyi-requirement -am test` | 通过，42 tests，0 failures，0 errors |
| L1 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，`ruoyi-admin` 打包成功 |
| L0 | `sh scripts/check-docs.sh` | 通过，输出“文档检查通过” |
| L3 数据库 | `jshell` 连接本地 `ry-vue` 执行 `sql/req_platform_req006_mcp_user_key.sql` | 迁移前 MCP 菜单 0 条；执行 8 条语句后 MCP 菜单/按钮 5 条，菜单 id 2028 |

## 未执行项

- L3/L4 HTTP 运行态联调未执行：本轮未启动后端服务，尚未通过登录态和 `X-MCP-Key` 调真实 `/requirement/mcp` 接口。当前已完成单元、编译、打包、文档检查和本地数据库菜单迁移验证。
- `sh scripts/check-harness.sh complete --spec ...` 将在最终 Review 报告更新后执行。

## Review 返修记录

- 已修复创建/重置响应明文 Key 进入操作日志的风险：`@Log(isSaveResponseData = false)`，并新增 Controller 反射测试。
- 已修复更新接口可换绑用户的风险：Service 读取原记录并拒绝 `userId` 变更，Mapper 更新语句不再更新 `user_id`。
- 已修复删除用户仍可通过 Key 鉴权的风险：绑定用户必须 `status='0'` 且 `delFlag='0'`。
- 已修复前端依赖 `/system/user/list` 的权限边界：新增 `/requirement/mcp/key/user-options`，前端改为调用 MCP 管理自己的用户选项接口。
