# 项目管理初始化向导后端执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/db/relationship.md`、`docs/domains/requirement-platform/README.md`
- Companion：`../../../../reqflow-ui/docs/specs/active/2026-06-09-REQ-003-项目管理初始化向导`
- 目标客户与基线分支：通用 / main

## 实施步骤

1. 初始化 DTO：新增项目初始化请求、响应、仓库项、客户基线项、模块摘要、索引摘要和检查项 DTO，覆盖 AC-BE-001、AC-BE-002。
2. 初始化 Service：新增 `IReqProjectInitService` 和实现类，聚合读取项目、仓库、客户基线、模块数量和索引批次摘要，覆盖 AC-BE-001。
3. 聚合保存事务：实现新增和更新初始化方法，在同一事务内保存项目、仓库和客户基线；任一校验失败整体回滚，覆盖 AC-BE-002、AC-BE-003。
4. 参数和路径校验：复用或抽取 REQ-002 的个人绝对路径校验规则，校验 Git 远端、默认分支、基线分支和备注，覆盖 AC-BE-004。
5. Controller 与权限：新增 `ReqProjectInitController`，提供 `GET /requirement/project/init/{projectId}`、`POST /requirement/project/init`、`PUT /requirement/project/init`；新增或组合权限校验，覆盖 AC-BE-005。
6. 测试：增加 Service 单元测试，覆盖成功保存、保存失败回滚、个人路径拒绝、初始化摘要聚合和权限边界；覆盖 AC-BE-001 至 AC-BE-005。
7. 文档：更新后端接口契约、数据库关系和领域说明，覆盖 AC-BE-006。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitRequest.java` | 初始化保存请求 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitResponse.java` | 初始化上下文响应 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitRepositoryItem.java` | 初始化仓库项 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitVariantItem.java` | 初始化客户基线项 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqProjectInitChecklist.java` | 初始化检查项 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqProjectInitService.java` | 初始化聚合服务接口 |
| 新增 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 初始化聚合服务实现 |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java` | 初始化聚合接口 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步初始化接口契约 |
| 修改 | `docs/db/relationship.md` | 同步初始化数据粒度和聚合风险 |
| 修改 | `docs/domains/requirement-platform/README.md` | 同步领域当前状态 |
| 新增 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 初始化服务测试 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：后端启动后验证初始化上下文查询、未登录拦截、登录态新增/编辑初始化保存。
- L4 跨端/端到端：与前端 companion 联调项目管理初始化向导保存、返回项目列表、再次打开可回显。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 初始化 DTO、初始化 Service | Service 测试和接口冒烟 |
| AC-BE-002 | 聚合保存事务 | Service 测试和跨端联调 |
| AC-BE-003 | 聚合保存事务 | 失败回滚测试 |
| AC-BE-004 | 参数和路径校验 | 个人路径拒绝测试 |
| AC-BE-005 | Controller 与权限 | 权限注解检查和运行态未登录拦截 |
| AC-BE-006 | 文档 | L0 文档检查和 harness 检查 |

## 执行约束

- Execution Agent 必须保持 REQ-003 代码和文档在 `feature/REQ-003-project-init-wizard` 分支内执行，不得回写到 REQ-002 分支。
- 后端当前仍有既有配置文件改动，除非用户明确说明属于本需求，否则不得暂存或提交。
- 不保存个人本机绝对路径；后端只保存 Git 远端、分支、commit、相对路径和结构化初始化数据。
- 不引入平台服务器 clone、Git 命令、shell 执行或本机目录扫描能力。
- 如果实现时需要新增数据库字段，必须先更新本 plan、DDL、数据库关系文档和 companion 前端契约。
- 保留现有基础 CRUD，不移除仓库管理、客户定制线、模块功能点等单独菜单。
