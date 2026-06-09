# 项目分支初始化与菜单重构后端执行报告

## 执行摘要

- 项目初始化保存从“必须前端仓库和后端仓库”调整为“至少一条有效代码仓库”，允许纯后端服务初始化。
- `req_variant` 增加 `mcp_key`，项目初始化分支返回 `mcpKey`，后端按 `项目编码:分支编码` 生成默认 key。
- `publish_repository_index` 支持 `mcpKey + remoteUrl` 定位项目分支和仓库，并兼容旧的 `projectId + repoId + branchName`。
- 项目初始化 Controller 权限收敛为 `req:project:add` 和 `req:project:edit`。
- 菜单 SQL 不再创建仓库管理、客户定制线、模块功能点菜单，并禁用已有环境中的旧菜单。
- 后端接口契约、数据库关系和领域文档同步新语义。

## 修改文件

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqVariantController.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/ReqVariant.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitVariantItem.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqRepositoryIndexImportRequest.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- `ruoyi-requirement/src/main/resources/mapper/requirement/ReqVariantMapper.xml`
- `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java`
- `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java`
- `sql/req_platform_schema.sql`
- `sql/req_platform_menu.sql`
- `sql/req_platform_req004_migration.sql`
- `docs/ai-harness/contracts/requirement-platform-api.md`
- `docs/db/relationship.md`
- `docs/domains/requirement-platform/README.md`

## 验证记录

- 命令：`mvn -pl ruoyi-requirement -am test`
- 状态：通过，24 个测试通过。
- 命令：`mvn -pl ruoyi-admin -am -DskipTests package`
- 状态：通过。

## 验收 ID 覆盖

| 验收 ID | 实现证据 | 验证证据 | 状态 |
|---|---|---|---|
| AC-BE-001 | `ReqProjectInitServiceImpl` 允许一条有效代码仓库 | 纯后端项目测试通过 | 通过 |
| AC-BE-002 | `repositoryReady` 改为至少一个有效仓库 | checklist 测试通过 | 通过 |
| AC-BE-003 | `ReqVariant.mcpKey`、DTO、Mapper、SQL 和生成逻辑 | MCP key 返回测试通过 | 通过 |
| AC-BE-004 | `ReqRepositoryIndexServiceImpl` 支持 `mcpKey + remoteUrl` | MCP key 导入测试通过 | 通过 |
| AC-BE-005 | `ReqProjectInitController` 仅依赖项目权限 | admin 打包通过 | 通过 |
| AC-BE-006 | 菜单 SQL 不再创建旧菜单并禁用已有旧菜单 | SQL diff 和 admin 打包通过 | 通过 |
| AC-BE-007 | API、DB、领域文档同步 | `sh scripts/check-docs.sh` 通过 | 通过 |
