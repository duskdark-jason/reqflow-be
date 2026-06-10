# 项目页签化与统一需求流转平台UI 后端 Review 报告

## Review 结论

结论：通过

当前实现满足后端验收要求，未发现阻断或有条件通过问题。按用户授权，Review 通过后允许合并到 `main`。

## 审查范围

- 分支：`feature/REQ-20260610-003-project-tabs-ui`
- 对比基线：`main..35a8718`
- 动作 token：`ReqActionToken`、`ReqActionInstruction`、`IReqActionTokenService`、`ReqActionTokenServiceImpl`、`ReqActionTokenMapper`、`ReqActionTokenMapper.xml`
- 初始化上下文：`ReqProjectInitVariantItem`、`ReqProjectInitServiceImpl`
- 索引导入兼容：`ReqRepositoryIndexImportRequest`、`ReqRepositoryIndexServiceImpl`
- 需求提交收束：`ReqDemandServiceImpl`
- 品牌清理：`application.yml`、`RuoYiApplication.java`、`SwaggerConfig.java`、`banner.txt`、SQL 清理脚本
- 测试与文档：后端服务测试、SQL、后端 API 契约、数据库关系、领域文档和 spec 文档

## 问题清单

未发现阻断或重要问题。

剩余风险：

- 未启动完整后端服务做带登录态的 REST/MCP 真实调用；当前结论基于服务层测试、Mapper 契约、编译打包和文档检查。
- `GET /requirement/project/init/{projectId}` 会为项目分支返回可复制初始化指令，生产环境需要执行 `sql/req_platform_req003_action_token.sql` 后才能使用动作 token 表。
- 动作 token 只定位动作上下文，不替代人员 `X-MCP-Key` 鉴权；后续 MCP 客户端文档和角色配置仍需保持两类 token 的边界。

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-BE-001 | 项目初始化上下文每个分支返回可复制初始化指令并保留 `mcpKey` | `ReqProjectInitVariantItem.initInstruction`、`ReqProjectInitServiceImpl.buildVariantItem` | `ReqProjectInitServiceImplTest`、`mvn -pl ruoyi-requirement -am test` | 通过 |
| AC-BE-002 | 初始化指令包含触发提示词和唯一 token，服务端可解析动作上下文 | `ReqActionTokenServiceImpl` 生成安全随机 token，落库哈希和前缀 | `ReqActionTokenServiceImplTest`、`mvn -pl ruoyi-requirement -am test` | 通过 |
| AC-BE-003 | 需求 token、开发 token 字段模型与初始化 token 一致 | `IReqActionTokenService` 动作类型和 `createInstruction` 统一模型 | `ReqActionTokenServiceImplTest.requirementAndDevelopInstructionsUseTheSameTokenShape` | 通过 |
| AC-BE-004 | `X-MCP-Key` 负责人员鉴权，动作 token 负责上下文定位 | `ReqRepositoryIndexServiceImpl` 仅在索引发布中解析动作 token，不替代人员 Key | 后端契约文档、`ReqRepositoryIndexServiceImplTest`、打包通过 | 通过 |
| AC-BE-005 | 后端用户可见若依品牌信息清理为统一需求流转平台 | 配置、Swagger、启动输出、banner 和品牌清理 SQL | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过 |
| AC-BE-006 | Harness、数据库关系和领域文档同步记录 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/db/relationship.md`、`docs/domains/requirement-platform/README.md` | `sh scripts/check-docs.sh`、`sh scripts/check-harness.sh init` | 通过 |
| AC-BE-007 | 新增或修改需求拒绝未初始化完成的项目分支 | `ReqDemandServiceImpl.validateDemandTargetInitialized` 校验分支归属、模块知识和仓库索引覆盖 | `ReqDemandServiceImplTest`、`mvn -pl ruoyi-requirement -am test` | 通过 |

## 验证评估

执行阶段已有验证覆盖核心逻辑与构建：

- `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：25 个测试通过。
- `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`：3 个测试通过。
- `mvn -pl ruoyi-requirement -am test`：51 个测试通过。
- `mvn -pl ruoyi-admin -am -DskipTests package`：通过。
- `sh scripts/check-docs.sh`：通过。
- `sh scripts/check-harness.sh init`：通过。

Review 后仍需重新执行 `check-harness.sh review` 和合并前后验证。

## 返修交接清单

无。

## 是否允许合并

允许。当前 Review 无阻断项和返修项。
