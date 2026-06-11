# 需求列表页签化与知识库提需执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 目标客户与基线分支：通用/main
- 影响模块：需求管理/需求列表、需求管理/Agent 交接资料
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 需求编号测试：补充服务测试，证明新增保存会覆盖前端传入编号并自动生成编号，覆盖 AC-BE-001。
2. 任务分支测试：补充执行包服务测试，锁定 `fix-功能模块-编号-标题` 语义和 ASCII slug 输出，覆盖 AC-BE-002。
3. 模块名解析：实现执行包上下文从人工模块、索引模块或新功能名称解析模块名，覆盖 AC-BE-003。
4. 新功能提需校验：调整需求保存兜底校验，允许已有仓库索引批次但没有既有模块知识的项目分支提交新功能需求，覆盖 AC-BE-004。
5. 执行包生成：调整 `ReqPackageServiceImpl` 的 `taskBranch` 生成规则，覆盖 AC-BE-002、AC-BE-003。
6. 文档同步：更新后端契约和模块 harness，覆盖 AC-BE-005。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImpl.java` | 明确新增编号覆盖行为，并允许有索引证据但没有既有模块的新功能提需。 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqPackageServiceImpl.java` | 生成新任务分支并解析模块名。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqDemandServiceImplTest.java` | 补充需求编号覆盖测试和无既有模块的新功能提需测试。 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqPackageServiceImplTest.java` | 补充任务分支格式测试。 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步接口和执行包契约。 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步模块规则。 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，记录需求执行包任务分支命名和新功能名称通过需求备注兼容承载。

## 代码注释计划

- 对任务分支 slug 生成补短注释，说明命名既承载业务语义又必须适配 Git 分支字符集。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`，`sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-001-需求列表页签化与知识库提需`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest,ReqPackageServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L3 运行态冒烟：若当前环境可启动，调用需求保存和执行包生成接口；无法启动时记录当前执行环境证据。
- L4 跨端/端到端（可选）：本次不默认执行，跨端提需保存可在具备登录态环境补验。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 需求编号测试 | 指定后端单测 |
| AC-BE-002 | 任务分支测试、执行包生成 | 指定后端单测 |
| AC-BE-003 | 模块名解析 | 指定后端单测 |
| AC-BE-004 | 新功能提需分支校验 | 指定后端单测 |
| AC-BE-005 | 文档同步与全量验证 | L0、L1、L2 命令 |
