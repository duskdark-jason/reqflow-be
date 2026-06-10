# MCP管理配置入口删除后端 Review 报告

## Review 结论

- 结论：通过
- 审查分支：feature/REQ-20260610-010-mcp-key-config-cleanup
- 审查范围：后端 MCP Key Controller、创建结果 DTO、创建/重置结果构造、单元测试、后端 harness 文档

## 审查输入

| 输入 | 路径或命令 |
|---|---|
| 需求说明 | `docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/requirement.md` |
| 执行计划 | `docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/plan.md` |
| 执行报告 | `docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/execution-report.md` |
| 代码差异 | `git diff -- ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java docs/ai-harness/contracts/requirement-platform-api.md docs/ai-harness/modules/requirement-platform.md` |

## 验收复核

| 验收 ID | 复核结果 | 证据 |
|---|---|---|
| AC-BE-001 | 通过 | `ReqMcpKeyController` 已删除 `/config` 映射，控制器反射测试覆盖该约束 |
| AC-BE-002 | 通过 | `ReqMcpUserKeyCreateResult` 已删除 `mcpAddress`、`headerName`、`codexConfig`、`codexGlobalSkillPackage` 独立字段，Service 不再构造这些字段 |
| AC-BE-003 | 通过 | 创建和重置结果仍设置 `codexSetupPackage`；单元测试覆盖安装包不包含人员明文 Key |
| AC-BE-004 | 通过 | `docs/ai-harness/modules/requirement-platform.md` 与 `docs/ai-harness/contracts/requirement-platform-api.md` 已同步删除旧配置接口契约 |

## 验证复核

| 层级 | 命令或方式 | 复核结论 |
|---|---|---|
| Red/Green/L2 | `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，12 tests，0 failures |
| L0 | `sh scripts/check-docs.sh` | 通过 |
| L1 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，后端打包成功 |
| L3 | `SPRING_PROFILES_ACTIVE=druid,test java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=18080` 与 `curl -i -s http://localhost:18080/requirement/mcp/key/config` | 后端可启动；未登录请求由安全链返回 `code=401`，Handler 删除由反射单测覆盖 |

## 问题清单

无阻断问题，无有条件通过问题。

## 返修交接清单

无 RF-* 返修项。

## 复审记录

无返修项，无需复审。

## 风险与备注

- 运行态 curl 未携带登录态，无法通过 HTTP 状态直接区分 Handler 是否存在；本次以控制器反射测试作为接口映射删除的主要证据。
