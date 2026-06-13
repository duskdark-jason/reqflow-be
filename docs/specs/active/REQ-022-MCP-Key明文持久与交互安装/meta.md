# MCP Key明文持久与交互安装元信息

- 状态：complete
- 当前角色：Execution Agent
- 流程模式：本地 Harness 模式
- 需求 Key：无，本地流程
- MCP 接入状态：未接入
- 平台回写状态：不适用
- 平台关联远端：当前仓库远端
- 平台目标分支：feature/req-020-mcp-multi-client-setup
- 执行模式：任务分支模式
- 当前分支：feature/req-020-mcp-multi-client-setup
- 执行授权：已授权
- Review 授权：已授权
- 目标客户：通用
- 基线分支：当前任务分支
- companion 仓库：../reqflow-ui
- 关联 spec：REQ-020-MCP多客户端安装支持
- 影响模块：MCP 管理、MCP 请求地址配置、MCP Key 持久化、多客户端安装脚本、本地 Harness 门禁、Harness 模板、合并归档指令、阶段短指令、轻量上下文
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用
- 最后更新：2026-06-13

## 状态说明

本需求把人员 MCP Key 明文从一次性返回调整为可持久返回，并把统一安装指令调整为执行后选择安装工具。后续补充已把 MCP 请求地址配置加入 MCP 管理页且仅管理员可通过弹窗配置，并确保再次打开使用指令时仍渲染真实明文 Key。阶段复制指令已进一步压缩为 token-only，并通过 `get_action_context` 与 `platformSync` 降低重复上下文拉取。已完成后端实现、数据库文档、companion 前端展示约束、本地 Harness 门禁、项目接入初始化模板同步和归档收尾规范同步。
