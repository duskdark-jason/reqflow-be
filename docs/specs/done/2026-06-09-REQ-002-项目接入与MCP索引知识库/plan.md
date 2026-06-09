# 项目接入与 MCP 索引知识库后端执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/db/relationship.md`、`docs/domains/requirement-platform/README.md`
- 目标客户与基线分支：通用 / main

## 实施步骤

1. 数据模型设计：新增索引批次、模块知识和影响面条目表，更新 domain、mapper 和 SQL 脚本，覆盖 AC-BE-002、AC-BE-003。
2. 索引导入服务：实现统一导入 Service，校验仓库身份、分支、commit、索引版本和个人绝对路径，覆盖 AC-BE-001、AC-BE-002、AC-BE-003。
3. MCP tool 扩展：在 `McpService` 的 `tools/list` 和 `tools/call` 中增加 `publish_repository_index`，复用索引导入 Service，覆盖 AC-BE-001、AC-BE-005。
4. 影响面推荐接口：新增索引查询和推荐 Controller/Service，按项目、客户线、模块或功能点返回影响页面、接口、数据表、权限和文档，覆盖 AC-BE-004、AC-BE-005。
5. 审计与权限：为索引导入、查询和推荐补充权限标识、菜单脚本和活动日志事件，覆盖 AC-BE-005。
6. 文档与测试：补充接口契约、数据库关系和领域文档，增加导入校验、路径拒绝、推荐查询和 MCP tool 单元测试，覆盖 AC-BE-006。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `sql/req_platform_schema.sql` | 增加索引知识库相关表 |
| 修改 | `sql/req_platform_menu.sql` | 增加索引相关权限或调整菜单权限 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/*Index*` | 索引批次和影响面领域对象 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/*Index*` | 索引导入和推荐服务 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 增加索引发布 MCP tool |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/*Index*Controller.java` | 索引查询、导入和推荐接口 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步接口和 MCP tool 契约 |
| 修改 | `docs/db/relationship.md` | 同步新增表关系和数据粒度 |
| 修改 | `docs/domains/requirement-platform/README.md` | 修正领域当前状态并补充索引闭环 |
| 新增或修改 | `ruoyi-requirement/src/test/**` | 覆盖索引导入和 MCP tool 行为 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh complete --spec docs/specs/active/2026-06-09-REQ-002-项目接入与MCP索引知识库`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：后端启动后验证 MCP `publish_repository_index`、索引批次查询和影响面推荐接口。
- L4 跨端/端到端：与前端 companion 联调项目接入、索引上传结果展示和新建需求自动带出影响面。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | MCP tool 扩展 | 单元测试和 MCP 冒烟 |
| AC-BE-002 | 索引导入服务 | 路径拒绝单元测试和数据库检查 |
| AC-BE-003 | 数据模型设计 | Mapper 测试和索引批次查询 |
| AC-BE-004 | 影响面推荐接口 | Service 测试和接口冒烟 |
| AC-BE-005 | 审计与权限 | 权限注解检查、菜单脚本检查和活动日志验证 |
| AC-BE-006 | 文档与测试 | L0 文档检查和 harness 检查 |

## 执行约束

- Execution Agent 必须按本计划执行，不得自行扩大范围。
- 不保存个人本机绝对路径；任何上传数据中出现个人目录都必须拒绝导入。
- 不引入平台服务器 clone 仓库、执行 shell 或保存 Git 凭证能力。
- MCP tool 只写平台索引表和活动日志，不触碰业务代码仓库文件。
- 如果前端 companion 需要字段调整，先同步本 spec 和接口契约，再调整代码。
