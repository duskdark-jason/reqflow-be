# ReqFlow 后端

ReqFlow 后端是统一需求管理平台的服务端工程，基于 RuoYi 前后端分离后端改造，并由 ChatGPT 最新模型辅助自动开发、维护和迭代。

本仓库保留 RuoYi 的认证、权限、菜单、系统管理、日志和监控底座，在此基础上新增需求平台业务模块，面向管理员、需求人员和开发人员提供项目接入、需求流转、执行资料、MCP 和知识库索引能力。

## 主要能力

- 项目、仓库、项目分支和模块知识库维护。
- 需求记录、执行包版本、状态流转和统计接口。
- MCP Key 管理、动作 Token、项目接入初始化和仓库索引发布。
- `docs/ai-harness` 长期知识库与 `docs/specs` 单次需求过程记录。
- `docs/db` 数据库结构、关系、迁移脚本和菜单脚本维护。

## 技术栈

| 层面 | 技术 |
|---|---|
| 基础框架 | Spring Boot、Spring Security、RuoYi 权限体系 |
| 数据访问 | MyBatis、MySQL、Druid |
| 缓存与认证 | Redis、JWT |
| 构建 | Maven 多模块 |
| 业务模块 | `ruoyi-requirement` |

## 目录说明

| 路径 | 说明 |
|---|---|
| `ruoyi-admin` | Web 启动模块和 Controller。 |
| `ruoyi-requirement` | ReqFlow 需求平台核心业务模块。 |
| `ruoyi-system`、`ruoyi-framework`、`ruoyi-common` | RuoYi 基础能力。 |
| `docs/db` | 数据库字典、关系说明和 SQL 脚本。 |
| `docs/ai-harness` | 面向 agent 的长期项目知识。 |
| `docs/specs` | 单次需求的需求、计划、执行和 Review 记录。 |
| `scripts` | 文档和 harness 校验脚本。 |

## 常用命令

```bash
# 文档检查
sh scripts/check-docs.sh

# harness 初始化检查
sh scripts/check-harness.sh init

# 后端打包
mvn -pl ruoyi-admin -am -DskipTests package

# 需求平台模块测试
mvn -pl ruoyi-requirement -am test
```

## 数据库脚本

后端 SQL 已统一迁移到 `docs/db/sql/`：

- `docs/db/sql/ry_20260417.sql`：RuoYi 基线库结构和基础数据。
- `docs/db/sql/quartz.sql`：定时任务相关表。
- `docs/db/sql/req_platform_schema.sql`：ReqFlow 需求平台表结构基线。
- `docs/db/sql/req_platform_req*.sql`：需求平台增量迁移、菜单和修复脚本。

修改数据库表、字段、索引、菜单权限或迁移脚本时，必须同步更新 `docs/db/table-dictionary.md`、`docs/db/relationship.md` 或相关 `docs/ai-harness` 文档。

## 文档入口

- 总入口：`docs/README.md`
- 新需求流程：`docs/process/new-requirement-flow.md`
- Git 工作流：`docs/process/git-workflow.md`
- 验证说明：`docs/ai-harness/verification.md`
