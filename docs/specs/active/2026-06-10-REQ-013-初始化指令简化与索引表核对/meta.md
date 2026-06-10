# 初始化指令简化与索引表核对元信息

- 状态：executed
- 当前角色：Execution Agent
- 流程模式：平台自身建设模式
- 需求 Key：无，本地平台建设
- 平台关联远端：未配置
- 平台目标分支：fix/REQ-20260610-013-init-instruction-slim-index-schema
- 执行模式：任务分支模式
- 当前分支：fix/REQ-20260610-013-init-instruction-slim-index-schema
- 执行授权：已授权
- Review 授权：未授权
- 目标客户：通用
- 基线分支：main
- companion 仓库：无
- 关联 spec：无
- 影响模块：需求管理、项目接入初始化、MCP 管理
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 数据库动作：核对并按既有 SQL 补齐本地平台库索引表
- 无需更新原因：不适用
- 最后更新：2026-06-10 23:22

## 状态说明

用户测试项目接入初始化后，两个仓库都已调用 `mcp__reqflow.publish_repository_index`，且 `actionToken` 放在 arguments 中，但平台库缺少 `req_repository_index_batch`，导致后端和前端索引发布均失败。平台已登记 harness 初始化结果为 `init_uncommitted_publish_fail`。

本需求同时简化平台默认初始化指令：安装全局 `reqflow-mcp` skill 后，初始化指令只保留动态上下文和关键约束，完整执行步骤由 skill 承接。

## 授权说明

- 用户明确要求“开始简化”。
- 用户要求对初始化项目仍存在的问题做完整核对。
- 本次属于平台自身建设模式，不通过 MCP 伪造需求回写。
