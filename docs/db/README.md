# 数据库维护规范

本目录沉淀数据库长期知识，供后续需求设计、SQL 修改、Mapper 调整和 Review 使用。涉及数据库表、字段、索引、数据迁移、join、统计口径或分页粒度时，先读本目录，再改 SQL 或数据访问代码。

## 文件职责

| 文件 | 用途 |
|---|---|
| `table-dictionary.md` | 表结构字典，记录表用途、主键、唯一键、关键字段和维护注意事项。 |
| `relationship.md` | 表关系、数据粒度、必要过滤、聚合风险和开发指导。 |
| `../templates/db-change-template.md` | 单次需求的数据库变更说明模板，复制到对应 spec 目录使用。 |
| `sql/req_platform_schema.sql` | 当前需求平台表结构基线。 |
| `sql/req_platform_mcp_key_plain_key.sql` | 已有库补充 MCP 人员 Key 明文字段的幂等升级脚本。 |
| `sql/req_platform_menu.sql` | 需求管理菜单和按钮权限脚本。 |
| `sql/req_platform_release_settings.sql` | 初始发布系统参数、角色授权和品牌清理脚本。 |

`docs/db/sql/` 是当前项目统一的可执行 SQL、迁移脚本、菜单脚本和历史基线目录。`docs/db/` 同时维护可阅读、可复用的数据库知识；执行报告需要分别记录执行脚本路径和同步过的数据库文档路径。

## 维护规则

- 新增或修改表、字段、索引或约束时，必须更新 `docs/db/sql/req_platform_schema.sql`；新增菜单、系统参数、角色授权或发布清理数据时，必须更新对应命名的可执行 SQL，并在当前 spec 的执行报告记录脚本路径。
- SQL 脚本必须说明用途；能幂等时优先幂等，不能幂等时在变更说明里写明原因、执行前检查和回滚方案。
- 新增或修改表、字段、索引或约束时，必须同步更新 `table-dictionary.md`。
- 修改表关系、join、聚合、分页或统计口径时，必须同步更新 `relationship.md` 的关系、数据粒度、必要过滤或聚合风险。
- DDL、数据迁移、Mapper XML、Service 查询链路和统计口径必须保持一致；不能只改 SQL 脚本或只改 Java 代码。
- 不确定的关系必须标注为“待确认关系”，不要把字段名相似直接写成确认关系。

## 缺少表结构来源时

项目初始化时如果仓库没有 DDL、迁移脚本、schema 导出或数据库字典，仍然可以创建 `docs/db/table-dictionary.md`，但必须按证据强度写，不得虚构表结构：

- 有 DDL、迁移脚本、schema 导出或可信数据库字典时，写入“确认表结构”，并记录来源路径。
- 没有 DDL，但存在 Mapper XML、注解 SQL、MyBatis-Plus 实体或 Service 查询链路时，只能写“推断表结构”，并在每张表记录证据来源和待确认字段。
- 连 Mapper、实体和查询链路都不足以支撑判断时，写“暂无确认表结构来源”，记录已扫描路径、未发现内容和后续补齐方式。
- 初始化 agent 可以生成待补齐骨架，但后续首次涉及数据库的真实需求必须先补齐对应表结构或关系证据，再改 SQL 或 Mapper。

## 变更闭环

数据库相关需求完成前至少确认：

- `requirement.md` 或 `plan.md` 写明数据库影响范围。
- `execution-report.md` 记录 SQL 脚本、数据库文档和验证命令。
- `docs/db/table-dictionary.md` 已同步表、字段、索引或约束变化。
- `docs/db/relationship.md` 已同步长期有效的表关系或口径变化。
- `docs/ai-harness/contracts/*.md` 已同步接口字段、状态或错误语义变化。
- Review 时重点检查数据粒度、join 放大、分页 count、历史数据兼容和回滚风险。
