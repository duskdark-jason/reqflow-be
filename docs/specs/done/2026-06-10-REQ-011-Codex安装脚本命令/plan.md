# Codex 安装脚本命令后端执行计划

## 输入文件

- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexSetupPackageTemplate.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/template/ReqflowCodexGlobalSkillTemplate.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqMcpUserKeyServiceImpl.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpKeyController.java`
- `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`
- `docs/ai-harness/contracts/requirement-platform-api.md`
- `docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：补 `ReqMcpUserKeyServiceImplTest`，断言 `codexSetupPackage.installCommands` 含 macOS/Linux 与 Windows PowerShell 命令模板，覆盖 AC-BE-001、AC-BE-002。
2. TDD Red：新增 `ReqCodexInstallControllerTest`，断言 shell/PowerShell 安装脚本内容，覆盖 AC-BE-003、AC-BE-004。
3. TDD Green：扩展安装包模板，增加安装脚本 URL、平台命令模板和 Key 占位符。
4. TDD Green：新增安装脚本 controller，生成 shell/PowerShell 脚本，复用现有 skill 内容。
5. 配置：放行安装脚本匿名 GET 访问。
6. 文档：同步 API 契约和模块 harness。
7. 验证：运行定向测试、模块测试、admin 打包、文档检查、harness init 和空白检查，覆盖 AC-BE-005。

## 验证计划

- L2 定向测试：`mvn -pl ruoyi-admin,ruoyi-requirement -am -Dtest=ReqMcpUserKeyServiceImplTest,ReqCodexInstallControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 模块测试：`mvn -pl ruoyi-requirement -am test`
- L1 打包：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档：`sh scripts/check-docs.sh`
- Harness 当前阶段：`sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-011-Codex安装脚本命令`
- 空白检查：`git diff --check`

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 安装命令模板 | `ReqMcpUserKeyServiceImplTest` |
| AC-BE-002 | 明文 Key 安全边界 | `ReqMcpUserKeyServiceImplTest`、`ReqCodexInstallControllerTest` |
| AC-BE-003 | shell 安装脚本 | `ReqCodexInstallControllerTest` |
| AC-BE-004 | PowerShell 安装脚本 | `ReqCodexInstallControllerTest` |
| AC-BE-005 | 回归验证 | Maven、打包、文档和 harness 检查 |
