# 需求列表页签化与知识库提需执行报告

## 执行概要

- 状态：完成
- 当前分支：fix/req-001-demand-tab-knowledge
- commit：complete-check后提交
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md

## 改动记录

- AC-BE-001：`ReqDemandServiceImpl` 新增保存时继续覆盖请求体 `demandNo` 并自动生成编号，`ReqDemandServiceImplTest` 补充覆盖前端传入编号的测试。
- AC-BE-002：`ReqPackageServiceImpl` 将执行包 `taskBranch` 调整为 `fix-模块slug-编号slug-标题slug`。
- AC-BE-003：执行包模块名解析顺序调整为人工模块、索引模块、新功能名称备注，`ReqPackageServiceImplTest` 覆盖新功能名称场景。
- AC-BE-004：需求保存兜底校验改为要求有效仓库和对应分支 imported 索引批次，不再把既有模块知识作为新功能提需前置条件；测试覆盖有索引无既有模块可保存、缺索引仍拒绝。
- AC-BE-005：同步 `docs/ai-harness/contracts/requirement-platform-api.md` 与 `docs/ai-harness/modules/requirement-platform.md`。
- 持久化结构/脚本：否；本次仅调整服务层校验和查询使用，已核对 `docs/db/README.md`，无需新增脚本。

## 代码注释处理

- 注释动作：新增
- 处理说明：在任务分支生成逻辑处补充短注释，说明分支名同时承载业务语义并保持 Git/命令行友好的 ASCII 片段。

## 验证记录

- 命令：`mvn -pl ruoyi-requirement -am -Dtest=ReqDemandServiceImplTest,ReqPackageServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
  - 结果：通过，7 个测试无失败。
- 命令：`mvn -pl ruoyi-requirement -am test`
  - 结果：通过，70 个测试无失败。
- 命令：`mvn -pl ruoyi-admin -am -DskipTests package`
  - 结果：通过。
- 命令：`git diff --check`
  - 结果：通过。
- 命令：`sh scripts/check-docs.sh`
  - 结果：通过。

## 运行态证据

- 执行目录：当前子仓库根目录。
- 启动命令：`SPRING_PROFILES_ACTIVE=druid,test java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=8081`
- 连通性检查命令：`curl -i -s --max-time 5 http://localhost:8081/system/config/configKey/sys.index.skinName`
- 错误摘要：受鉴权保护的配置接口返回业务 JSON，HTTP 200 且内容为未认证提示 `code=401`，说明当前分支构建的后端 jar 已启动并能处理请求。
- 当前执行 agent 环境：后端 jar 使用 test profile 在 8081 启动成功，完成连通性检查后已停止该额外进程。

## Review 返修记录

- 无 RF 项。
