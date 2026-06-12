# Harness 初始化指令

项目：${projectName}
仓库：${repoName}
项目分支：${variantName}
基线分支：${baselineBranch}

请在目标仓库中按以下顺序执行：

1. 阅读仓库根目录 AGENTS.md。
2. 切换到基线分支并拉取最新代码：`git switch ${baselineBranch}`、`git pull --ff-only`。
3. 创建或更新 docs/ai-harness/harness-index.json。
4. 创建流程、模板和检查脚本。
5. 运行 sh scripts/check-docs.sh 和 sh scripts/check-harness.sh init。
6. 校验通过后提交并推送初始化生成或升级的 AGENTS.md、docs/ 和 scripts/。
7. 将初始化模式、提交号、push 结果和异常说明回填到需求平台。
