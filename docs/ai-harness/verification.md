# 验证说明

除非任务另有要求，命令均在项目仓库根目录执行。

## 验证原则

编译或构建只证明静态阶段通过，不证明应用能启动、接口能调用、页面能打开、权限能生效或用户流程可用。每次变更都要按影响范围选择“最小充分验证组合”。

涉及 L3/L4 时，执行 agent 必须先阅读 `docs/runbooks/local-run.md` 并尝试启动项目。未执行启动命令、未记录连通性或浏览器检查命令和错误摘要时，不允许写“DB 不可达”“服务不可用”“环境不可达”等绝对结论。

环境相关结论只能描述为“当前执行 agent 所在环境未验证通过”，不能推断用户本机、同事机器、测试环境或生产环境不可用。

## 分层验证

| 层级 | 目标 | 适用场景 | 示例 |
|---|---|---|---|
| L0 文档/规范 | 文档没有个人路径、占位符和旧路径 | 修改 `docs/`、`AGENTS.md`、harness | `sh scripts/check-docs.sh` |
| L1 编译/构建 | 代码能通过静态编译或打包 | 大多数代码修改 | `mvn -pl ruoyi-admin -am -DskipTests package` |
| L2 单元/契约 | 核心逻辑、接口契约、组件契约可验证 | Service、utils、DTO/VO、组件状态 | `mvn -pl ruoyi-requirement -am test` |
| L3 运行态冒烟 | 应用能启动，接口/页面能真实运行 | Controller、页面、权限、配置 | `SPRING_PROFILES_ACTIVE=druid,test mvn -pl ruoyi-admin spring-boot:run` |
| L4 端到端/跨端 | 真实用户流程可用 | 保存、导出、异步任务、跨服务联调 | `curl -s http://localhost:8080/system/config/configKey/sys.index.skinName` |

只改纯文档时通常跑 L0。改代码至少跑 L1。改业务逻辑或契约应补 L2。改接口、配置、权限或用户可见页面应补 L3。影响跨端、导出、保存、异步任务或核心流程时应补 L4。

Harness 初始化或纯文档接入只跑 L0/init 检查，不启动项目、不运行项目构建、测试或 L3/L4 冒烟。

## L0 文档检查

```bash
sh scripts/check-docs.sh
```

修改 `docs/`、`AGENTS.md` 或 harness 文档后运行。脚本只依赖 POSIX `sh/find/grep`，也支持传入其他检查目录。

Harness 初始化时同时运行：

```bash
sh scripts/check-harness.sh init
```

Windows 原生命令行可通过 `scripts\check-docs.cmd` 与 `scripts\check-harness.cmd init` 调用 Git Bash；WSL 用户进入 WSL shell 后直接运行同名 `.sh`。

## L1 编译或构建

后端编译或打包命令：

```bash
mvn -pl ruoyi-admin -am -DskipTests package
```

## L2 单元或契约测试

后端单元或契约测试命令：

```bash
mvn -pl ruoyi-requirement -am test
```

## L3 运行态冒烟

涉及接口、页面、权限、配置或外部依赖时，仅 L1/L2 不够。应在本地或测试环境验证：

- 按 `docs/runbooks/local-run.md` 尝试启动应用。
- 记录执行目录、启动命令、profile/env 或 mode。
- 应用能启动。
- 目标接口或页面能访问。
- 参数、字段、权限和错误提示符合预期。

前端或浏览器场景可使用 Playwright CLI 做低 token 冒烟：

```bash
playwright-cli open http://localhost:8080
playwright-cli snapshot
playwright-cli console
playwright-cli requests
```

后端不可用时，页面 L3 可使用 mock/intercept 验证页面打开、弹窗、表单校验、空态、错误态和 console 错误；这类验证不能计为 L4。

## L4 端到端或跨端验证

涉及保存、删除、导出、历史记录、异步任务、跨端联调或核心用户流程时，应完成真实流程冒烟。

可回归的 Web 流程优先沉淀为 Playwright Test：

```bash
npx playwright test --grep @smoke --reporter=line
```

默认只读取摘要输出。trace、screenshot、video 只在失败时保存并在 `execution-report.md` 记录路径，不要把完整 HTML report 或 trace 内容复制进对话。

如果当前执行 agent 环境缺失导致无法执行 L3/L4，完成说明中必须写明已执行命令、错误摘要、该结论只代表当前执行 agent 环境，以及后续补验环境。

## 验收点

- 接口路径、请求方式、参数位置和响应字段正确。
- 数据粒度、权限过滤、分页和统计口径正确。
- 页面、导出、历史记录或异步状态符合需求。
- 错误状态可见且可恢复。
