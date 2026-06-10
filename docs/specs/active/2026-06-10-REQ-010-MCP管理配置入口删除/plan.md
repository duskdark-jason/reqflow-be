# MCP管理配置入口删除后端执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求管理、MCP 管理
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：调整 `ReqMcpKeyControllerTest`，断言 Controller 不存在 `/config` 映射，覆盖 AC-BE-001。
2. TDD Red：调整 `ReqMcpUserKeyServiceImplTest`，断言创建结果没有独立地址、Header、Codex 配置和全局 Skill 字段，并仍返回不含明文 Key 的安装包，覆盖 AC-BE-002、AC-BE-003。
3. TDD Green：删除 `ReqMcpKeyController.config`、相关 import 和只服务于配置接口的私有方法，覆盖 AC-BE-001。
4. TDD Green：收敛 `ReqMcpUserKeyCreateResult` 字段与 `ReqMcpUserKeyServiceImpl.buildCreateResult`，覆盖 AC-BE-002、AC-BE-003。
5. 文档同步：更新 `docs/ai-harness/modules/requirement-platform.md` 和 `docs/ai-harness/contracts/requirement-platform-api.md`，覆盖 AC-BE-004。
6. 验证收尾：运行指定测试、后端打包、文档检查和 harness 完成检查；如无法执行 L3，记录后续补验方式。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | 删除配置查询接口 |
| 修改 | `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 覆盖配置接口删除 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/ReqMcpUserKeyCreateResult.java` | 收敛创建/重置响应字段 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java` | 收敛创建/重置结果构造 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImplTest.java` | 覆盖响应字段收敛和安装包安全 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 更新 MCP 管理模块说明 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 更新后端接口契约 |
| 新增 | `docs/specs/active/2026-06-10-REQ-010-MCP管理配置入口删除/*` | 本次需求阶段文档 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md` 的 MCP 管理风险点。
- 更新 `docs/ai-harness/contracts/requirement-platform-api.md` 中 MCP 人员 Key 管理接口表和字段说明。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest,ReqMcpUserKeyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L3 运行态冒烟：如本地后端可启动，验证 `/requirement/mcp/key/config` 不再可用、创建/重置响应字段收敛；如当前环境无法联调，在执行报告记录后续补验环境。
- L4 跨端/端到端：本次未改 MCP 协议调用和保存/导出/异步流程，不默认执行。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | Controller 删除 | `ReqMcpKeyControllerTest` |
| AC-BE-002 | DTO/Service 收敛 | `ReqMcpUserKeyServiceImplTest` |
| AC-BE-003 | 安装包保留与安全 | `ReqMcpUserKeyServiceImplTest` |
| AC-BE-004 | 文档同步 | `sh scripts/check-docs.sh` |

## 执行约束

- 不修改 MCP 协议入口 `/requirement/mcp`。
- 不修改数据库、菜单 SQL 或权限点。
- Execution Agent 按计划实现后必须提交，并自动进入 Review。
