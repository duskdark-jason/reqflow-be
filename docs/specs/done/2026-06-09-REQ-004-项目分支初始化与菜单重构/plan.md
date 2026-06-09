# 项目分支初始化与菜单重构后端执行计划

## 影响文件

- `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java`：新增纯后端初始化和 MCP key 红测。
- `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java`：新增 `mcpKey + remoteUrl` 导入红测。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqVariant.java`：增加 `mcpKey` 字段。
- `ruoyi-requirement/src/main/resources/mapper/requirement/ReqVariantMapper.xml`：映射、查询、插入、更新 `mcp_key`。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitVariantItem.java`：返回 `mcpKey`。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqRepositoryIndexImportRequest.java`：支持 MCP key 参数。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java`：放宽仓库校验、生成 MCP key、更新初始化检查项。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java`：按 `mcpKey + remoteUrl` 解析索引导入上下文。
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`：读取 MCP key 参数。
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java`：收敛项目初始化权限。
- `sql/req_platform_schema.sql`、`sql/req_platform_menu.sql`：同步字段和菜单。
- `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/relationship.md`、`docs/domains/requirement-platform/README.md`：同步契约和领域语义。

## 执行步骤

1. 写后端失败测试，覆盖纯后端初始化、仓库就绪口径、MCP key 返回和 MCP key 导入，覆盖 AC-BE-001、AC-BE-002、AC-BE-003、AC-BE-004。
2. 运行目标测试，确认因旧强校验或缺字段失败，覆盖 AC-BE-001 至 AC-BE-004。
3. 增加 `mcpKey` 字段映射、DTO 和 SQL，覆盖 AC-BE-003、AC-BE-007。
4. 调整项目初始化 Service：至少一条仓库即可，生成稳定 `mcpKey`，返回分支 key，覆盖 AC-BE-001、AC-BE-002、AC-BE-003。
5. 调整 MCP 索引导入：当传入 `mcpKey` 时解析项目分支，并用 `remoteUrl` 在同项目内定位仓库，覆盖 AC-BE-004。
6. 调整 Controller 权限和菜单 SQL，覆盖 AC-BE-005、AC-BE-006。
7. 更新后端 harness、数据库关系和领域文档，覆盖 AC-BE-007。
8. 运行 `mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`、`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec docs/specs/done/2026-06-09-REQ-004-项目分支初始化与菜单重构`，覆盖 AC-BE-001 至 AC-BE-007。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec docs/specs/done/2026-06-09-REQ-004-项目分支初始化与菜单重构`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：后端启动后验证初始化上下文查询、未登录拦截、登录态纯后端项目保存和 MCP key 回显。
- L4 跨端/端到端：与前端 companion 联调项目维护弹窗、项目接入中心和 MCP 索引导入。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 项目初始化 Service 放宽仓库要求 | Service 测试 |
| AC-BE-002 | checklist 仓库就绪新口径 | Service 测试 |
| AC-BE-003 | `mcpKey` 字段、生成和返回 | Service 测试、编译 |
| AC-BE-004 | MCP key 导入解析 | Repository index Service 测试 |
| AC-BE-005 | Controller 权限收敛 | Admin 打包 |
| AC-BE-006 | 菜单 SQL 下线旧菜单 | SQL diff、文档检查 |
| AC-BE-007 | harness 和数据库文档同步 | L0 文档和 harness 检查 |
