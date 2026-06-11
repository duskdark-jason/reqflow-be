# 未办结任务清理与接入中心删除执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求管理、项目管理、项目维护、AI Harness
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 历史任务清理：核对 `REQ-012`、`REQ-013`、`REQ-014` 对应提交已在主干，补齐完成态记录并移入 `docs/specs/done`，覆盖 AC-001。
2. 后端知识库同步：删除“项目接入中心”独立入口描述，改为项目维护和分支知识库说明，覆盖 AC-002。
3. 前端 companion 配合：由前端删除入口、路由和页面文件，后端记录 companion 验收依赖，覆盖 AC-004。
4. 收口报告：补齐本次执行报告和 Review 报告，记录验证命令和影响范围，覆盖 AC-003。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 移动 | `docs/specs/active/REQ-012-CodexSkillYAML修复` | 已合入主干，归档到完成区。 |
| 移动 | `docs/specs/active/REQ-013-初始化指令简化与索引表核对` | 已合入主干，归档到完成区。 |
| 移动 | `docs/specs/active/REQ-014-harness命名规则移除日期` | 已完成但未归档，归档到完成区。 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步项目维护和分支知识库长期入口。 |
| 新增 | `docs/specs/active/REQ-015-未办结任务清理与接入中心删除/*` | 记录本次清理与 companion 页面删除需求。 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`。
- 模块文档删除“项目接入中心”独立入口，保留项目维护、分支知识库和需求列表等长期入口。

## 代码注释计划

- 后端不修改业务代码，无需新增代码注释。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`
- L0 Harness：`sh scripts/check-harness.sh complete --spec docs/specs/done/REQ-015-未办结任务清理与接入中心删除`
- L1 编译/构建：不适用，后端不改业务代码。
- L2 单元/契约：不适用，后端不改接口或业务逻辑。
- L3 运行态冒烟：不适用，后端只做过程文档和知识库清理。
- L4 跨端/端到端：由前端 companion 静态检查和构建验证覆盖页面链路删除。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-001 | 历史任务清理 | `find docs/specs/active docs/specs/done`、harness 完成态检查 |
| AC-002 | 后端知识库同步 | `rg "项目接入中心" docs/ai-harness/modules/requirement-platform.md` |
| AC-003 | 收口报告 | `sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec ...` |
| AC-004 | 前端 companion 配合 | 前端 `node scripts/test-access-center-status.js` 和 `npm run build:prod` |

## 执行约束

- 只清理已确认合入主干的 active spec。
- 不直接修改本地平台库需求状态。
- 不删除仍被项目维护和分支知识库使用的后端接口。
- 任务分支模式下完成修改和验证后直接提交；merge、push、rebase 仍需用户确认。
