# 项目管理初始化向导后端执行报告

## 执行结论

- 状态：已完成后端代码实现，等待 Review Agent 复核。
- 分支：feature/REQ-003-project-init-wizard
- commit：`641af12 feat: 新增项目初始化聚合接口`；`d58500c fix: 保留初始化更新新增子项`；本报告所在提交以 `git log -1` 为准

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java` | 新增项目初始化聚合查询、新增和修改接口 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/dto/*ProjectInit*.java` | 新增初始化请求、响应、仓库项、分支项、摘要和检查项 DTO |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/IReqProjectInitService.java` | 新增项目初始化聚合服务接口 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 实现项目、仓库、分支配置、模块摘要和索引摘要聚合；实现事务保存、本机路径拒绝和 `variantCode` 兜底生成 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqRepositoryMapper.java` | 新增按项目删除仓库和保留 ID 删除方法 |
| `ruoyi-requirement/src/main/resources/mapper/requirement/ReqRepositoryMapper.xml` | 新增仓库同步删除 SQL |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/ReqVariantMapper.java` | 新增按项目删除分支配置和保留 ID 删除方法 |
| `ruoyi-requirement/src/main/resources/mapper/requirement/ReqVariantMapper.xml` | 新增分支配置同步删除 SQL |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 增加初始化聚合读取、保存、路径拒绝、同步删除、新增子项保留和分支标签兼容测试 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 同步项目初始化接口契约 |
| `docs/db/relationship.md` | 同步初始化聚合的数据粒度、关系和同步删除风险 |
| `docs/domains/requirement-platform/README.md` | 同步需求平台领域当前状态 |

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L1 | AC-BE-001, AC-BE-002, AC-BE-003, AC-BE-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest#acceptsBranchLabelAndRealBranchNameWithoutManualVariantCode -Dsurefire.failIfNoSpecifiedTests=false test` | 先因缺少 `branchLabel` 编译失败，补齐 DTO 和 Service 后通过；验证只填分支中文标签和真实分支名也可保存 |
| L1 | AC-BE-002, AC-BE-003 | `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest#generatesDistinctVariantCodeWhenBranchNameHasNoAsciiToken -Dsurefire.failIfNoSpecifiedTests=false test` | 先失败后修复并通过；验证中文真实分支名会生成 `BRANCH_` 加 hash 的稳定兼容编码，避免多条分支撞固定编码 |
| L1 | AC-BE-001, AC-BE-002, AC-BE-003, AC-BE-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest#keepsNewChildrenInsertedDuringAggregateUpdate -Dsurefire.failIfNoSpecifiedTests=false test` | 先失败后修复并通过，验证更新维护弹窗中新插入的仓库和分支配置不会被同步删除 |
| L2 | AC-BE-001, AC-BE-002, AC-BE-003, AC-BE-004 | `mvn -pl ruoyi-requirement -am test` | 通过，21 个测试通过 |
| L1 | AC-BE-001, AC-BE-002, AC-BE-005 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，Controller、权限注解和 admin 打包可编译 |
| L0 | AC-BE-006 | `sh scripts/check-docs.sh` | 通过 |
| L0 | AC-BE-006 | `sh scripts/check-harness.sh review --spec docs/specs/active/2026-06-09-REQ-003-项目管理初始化向导` | 通过 |
| L3 | AC-BE-001, AC-BE-002, AC-BE-005 | 登录态 REST 冒烟：查询初始化上下文、新增初始化、编辑回显 | 未执行；当前执行 agent 未持有可使用的测试账号或 token |
| L4 | AC-BE-001, AC-BE-002, AC-BE-004, AC-BE-005 | 与前端初始化向导联调新增、编辑、列表状态刷新 | 未执行；需要前后端登录态和测试数据同时具备后补验 |

## 运行态证据

- 执行目录：`reqflow-be` 仓库根目录
- 启动命令：未执行后端运行态启动；本轮以后端测试和 admin 打包作为实现验证
- profile/env/mode：本地 Maven 构建和单元测试环境
- 检查命令：`mvn -pl ruoyi-requirement -am -Dtest=ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`、`mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package`
- 原始错误摘要：新增 `keepsNewChildrenInsertedDuringAggregateUpdate` 用例首次执行失败，显示同步删除保留 ID 只有旧仓库 ID；新增 `acceptsBranchLabelAndRealBranchNameWithoutManualVariantCode` 用例首次编译失败，显示 DTO 缺少 `branchLabel`；新增 `generatesDistinctVariantCodeWhenBranchNameHasNoAsciiToken` 用例首次失败，显示中文分支名兜底编码不够唯一；修复后定向测试和全量 requirement 测试均通过
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：具备测试账号或 token 的本地或测试环境，继续验证登录态 REST 和前后端联调

## 计划偏差

- 需求计划中提到“失败回滚测试”，当前以服务层写入前的路径拒绝测试证明校验失败不触发 Mapper 写入；未在真实事务容器中构造数据库回滚测试。
- 登录态 REST 冒烟和 L4 跨端保存回显未执行，原因是当前执行 agent 未持有可使用的测试账号或 token。

## Review 返修记录

- 暂无。等待 Review Agent 复核后补充 RF 项处理结果。

## 风险与后续

- 初始化更新会按维护弹窗提交的仓库和分支配置列表同步删除缺失项；前端必须避免在未加载完整上下文时提交不完整列表。
- 当前实现不新增数据库表字段，初始化状态来自现有项目、仓库、分支配置、模块和索引批次派生。
- 平台仍不保存个人本机目录；本机仓库目录只属于 MCP 本地索引执行时的临时输入。
