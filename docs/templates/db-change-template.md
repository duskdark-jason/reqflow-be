# 【需求名称】数据库变更说明

## 基本信息

- 需求 ID：【需求目录或验收 ID】
- 影响模块：【模块名称】
- 影响表：【表名列表】
- 影响数据访问层：【Mapper/Repository/DAO】

## 正向 SQL（Forward SQL）

```sql
-- 填写正向 SQL
```

## 回滚 SQL（Rollback SQL）

```sql
-- 填写回退 SQL
```

## 执行语义

- 是否幂等：【是/否，说明】
- 是否会丢数据：【是/否，说明】
- 是否需要停机：【是/否，说明】
- 是否需要备份：【是/否，说明】

## 文档同步

- [ ] 数据库字典
- [ ] 表关系或查询链路
- [ ] 接口或结果契约

## 验证

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh
```
