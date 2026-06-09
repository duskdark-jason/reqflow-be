# 项目分支初始化与菜单重构后端 Review 报告

## Review 结论

- 结论：通过
- Review Agent：Codex Review Agent
- Review 时间：2026-06-09

## 问题清单

未发现阻断或重要问题。

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-BE-001 | 纯后端项目可保存 | `validateRepositories`、纯后端测试 | `mvn -pl ruoyi-requirement -am test` 通过 | 通过 |
| AC-BE-002 | 仓库就绪不要求前后端齐全 | `buildChecklist` 改为任一有效仓库 | checklist 测试通过 | 通过 |
| AC-BE-003 | 生成并返回稳定 MCP key | `ReqVariant.mcpKey`、`buildMcpKey` | MCP key 返回测试通过 | 通过 |
| AC-BE-004 | MCP key 定位索引上下文 | `resolveRequestContext`、`resolveRepository` | MCP key 导入测试通过 | 通过 |
| AC-BE-005 | 初始化权限收敛 | `ReqProjectInitController` 权限注解 | admin 打包通过 | 通过 |
| AC-BE-006 | 菜单 SQL 下线旧入口 | `req_platform_menu.sql` | SQL diff 复核 | 通过 |
| AC-BE-007 | harness 和数据库文档同步 | API 契约、数据库关系、领域文档 | `sh scripts/check-docs.sh` 通过 | 通过 |

## 剩余风险

- 当前未执行登录态 REST/MCP 冒烟；需在具备测试账号和数据库迁移后的环境补验保存、回显和真实 MCP 导入。
- `sql/req_platform_req004_migration.sql` 是一次性迁移脚本，重复执行前需确认 `mcp_key` 列和唯一索引是否已存在。
