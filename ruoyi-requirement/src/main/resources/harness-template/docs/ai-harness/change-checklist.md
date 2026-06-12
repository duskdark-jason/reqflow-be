# Harness 变更检查清单

任务完成前检查本文件。勾中的项目如果属于长期知识，应同步更新对应 harness 文档。

## 接口契约

- [ ] 新增或修改接口路径。
- [ ] 修改请求方式、参数位置、分页字段或请求结构。
- [ ] 修改响应字段、嵌套结构、枚举值、时间格式或空值语义。
- [ ] 修改错误提示、异常处理或用户可见错误语义。

## 数据与口径

- [ ] 新增或修改数据库表、字段、索引或约束。
- [ ] 修改 SQL、join、聚合、分页或统计口径。
- [ ] 修改权限范围、机构范围、时间范围或逻辑删除条件。
- [ ] 如果项目存在 `docs/db/`，已读取 `docs/db/README.md`、`docs/db/table-dictionary.md` 和 `docs/db/relationship.md`。
- [ ] 数据库变更已在当前 spec 目录填写数据库变更说明，或说明无需单独 SQL/迁移说明的原因。
- [ ] DDL、DML、迁移或清理脚本已沉淀到稳定 `docs/db/sql/` 路径，并在执行报告记录路径。
- [ ] Mapper、join、聚合、分页粒度或统计口径变化已同步 `docs/db/relationship.md`，或在执行报告说明无需更新原因。

## 页面与交互

- [ ] 新增或修改页面、组件、表格、表单、弹窗或抽屉。
- [ ] 新增或修改菜单目录、子菜单、隐藏页签、路由或入口按钮。
- [ ] 修改 loading、empty、error、saving、done 等状态。
- [ ] 修改导出、历史记录、异步任务或长耗时流程。

## 权限与安全

- [ ] 修改菜单、路由、按钮或后端权限。
- [ ] 新增可能包含敏感信息的日志或导出。

## 需求平台流程

- [ ] 如果任务来自需求平台 Key，已按 `docs/process/platform-key-workflow.md` 判断需求设计模式、开发模式或项目接入初始化模式。
- [ ] 如果没有需求平台 Key、未接入 MCP 或 MCP 不可用，已按 `docs/process/local-harness-workflow.md` 使用本地 Harness 模式。
- [ ] 本地 Harness 模式下未把本地文件写入伪造成 MCP 回写成功。
- [ ] 本地或平台需求设计阶段仍停留在 `planning`，需求确认前未生成 `plan.md`、`execution-report.md` 或 `review-report.md`。
- [ ] 项目接入初始化时，已校验目标 workspace Git 远端与需求平台登记一致，先拉取默认基线最新代码，并同步下发 workspace `AGENTS.md` 和子仓库 harness。
- [ ] 项目接入初始化校验通过后，已提交并推送初始化生成或升级的文件，并把 commit、push 结果回写需求平台。
- [ ] 项目接入初始化时，已生成至少一个 `docs/ai-harness/modules/*.md` 非模板模块文档，并按菜单目录、子菜单、接口、权限和涉及文件建立初始索引。
- [ ] 需求平台需求设计模式下，已在最新目标基线上创建或切换到平台建议的 ASCII 任务分支，先回写需求可行性评估；结论允许继续后才生成/调整 `requirement.md`。
- [ ] 需求平台开发模式下，已校验当前分支为需求设计阶段任务分支，未在开发阶段另建不同任务分支。

## 模块知识库

- [ ] 已读取 `docs/ai-harness/search-map.md` 并按关键词定位相关模块、契约、决策或数据库文档。
- [ ] `meta.md` 已记录影响模块。
- [ ] `meta.md` 已记录模块知识库动作：新增、更新或无需更新。
- [ ] 如果模块知识库动作是新增或更新，已同步 `docs/ai-harness/modules/*.md`。
- [ ] 模块文档已对齐前端菜单目录、子菜单或隐藏页签，并记录对应功能接口、权限标识和涉及文件。
- [ ] 如果模块知识库动作是无需更新，已在 `meta.md` 或执行报告写明原因。
- [ ] 新增、拆分、重命名模块、契约、决策、菜单、接口、权限、数据库口径或核心流程时，已同步 `docs/ai-harness/search-map.md`。

## 验证

- [ ] 已按 `verification.md` 判断需要验证到 L0/L1/L2/L3 哪些层；L4 仅在本次风险需要时选择。
- [ ] 代码修改已运行编译或构建。
- [ ] 业务逻辑、接口契约或组件契约有专项测试时已运行。
- [ ] 接口、权限、配置、页面或外部依赖变更已做运行态冒烟，或已说明无法验证原因。
- [ ] 如计划选择 L4，保存、导出、历史记录、异步任务或跨端流程变更已完成对应验证，或已说明无法验证原因。
- [ ] L3 未通过，或已选择 L4 但未通过时，已记录启动命令、执行目录、profile/env 或 mode、检查命令和错误摘要。
- [ ] 环境结论只描述当前执行 agent 环境，不推断用户本机或测试环境不可用。
- [ ] 文档或 harness 变更后已运行 `sh scripts/check-docs.sh`。
- [ ] Harness 初始化或纯文档接入已运行 `sh scripts/check-harness.sh init`，且未启动项目、未跑业务测试。
- [ ] 真实需求完成态已运行 `sh scripts/check-harness.sh complete`。
- [ ] Review 阶段使用 `sh scripts/check-harness.sh review`，完成态使用 `sh scripts/check-harness.sh complete`。
- [ ] 无法运行的命令已说明原因。
