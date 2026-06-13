# 部署说明

本文说明 ReqFlow 初始发布或测试环境重建时的部署顺序。SQL 脚本统一以当前后端仓库 `docs/db/sql/` 为准；前端仓库不包含数据库脚本。

## 适用范围

- 后端：RuoYi 后端启动模块 `ruoyi-admin`，默认端口 `8080`，默认 context-path 为 `/reqflow-api`。
- 前端：companion 前端仓库，生产静态访问前缀为 `/reqflow/`，生产 API 前缀为 `/reqflow-api`。
- 数据库：MySQL，字符集建议使用 `utf8mb4`。
- 缓存：Redis，默认配置在后端 `ruoyi-admin/src/main/resources/application.yml`。

## 部署前检查

1. 确认本次是全新库、测试库重建，还是已有库升级。
2. 备份目标数据库；DDL 和部分脚本不可依赖事务整体回滚。
3. 确认后端配置中的 MySQL、Redis、上传目录、token 密钥和 Druid 控制台账号已经按环境调整。
4. 确认反向代理或网关把 `/reqflow/` 指向前端静态资源，把 `/reqflow-api/` 转发到后端服务。
5. 如果对外使用 MCP，部署后在系统参数 `reqflow.mcp.public-host` 中填写外部可访问的 `IP:端口` 或域名端口；不要填写 `/reqflow-api` 和 `/requirement/mcp` 路径。

## SQL 脚本执行顺序

### 全新库或测试库重建

按以下顺序执行：

| 顺序 | 脚本 | 是否必须 | 作用 | 注意事项 |
|---|---|---|---|---|
| 1 | `docs/db/sql/ry_20260417.sql` | 必须 | 创建 RuoYi 基础表和初始化系统数据 | 脚本包含 `DROP TABLE IF EXISTS`，会重建 `sys_*`、`gen_*` 等基础表，只能用于全新库或明确重建的库。 |
| 2 | `docs/db/sql/quartz.sql` | 必须 | 创建 Quartz 定时任务表 | 脚本包含 `DROP TABLE IF EXISTS QRTZ_*`，会重建调度表。 |
| 3 | `docs/db/sql/req_platform_init.sql` | 必须 | 初始化 ReqFlow 需求平台 | 包含 `req_*` 业务表、需求管理菜单和按钮权限、MCP 服务参数、角色授权和品牌清理；依赖 RuoYi 基础数据已存在。 |

示例命令：

```bash
mysql -h <db-host> -P <db-port> -u <db-user> -p <db-name> --default-character-set=utf8mb4 < docs/db/sql/ry_20260417.sql
mysql -h <db-host> -P <db-port> -u <db-user> -p <db-name> --default-character-set=utf8mb4 < docs/db/sql/quartz.sql
mysql -h <db-host> -P <db-port> -u <db-user> -p <db-name> --default-character-set=utf8mb4 < docs/db/sql/req_platform_init.sql
```

### 已有库升级

已有库升级时不要直接重跑 `ry_20260417.sql` 或 `quartz.sql`，除非已经确认目标库可以被重建。

建议顺序：

1. 先备份目标库。
2. 检查基础表是否已存在：`sys_menu`、`sys_config`、`sys_role`、`QRTZ_JOB_DETAILS`。
3. 基础表缺失时，先在临时库验证 `ry_20260417.sql` 和 `quartz.sql` 对现有数据的影响，再决定是否手工补齐或重建。
4. 执行 `docs/db/sql/req_platform_init.sql`，补齐 ReqFlow 业务表、需求管理菜单权限、发布参数、角色授权和品牌清理。

升级命令示例：

```bash
mysql -h <db-host> -P <db-port> -u <db-user> -p <db-name> --default-character-set=utf8mb4 < docs/db/sql/req_platform_init.sql
```

## 后端打包与启动

在后端仓库根目录执行：

```bash
mvn -pl ruoyi-admin -am -DskipTests package
```

打包产物位于 `ruoyi-admin/target/`。启动前确认生产环境配置已覆盖以下内容：

- `server.port` 和 `server.servlet.context-path`，默认后端访问前缀为 `/reqflow-api`。
- `spring.datasource.druid.master.url`、`username`、`password`。
- `spring.data.redis.host`、`port`、`password`。
- `ruoyi.profile` 上传目录。
- `token.secret`。
- Druid 控制台账号、密码和访问白名单。

启动示例：

```bash
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

如通过外部配置文件或环境变量覆盖配置，以发布环境实际启动脚本为准。

## 前端打包与静态部署

在前端仓库根目录执行：

```bash
npm install
npm run build:prod
```

生产构建使用：

- `VUE_APP_PUBLIC_PATH=/reqflow/`
- `VUE_APP_BASE_API=/reqflow-api`

将前端 `dist/` 目录发布到 Web 服务器的 `/reqflow/` 静态路径。反向代理需要把 `/reqflow-api/` 转发到后端服务，不能把 MCP 地址拼到 `/reqflow/` 下面。

## 部署后检查

1. 访问前端 `/reqflow/`，确认登录页能打开。
2. 使用管理员账号登录，确认能看到“需求管理”菜单。
3. 打开“项目管理”“需求列表”“MCP管理”“使用统计”，确认页面能正常加载。
4. 在系统参数中确认 `reqflow.mcp.public-host` 已按发布入口填写。
5. 验证 MCP endpoint 为 `/reqflow-api/requirement/mcp`，前端静态路径 `/reqflow/` 不参与该地址。
6. 查看后端日志，确认没有数据库连接、Redis 连接、权限菜单或 Mapper SQL 报错。

## 常见风险

- `ry_20260417.sql` 和 `quartz.sql` 会重建基础表，不要在已有生产库上无确认执行。
- `req_platform_init.sql` 依赖 RuoYi 基础表和系统数据，必须在 `ry_20260417.sql` 和 `quartz.sql` 之后执行。
- `req_platform_init.sql` 会调整初始角色授权和清理模板品牌数据，生产已有库执行前必须先备份并确认影响范围。
- 前端 `/reqflow/` 是静态访问项目前缀；后端 `/reqflow-api` 是 API context-path；MCP 对外地址使用后端路径。
