# MCP人员Key管理 Review 报告

## Review 结论

结论：通过

此前复审发现的安全阻断项已返修并验证通过。当前实现满足后端验收要求，可以按用户授权进入自动办结收口。

## 审查范围

- 后端管理接口：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- MCP 鉴权入口：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java`
- 安全配置：`ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`
- Key 服务与数据访问：`ReqMcpUserKey`、`ReqMcpUserKeyCreateResult`、`ReqMcpUserOption`、`ReqMcpUserKeyMapper`、`IReqMcpUserKeyService`、`ReqMcpUserKeyServiceImpl`、`ReqMcpUserKeyMapper.xml`
- SQL：`sql/req_platform_schema.sql`、`sql/req_platform_menu.sql`、`sql/req_platform_req006_mcp_user_key.sql`
- 测试与文档：`ReqMcpUserKeyServiceImplTest`、后端 API 契约、数据库关系、spec 与 execution-report

## 发现问题

未发现新的阻断或重要问题。

剩余风险：

- L3/L4 真实后端 HTTP 联调未执行，尚未通过登录态和 `X-MCP-Key` 调真实 `/requirement/mcp` 接口。该风险已在 `execution-report.md` 记录，不影响当前代码静态审查结论。
- `req:mcp:key:query` 与 `req:mcp:key:edit` 分离，实际角色配置时应同时给维护人员分配查询权限，否则编辑弹窗读取详情会被后端拒绝；当前菜单 SQL 已创建查询权限点，属于角色配置注意事项。

## 验收覆盖

- AC-BE-001：通过。菜单 SQL 已新增 MCP 管理菜单和 `req:mcp:key:list/query/add/edit/remove` 权限点；管理接口均使用 RuoYi 权限注解；本地 `ry-vue` 迁移后确认 5 条 MCP 菜单/按钮记录。
- AC-BE-002：通过。服务生成 `reqflow_mcp_` 随机 Key，落库 `key_hash` 与 `key_prefix`，明文只在 `ReqMcpUserKeyCreateResult` 返回；创建和重置接口关闭操作日志响应保存，测试覆盖明文不入库和不写响应日志。
- AC-BE-003：通过。表设计允许同一 `user_id` 多 Key；停用 Key 在 `authenticate` 拒绝，删除接口删除 Key；PUT 更新不能换绑用户；绑定用户停用或删除后 Key 鉴权失效。
- AC-BE-004：通过。`/requirement/mcp` 支持 `X-MCP-Key`，鉴权后构造绑定用户 `LoginUser` 和权限集合，进入 `McpService` 后继续执行 tool 级权限校验。
- AC-BE-005：通过。后端契约和数据库关系已记录人员 Key、MCP 入口鉴权和安全边界。

## 验证评估

已执行验证覆盖后端核心逻辑与编译：

- `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：8 tests 通过。
- `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`：1 test 通过。
- `mvn -pl ruoyi-requirement -am test`：42 tests 通过。
- `mvn -pl ruoyi-admin -am -DskipTests package`：通过。
- `sh scripts/check-docs.sh`：通过。
- 本地 `ry-vue` 数据库已执行 `req_platform_req006_mcp_user_key.sql`：MCP 菜单/按钮从 0 条变为 5 条。

未执行真实后端服务启动和 MCP HTTP 联调，建议在部署或本地联调阶段补验。

## 是否允许自动办结

允许自动办结。当前 Review 无阻断项和返修项。
