# MCP Key明文持久与交互安装需求说明

## 背景

MCP 管理页已支持 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 的统一安装指令。用户进一步要求：

- MCP Key 始终保持明文可用于安装指令，不再只在创建时一次性返回。
- 统一执行命令执行后应让用户选择要安装的工具，而不是默认安装全部工具。
- MCP 管理页面不展示明文 Key 和 Key 前缀字段。
- MCP 明文 Key 下次打开使用指令时也不能隐藏，必须继续放在安装命令中供用户复制。
- MCP 请求地址配置不易发现，需要直接加到 MCP 管理页面中，并且只有管理员可以配置。
- 用户测试发现安装到 OpenCode 时 MCP 没有安装成功，且要求不只修 OpenCode，也要确保 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 的安装结果边界清晰。
- 需求详情页合并归档阶段需要避免“生成合并归档指令”和“确认归档完成”同时展示，后端需要提供只读归档验证结果供前端判断。
- 本地 Harness `--spec` 只能检查 `docs/specs/active/` 中的执行中需求，避免执行过程误写 `docs/specs/done/`。
- 项目接入初始化下发的 Harness 模板也必须同步上述 `active/` 与 `done/` 边界，避免新项目继续继承旧规范。
- 本地 Harness 在收到归档、办结或结束任务指令时，必须在完成态门禁通过后把需求目录从 `active/` 迁移到 `done/`。

## 目标

- 后端新增并维护 `req_mcp_user_key.plain_key`，新建 Key 时保存明文，后续列表、详情和使用指令接口均可返回。
- 鉴权继续使用 `key_hash`，不因为保存明文改变认证路径。
- 安装包顶层 `installCommands[]` 只展示一组统一指令；脚本不传 client 时交互选择工具，传 `all` 或单个客户端时保持自动化能力。
- 前端列表和结果弹窗不单独展示明文 Key、Key 前缀或哈希，只用 `plainKey` 渲染可复制命令。
- 后端提供管理员专用 `/requirement/mcp/key/config` 读写接口，用于在 MCP 管理页维护 `reqflow.mcp.public-host`。
- MCP 管理页仅管理员展示“配置请求地址”入口，点击后以弹窗维护请求地址；开发人员不展示入口，仍可管理自己有权限的 MCP Key。
- 安装脚本对能自动落盘的客户端必须真实写入或合并配置，对只能生成 JSON 片段的客户端必须输出 `Manual MCP import required`，不得笼统提示 MCP 已安装。
- 同步 API 契约、数据库字典、模块知识库和 companion 前端文档。
- 收紧 `check-harness.sh --spec` 目标路径，拒绝 `docs/specs/done/` 并补充自测。
- 同步 `ruoyi-requirement/src/main/resources/harness-template/` 下发模板中的流程说明、检查脚本和自测。
- MCP 合并归档指令、全局 skill 和本地 Harness 流程都明确：先在任务分支完成 `active -> done` 迁移，再 squash merge 到需求基线分支。
- 新增合并归档验证只读接口，复用 `closeout_pending -> completed` 的平台验证口径，前端只在验证通过后展示确认归档完成。

## 非目标

- 不回填升级前只保存哈希的历史 Key 明文，历史记录无法从哈希反推明文，需要重新生成 Key。
- 不改变 MCP actionToken 的一次性和阶段性规则。
- 不自动调用任何 reqflow MCP tool。

## 验收标准

- AC-001：创建 MCP Key 时，保存 `plain_key`、`key_prefix` 和 `key_hash`，返回 `plainKey`。
- AC-002：打开历史 Key 使用指令时，返回已保存的 `plainKey`，前端从顶层 `plainKey` 或 `key.plainKey` 渲染真实安装命令，不写入隐藏或占位 Key。
- AC-003：顶层统一命令不再带 `--client all` 或 `-Client "all"`，脚本执行后交互选择工具。
- AC-004：脚本仍支持 `--client all|codex|claude-code|trae|qoder|codebuddy|opencode` 和 PowerShell `-Client` 自动化安装。
- AC-005：MCP 管理页不展示明文 Key 或 Key 前缀字段。
- AC-006：数据库和 harness 文档同步新字段、API 语义和页面展示约束。
- AC-007：`check-harness.sh complete --spec docs/specs/done/...` 必须失败，并提示使用 `docs/specs/active/`。
- AC-008：`get_harness_template` 下发的 Harness 模板必须包含同样的 active-only `--spec` 约束、done 目录失败自测和流程说明。
- AC-009：收到归档、办结或结束任务指令时，本地 Harness 和 MCP 合并归档指令都必须要求先完成 active spec 门禁，再通过 `git mv "$SPEC_DIR" docs/specs/done/` 归档，最后再合并归档分支。
- AC-010：`/requirement/mcp/key/config` 仅 `admin` 角色可读写，返回 `configKey`、`publicHost` 和完整 `mcpAddress`，保存时拒绝协议、路径、查询串或空白字符。
- AC-011：MCP 管理页仅管理员展示 MCP 请求地址配置入口，点击后以弹窗保存 `publicHost` 并展示后端返回的完整地址，普通开发人员不可配置。
- AC-012：OpenCode 已有 `~/.config/opencode/opencode.json` 时，脚本选择 `opencode` 必须自动合并 `mcp.reqflow` 并保留既有配置；Codex、Claude Code、CodeBuddy、OpenCode 自动配置成功时输出 `Reqflow automatic MCP configuration completed`，Trae、Qoder 或自动合并失败场景输出 `Manual MCP import required` 和片段路径。
- AC-013：`/requirement/demand/{demandId}/closeout-verification` 返回 `{ verified, message }`，复用合并归档办结验证口径；未通过时不更新需求状态并返回未通过原因，通过后返回 `verified=true`。
- AC-014：需求创建人或管理员在 `review` 待验收状态提交返修时，必须通过 `/requirement/demand/{demandId}/repair` 提交非空返修问题说明；服务端追加 `requirement_supplement` 版本并进入 `repairing`，普通 `/status/repairing` 必须拒绝并提示先填写返修问题说明。
- AC-015：指定开发人员在 `repairing` 返修中状态提交返修验收前，服务端必须校验最新“需求人返修问题说明”之后已有新的 `execution_report` 和 `review_report`；未回写齐全时拒绝 `repairing -> review`，提示先复制返修任务指令并回写返修执行报告和 Review 报告。
- AC-016：返修阶段 MCP `upload_execution_report` 或 `upload_review_report` 上传完整本地报告时按完整报告保存新版本；只上传返修片段时，服务端必须基于上一版报告追加“返修执行记录”或“返修 Review 记录”后保存新版本，不得让返修片段成为最新版全文并掩盖原执行报告或 Review 报告。

## 影响范围

- 接口：是，`/requirement/mcp/key/**` 返回 `plainKey` 的长期语义变化，并新增管理员 `/requirement/mcp/key/config` 读写接口。
- 需求流程接口：是，新增 `/requirement/demand/{demandId}/closeout-verification` 只读接口。
- 返修流程接口：是，新增 `/requirement/demand/{demandId}/repair` 提交返修问题说明。
- MCP 回写语义：是，返修阶段执行报告和 Review 报告上传支持完整文件或增量片段，服务端保存新版本时保留上一版正文。
- 数据库：是，新增 `req_mcp_user_key.plain_key`。
- 权限：是，MCP 请求地址配置使用 `admin` 角色控制，不扩展给开发人员角色。
- 页面展示：是，MCP 管理页隐藏明文 Key 和 Key 前缀字段，统一命令执行后选择工具；管理员额外通过弹窗配置 MCP 请求地址。
- 安装脚本：是，OpenCode/CodeBuddy 已有可解析 JSON 配置时自动合并，Trae/Qoder 或无法自动合并的配置明确提示手工导入。
- 流程门禁：是，当前仓库和项目接入初始化 Harness 模板中的 `check-harness.sh --spec` 都只允许指向 `docs/specs/active/`；收到归档、办结或结束任务指令时，完成态门禁通过后必须迁移到 `docs/specs/done/`。
