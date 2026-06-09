# 后端本地运行说明初稿

本文件为 Harness 初始化初稿，仅用于记录静态扫描结果。人工确认前不要重命名为 `local-run.md`。

## 技术栈

- RuoYi-Vue 后端
- Spring Boot
- Spring Security
- MyBatis
- MySQL
- Redis

## 静态扫描结果

- 构建入口：`pom.xml`
- 启动模块：`ruoyi-admin`
- 默认启动类：`ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java`
- 默认端口：`8080`
- 默认 context path：`/`
- 主配置：`ruoyi-admin/src/main/resources/application.yml`
- 数据源配置：`ruoyi-admin/src/main/resources/application-druid.yml`
- 测试配置预留：`ruoyi-admin/src/main/resources/application-test.yml`
- 测试 profile 用法：`SPRING_PROFILES_ACTIVE=druid,test`
- MyBatis mapper 配置：`classpath*:mapper/**/*Mapper.xml`

## 初始化阶段验证

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh init
```

## 待人工确认

- 后端启动 profile
- MySQL 连接方式
- Redis 连接方式
- 测试账号
