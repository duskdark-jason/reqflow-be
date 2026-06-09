# 本地运行手册

本文件由项目适配时生成。不要直接把模板当成真实命令；必须根据项目文件填写启动方式、端口、配置来源和冒烟入口。

## 项目类型

- 技术栈：【如 Java/Spring、Node/Vite、Python、Go】
- 主启动模块：【模块或入口】
- 默认端口：【端口】
- 默认上下文路径或 base path：【路径】

## 配置来源

- 主配置：【配置文件路径】
- 环境变量：【变量名列表，不写明文值】
- 外部依赖配置：【数据库/Redis/MQ/对象存储/模型服务等配置位置】

账号、密码、token、API key 等敏感值以配置文件、环境变量或密钥系统为准，不在报告中复制明文。

## 启动命令

```bash
【填写启动命令】
```

如需 profile、mode 或 env，写明命令：

```bash
【填写带 profile/env 的启动命令】
```

## 启动成功检查

```bash
【填写健康检查或页面访问命令】
```

如果没有健康接口，使用目标接口、登录页、首页或业务页面作为运行态验证入口。

## L3 运行态冒烟

- 【接口或页面能访问】
- 【关键交互能执行】
- 【console/network 或日志无本次变更相关错误】

## L4 端到端或跨端验证（可选）

- 【如本需求选择 L4，填写真实用户流程】
- 【真实后端/DB/权限/测试账号要求】
- 【保存、删除、导出、异步任务等关键链路】

## Playwright 冒烟

```bash
playwright-cli open 【本地 URL】
playwright-cli snapshot
playwright-cli console
playwright-cli requests
```

可回归流程：

```bash
npx playwright test --grep @smoke --reporter=line
```

## 失败证据格式

- 执行目录：【目录】
- 启动命令：【命令】
- profile/env/mode：【说明】
- 检查命令：【命令】
- 原始错误摘要：【摘要】
- screenshot/trace 路径：【无或路径】
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：【本地/测试环境/CI】
