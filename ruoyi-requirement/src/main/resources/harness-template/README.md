# Harness 模板

这是一套可由需求平台存储并下发到其他项目的 agentic coding / vibecoding harness 模板。目标是让模型和团队成员在新需求开始前，先理解业务边界、接口契约、数据口径和验证方式，再进入编码。

优先落地方式是：需求平台保存本模板，项目接入时通过需求平台 MCP 或接口生成初始化包，由 Codex 在目标 workspace 校验远端仓库后写入 `docs/`、`scripts/`、子仓库 `AGENTS.md` 和 workspace 根目录 `AGENTS.md`。本地复制方式仅作为平台自身建设、离线接入或 MCP 不可用时的降级方案。

## 复制方式

### 需求平台下发

项目接入需求平台后，Codex 根据需求平台返回的项目、仓库、companion 仓库、默认基线分支和任务分支前缀，自动写入：

- workspace 根目录 `AGENTS.md`，来自 `WORKSPACE_AGENTS.snippet.md`。
- 每个子仓库 `AGENTS.md`，来自 `AGENTS.snippet.md`。
- 每个子仓库 `docs/`、`scripts/` 和基础 harness 模板。

写入前必须校验当前 workspace 的 Git 远端与需求平台登记的仓库一致；不一致时停止并说明当前仓库、期望仓库和切换失败原因。

写入前还必须判定初始化模式：

- `fresh-init`：目标仓库没有有效 `docs/ai-harness/harness-index.json`，或索引中 `initialized` 不是 `true`，允许按模板初始化。
- `bind-existing`：目标仓库已有 `initialized=true` 的 harness，且无需升级模板；只绑定需求平台项目、仓库、项目分支和任务分支前缀等身份信息，不覆盖既有 docs/scripts。
- `upgrade-existing`：目标仓库已有 harness，但模板版本或脚本能力落后；只能对平台托管的流程、模板和脚本做 diff/merge 升级，不得覆盖项目沉淀文档。

已初始化项目禁止整包覆盖。必须保留项目自有的 `docs/ai-harness/modules/**`、`docs/ai-harness/contracts/**`、`docs/ai-harness/decisions/**`、`docs/domains/**`、`docs/db/**`、`docs/specs/**` 和 `docs/runbooks/local-run.md`；除非用户明确授权，不得删除、重命名或改写这些文件。

### 单仓项目

在目标项目根目录执行：

```bash
cp -R harness-template/docs .
cp -R harness-template/scripts .
cp harness-template/AGENTS.snippet.md ./AGENTS.md
```

如果目标项目已有 `AGENTS.md`，不要覆盖，手动把 `AGENTS.snippet.md` 中的内容合并进去。

### 多仓 workspace

在每个子仓库内复制单仓模板：

```bash
cp -R harness-template/docs <repo>/
cp -R harness-template/scripts <repo>/
cp harness-template/AGENTS.snippet.md <repo>/AGENTS.md
```

在 workspace 根目录使用轻量导航模板：

```bash
cp harness-template/WORKSPACE_AGENTS.snippet.md ./AGENTS.md
```

如果根目录或子仓库已有 `AGENTS.md`，不要覆盖，手动合并对应 snippet。

## 落地步骤

1. 替换所有 `【...】` 占位符。
2. 按项目实际技术栈修改 `docs/ai-harness/verification.md`。
3. 修改 `docs/ai-harness/harness-index.json` 中的仓库名称、仓库角色、companion 仓库和客户定制分支；真实项目必须把 `template` 改为 `false`，并在初始化完成后把 `initialized` 改为 `true`。它是 agent 的机器可读入口索引，不替代正文文档。
4. 根据项目事实生成 `docs/runbooks/local-run.detected.md` 初稿；人工确认后再固化为 `docs/runbooks/local-run.md`，不要直接把模板当成真实命令。
5. 为核心业务模块复制 `docs/ai-harness/templates/module-template.md`。
6. 为前后端接口、任务结果或关键 UI 状态复制 `docs/ai-harness/templates/contract-template.md`。
7. 新需求使用 `docs/specs/active/REQ-001-中文需求标题/` 目录，并按 `docs/templates/README.md` 复制主流程模板；Git 任务分支另用 ASCII 名称。
8. 如果项目存在数据库、Mapper、SQL 或统计口径维护需求，创建 `docs/db/README.md`、`docs/db/table-dictionary.md` 和 `docs/db/relationship.md`；纯前端或无数据库项目可以不创建 `docs/db/`。可执行 SQL、迁移脚本、菜单脚本和历史基线统一放入 `docs/db/sql/`。如果仓库没有 DDL、迁移脚本、schema 导出或可信数据库字典，只生成“暂无确认表结构来源/暂无确认关系来源”的待补齐骨架，并记录已扫描路径。
9. 初始化阶段运行 `sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh init`，确认没有个人路径、旧路径、未替换占位符和缺失的基础 harness 文件。

## local-run 生成流程

`docs/runbooks/local-run.md` 是项目适配产物，不是通用文档。初始化时先生成 `docs/runbooks/local-run.detected.md`，人工确认后再重命名或复制为 `docs/runbooks/local-run.md`。生成时先扫描项目事实：

```bash
rg --files -g 'package.json' -g 'pom.xml' -g 'build.gradle' -g 'pyproject.toml' -g 'go.mod' -g 'docker-compose*.yml'
rg -n "server.port|port:|localhost|proxy|target|baseURL|context-path|VITE_APP_BASE_API"
```

然后把 `docs/runbooks/local-run-template.md` 复制为 `docs/runbooks/local-run.detected.md`，填入静态扫描得到的启动命令、端口、配置来源、健康检查或页面冒烟入口。只记录配置来源，不复制明文密码、token 或 API key。人工确认后再固化为 `docs/runbooks/local-run.md`，并同步更新 `harness-index.json` 的 `entrypoints.localRun`。

## 推荐目录

```text
docs/
  README.md
  process/
    code-guidelines.md
  ai-harness/
    contracts/
    decisions/
    harness-index.json
    modules/
    templates/
  domains/
  runbooks/
  specs/
  templates/
scripts/
  check-docs.sh
  check-docs.cmd
  check-harness.sh
  check-harness.cmd
  test-check-docs.sh
  test-check-docs.cmd
  test-check-harness.sh
  test-check-harness.cmd
AGENTS.md
```

## Windows 使用方式

脚本主体使用 POSIX `sh/find/grep/sed`，在 macOS、Linux、Git Bash 和 WSL 中可直接运行 `.sh` 文件。Windows 原生命令行可使用 `.cmd` 包装入口，前提是已安装 Git Bash；`.cmd` 会优先查找 Git for Windows 的 `bash.exe`，并透传脚本退出码：

```bat
scripts\check-docs.cmd
scripts\check-harness.cmd init
scripts\check-harness.cmd review
scripts\check-harness.cmd complete
scripts\test-check-docs.cmd
scripts\test-check-harness.cmd
```

WSL 用户建议先进入 WSL shell 和仓库目录，再直接运行 `sh scripts/check-docs.sh`、`sh scripts/check-harness.sh init|review|complete`。如果 Windows 环境没有 Git Bash，`.cmd` 会直接失败并提示安装；不要维护 PowerShell 版并行逻辑，避免两套检查规则漂移。

## 使用原则

- `AGENTS.md` 只写工作规则和导航，不塞业务细节。
- 多仓 workspace 根目录只做子仓库导航，不沉淀具体业务规则。
- `docs/ai-harness/` 写长期有效的 agent 护栏。
- `docs/ai-harness/harness-index.json` 只写机器可读入口、仓库角色、companion 仓库和客户分支索引，不写业务正文。
- `docs/domains/` 写当前业务领域入口。
- `docs/db/` 写数据库维护规范、表结构字典、表关系、数据粒度、必要过滤和聚合风险；没有数据库职责的项目可以不创建。
- `docs/runbooks/` 写项目启动、联调和排查方式。
- `docs/specs/active/` 写正在开发的单次需求。
- `docs/specs/done/` 放已完成但仍有参考价值的历史方案。
- `docs/templates/` 放团队可复制模板。
- Harness 初始化或纯文档接入只运行 L0/init 检查，不启动项目、不跑业务测试。
- 多 agent 协作时，计划、执行和 review 必须通过 spec 目录内文件交接，不依赖单次对话上下文；执行完成后默认自动进入 Review，产生 `RF-*` 后自动返修并复审，直到最终 Review 通过。
- L3/L4 验证必须先按项目 runbook 尝试启动，不能无证据声明环境不可达。
- Harness 初始化完成后运行 `sh scripts/check-harness.sh init`；Review Agent 完成中间审查后运行 `sh scripts/check-harness.sh review`；Execution Agent 完成返修、Review Agent 最终复审通过后运行 `sh scripts/check-harness.sh complete`。
- 在多仓 workspace 根目录启动工具时，只维护 `docs/templates/agent-prompts/workspace-*.md` 三类 workspace 级启动提示词；子仓库不再维护独立启动提示词，子仓库规则以 `AGENTS.md`、`docs/process/` 和 `docs/ai-harness/` 为准。
