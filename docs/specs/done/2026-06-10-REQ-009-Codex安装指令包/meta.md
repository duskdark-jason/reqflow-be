# Codex 安装指令包元信息

- 状态：complete
- 当前角色：Execution Agent
- 流程模式：平台自身建设模式
- 需求 Key：无，本地平台建设
- 平台关联远端：未配置
- 平台目标分支：feature/REQ-20260610-009-codex-setup-package
- 执行模式：任务分支模式
- 当前分支：feature/REQ-20260610-009-codex-setup-package
- 执行授权：已授权
- Review 授权：已授权
- 目标客户：通用
- 基线分支：main
- companion 仓库：reqflow-ui docs/specs/done/2026-06-10-REQ-009-Codex安装指令包
- 关联 spec：docs/specs/done/2026-06-10-REQ-008-全局ReqflowMCP技能安装
- 影响模块：需求管理、MCP 管理、Codex 安装指令
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用
- 最后更新：2026-06-10

## 状态说明

本需求在已有全局 `reqflow-mcp` skill 包基础上，新增 Codex 安装指令包返回能力，参考 Context7 式“配置 + rule/skill”体验和 GitHub MCP `server.json` 元数据思路，但不做 Codex plugin。

## 授权说明

- 用户先提出 plugin 方向，随后明确“改成这个方向”，即采用安装指令包与 server metadata 方案。
- 用户已明确要求合并并清理分支，授权进入收口合并；本轮不执行独立代码 Review，后续由用户人工测试。
- 当前仓库未配置远端，不执行 pull 或 push。
- 本次同步前端 companion 仓库展示和复制 Codex 安装指令包。
