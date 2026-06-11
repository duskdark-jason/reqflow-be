# 项目文档与目录清理需求说明

## 背景

当前 workspace、后端和前端 README 仍有若依默认介绍或早期临时文档痕迹。后端 SQL 脚本仍放在仓库根目录 `sql/`，与长期数据库知识目录 `docs/db/` 分离。后端还保留旧 `doc/若依环境使用手册.docx`。

## 范围

- 清理项目内无用文件和 workspace 根目录旧临时文档。
- 重写 README，说明 ReqFlow 基于 RuoYi 前后端分离项目，并由 ChatGPT 最新模型辅助自动开发。
- 将后端 SQL 脚本整体迁移到 `docs/db/sql/`。
- 清理后端旧 `doc/` 目录。
- 同步数据库文档、AI Harness 文档和模板里的 SQL 路径。

## 不在范围

- 不修改 Java 业务逻辑、Mapper SQL 内容或数据库表结构。
- 不修改前端页面行为。
- 不执行 merge、push 或远端分支删除。

## 验收标准

- AC-001：后端根目录不再存在 tracked `sql/` 和 `doc/` 目录。
- AC-002：SQL 脚本位于 `docs/db/sql/`，相关长期文档引用新路径。
- AC-003：root、后端、前端 README 不再保留若依默认宣传内容。
- AC-004：文档和 harness 校验通过。
