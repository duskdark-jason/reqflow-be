# 需求管理平台 MVP-lite 后端执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：实现过程中同步补充 `docs/ai-harness/contracts/`
- 相关模块文档：`docs/domains/requirement-platform/README.md`
- 目标客户与基线分支：通用 / main

## 实施步骤

1. 数据库初始化：新增 `sql/req_platform_schema.sql` 和 `sql/req_platform_menu.sql`，覆盖 AC-BE-001。
2. 模块骨架：新增 `ruoyi-requirement` Maven 模块、domain、POM 依赖和 admin 依赖，覆盖 AC-BE-003。
3. 基础 CRUD：实现项目、仓库、客户定制线和模块功能点 Mapper、Service、Controller，覆盖 AC-BE-004。
4. 需求和执行包：实现需求编号、状态流转、执行包版本追加和 REST 接口，覆盖 AC-BE-002、AC-BE-004。
5. 模板生成：实现确定性模板渲染和草稿包生成，覆盖 AC-BE-002。
6. MCP Endpoint：实现 JSON-RPC 风格 MCP 资源、提示词和安全工具，覆盖 AC-BE-005。
7. 统计与事件：实现业务事件记录和统计接口，覆盖 AC-BE-004。
8. 验证与报告：执行 Maven 测试、Maven 打包、MySQL 脚本落库和 harness 检查，更新 `execution-report.md`。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `sql/req_platform_schema.sql` | 需求平台业务表 |
| 新增 | `sql/req_platform_menu.sql` | 需求平台菜单和按钮权限 |
| 修改 | `pom.xml` | 新增后端业务模块 |
| 修改 | `ruoyi-admin/pom.xml` | admin 引入后端业务模块 |
| 新增 | `ruoyi-requirement/**` | 需求平台后端业务模块 |
| 新增 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/**` | REST 和 MCP Controller |
| 新增 | `docs/specs/active/2026-06-09-REQ-PLATFORM-MVP-lite/**` | active spec 执行追踪 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh init`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：本地 backend 启动后用 curl 验证 MCP 和至少一个 REST 列表接口。
- L4 跨端/端到端：前后端均完成后再与 companion spec 联调。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 数据库初始化 | `mysql -uroot -p123456 ry-vue < sql/req_platform_schema.sql` 和菜单脚本 |
| AC-BE-002 | 需求、执行包、模板 | `mvn -pl ruoyi-requirement -am test` |
| AC-BE-003 | 模块骨架和整体编译 | `mvn -pl ruoyi-admin -am -DskipTests package` |
| AC-BE-004 | REST 接口 | Maven 编译和运行态 curl |
| AC-BE-005 | MCP 安全边界 | 代码检查和 MCP 冒烟 |

## 执行约束

- Execution Agent 必须按本计划执行，不得自行扩大范围。
- 不实现仓库克隆、shell 执行、Git 分支操作或大模型调用。
- 发现接口契约变更时同步通知前端 companion spec。
- 隔离分支内允许阶段性 commit；merge、push、rebase 仍需用户确认。
