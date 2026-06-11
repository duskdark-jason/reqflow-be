# AI Harness 说明

本目录是给 agentic coding / vibecoding 工具使用的共享上下文。它应该随代码一起提交，让后续模型和同事在修改前理解业务边界、接口契约和验证方式。

各类落地文档必须使用中文描述；必要英文术语、命令、接口名和工具名可以保留，但标题和说明必须给出中文语义。

新需求 spec 目录必须使用 `REQ-001-中文需求标题` 形式，包含稳定需求编号和中文标题；Git 任务分支必须使用 ASCII。

## 使用方式

1. 先读项目 `AGENTS.md`。
2. 读取 `harness-index.json`，确认仓库角色、companion 仓库和文档入口。
3. 如果 `template` 仍为 `true` 或 `initialized` 不是 `true`，先完成 harness 初始化，不要开始真实需求开发。
4. 判断任务类型。
5. 按下表继续读取对应文件。
6. 小范围修改。
7. 按 `verification.md` 运行最小必要验证。

## 任务入口

| 任务类型 | 必读文件 |
|---|---|
| 任意修改 | `harness-index.json`、`change-checklist.md`、`verification.md` |
| 需求平台 Key、项目接入初始化或 MCP 回写 | `../process/platform-key-workflow.md` |
| 业务模块、菜单或页面 | `modules/【菜单目录或模块名】.md` |
| 接口、结果结构、UI 状态 | `contracts/【契约名】.md` |
| 数据库、SQL、统计口径 | `../db/README.md`、`../db/table-dictionary.md`、`../db/relationship.md`，如果项目存在 |
| 重要业务或技术决策 | `decisions/*.md` |

## 维护规则

只要变更包含以下内容，就要同步更新本目录对应文件：

- 接口路径、请求方式、请求字段、响应字段或错误语义。
- 数据库表、字段、join、统计粒度或分页粒度。
- 核心业务不变量。
- 权限、菜单、路由或按钮行为。
- 新模块、新页面、新报表、新导出或新异步工作流。
- 构建、测试、联调或验收方式。

模块文档必须尽量对齐前端菜单结构，写清一级菜单、子菜单或隐藏页签、用户动作、前端文件、API 封装、后端接口、权限标识和核心后端文件。纯后端能力也要说明它服务哪个菜单、MCP 能力或后台流程。

项目接入初始化时，不能只保留 `modules/.gitkeep`；必须至少落地一个非模板模块文档。初始内容必须来自前端路由、菜单、页面组件和 API 封装分析，按菜单目录、子菜单、隐藏页签或页面业务功能沉淀为具体业务知识库；纯后端项目按 companion 前端菜单、MCP 能力或后台任务沉淀，不得只生成仓库概览。

如果项目存在 `docs/db/`，该目录必须至少包含 `README.md`、`table-dictionary.md` 和 `relationship.md`。可执行 SQL、迁移脚本和历史基线统一放入 `docs/db/sql/`；`docs/db/` 同时维护表结构字典、表关系、数据粒度和口径说明。数据库相关需求必须先读 `docs/db/README.md`，涉及表、字段、索引或约束时更新 `table-dictionary.md`；涉及 DDL、DML、迁移、清理 SQL、Mapper、join、聚合、统计口径或分页粒度时，同步更新 `relationship.md` 或在执行报告写明无需更新原因；完成态执行报告必须记录相关 `docs/db/sql/` 或 `docs/db/` 路径。

如果判断无需更新 harness，请在完成说明中写明原因。
