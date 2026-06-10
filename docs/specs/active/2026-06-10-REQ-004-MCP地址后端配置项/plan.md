# MCP地址后端配置项执行计划

## 输入文件

- 控制器：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- 控制器测试：`ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java`
- 默认配置：`ruoyi-admin/src/main/resources/application.yml`
- 后端契约：`docs/ai-harness/contracts/requirement-platform-api.md`

## 实施步骤

1. 先补 `ReqMcpKeyControllerTest`，覆盖配置项优先级和空配置兜底行为，覆盖 AC-BE-001、AC-BE-002、AC-BE-003。
2. 在 `ReqMcpKeyController` 增加 `reqflow.mcp.public-url` 配置读取，统一用于 `mcpAddress` 和 Codex 配置模板，覆盖 AC-BE-001、AC-BE-003。
3. 在 `application.yml` 增加默认配置项和说明，覆盖 AC-BE-004。
4. 更新 API 契约文档，记录配置优先级、完整地址格式和兜底规则，覆盖 AC-BE-004。
5. 执行后端定向测试、编译打包和文档检查。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java` | 增加 MCP 公开地址配置优先逻辑 |
| 修改 | `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java` | 增加配置优先和兜底推导测试 |
| 修改 | `ruoyi-admin/src/main/resources/application.yml` | 增加 `reqflow.mcp.public-url` 默认配置项 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP 配置地址契约 |

## 验证计划

- TDD Red：`mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`
- TDD Green：`mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档/规范：`sh scripts/check-docs.sh`
- L3 运行态：本次仅改地址生成逻辑且已通过 Controller 层单元测试覆盖配置来源；如需真实环境验证，可在部署配置 `reqflow.mcp.public-url` 后访问 `/requirement/mcp/key/config` 确认返回。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 测试与控制器实现 | 定向单元测试校验配置优先 |
| AC-BE-002 | 测试与控制器实现 | 定向单元测试校验请求头兜底 |
| AC-BE-003 | 控制器实现 | `mcpAddress(request)` 统一传入模板生成 |
| AC-BE-004 | 配置与文档 | `application.yml`、契约文档和 L0 检查 |

## 执行约束

- 不修改 MCP Key 权限、菜单、数据库表和鉴权流程。
- 不触碰当前工作区已有的无关 `REQ-003` 未跟踪 spec。
- 任务分支完成后记录 commit；主分支合并需明确授权。
