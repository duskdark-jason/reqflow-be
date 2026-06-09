# Harness 初始化指令

项目：${projectName}
仓库：${repoName}
客户线：${variantName}
基线分支：${baselineBranch}

请在目标仓库中按以下顺序执行：

1. 阅读仓库根目录 AGENTS.md。
2. 创建或更新 docs/ai-harness/harness-index.json。
3. 创建流程、模板和检查脚本。
4. 运行 sh scripts/check-docs.sh 和 sh scripts/check-harness.sh init。
5. 将初始化结果、提交号和异常说明回填到需求平台。

