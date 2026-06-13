# MCP Key明文持久与交互安装执行报告

## 执行结论

- 状态：已完成
- 分支：feature/req-020-mcp-multi-client-setup
- commit：待提交
- 流程模式：本地 Harness 模式
- MCP 回写：未接入 MCP，本地文件闭环

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ReqMcpUserKey.java`、`ReqMcpUserKeyMapper.xml`、`ReqMcpUserKeyServiceImpl.java` | 新增 `plainKey/plain_key` 映射，新建 Key 时保存明文，打开使用指令时返回已保存明文，鉴权仍使用哈希。 |
| `ReqflowCodexSetupPackageTemplate.java`、`ReqflowCodexInstallScriptTemplate.java` | 顶层统一命令不再默认 `all`，脚本未传 client 时交互选择工具，传 `all` 或单个客户端时直接安装。 |
| `docs/db/sql/req_platform_schema.sql`、`docs/db/sql/req_platform_mcp_key_plain_key.sql` | 基线表结构和已有库幂等升级脚本新增 `plain_key`。 |
| `ReqMcpUserKeyServiceImplTest.java`、`ReqflowCodexSetupPackageTemplateTest.java`、`ReqCodexInstallControllerTest.java` | 覆盖明文持久、历史指令返回明文、统一命令交互选择和脚本内容。 |
| `docs/ai-harness/**`、`docs/db/**`、`docs/process/local-harness-workflow.md` | 同步 API、模块、数据库、展示约束和 `active/`/`done/` 执行边界。 |
| `scripts/check-harness.sh`、`scripts/test-check-harness.sh` | 限制 `--spec` 只能指向 `docs/specs/active/`，并补充 done 目录失败用例。 |
| `../reqflow-ui/src/views/requirement/mcpKey/index.vue`、`../reqflow-ui/scripts/test-mcp-install-dialog-unified.js` | 页面不展示明文 Key 和 Key 前缀字段，仅用明文渲染统一安装命令；静态检查防回归。 |

## 模块知识库沉淀

- 影响模块：MCP 管理、MCP Key 持久化、多客户端安装脚本
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`、`docs/ai-harness/contracts/requirement-platform-api.md`

## 数据库变更沉淀

- 数据库影响：新增字段
- SQL 脚本路径：`docs/db/sql/req_platform_schema.sql`、`docs/db/sql/req_platform_mcp_key_plain_key.sql`
- 数据库文档路径：`docs/db/README.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md`
- 数据库变更说明：`req_mcp_user_key` 新增 `plain_key varchar(128)`，用于后续安装命令渲染。升级前无明文的历史 Key 不能从哈希恢复，需要重新生成。

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：现有注释已能说明哈希和明文字段职责，脚本交互逻辑由测试和文档覆盖。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L2 | AC-001、AC-002、AC-003、AC-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqflowCodexSetupPackageTemplateTest,ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，15 个测试通过 |
| L2 | AC-005 | `node scripts/test-mcp-install-dialog-unified.js`（companion 前端） | 通过 |
| L2 | AC-007 | `sh scripts/test-check-harness.sh` | 通过 |
| L1 | AC-005 | `npm run build:prod`（companion 前端） | 通过，存在历史体积告警 |
| L0 | AC-006、AC-007 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-022-MCP-Key明文持久与交互安装` | 通过 |

## 运行态证据

- 执行目录：当前后端子仓库根目录、companion 前端子仓库根目录
- 启动命令：未启动服务
- profile/env/mode：本地单测、静态检查和构建验证
- 原始错误摘要：后端先红于缺少 `plainKey` 持久字段和交互脚本入口；前端先红于列表未按新约束隐藏字段
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：如需真实客户端安装，应在用户本机执行统一命令并选择目标工具验证。

## 计划偏差

- 用户补充“不展示明文 Key、Key 前缀字段”，已调整为后端返回明文但前端仅用于命令渲染。
- 用户指出执行中不应写 `docs/specs/done/`，已将当前 spec 移回 `active/`，并收紧 `check-harness.sh --spec` 目标路径。

## Review 返修记录

无。
