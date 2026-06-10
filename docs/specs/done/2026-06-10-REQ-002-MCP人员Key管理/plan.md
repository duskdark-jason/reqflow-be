# MCP人员Key管理执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-002-MCP人员Key管理/requirement.md`
- 后端契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 数据库关系：`docs/db/relationship.md`
- 当前 MCP 入口：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java`
- 当前 MCP 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- 菜单 SQL：`sql/req_platform_menu.sql`
- companion 前端 spec：`../reqflow-ui/docs/specs/active/2026-06-10-REQ-002-MCP人员Key管理`

## 实施步骤

1. 后端表与 Mapper：新增 `req_mcp_user_key` 表、迁移 SQL、Domain、Mapper 接口和 XML，覆盖 AC-BE-002、AC-BE-003。
2. 后端服务与测试：新增 Key 生成、哈希、唯一性重试、状态变更、重置和最近使用记录逻辑，先补单元测试，再实现服务，覆盖 AC-BE-002、AC-BE-003。
3. MCP 鉴权接入：调整 `/requirement/mcp` 允许 `X-MCP-Key` 鉴权，验证 Key 后构造绑定用户的安全上下文，继续复用 `McpService` 内部权限校验，覆盖 AC-BE-004。
4. 管理接口与菜单：新增 `ReqMcpKeyController` 和菜单权限 SQL，管理接口全部使用 `req:mcp:key:*`，覆盖 AC-BE-001。
5. Harness 更新：更新 API 契约、数据库关系和 MCP 安全边界说明，覆盖 AC-BE-005。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqMcpUserKey.java` | 人员 MCP Key 领域对象 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqMcpUserKeyMapper.java` | MCP Key Mapper 接口 |
| 新增 | `ruoyi-requirement/src/main/resources/mapper/requirement/ReqMcpUserKeyMapper.xml` | MCP Key SQL |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqMcpUserKeyService.java` | MCP Key 管理服务接口 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | Key 生成、哈希、鉴权和管理实现 |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | MCP Key 管理接口 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | 接入 `X-MCP-Key` 鉴权 |
| 修改 | `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` | 允许 `/requirement/mcp` 进入 Controller 内部 Key 鉴权 |
| 修改 | `sql/req_platform_schema.sql` | 增加 `req_mcp_user_key` 建表 |
| 新增 | `sql/req_platform_req006_mcp_user_key.sql` | 线上库迁移脚本 |
| 修改 | `sql/req_platform_menu.sql` | 增加 MCP 管理菜单和权限点 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | Key 生成、哈希和鉴权单元测试 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 覆盖 Key 鉴权后的权限行为，如需要 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 新增接口契约和 MCP Key 安全边界 |
| 修改 | `docs/db/relationship.md` | 新增表关系与数据粒度说明 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：启动后端后，用登录态验证 `/requirement/mcp/key/list` 权限控制；用创建出的 `X-MCP-Key` 调用 `/requirement/mcp` 的 `tools/list` 和一个需要权限的 tool。
- L4 跨端/端到端：与前端 companion 联调 MCP 管理页创建 Key、复制地址和使用 Key 调 MCP；如当前环境无法启动完整前后端，在执行报告记录启动命令、错误摘要和后续补验方式。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 管理接口与菜单 | SQL 检查、接口 403/200 权限冒烟 |
| AC-BE-002 | 后端服务与测试 | `ReqMcpUserKeyServiceImplTest` 验证明文只返回一次、哈希入库 |
| AC-BE-003 | 后端服务与测试 | 单元测试和接口冒烟验证停用/删除不可用 |
| AC-BE-004 | MCP 鉴权接入 | 单元测试和 `curl` 携带 `X-MCP-Key` 冒烟 |
| AC-BE-005 | Harness 更新 | `sh scripts/check-docs.sh` 和人工检查契约段落 |

## 执行约束

- 当前工作区已有未提交改动，执行阶段不得覆盖或回退既有改动。
- 开始实现前必须获得明确执行授权；如用户要求隔离开发，应先按 Git 工作流创建同名 ASCII 任务分支或 worktree。
- Key 明文不得写日志、不得写活动记录、不得进入数据库明文字段。
- `req_variant.mcp_key` 仍只表示项目分支识别 Key，不得复用为人员访问凭据。
- Execution Agent 不得自我 Review；实现完成后等待明确 Review 授权。
