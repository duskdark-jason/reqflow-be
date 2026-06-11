# harness 命名规则移除日期执行报告

## 执行结论

- 状态：已完成
- 分支：fix/harness-naming-no-date
- commit：本分支最终提交

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `scripts/check-harness.sh` | active spec 目录改为 `REQ-001-中文需求标题`，日期前缀直接失败。 |
| `scripts/test-check-harness.sh` | 测试夹具改为无日期目录，并新增日期前缀拒绝用例。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqPackageServiceImpl.java` | 任务分支生成时将带 8 位日期的需求号折叠为 `REQ-001`。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqPackageServiceImplTest.java` | 锁定执行包分支名不含日期。 |
| `docs/**` 和 `ruoyi-requirement/src/main/resources/harness-template/**` | 同步流程文档、模板和 prompt 示例为无日期命名。 |
| `docs/specs/active/REQ-*` | 迁移当前 active spec 目录和互链。 |

## 模块知识库沉淀

- 影响模块：AI Harness、需求执行包
- 模块知识库动作：无需更新
- 模块知识库文档：无
- 无需更新原因：本次仅调整流程规则、校验脚本、模板和执行包分支命名，不新增业务菜单或模块知识库条目。

## 数据库变更沉淀

- 数据库影响：无
- SQL 脚本路径：无
- 数据库文档路径：无
- 数据库变更说明：无
- 无需更新原因：本次不涉及持久化结构、SQL、Mapper、join、统计口径或分页粒度变化。

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：新增逻辑为局部命名 token 归一化，方法名和测试已表达意图，无复杂业务分支或外部系统约束。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-001, AC-002, AC-004 | `sh scripts/check-docs.sh` | 通过 |
| L0 | AC-001 | `sh scripts/test-check-harness.sh` | 通过 |
| L0 | AC-001, AC-002, AC-004 | `sh scripts/check-harness.sh complete --spec docs/specs/done/REQ-014-harness命名规则移除日期` | 通过 |
| L1 | AC-003 | `mvn -pl ruoyi-requirement -am -Dtest=ReqPackageServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过 |
| L2 | AC-002, AC-004 | `rg` 扫描旧日期命名残留 | 已执行，除日期拒绝测试样例外无旧 spec/branch 命名残留 |
| L3 | AC-001, AC-003 | Review diff | 通过 |
| L4 | AC-001, AC-003 | 不适用 | 本次无页面或跨服务运行态流程 |

## 运行态证据

- 执行目录：当前子仓库根目录
- 启动命令：无
- profile/env/mode：本地文档、脚本和单测验证
- 检查命令：见验证结果
- 原始错误摘要：无
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：无

## 计划偏差

- Maven 首次预跑因远端仓库 TLS 握手中断，后续验证会再次执行并记录最终结果。

## Review 返修记录

无。

## 风险与后续

- 历史 done spec 未批量改名，后续只对新 active spec 强制无日期命名。
