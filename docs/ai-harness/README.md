# AI Harness

本目录是给 agentic coding / vibecoding 工具使用的共享上下文。它应该随代码一起提交，让后续模型和同事在修改前理解业务边界、接口契约和验证方式。

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
| 业务模块 | `modules/*.md`，如果项目已沉淀对应模块 |
| 接口、结果结构、UI 状态 | `contracts/*.md`，如果项目已沉淀对应契约 |
| 数据库、SQL、统计口径 | `../db/`，如果项目存在 |
| 重要业务或技术决策 | `decisions/*.md` |

## 维护规则

只要变更包含以下内容，就要同步更新本目录对应文件：

- 接口路径、请求方式、请求字段、响应字段或错误语义。
- 数据库表、字段、join、统计粒度或分页粒度。
- 核心业务不变量。
- 权限、菜单、路由或按钮行为。
- 新模块、新页面、新报表、新导出或新异步工作流。
- 构建、测试、联调或验收方式。

如果判断无需更新 harness，请在完成说明中写明原因。
