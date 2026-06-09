# 需求管理平台 MVP-lite 后端 Review 报告

- 结论：通过
- Review Agent：backend reviewer subagent
- Review 时间：2026-06-09

## Review 范围

- 数据库脚本幂等性和字段完整性。
- REST 接口路径、权限标识、请求参数和响应对象。
- 需求状态流转和执行包版本追加规则。
- MCP 安全边界，不执行仓库命令和文件系统命令。
- 统计查询是否保持项目、需求、执行包和用户使用记录的数据粒度。

## 发现问题

- Critical：通用编辑接口可绕过状态机。已修正并增加单元测试。
- Important：MCP 写入口缺少权限。已增加 `req:package:save`。
- Important：项目排行生成率被历史版本放大。已改为按需求去重。
- Important：用户使用统计按角色拆行。已改为用户粒度并聚合角色名。
- Important：执行包 artifact type 缺少白名单。已增加服务端校验。
- Minor：统计菜单挂到系统管理。已改为需求管理父菜单。
- Minor：active spec 状态过期。已更新为 reviewed。

## 验收覆盖

| 验收 ID | 需求来源 | 证据 | 命令 | 结果 |
|---|---|---|---|---|
| AC-BE-001 | requirement.md | 基础表、8 张需求平台表和 27 条 `req:%` 权限已落库 | JDBC SQL runner + 只读查询 | 通过 |
| AC-BE-002 | requirement.md | 5 个单元测试通过 | `mvn -pl ruoyi-requirement -am test` | 通过 |
| AC-BE-003 | requirement.md | 8 个 reactor 模块打包通过 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过 |
| AC-BE-004 | requirement.md | 后端启动成功，REST 列表和 MCP resources/list 冒烟通过 | curl REST/MCP 冒烟 | 通过 |
| AC-BE-005 | requirement.md | MCP 权限和白名单已静态修正 | Maven test/package | 通过 |
