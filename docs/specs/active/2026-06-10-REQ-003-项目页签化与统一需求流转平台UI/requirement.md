# 项目页签化与统一需求流转平台UI 后端需求说明

## 背景

前端将把项目维护、分支初始化和知识库详情从弹窗与表格展开改为独立页签，同时把裸 `MCP Key` 改成可复制的初始化指令。后端需要为前端提供稳定的指令字段、token 解析语义和用户可见品牌信息清理边界，避免前端只做文案包装后仍暴露不清晰的 key 语义。

本需求仍处于需求平台自身建设阶段，不通过外部需求平台 Key 驱动。

## 目标

- 为项目分支返回初始化指令数据，包含动作类型、简短提示词、加密唯一 token 和可复制内容。
- 规划初始化 token、需求 token、开发 token 使用同一套动作指令模型，后续 MCP 可按 token 识别调用接口和上下文。
- 保持现有 `mcpKey + remoteUrl` 的索引导入兼容，避免破坏已落地的 `publish_repository_index`。
- 清理后端用户可见若依品牌信息，把可见系统名称调整为统一需求流转平台。
- 同步后端接口契约、数据库关系说明和领域文档。

## 范围

本次包含：

- 扩展项目初始化上下文中 `variants` 的响应字段，返回分支初始化指令。
- 设计统一 token 指令模型，覆盖初始化、需求编排和开发执行三类动作的字段命名和校验方向。
- 新增轻量 `req_action_token` 表和服务端 token 服务；前端只消费 copy 内容和 token，不直接拼装敏感字段。
- 调整 MCP 服务或后续接口入口，使 token 能够映射到动作类型、项目、项目分支、目标接口和权限校验。
- 清理后端配置、启动输出、Swagger 标题、基础 SQL 初始化数据中用户可见的若依品牌。
- 在需求新增和修改服务层增加提交目标校验：未初始化完成的项目分支不能作为需求提交分支。
- 更新后端 harness 契约、数据库关系和领域说明。

本次不包含：

- 不重命名 Java 包名 `com.ruoyi`、Maven module 名、底层框架类名或大量历史注释。
- 不替换 Spring Security、MyBatis、RuoYi 权限体系。
- 不让 token 绕过人员 `X-MCP-Key` 鉴权；token 只定位动作和上下文，人员权限仍独立校验。
- 不执行 Git、shell、clone、branch 或本地文件系统写入。
- 不在未授权状态创建任务分支、实现代码、提交或进入 Review。

## 影响范围

- 接口/API：是，`/requirement/project/init/{projectId}` 和相关 MCP 入口会新增响应或解析语义。
- 数据库/SQL：是，新增 `req_action_token` 表保存动作 token 哈希、动作类型和上下文，另按需调整用户可见初始化 SQL 文案。
- 权限/菜单：可能是，token 解析后的动作仍需校验 `req:project:query`、`req:index:import`、`req:package:save` 等既有权限。
- 页面/交互：后端不直接改页面，但为前端展示和复制提供字段。
- 导出/异步/任务：否。

## 契约与数据口径

- 项目初始化详情接口继续为 `GET /requirement/project/init/{projectId}`。
- `variants` 每行保留 `mcpKey` 兼容字段，同时新增初始化指令字段，建议结构如下：

```json
{
  "actionType": "project_init",
  "token": "reqflow_action_xxx",
  "prompt": "请使用统一需求流转平台执行项目分支初始化",
  "content": "请使用统一需求流转平台执行项目分支初始化，token=reqflow_action_xxx",
  "copyLabel": "复制初始化指令",
  "expireTime": null
}
```

- 后续需求 token 和开发 token 使用同一结构，`actionType` 可扩展为 `requirement_plan`、`requirement_develop` 或更贴近工具名的值。
- token 数据粒度：一条 token 只定位一个动作上下文，不代表人员认证；人员认证仍使用 `X-MCP-Key`。
- token 必须能让 MCP 服务识别目标动作、项目、项目分支和目标接口，且不能包含个人本机绝对路径。
- `publish_repository_index` 在完成兼容期内继续支持 `mcpKey + remoteUrl`，后续可增加 `token + remoteUrl`。

## 验收标准

- AC-BE-001：项目初始化上下文中每个项目分支返回可供前端复制的初始化指令字段，且保留既有 `mcpKey` 兼容字段。
- AC-BE-002：初始化指令内容包含简短触发提示词和唯一 token，token 能被服务端解析为项目分支初始化动作上下文。
- AC-BE-003：需求 token、开发 token 的字段模型与初始化 token 一致，并在契约文档中明确动作类型、权限边界和后续接口用途。
- AC-BE-004：MCP 权限边界保持清晰：`X-MCP-Key` 负责人员鉴权，动作 token 负责上下文定位，二者不能互相替代。
- AC-BE-005：后端用户可见若依品牌信息已清理为统一需求流转平台，不改动高风险底层包名。
- AC-BE-006：后端 harness、数据库关系和领域文档同步记录指令 token、品牌清理边界和兼容策略。
- AC-BE-007：新增或修改需求时，后端拒绝提交到未初始化完成的项目分支；已初始化分支需具备分支模块知识并覆盖该真实分支的有效仓库索引。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/2026-06-10-REQ-003-项目页签化与统一需求流转平台UI`
- 关联分支：未创建，执行阶段建议使用 `feature/REQ-20260610-003-project-tabs-ui`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：未创建

## 约束与假设

- 当前后端仓库处于 `main` 分支；计划阶段不得直接实现代码。
- 新增 token 表需要配套 SQL、Mapper、Service 和测试；明文 token 只在复制指令中出现，服务端落库哈希和前缀。
- 本次优先满足“可复制指令 + 服务端可识别动作上下文”，不把 token 设计成完整审批流。
- 品牌清理以用户可见输出为准，不做大规模源码包名迁移。
