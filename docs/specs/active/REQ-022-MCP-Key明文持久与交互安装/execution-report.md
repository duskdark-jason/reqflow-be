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
| `ReqflowCodexInstallScriptTemplate.java`、`ReqflowCodexSetupPackageTemplate.java`、`ReqCodexInstallControllerTest.java`、`ReqflowCodexSetupPackageTemplateTest.java` | 修复 OpenCode 已有配置只输出片段导致 MCP 未实际安装的问题；OpenCode/CodeBuddy 可解析 JSON 配置自动合并，Trae/Qoder 或无法自动合并场景输出 `Manual MCP import required`，最终输出区分自动配置、手工导入和 skill 安装结果。 |
| `ReqMcpKeyController.java`、`ReqMcpPublicHostConfig.java` | 新增管理员专用 MCP 请求地址配置读写接口，保存 `reqflow.mcp.public-host` 并回显完整 MCP 地址。 |
| `docs/db/sql/req_platform_schema.sql`、`docs/db/sql/req_platform_mcp_key_plain_key.sql` | 基线表结构和已有库幂等升级脚本新增 `plain_key`。 |
| `ReqMcpUserKeyServiceImplTest.java`、`ReqflowCodexSetupPackageTemplateTest.java`、`ReqCodexInstallControllerTest.java` | 覆盖明文持久、历史指令返回明文、统一命令交互选择和脚本内容。 |
| `ReqMcpKeyControllerTest.java` | 覆盖 MCP 请求地址配置接口 admin-only、地址回显、插入系统配置和非法 URL 拒绝。 |
| `docs/ai-harness/**`、`docs/db/**`、`docs/process/local-harness-workflow.md` | 同步 API、模块、数据库、展示约束和 `active/`/`done/` 执行边界。 |
| `scripts/check-harness.sh`、`scripts/test-check-harness.sh` | 限制 `--spec` 只能指向 `docs/specs/active/`，并补充 done 目录失败用例。 |
| `ruoyi-requirement/src/main/resources/harness-template/docs/process/local-harness-workflow.md`、`ruoyi-requirement/src/main/resources/harness-template/scripts/check-harness.sh`、`ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh` | 同步项目接入初始化下发模板的 active-only `--spec` 约束、流程说明和自测。 |
| `McpServiceTest.java` | 锁定 `get_harness_template` 下发内容必须包含 active-only 约束、done 失败用例和流程说明。 |
| `ReqDemandServiceImpl.java`、`ReqflowCodexGlobalSkillTemplate.java`、`docs/process/**`、`docs/specs/README.md`、`ruoyi-requirement/src/main/resources/harness-template/docs/**` | 同步归档、办结、结束任务时的 `active -> done` 收尾规范，要求在任务分支完成迁移后再 squash merge。 |
| `ReqDemandServiceImplTest.java`、`ReqflowCodexGlobalSkillTemplateTest.java`、`McpServiceTest.java` | 覆盖归档指令、全局 skill 和模板下发内容必须包含 `git mv "$SPEC_DIR" docs/specs/done/` 收尾动作。 |
| `ReqDemandController.java`、`IReqDemandService.java`、`ReqDemandServiceImpl.java`、`ReqCloseoutVerificationResult.java`、`ReqDemandServiceImplTest.java` | 新增合并归档验证只读接口，供前端按平台验证结果互斥展示合并归档指令和确认归档完成。 |
| `ReqDemandController.java`、`IReqDemandService.java`、`ReqDemandServiceImpl.java`、`ReqDemandServiceImplTest.java` | 新增返修问题说明提交接口，服务端禁止普通状态接口直接提交返修，返修说明追加为 `requirement_supplement` 版本。 |
| `ReqDemandServiceImpl.java`、`ReqDemandServiceImplTest.java` | 提交返修验收前校验最新返修问题说明之后的新执行报告和 Review 报告，旧报告不能直接复用。 |
| `McpService.java`、`McpServiceTest.java` | 返修阶段 MCP 上传执行报告或 Review 报告时，片段内容会基于上一版追加返修记录，完整报告内容直接保存为新版本，避免最新版报告丢失原正文。 |
| `ReqDemandServiceImpl.java`、`ReqDemandServiceImplTest.java`、`ReqflowCodexGlobalSkillTemplate.java`、`ReqflowCodexGlobalSkillTemplateTest.java` | 将需求分析、需求生成、开发、返修和合并归档复制指令压缩为短动态上下文；全局 skill 增加 `stage -> MCP tool` 映射表承接详细执行规则。 |
| `../reqflow-ui/src/api/requirement/mcpKey.js`、`../reqflow-ui/src/views/requirement/mcpKey/index.vue`、`../reqflow-ui/scripts/test-mcp-install-dialog-unified.js` | 页面不展示明文 Key 和 Key 前缀字段，仅用明文渲染统一安装命令；管理员通过弹窗配置 MCP 请求地址；静态检查防回归。 |

## 模块知识库沉淀

- 影响模块：MCP 管理、MCP 请求地址配置、MCP Key 持久化、多客户端安装脚本、本地 Harness 门禁、合并归档指令、需求返修流程、返修 MCP 报告回写、阶段短指令与全局 skill
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
| L2 | AC-001、AC-002、AC-003、AC-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqflowCodexSetupPackageTemplateTest,ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，16 个测试通过 |
| L2 | AC-012 | `mvn -pl ruoyi-requirement -am -Dtest=ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，先红于 OpenCode 已有配置未合并，修复后 4 个测试通过 |
| L2 | AC-012 | 生成 `install.sh` 后用临时 HOME 和假 `npx`、`claude`、`codebuddy` 执行 `--client all` 冒烟 | 通过，6 次 `npx skills add`，OpenCode 既有配置保留并新增 `mcp.reqflow`，Trae/Qoder 输出 `Manual MCP import required` |
| L2 | AC-010 | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，9 个测试通过 |
| L2 | AC-002、AC-005、AC-011 | `node scripts/test-mcp-install-dialog-unified.js`（companion 前端） | 通过 |
| L2 | AC-007 | `sh scripts/test-check-harness.sh` | 通过 |
| L2 | AC-008 | `sh ruoyi-requirement/src/main/resources/harness-template/scripts/test-check-harness.sh` | 通过 |
| L2 | AC-008 | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，29 个测试通过 |
| L2 | AC-009、AC-013 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest,ReqflowCodexGlobalSkillTemplateTest,McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，覆盖归档验证只读结果和合并归档收尾规范 |
| L2 | AC-014 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，覆盖普通状态接口拒绝返修和专用接口保存返修问题说明 |
| L2 | AC-015 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，覆盖返修验收必须等待本轮返修执行报告和 Review 报告回写 |
| L2 | AC-016 | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，先红于返修片段直接成为最新版报告，修复后 30 个测试通过 |
| L2 | AC-017 | `mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest,ReqflowCodexGlobalSkillTemplateTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，先红于平台指令仍包含具体 `mcpTool` 和 skill 缺少阶段映射，修复后 44 个测试通过 |
| L1 | AC-005 | `npm run build:prod`（companion 前端） | 通过，存在历史体积告警 |
| L0 | AC-006、AC-007、AC-008、AC-009、AC-010、AC-011、AC-013、AC-014、AC-015、AC-016、AC-017 | `sh scripts/check-docs.sh && sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-022-MCP-Key明文持久与交互安装` | 通过 |

## 运行态证据

- 执行目录：当前后端子仓库根目录、companion 前端子仓库根目录
- 启动命令：未启动服务
- profile/env/mode：本地单测、静态检查和构建验证
- 原始错误摘要：后端先红于缺少 `plainKey` 持久字段和交互脚本入口；前端先红于列表未按新约束隐藏字段；本轮补充先红于缺少管理员 MCP 请求地址配置入口和弹窗
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：如需真实客户端安装，应在用户本机执行统一命令并选择目标工具验证。

## 计划偏差

- 用户补充“不展示明文 Key、Key 前缀字段”，已调整为后端返回明文但前端仅用于命令渲染。
- 用户指出执行中不应写 `docs/specs/done/`，已将当前 spec 移回 `active/`，并收紧 `check-harness.sh --spec` 目标路径。
- 用户补充“harness 模板也要同步更新”，已将同样约束同步到项目接入初始化模板源。
- 用户补充“本地 harness 在收到归档、办结等结束任务指令时也要将 active 迁到 done”，已同步本地流程、MCP 合并归档指令、全局 skill 和模板。
- 用户要求检查完整需求流转阶段的按钮互斥，发现合并归档阶段需要前端先知道平台归档验证结果，已新增只读验证接口并复用办结验证口径。
- 用户指出需求人员提交返修时需要指出问题，已新增返修问题说明提交接口并禁止普通状态接口直接进入返修。
- 用户补充开发人员返修过程也应先复制返修任务指令再提交返修验收，已补服务端门禁，必须在最新返修说明之后回写新的执行报告和 Review 报告。
- 用户补充返修 MCP 推送不能把返修片段直接替换平台上的执行报告，已补服务端返修阶段报告增量合并逻辑：上传完整本地报告时保存完整报告，上传片段时基于上一版报告追加返修记录并保存新版本。
- 用户确认平台各阶段复制指令应尽量简短，只明确当前阶段，详细工具选择和阶段规则由全局 skill 承接；已将需求分析、需求生成、开发、返修和合并归档指令改为短动态上下文，并在 `reqflow-mcp` skill 中增加阶段工具映射表。
- 用户补充“MCP 明文 KEY 下次打开仍放在指令里”和“MCP 请求地址加到 MCP 管理页且仅管理员可配置”，已补后端配置接口、前端管理员配置入口和静态检查；后续补充“请求地址改为弹窗配置”，已将前端从顶部表单调整为按钮入口加弹窗。
- 用户实测 OpenCode MCP 没有安装成功，并要求同时确认其它工具；已按官方文档把 OpenCode/CodeBuddy 调整为可自动合并配置，Claude/CodeBuddy 优先 CLI，Trae/Qoder 明确为设置页手工导入，不再把生成片段误报为已安装。

## Review 返修记录

无。
