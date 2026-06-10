# MCP管理配置入口删除后端执行报告

## 执行结论

- 状态：已完成
- 分支：feature/REQ-20260610-010-mcp-key-config-cleanup
- commit：290a434a711cdd7e5b5c19b56fdf3f1b49f69e13（主体实现提交；闭环文档回填见本分支最新提交）

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | 删除 `/requirement/mcp/key/config` 映射和相关返回构造 |
| `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 删除旧配置接口断言，新增 Controller 不暴露 `/config` 的反射测试 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 删除独立 `mcpAddress`、`headerName`、`codexConfig`、`codexGlobalSkillPackage` 字段 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | 创建/重置结果只设置 `plainKey`、`key` 和 `codexSetupPackage` |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 覆盖旧 getter 不再暴露、安装包仍不包含明文 Key |
| `docs/ai-harness/modules/requirement-platform.md` | 更新 MCP 管理模块说明和风险点 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 删除配置接口契约，更新创建/重置响应字段说明 |
| `docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/*` | 新增本次需求、计划、执行和 Review 交接文件 |

## 模块知识库沉淀

- 影响模块：需求管理、MCP 管理
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：无
- SQL 脚本路径：无
- 数据库文档路径：无
- 数据库变更说明：无
- 无需更新原因：本次只删除 Controller 配置接口和收敛响应 DTO，不涉及表结构、SQL、join、分页或统计口径。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| Red | AC-BE-001, AC-BE-002 | `mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 预期失败：`getMcpAddress should not be exposed` |
| Green/L2 | AC-BE-001, AC-BE-002, AC-BE-003 | 同上 | 通过：12 tests，0 failures |
| L0 | AC-BE-004 | `sh scripts/check-docs.sh` | 通过：文档检查通过 |
| L1 | AC-BE-001, AC-BE-002, AC-BE-003 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过：后端打包成功 |
| L3 | AC-BE-001 | `SPRING_PROFILES_ACTIVE=druid,test java -jar ruoyi-admin/target/ruoyi-admin.jar` | 未通过：8080 端口已占用 |
| L3 | AC-BE-001 | `SPRING_PROFILES_ACTIVE=druid,test java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=18080` | 通过：后端启动成功 |
| L3 | AC-BE-001 | `curl -i -s http://localhost:18080/requirement/mcp/key/config` | 受限：未登录安全链返回 `code=401`；是否删除 Handler 由 Controller 反射测试覆盖 |
| L4（可选） | 全部 | 不执行 | 本次未改 MCP 协议 tool、保存、导出或异步流程 |

## 运行态证据

- 执行目录：当前 `reqflow-be` 子仓库根目录
- 启动命令：`SPRING_PROFILES_ACTIVE=druid,test java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=18080`
- profile/env/mode：`druid,test`，端口 `18080`
- 检查命令：`curl -i -s http://localhost:18080/requirement/mcp/key/config`
- 原始错误摘要：首次使用 8080 启动失败，原因是端口占用；换 18080 启动成功。未登录 curl 返回 `{"msg":"请求访问：/requirement/mcp/key/config，认证失败，无法访问系统资源","code":401}`。
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：具备登录态或接口测试 token 的本地或测试环境

## 计划偏差

- 无计划外后端范围变更。

## Review 返修记录

无。

## 风险与后续

- 运行态未登录请求无法区分 Handler 是否存在；本次通过反射单测验证 `/config` 映射已删除。
