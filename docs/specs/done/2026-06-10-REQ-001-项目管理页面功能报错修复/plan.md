# 项目管理页面功能报错修复后端执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复/requirement.md`
- 前端 companion：`../../../../../reqflow-ui/docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`、`docs/domains/requirement-platform/README.md`
- 目标客户与基线分支：通用 / `main`

## 实施步骤

1. 运行态复现：在任务分支或 worktree 中启动后端，结合前端 companion 的页面冒烟记录，定位 `/requirement/project/**` 与 `/requirement/project/init/**` 的 HTTP 状态、响应体、权限失败、业务异常和日志摘要，覆盖 AC-BE-001 至 AC-BE-005。
2. 项目 CRUD 检查：核对 `ReqProjectController`、`IReqProjectService`、Mapper XML 和列表分页参数，修复列表、详情、删除接口中的响应结构、查询条件或删除一致性问题，覆盖 AC-BE-001、AC-BE-005。
3. 初始化查询修复：核对 `ReqProjectInitController`、DTO 和 `ReqProjectInitServiceImpl#selectProjectInit`，确保项目、仓库、分支、模块摘要、索引摘要和 `initChecklist` 字段稳定返回，覆盖 AC-BE-002。
4. 初始化新增修复：核对 `insertProjectInit` 的字段校验、个人路径拒绝、仓库/分支必填、`variantCode` 兜底、`mcpKey` 生成和事务边界，覆盖 AC-BE-003、AC-BE-005。
5. 初始化更新修复：核对 `updateProjectInit` 的已有子项更新、新增子项插入、移除子项同步删除、异常回滚和返回上下文刷新，覆盖 AC-BE-004、AC-BE-005。
6. 测试补强：在 `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` 或同模块测试中补充红测/回归测试，覆盖 AC-BE-002 至 AC-BE-004。
7. 文档同步：如修复改变接口字段、权限口径、数据口径、验证命令或风险点，同步更新 `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md` 或 `docs/domains/requirement-platform/README.md`，覆盖 AC-BE-006。
8. 验证收尾：运行后端单元测试、admin 打包、文档/harness 检查和运行态接口冒烟；将错误清单、修改文件、验证命令和残余风险写入执行报告，覆盖 AC-BE-001 至 AC-BE-006。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectController.java` | 仅在项目 CRUD 响应、权限或参数绑定存在问题时修改 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java` | 修复初始化聚合接口权限、路径、响应或参数绑定问题 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 修复初始化查询、新增、更新、事务、路径拒绝和子项同步逻辑 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/*.java` | 仅在响应字段或请求字段与前端契约不一致时修改 |
| 修改 | `ruoyi-requirement/src/main/resources/mapper/requirement/*.xml` | 仅在查询条件、字段映射或同步删除 SQL 存在问题时修改 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 补充项目初始化查询、新增、更新和事务回归测试 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步接口字段、权限、错误语义或数据口径变化 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步项目管理后端不变量、风险点或验证建议 |
| 修改 | `docs/domains/requirement-platform/README.md` | 仅在业务形态或项目初始化口径发生长期变化时修改 |

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`；`sh scripts/check-harness.sh review --spec docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`；必要时运行 `mvn -pl ruoyi-requirement -am test`
- L3 运行态冒烟：启动后端后检查 `/requirement/project/list`、`/requirement/project/init/{projectId}`、`POST /requirement/project/init`、`PUT /requirement/project/init`、`DELETE /requirement/project/{projectIds}` 在未登录、无权限和认证态下的响应；记录执行目录、启动命令、检查命令、HTTP 状态和错误摘要。
- L4 跨端/端到端：与前端 companion 使用真实登录态验证项目管理页面新增、编辑回显、仓库行删除、分支行删除、保存并进入接入中心、删除项目；如认证态或数据库不满足条件，执行报告必须记录补验环境。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 运行态复现、项目 CRUD 检查、验证收尾 | 后端接口冒烟、Mapper/Service 检查、admin 打包 |
| AC-BE-002 | 初始化查询修复、测试补强、验证收尾 | 初始化查询测试、接口冒烟、admin 打包 |
| AC-BE-003 | 初始化新增修复、测试补强、验证收尾 | 新增初始化测试、路径拒绝测试、接口冒烟 |
| AC-BE-004 | 初始化更新修复、测试补强、验证收尾 | 更新同步删除测试、事务回滚测试、接口冒烟 |
| AC-BE-005 | 项目 CRUD 检查、初始化新增修复、初始化更新修复、验证收尾 | 权限注解检查、未授权/认证态接口冒烟 |
| AC-BE-006 | 文档同步、验证收尾 | L0 文档和 harness 检查 |

## 执行约束

- 本计划完成后仍只代表计划阶段完成；开始实现必须另有明确执行授权。
- 当前分支为 `main`，Execution Agent 必须先获得任务分支/worktree 授权，或获得明确主分支修改授权并写入 `meta.md`。
- Execution Agent 必须先结合前端冒烟和后端日志形成接口错误清单，再修复；不得仅凭静态阅读宣称页面报错已解决。
- Execution Agent 必须保持 RuoYi 通用响应结构，不得引入新依赖或改变全局安全模型。
- Execution Agent 不得自行扩大到需求列表、执行包、统计或 MCP 索引导入修复。
- Execution Agent 不得自我 Review；进入 Review 必须有明确 Review 授权或独立 Review 请求。
