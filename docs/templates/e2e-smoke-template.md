# 【需求名称】E2E Smoke 说明

## 基本信息

- 需求 ID：【需求目录或验收 ID】
- 页面或接口入口：【URL/路由/接口】
- 后端依赖：【接口或无】
- 测试标签：`@smoke`

## 准备

- 登录方式：【账号来源或登录态来源】
- 测试数据：【创建方式或复用数据说明】
- 环境变量：【变量名，不写明文值】

## 执行

```bash
npx playwright test --grep @smoke --reporter=line
```

探索式 L3 可使用：

```bash
playwright-cli open 【本地 URL】
playwright-cli snapshot
playwright-cli console
playwright-cli requests
```

## 断言

- AC-001：【断言页面、接口或交互结果】
- AC-002：【断言数据或状态结果】

## 清理

- 【删除测试数据或恢复状态】

## 失败留证

- screenshot 路径：【路径】
- trace 路径：【路径】
- console/network 摘要：【摘要】
