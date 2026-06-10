# MCP地址后端配置项 Review 报告

## Review 结论

结论：通过

本次变更范围集中在 MCP Key 配置接口的地址来源、默认配置和契约文档。未发现阻断或重要问题，可以办结当前需求分支；主分支合并需另行授权。

## 审查范围

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- `ruoyi-admin/src/test/java/com/ruoyi/web/controller/requirement/ReqMcpKeyControllerTest.java`
- `ruoyi-admin/src/main/resources/application.yml`
- `docs/ai-harness/contracts/requirement-platform-api.md`
- `docs/specs/active/2026-06-10-REQ-004-MCP地址后端配置项/`

## 发现问题

未发现新的阻断或重要问题。

剩余风险：

- 标准 L1 命令已在临时 clean worktree 中通过。主工作区存在无关 `ruoyi-requirement` 动作 token 改动，不能作为本需求干净验证环境。
- L3 运行态未执行，部署后仍建议在目标环境配置 `reqflow.mcp.public-url` 并访问 `/requirement/mcp/key/config` 做一次真实返回校验。

## 验收覆盖

- AC-BE-001：通过。配置项优先逻辑在 controller 测试中覆盖，能够避免页面继续显示 `localhost:1024` 这类代理 Host。
- AC-BE-002：通过。配置为空时的转发头和上下文路径推导保持原行为。
- AC-BE-003：通过。`config()` 先计算单个 `mcpAddress` 变量，再同时写入响应字段和模板，测试覆盖一致性。
- AC-BE-004：通过。默认配置和后端契约均已记录配置项与使用方式。

## 验证评估

已执行并通过的验证：

- `mvn -pl ruoyi-admin -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`：4 tests 通过。
- 临时 clean worktree 中执行 `mvn -pl ruoyi-admin -am -DskipTests package`：通过。
- `mvn -pl ruoyi-admin -am -Dmaven.test.skip=true package`：生产代码打包通过。
- `sh scripts/check-docs.sh`：通过。
- `git diff --check`：通过。

主工作区观察：

- 主工作区曾因无关 `ruoyi-requirement` 动作 token 改动导致标准 L1 测试编译阶段失败；隔离到 clean worktree 后已确认当前提交自身可通过标准 L1。

## 是否允许办结

允许办结。当前 Review 无阻断项和返修项。
