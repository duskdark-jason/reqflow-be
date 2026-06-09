# 项目管理页面功能报错修复后端 Review 报告

## Review 结论

结论：通过

本次审查未发现阻断或必须返修的问题。后端实现把缺少 `req_index_module`、`req_repository_index_batch` 的兼容降级限定在项目初始化上下文和索引只读展示接口；索引导入、影响面推荐和其他数据库异常仍保持真实失败，不会默默吞掉写入链路风险。

## 审查范围

- 需求与计划：`requirement.md`、`plan.md`
- 执行报告：`execution-report.md`
- 后端代码 diff：`ReqProjectInitServiceImpl`、`ReqRepositoryIndexServiceImpl`、`ReqOptionalIndexTableGuard`
- 后端测试 diff：`ReqProjectInitServiceImplTest`、`ReqRepositoryIndexServiceImplTest`
- 契约文档：`docs/ai-harness/contracts/requirement-platform-api.md`
- 数据访问证据：`ReqIndexModuleMapper.xml`、`ReqRepositoryIndexBatchMapper.xml`、`sql/req_platform_schema.sql`

## 审查发现

- 未发现阻断问题。
- 未发现需要 Execution Agent 立即返修的重要问题。
- 一般建议：`ReqOptionalIndexTableGuard` 当前通过异常链 message 判断缺表，已满足 MySQL 本地缺表场景；后续如果接入其他数据库方言，可考虑进一步结合 SQLState 或 vendor code 收紧判断，避免把非缺表的 schema drift 误判为空列表。

## 验收 ID 覆盖

| 验收 ID | Review 结论 |
|---|---|
| AC-BE-001 | 通过。执行报告包含项目列表、详情、临时项目删除和列表刷新证据。 |
| AC-BE-002 | 通过。缺可选索引表时初始化上下文返回项目、仓库、分支、空摘要和检查项；测试和接口冒烟均覆盖。 |
| AC-BE-003 | 通过。新增初始化已有测试覆盖项目、仓库、分支、`variantCode`、`mcpKey` 和个人路径拒绝；运行态创建临时项目成功。 |
| AC-BE-004 | 通过。更新同步删除已有回归测试覆盖；本轮未改事务边界，`@Transactional` 保持。 |
| AC-BE-005 | 通过。未改权限注解；Controller 权限与前端契约一致。 |
| AC-BE-006 | 通过。后端 API 契约已同步缺可选索引表时的只读降级语义。 |

## 验证记录

- `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：Review 阶段复验通过，18 个测试。
- `mvn -pl ruoyi-admin -am -DskipTests package`：Review 阶段复验通过。
- `sh scripts/check-docs.sh`：Review 阶段复验通过。
- `sh scripts/check-harness.sh review --spec docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复`：Review 阶段复验通过。

## 返修交接清单

无。

## 残余风险

- 当前本地执行环境缺少索引表，Execution Agent 已验证缺表降级路径；完整 DDL 环境下的真实索引导入和影响面推荐仍应在后续索引链路需求中单独验证。
