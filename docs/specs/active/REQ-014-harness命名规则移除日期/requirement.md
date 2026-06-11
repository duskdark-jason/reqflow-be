# harness 命名规则移除日期需求说明

## 背景

当前 harness 仍要求 active spec 使用日期前缀目录，执行包生成的任务分支也会把需求编号中的日期带入分支名。新的流程要求 active spec 和任务分支不再携带日期。

## 目标

- active spec 目录使用 `REQ-001-中文需求标题`，不包含日期前缀。
- 任务分支示例和执行包生成逻辑不再把 `YYYYMMDD` 日期片段带入分支名。
- 项目接入下发的 harness 模板与当前仓库规则保持一致。

## 范围

本次包含：

- 后端 harness 校验脚本、测试、流程文档和项目接入模板。
- 后端需求执行包 `taskBranch` 生成逻辑。
- 当前 active spec 目录和互链迁移为无日期命名。
- companion 前端仓库的 harness 校验、测试、流程文档和 active spec 迁移。

本次不包含：

- 调整需求编号生成规则或历史 done spec。
- 业务接口变更、持久化结构变更、权限或页面业务功能变更。

## 影响范围

- 接口/API：否。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：否。
- 导出/异步/任务：否。

## 契约与数据口径

- 接口路径和方法：无。
- 请求参数：无。
- 响应字段：无。
- 数据粒度：单个 active spec 目录表示一项需求过程文档。

## 验收标准

- AC-001：`check-harness.sh` 接受 `REQ-001-中文需求标题`，并拒绝 active spec 日期前缀。
- AC-002：流程文档、agent prompt 和 harness-template 示例不再要求日期前缀目录或带日期的任务分支编号。
- AC-003：需求执行包从 `REQ-20260611-007` 生成任务分支时输出 `REQ-007`，不携带日期。
- AC-004：现有 active spec 目录和互链迁移到无日期目录名。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/REQ-014-harness命名规则移除日期`
- 关联分支：`fix/harness-naming-no-date`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：fix/harness-naming-no-date

## 约束与假设

- 历史 done spec 保持原样，避免改写已归档证据。
- 需求编号本身仍按现有业务规则保留，本次仅移除 spec 和任务分支命名中的日期要求。
