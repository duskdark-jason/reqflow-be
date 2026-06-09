# 需求管理平台 MVP-lite 后端执行报告

- 状态：后端实现、review 返修、数据库初始化和运行态冒烟已完成
- 分支：feature/REQ-PLATFORM-MVP-lite
- commit：已提交，详见当前分支 git log

## 已完成

- 已在后端仓库创建 active spec。
- 已写入本地测试配置，MySQL 使用 `root/123456`，Redis 使用本地服务。
- 已新增 `ruoyi-requirement` 模块并接入根 POM、`ruoyi-admin` 依赖。
- 已新增需求平台初始化 SQL、菜单权限 SQL 和本地测试配置 SQL。
- 已实现项目、仓库、版本、模块、需求、执行包、统计和 MCP 后端接口。
- 已补充需求状态流转、模板生成单元测试。
- 已修正执行包生成接口路径为 `/requirement/package/generate/{demandId}`，避免和保存产物接口 `/requirement/package/{demandId}/{artifactType}` 映射歧义。
- 已修正项目统计需求数为 `count(distinct d.demand_id)`，避免多表 join 放大。
- 已根据 review 修正通用编辑接口状态机绕过、MCP 权限、执行包 artifact 白名单、统计历史版本放大、用户使用统计粒度和统计菜单父级。

## 计划内待完成

- 与前端 companion spec 做页面级浏览器冒烟。

## 验证记录

| 层级 | 验收 ID | 命令 | 结果 |
|---|---|---|---|
| L0 | AC-BE-001 | `sh scripts/check-docs.sh` | 通过：文档检查通过 |
| L0 | AC-BE-001 | `sh scripts/check-harness.sh init` | 通过：Harness 检查通过（init 模式） |
| L1 | AC-BE-003 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过：BUILD SUCCESS，8/8 reactor success |
| L2 | AC-BE-002 | `mvn -pl ruoyi-requirement -am test` | 通过：Tests run: 5, Failures: 0, Errors: 0 |
| L2 | AC-BE-005 | `mvn -pl ruoyi-requirement -am test`、`mvn -pl ruoyi-admin -am -DskipTests package` | 通过：MCP 权限、artifact 白名单和无副作用 resources/list 已完成验证 |
| L3 | AC-BE-004 | `java -Dorg.springframework.boot.logging.LoggingSystem=none -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=druid,test` | 通过：Druid 数据源初始化，Tomcat 8080 启动成功 |
| L3 | AC-BE-004 | `curl http://127.0.0.1:8080/` | 通过：HTTP 200，返回 RuoYi 后端欢迎信息 |
| L3 | AC-BE-004 | `curl http://127.0.0.1:8080/requirement/project/list` | 通过：未登录返回 `code=401`，需求接口已进入安全链 |
| L3 | AC-BE-004 | 登录后 `curl /requirement/project/list` | 通过：认证态返回 `code=200,total=0,rows=[]` |
| L3 | AC-BE-004 | 登录后 `POST /requirement/mcp`，method=`resources/list` | 通过：返回需求、项目、客户线和工作空间资源清单 |
| 环境 | AC-BE-001 | JDBC 执行 `sql/ry_20260417.sql`、`sql/req_platform_schema.sql`、`sql/req_platform_menu.sql` | 通过：基础脚本 289 条、业务表脚本 8 条、菜单脚本 35 条均执行完成 |
| 环境 | AC-BE-001 | JDBC 只读查询 `sys_menu` 和需求平台表 | 通过：`sys_menu` 113 条、需求管理目录 1 条、`req:%` 权限 27 条、8 张需求平台表全部存在 |
| 环境 | AC-BE-001 | JDBC 执行 `sql/req_platform_test_settings.sql` 后 `DELETE /system/config/refreshCache` | 通过：`captchaImage` 返回 `captchaEnabled=false`，登录不再需要验证码 |

## 偏差说明

- 使用 `root/123456` 后，已按确认执行 `sql/ry_20260417.sql` 初始化 RuoYi 基础表，并重放需求平台业务表和菜单权限脚本。
- 已按测试约定关闭验证码。若服务已启动，执行 `sql/req_platform_test_settings.sql` 后需调用 `DELETE /system/config/refreshCache` 刷新 `sys_config` 缓存。
- 根目录设计和开发计划保留，子仓库 active spec 用于执行追踪。

## Review 返修记录

- 修正通用编辑接口可绕过状态机的问题，新增 `ReqDemandServiceImplTest`。
- 为执行包保存和查询增加 artifact type 白名单，新增 `ReqPackageServiceImplTest`。
- 为 MCP 统一入口增加 `req:package:save` 权限。
- 修正项目排行生成率按需求去重，避免历史版本放大到 100% 以上。
- 修正用户使用统计为用户粒度，角色名称聚合展示。
- 修正统计菜单父级为需求管理目录。

## 剩余风险

- 前后端字段和权限已完成静态契约校对，仍需前端 dev server 和浏览器页面冒烟。
