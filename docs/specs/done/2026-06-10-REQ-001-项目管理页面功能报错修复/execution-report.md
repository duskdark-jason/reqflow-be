# 项目管理页面功能报错修复后端执行报告

## 执行摘要

本阶段按用户要求在实际任务分支 `fix/REQ-20260610-001-project-page-errors` 执行，不使用 worktree。运行态复现确认项目管理页面主要错误来自本地部分迁移库缺少可选索引表：

- `GET /requirement/project/init/1` 原返回业务 500，错误为 `Table 'ry-vue.req_index_module' doesn't exist`，导致项目列表初始化状态显示 `项目信息未完成`。
- 项目接入中心依赖的 `/requirement/index/batch/list` 与 `/requirement/index/module/tree` 原返回业务 500，导致详情页 `Promise.all` 失败后显示 `未选择项目`。

已修复为：项目、仓库和项目分支基础信息保持可用；缺少 `req_index_module` 或 `req_repository_index_batch` 时，初始化摘要和索引只读接口按空集合返回。除这两个可选索引表缺失外，其他数据库异常继续抛出。

## 修改文件

| 文件 | 说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqOptionalIndexTableGuard.java` | 新增可选索引表缺失判断工具，统一读取异常链并识别指定表缺失。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 初始化上下文查询缺少可选索引表时返回空索引/模块摘要，基础项目数据继续返回。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java` | 索引批次列表和模块知识只读接口缺少对应可选表时返回空列表。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 补充初始化查询缺少索引模块表时仍可返回项目上下文的回归测试。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java` | 补充索引批次表、索引模块表缺失时只读列表返回空集合的回归测试。 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 同步可选索引表缺失时的接口降级语义。 |

## TDD 证据

- 新增 `keepsProjectInitUsableWhenIndexModuleTableIsMissing` 后，旧实现失败于 `BadSqlGrammarException: Table 'ry-vue.req_index_module' doesn't exist`。
- 新增 `returnsEmptyBatchListWhenIndexBatchTableIsMissing` 与 `returnsEmptyModuleListWhenIndexModuleTableIsMissing` 后，旧实现失败于 `BadSqlGrammarException`。
- 实现后上述用例均通过。

## 运行态证据

- `GET /requirement/index/batch/list?pageNum=1&pageSize=20&projectId=1` 返回 `code:200`、`rows:[]`、`total:0`。
- `GET /requirement/index/module/tree?projectId=1&status=0` 返回 `code:200`、`data:[]`。
- `GET /requirement/project/init/1` 返回 `code:200`，`projectReady/repositoryReady/variantReady=true`，`moduleReady/indexReady=false`。
- 前端项目列表显示 `自查自纠平台`，初始化状态为 `缺模块知识`，不再显示 `项目信息未完成`。
- 项目接入中心能显示项目基础信息、仓库、项目分支、MCP key 和 MCP 指引；模块知识库显示空数据。

## 验证命令

- `mvn -pl ruoyi-requirement -am -Dtest=ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：通过，7 个测试。
- `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：通过，11 个测试。
- `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：通过，18 个测试。
- `mvn -pl ruoyi-admin -am -DskipTests package`：通过。
- `sh scripts/check-docs.sh`：通过。
- `sh scripts/check-harness.sh review --spec docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复`：补齐验收覆盖和提交记录后复验。
- 运行态接口冒烟：登录后手工请求上述 3 个接口，均返回 `code:200`。

## 验收 ID 覆盖

| 验收 ID | 执行结果 |
|---|---|
| AC-BE-001 | 项目列表、详情和临时项目删除链路通过运行态冒烟；列表分页、查询条件和删除后数据变化正确。 |
| AC-BE-002 | 初始化上下文在缺少可选索引表时返回项目、仓库、分支、空模块摘要、空索引摘要和检查项。 |
| AC-BE-003 | 通过 `POST /requirement/project/init` 创建临时验收项目，后端生成 `variantCode` 和 `mcpKey`。 |
| AC-BE-004 | 编辑保存已有项目后刷新上下文；本轮未改事务同步逻辑，既有回归测试保持通过。 |
| AC-BE-005 | 未修改权限注解；认证态页面按钮对应接口均可调用。 |
| AC-BE-006 | 已同步 `docs/ai-harness/contracts/requirement-platform-api.md`。 |

## 阶段性提交

- 阶段性 commit：234a00a（fix: 兼容缺失索引表的项目页面查询）。

## 影响说明

- 接口/API：影响 `/requirement/project/init/{projectId}`、`/requirement/index/batch/list`、`/requirement/index/module/tree` 的部分迁移库兼容语义。
- 数据库/SQL：不新增或修改表结构；仅在指定可选索引表缺失时对只读查询降级为空集合。
- 权限/菜单：未修改权限注解、菜单和按钮权限。
- 页面展示：支撑项目管理列表状态和项目接入中心在缺索引表环境下继续展示基础项目信息。

## 残余风险

- 当前本地库仍缺少索引相关表，真实索引导入和影响面推荐仍需要完整 DDL 环境验证。
- 本阶段是 Execution Agent 执行报告，未写 `review-report.md`，后续需要独立 Review Agent 审查。
