# MCP Key明文持久与交互安装Review报告

## Review 结论

- 结论：通过
- Review 类型：本地自检
- 范围：后端 Key 持久化、安装脚本交互、MCP 请求地址配置、数据库文档、companion 前端展示约束

## 检查结果

| 检查项 | 结论 | 说明 |
|---|---|---|
| 明文持久 | 通过 | 覆盖 AC-001、AC-002。新建 Key 保存 `plain_key`，使用指令返回已保存明文，哈希认证路径未改变。 |
| 页面展示 | 通过 | 覆盖 AC-005。前端不再展示明文 Key 和 Key 前缀字段，只用明文渲染命令。 |
| 明文命令复开 | 通过 | 覆盖 AC-002。前端兼容顶层 `plainKey` 和 `key.plainKey`，下次打开使用指令仍渲染真实安装命令。 |
| 请求地址配置 | 通过 | 覆盖 AC-010、AC-011。配置接口仅 admin 角色可读写，页面仅管理员展示入口并用弹窗配置，保存值限制为 host/port。 |
| 交互安装 | 通过 | 覆盖 AC-003、AC-004。顶层统一命令不默认 `all`，脚本未传 client 时提示用户选择；自动化参数保留。 |
| 多客户端安装结果 | 通过 | 覆盖 AC-012。OpenCode 已有 JSON 配置会自动合并 `mcp.reqflow`，CodeBuddy 在 CLI 不可用时写入或合并用户级配置；Trae/Qoder 和无法自动合并的配置列入 `Manual MCP import required`，不再误报 MCP 已安装。 |
| 数据库闭环 | 通过 | 覆盖 AC-006。基线 SQL、幂等升级 SQL、表字典和关系说明已同步。 |
| 敏感信息 | 通过 | 覆盖 AC-001、AC-002、AC-005。创建操作日志仍关闭响应保存，实体 `toString()` 不输出 `plainKey`。 |
| Harness 门禁 | 通过 | 覆盖 AC-007。`--spec` 指向 `docs/specs/done/` 会失败，当前执行 spec 保持在 `active/`。 |
| Harness 模板 | 通过 | 覆盖 AC-008。项目接入初始化模板已同步 active-only `--spec` 约束、done 失败自测和流程说明，并由模板脚本自测与 `McpServiceTest` 覆盖。 |
| 归档收尾 | 通过 | 覆盖 AC-009。本地 Harness、MCP 合并归档指令、全局 skill 和初始化模板均要求完成态门禁通过后 `git mv` 到 `docs/specs/done/`，再合并归档分支。 |
| 归档验证接口 | 通过 | 覆盖 AC-013。`closeout-verification` 复用办结前逐仓验证口径，只读返回 `verified/message`，不推进需求状态。 |
| 返修问题说明 | 通过 | 覆盖 AC-014。需求人提交返修必须走 `/requirement/demand/{demandId}/repair` 并填写问题说明，普通 `/status/repairing` 会被拒绝。 |
| 返修验收门禁 | 通过 | 覆盖 AC-015。开发人员提交返修验收前，服务端要求最新返修说明之后的新执行报告和 Review 报告都已回写。 |

## 风险说明

- 升级前历史 Key 没有明文，无法恢复；需要用户重新生成 Key。
- 明文持久化本身是明确产品选择，后续必须避免把 `plainKey` 写入操作日志、活动记录、本地存储或列表字段。
- MCP 请求地址只保存 host/port，完整地址由后端按协议、context-path 和 `/requirement/mcp` 生成；部署时不能把前端静态项目名当作 MCP 路径前缀。
- Trae/Qoder 官方路径以设置页 JSON 导入为主，脚本只能生成片段和全局 skill；用户必须按 `Manual MCP import required` 完成导入后再验证 MCP 连接。

## RF项

无。
