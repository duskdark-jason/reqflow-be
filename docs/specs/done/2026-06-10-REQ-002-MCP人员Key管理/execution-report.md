# MCP人员Key管理执行报告

## 执行摘要

已按计划实现后端 MCP 人员 Key 管理能力：

- 新增 `req_mcp_user_key` 表、迁移 SQL、领域对象、Mapper、Service 和单元测试。
- 新增 `/requirement/mcp/key/**` 管理接口，支持列表、详情、配置、创建、修改、重置和删除。
- 调整 `/requirement/mcp`，支持登录态或请求头 `X-MCP-Key` 鉴权，并继续复用 `McpService` 内部 tool 级权限校验。
- 新增 MCP 管理菜单 SQL 和 `req:mcp:key:list/query/add/edit/remove` 权限点。
- 同步后端 API 契约、数据库关系和安全边界文档。

当前角色仍为 Execution Agent；Review 授权未获得，未执行自我 Review。

## 文件变更

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqMcpUserKey.java` | 人员 MCP Key 领域对象，`keyHash` 不参与 JSON 输出 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 创建/重置后的一次性明文响应 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqMcpUserKeyMapper.java` | MCP Key Mapper 接口 |
| 新增 | `ruoyi-requirement/src/main/resources/mapper/requirement/ReqMcpUserKeyMapper.xml` | MCP Key MyBatis SQL |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqMcpUserKeyService.java` | MCP Key 服务接口 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | Key 生成、哈希、重置、停用校验和鉴权 |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | MCP Key 管理接口 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | 接入 `X-MCP-Key`，并做 MCP 粗权限校验 |
| 修改 | `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` | 允许 `/requirement/mcp` 进入 Controller 内部鉴权 |
| 修改 | `sql/req_platform_schema.sql`、`sql/req_platform_menu.sql` | 新增表和菜单权限 |
| 新增 | `sql/req_platform_req006_mcp_user_key.sql` | MCP 人员 Key 迁移脚本 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | Key 生成、哈希和鉴权测试 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/relationship.md` | 同步契约和表关系 |

## 验收覆盖

- AC-BE-001：已在 `req_platform_menu.sql` 和 `req_platform_req006_mcp_user_key.sql` 增加 MCP 管理菜单及 `req:mcp:key:*` 权限点；管理接口均使用权限注解。
- AC-BE-002：`ReqMcpUserKeyServiceImplTest.createsRandomKeyAndStoresOnlyHashAndPrefix` 覆盖随机明文、哈希入库、前缀展示和一次性响应。
- AC-BE-003：服务支持同一用户多 Key，停用 Key 在 `authenticate` 中拒绝；删除接口物理删除 Key。
- AC-BE-004：`/requirement/mcp` 支持 `X-MCP-Key` 构造绑定用户登录上下文，粗权限和 `McpService` 细权限共同生效。
- AC-BE-005：已更新 `docs/ai-harness/contracts/requirement-platform-api.md` 和 `docs/db/relationship.md`。

## 验证命令

| 层级 | 命令 | 结果 |
|---|---|---|
| TDD Red | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 实现前按预期失败，缺少 `ReqMcpUserKey`、`ReqMcpUserKeyMapper`、`ReqMcpUserKeyServiceImpl` 等类型 |
| TDD Green | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，5 tests，0 failures，0 errors |
| L2 | `mvn -pl ruoyi-requirement -am test` | 通过，39 tests，0 failures，0 errors |
| L1 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，`ruoyi-admin` 打包成功 |
| L0 | `sh scripts/check-docs.sh` | 通过，输出“文档检查通过” |

## 未执行项

- L3/L4 运行态联调未执行：本轮未启动前后端服务，也未连接真实数据库执行菜单 SQL 和接口调用。当前已完成单元、编译、打包和文档检查；后续可在本地启动后补验 `/requirement/mcp/key/**`、菜单权限和 `X-MCP-Key` 调 `/requirement/mcp`。
- 未运行 `sh scripts/check-harness.sh complete --spec ...`：当前 Review 授权未获得，active spec 保持 `executing` 状态；complete 模式要求完成态和 Review 报告，不适合由 Execution Agent 自行收口。

## Review 返修记录

未开始 Review；无返修项。
