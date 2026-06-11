# 【需求名称】数据库变更说明

## 基本信息

- 需求 ID：【需求目录或验收 ID】
- 影响模块：【模块名称】
- 影响表：【表名列表】
- 影响数据访问层：【Mapper/Repository/DAO】
- 基线结构来源：【docs/db/sql/xxx.sql、docs/db/relationship.md、实际库快照或说明】
- 正向 SQL 脚本路径：【docs/db/sql/req_xxx.sql；没有脚本时说明原因】
- 回滚 SQL 脚本路径：【docs/db/sql/rollback_xxx.sql / 无，说明原因】

## 正向 SQL

- 脚本路径：【docs/db/sql/req_xxx.sql】
- 执行顺序：【第几步，依赖什么前置条件】
- SQL 摘要：【新增表/新增字段/迁移数据/修正菜单等】

```sql
-- 仅粘贴关键片段；完整脚本以脚本路径为准
```

## 回滚方案

- 回滚脚本路径：【docs/db/sql/rollback_xxx.sql / 无】
- 无法自动回滚原因：【无或说明】
- 回滚前检查：【查询语句或人工确认项】

## 执行语义

- 是否幂等：【是/否，说明】
- 是否会丢数据：【是/否，说明】
- 是否需要停机：【是/否，说明】
- 是否需要备份：【是/否，说明】
- 历史数据兼容或补偿：【无或说明】
- 执行前检查：【SQL 或检查方式】
- 执行后检查：【SQL、单测、接口或页面验证】

## 代码同步

- 受影响 Mapper/XML：【文件或无】
- 受影响 Service/任务：【文件或无】
- 受影响接口/契约：【文件或无】
- 受影响页面/模块文档：【文件或无】

## 文档同步

- [ ] `docs/db/table-dictionary.md`
- [ ] `docs/db/relationship.md`
- [ ] `docs/ai-harness/contracts/*.md`
- [ ] `docs/ai-harness/modules/*.md`
- [ ] 当前 spec 的 `execution-report.md` 已记录 `docs/db/sql/` 或 `docs/db/` 路径

## 验证

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh complete
```
