# MCP协议握手与工具暴露修复元信息

- 状态：complete
- 当前角色：Review Agent
- 流程模式：平台自身建设模式
- 需求 Key：无，本地平台建设
- 平台关联远端：未配置
- 平台目标分支：fix/REQ-20260610-005-mcp-protocol-service
- 执行模式：任务分支模式
- 当前分支：fix/REQ-20260610-005-mcp-protocol-service
- 执行授权：已授权
- Review 授权：已授权
- 目标客户：通用
- 基线分支：main
- companion 仓库：无
- 关联 spec：无
- 影响模块：需求管理、MCP 管理、项目接入初始化
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用
- 最后更新：2026-06-10

## 状态说明

本需求修复需求平台自身 MCP 服务协议兼容问题。当前外部接入项目会话通过 Codex 加载 reqflow MCP server 时，因 `/requirement/mcp` 不支持 `initialize` 握手而无法暴露 `publish_repository_index` 工具。本需求同时修复项目创建/初始化指令未明确指定 MCP server 与 tool 的问题，避免接入项目只拿到泛化提示却无法准确映射到 `reqflow.publish_repository_index`。Review 已通过，当前进入完成态。

## 授权说明

- 用户目标为“完成真正MCP服务搭建”。
- 用户补充要求：项目创建的调用 MCP 指令要能准确识别到调用指定 MCP 的 tools。
- 用户已授权开始 Review 与办结流程。
- 本次属于平台自身建设模式，不伪造 MCP 回写结果。
- 当前分支基于本地 `main` 创建；当前仓库未配置远端，无法执行 `git pull --ff-only`。
