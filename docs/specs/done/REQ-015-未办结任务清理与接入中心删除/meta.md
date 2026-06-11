# 未办结任务清理与接入中心删除元信息

- 状态：complete
- 当前角色：Review Agent
- 流程模式：平台自身建设模式
- 需求 Key：无，本地平台建设
- 平台关联远端：未配置
- 平台目标分支：chore/req-015-closeout-access-center-cleanup
- 执行模式：任务分支模式
- 当前分支：chore/req-015-closeout-access-center-cleanup
- 执行授权：已授权
- Review 授权：已授权
- 目标客户：通用
- 基线分支：main
- companion 仓库：../reqflow-ui/docs/specs/done/REQ-015-未办结任务清理与接入中心删除
- 关联 spec：../reqflow-ui/docs/specs/done/REQ-015-未办结任务清理与接入中心删除
- 影响模块：需求管理、项目管理、项目维护、AI Harness
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用
- 最后更新：2026-06-11

## 状态说明

本次清理当前仓库中已合入主干但仍停留在 `docs/specs/active` 的过程文档，并配合前端删除“项目接入中心”页面入口。后端不改变业务接口、持久化结构和权限标识，仅归档历史 spec 并同步长期模块知识库。

## 授权说明

- 用户要求“已经合并但未办结的任务，请清理”。
- 用户要求删除项目管理菜单中的接入中心，只保留维护。
- 本次属于平台自身建设模式，不通过 MCP 伪造需求回写。
