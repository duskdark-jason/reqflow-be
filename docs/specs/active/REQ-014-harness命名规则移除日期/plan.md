# harness 命名规则移除日期计划

## 执行步骤

1. 补充 `scripts/test-check-harness.sh` 回归用例，使用 `REQ-001-演示需求` 并拒绝日期前缀目录，覆盖 AC-001。
2. 补充 `ReqPackageServiceImplTest` 期望，锁定任务分支去日期输出，覆盖 AC-003。
3. 修改 `check-harness.sh` 的 active spec 目录名校验，覆盖 AC-001。
4. 修改 `ReqPackageServiceImpl` 的任务分支编号 token 生成，覆盖 AC-003。
5. 同步 AGENTS、流程文档、agent prompt、harness-template 和 workspace snippet，覆盖 AC-002。
6. 迁移当前 active spec 目录及内部互链、分支名记录，覆盖 AC-004。
7. 运行脚本、单测和 harness 验证。

## 验证计划

| 层级 | 验收 ID | 命令或方式 |
|---|---|---|
| L0 | AC-001, AC-002, AC-004 | `sh scripts/check-docs.sh` |
| L0 | AC-001 | `sh scripts/test-check-harness.sh` |
| L0 | AC-001, AC-002, AC-004 | `sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-014-harness命名规则移除日期` |
| L1 | AC-003 | `mvn -pl ruoyi-requirement -Dtest=ReqPackageServiceImplTest test` |
| L2 | AC-002, AC-004 | `rg` 扫描旧日期命名残留 |
| L3 | AC-001, AC-003 | Review diff |
| L4 | AC-001, AC-003 | 不适用，本次无页面或跨服务运行态流程 |

## 风险

- Maven 依赖下载可能受远端仓库 TLS 握手影响；若失败，需要记录为环境阻断并保留脚本验证结果。
- active spec 迁移会改变过程文档路径，必须同步 companion 互链。
