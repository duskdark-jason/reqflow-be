#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CHECK_SCRIPT="$SCRIPT_DIR/check-harness.sh"

TMP_DIR=$(mktemp -d "${TMPDIR:-/tmp}/check-harness-test.XXXXXX")
OUT_FILE="$TMP_DIR/check-harness.out"
trap 'rm -rf "$TMP_DIR"' EXIT HUP INT TERM

for script_name in check-harness test-check-harness; do
  cmd_script="$SCRIPT_DIR/$script_name.cmd"
  sh_script="$script_name.sh"

  if [ ! -f "$cmd_script" ]; then
    echo "missing Windows wrapper: $cmd_script"
    exit 1
  fi

  if ! grep -q "$sh_script" "$cmd_script"; then
    echo "Windows wrapper must invoke $sh_script"
    exit 1
  fi

  if ! grep -q 'exit /b %ERRORLEVEL%' "$cmd_script"; then
    echo "Windows wrapper must propagate the script exit code"
    exit 1
  fi
done

write_harness_index() {
  file=$1
  state=${2:-initialized}

  case "$state" in
    template)
      template_value=true
      initialized_value=false
      repo_name=""
      repo_role="unknown"
      local_run="docs/runbooks/local-run-template.md"
      ;;
    uninitialized)
      template_value=false
      initialized_value=false
      repo_name=""
      repo_role="unknown"
      local_run="docs/runbooks/local-run.detected.md"
      ;;
    initialized)
      template_value=false
      initialized_value=true
      repo_name="demo"
      repo_role="backend"
      local_run="docs/runbooks/local-run.detected.md"
      ;;
    confirmed)
      template_value=false
      initialized_value=true
      repo_name="demo"
      repo_role="backend"
      local_run="docs/runbooks/local-run.md"
      ;;
    *)
      echo "unknown harness index state: $state"
      exit 1
      ;;
  esac

  printf '%s\n' \
    '{' \
    '  "schemaVersion": 1,' \
    "  \"template\": $template_value," \
    '  "harnessVersion": "2026-06-09",' \
    "  \"initialized\": $initialized_value," \
    '  "repository": {' \
    "    \"name\": \"$repo_name\"," \
    "    \"role\": \"$repo_role\"," \
    '    "workspace": "",' \
    '    "companionRepositories": []' \
    '  },' \
  '  "entrypoints": {' \
  '    "workflow": "docs/process/agent-workflow.md",' \
  '    "platformKeyWorkflow": "docs/process/platform-key-workflow.md",' \
  '    "verification": "docs/ai-harness/verification.md",' \
    "    \"localRun\": \"$local_run\"," \
    '    "localRunTemplate": "docs/runbooks/local-run-template.md",' \
    '    "localRunDetected": "docs/runbooks/local-run.detected.md",' \
    '    "localRunConfirmed": "docs/runbooks/local-run.md",' \
    '    "activeSpecs": "docs/specs/active"' \
    '  },' \
    '  "commands": {' \
    '    "init": "sh scripts/check-docs.sh && sh scripts/check-harness.sh init",' \
    '    "review": "sh scripts/check-harness.sh review",' \
    '    "complete": "sh scripts/check-harness.sh complete"' \
    '  },' \
    '  "customization": {' \
    '    "customerBranches": [],' \
    '    "taskBranchPrefix": "feature"' \
    '  }' \
    '}' > "$file"
}

make_root() {
  root=$1
  rm -rf "$root"
  mkdir -p "$root/docs/ai-harness/contracts" "$root/docs/ai-harness/decisions" "$root/docs/ai-harness/modules" "$root/docs/process" "$root/docs/runbooks" "$root/docs/specs/active/2026-06-09-REQ-001-演示需求"
  printf '%s\n' '# verification' > "$root/docs/ai-harness/verification.md"
  write_harness_index "$root/docs/ai-harness/harness-index.json" confirmed
  printf '%s\n' '# workflow' > "$root/docs/process/agent-workflow.md"
  printf '%s\n' '# platform key workflow' > "$root/docs/process/platform-key-workflow.md"
  printf '%s\n' '# local run' > "$root/docs/runbooks/local-run.md"
  printf '%s\n' '# meta' '' '- 状态：executing' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：未授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
  printf '%s\n' '# requirement' '' '- AC-001: demo' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/requirement.md"
  printf '%s\n' '# plan' '' '- AC-001: L0 L1 L2 L3' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/plan.md"
}

make_init_root() {
  root=$1
  rm -rf "$root"
  mkdir -p "$root/docs/ai-harness/contracts" "$root/docs/ai-harness/decisions" "$root/docs/ai-harness/modules" "$root/docs/process" "$root/docs/runbooks" "$root/docs/specs/active/2026-06-09-REQ-001-演示需求"
  printf '%s\n' '# verification' > "$root/docs/ai-harness/verification.md"
  write_harness_index "$root/docs/ai-harness/harness-index.json"
  printf '%s\n' '# workflow' > "$root/docs/process/agent-workflow.md"
  printf '%s\n' '# platform key workflow' > "$root/docs/process/platform-key-workflow.md"
  printf '%s\n' '# detected local run' > "$root/docs/runbooks/local-run.detected.md"
  printf '%s\n' '# requirement' '' '- AC-999: intentionally incomplete during init' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/requirement.md"
}

expect_fail() {
  if sh "$CHECK_SCRIPT" "$1" "$2" >"$OUT_FILE" 2>&1; then
    echo "expected check-harness to fail: $3"
    exit 1
  fi
  if ! grep -q "$4" "$OUT_FILE"; then
    echo "expected failure output to mention: $4"
    cat "$OUT_FILE"
    exit 1
  fi
}

root="$TMP_DIR/root"

make_init_root "$root"
sh "$CHECK_SCRIPT" "$root" init >"$OUT_FILE" 2>&1

make_init_root "$root"
write_harness_index "$root/docs/ai-harness/harness-index.json" uninitialized
expect_fail "$root" init "uninitialized harness index" "尚未完成初始化"

make_init_root "$root"
write_harness_index "$root/docs/ai-harness/harness-index.json" template
expect_fail "$root" init "template index copied to project" "模板索引"

make_init_root "$root"
rm "$root/docs/ai-harness/harness-index.json"
expect_fail "$root" init "missing harness index" "harness-index.json"

make_init_root "$root"
rm -rf "$root/docs/ai-harness/contracts"
expect_fail "$root" init "missing contracts directory" "contracts"

make_root "$root"
rm "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "missing spec meta" "缺少文件"

make_root "$root"
printf '%s\n' '# meta' '' '- 状态：unknown' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：未授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "invalid spec status" "状态"

make_root "$root"
printf '%s\n' '# meta' '' '- 状态：planning' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：未授权' '- Review 授权：未授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "status role mismatch" "状态和当前角色不匹配"

make_root "$root"
printf '%s\n' '# requirement' '' '- AC-001: demo' '- AC-002: another demo' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/requirement.md"
expect_fail "$root" complete "plan misses acceptance id" "AC-002"

make_root "$root"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
expect_fail "$root" review "review without execution report" "缺少执行报告"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：待定' '' '- AC-001：未验证' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：review' '- 当前角色：Review Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" review "invalid review conclusion" "Review 结论必须"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "complete without review report" "缺少 Review 报告"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' '' '当前执行 agent 实测：环境不可达。' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
expect_fail "$root" complete "environment claim without runtime evidence" "环境断言"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' '' '旧结论：环境不可达 是错误结论，已由实测覆盖。' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
sh "$CHECK_SCRIPT" "$root" complete >"$OUT_FILE" 2>&1

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' '' '## 运行态证据' '' '- 执行目录：/workspace/repo' '- 启动命令：sh run.sh' '- profile/env/mode：test' '- 检查命令：curl http://localhost:8080/health' '- 原始错误摘要：connection refused' '- 是否代表用户环境：否，仅代表当前执行 agent 环境' '' '当前执行 agent 所在环境实测：环境不可达。' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
sh "$CHECK_SCRIPT" "$root" complete >"$OUT_FILE" 2>&1

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' '' '## 运行态证据' '' '- 执行目录：/workspace/repo' '- 启动命令：sh run.sh' '- profile/env/mode：test' '- 检查命令：curl http://localhost:8080/health' '- console/network 错误摘要：connection refused' '- 是否代表用户环境：否，仅代表当前执行 agent 环境' '' '当前执行 agent 所在环境实测：环境不可达。' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
sh "$CHECK_SCRIPT" "$root" complete >"$OUT_FILE" 2>&1

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：阻断' '' '## 返修交接清单' '' '| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |' '|---|---|---|---|---|---|' '| RF-001 | 阻断 | AC-001 | demo | fix | sh test.sh |' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：review' '- 当前角色：Review Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
sh "$CHECK_SCRIPT" "$root" review >"$OUT_FILE" 2>&1
expect_fail "$root" complete "blocking review without repair" "Review 结论仍为阻断"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# meta' '' '- 状态：planning' '- 当前角色：Plan Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：未授权' '- Review 授权：未授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "planning state with execution report" "planning"

make_root "$root"
mkdir -p "$root/docs/specs/active/broken"
printf '%s\n' '# requirement' '' '- AC-999: broken' > "$root/docs/specs/active/broken/requirement.md"
printf '%s\n' '# plan' '' '- AC-999: L0' > "$root/docs/specs/active/broken/plan.md"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
sh "$CHECK_SCRIPT" "$root" complete --spec docs/specs/active/2026-06-09-REQ-001-演示需求 >"$OUT_FILE" 2>&1

make_root "$root"
mv "$root/docs/specs/active/2026-06-09-REQ-001-演示需求" "$root/docs/specs/active/2026-06-09-REQ-002-english-title"
expect_fail "$root" complete "english spec directory title" "中文需求标题"

make_root "$root"
mv "$root/docs/specs/active/2026-06-09-REQ-001-演示需求" "$root/docs/specs/active/2026-06-09-缺少编号"
expect_fail "$root" complete "spec directory without stable requirement id" "REQ-001"

make_root "$root"
printf '%s\n' '# execution' '' '## 执行结论' '' '- 状态：已完成' '- 分支：feature/demo' '- commit：无' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
printf '%s\n' '# meta' '' '- 状态：complete' '- 当前角色：Execution Agent' '- 流程模式：平台自身建设模式' '- 需求 Key：无，本地平台建设' '- 平台关联远端：未配置' '- 平台目标分支：feature/demo' '- 执行模式：隔离开发模式' '- 执行授权：已授权' '- Review 授权：已授权' '- 主分支修改授权：不适用' '- 当前分支：feature/demo' '- companion 仓库：无' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/meta.md"
expect_fail "$root" complete "isolated mode without commit record" "隔离开发模式"

make_root "$root"
printf '%s\n' '# execution' '' '## 验证结果' '' '- AC-001 命令：sh test.sh' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/execution-report.md"
printf '%s\n' '# review' '' '## Review 结论' '' '- 结论：通过' '' '- AC-001：通过' > "$root/docs/specs/active/2026-06-09-REQ-001-演示需求/review-report.md"
if sh "$CHECK_SCRIPT" "$root" complete --spec docs/specs/active/2026-06-09-REQ-001-演示需求 >"$OUT_FILE" 2>&1; then
  echo "expected target spec complete check to fail"
  exit 1
fi
if ! grep -q "状态为 complete" "$OUT_FILE"; then
  echo "expected failure output to mention: 状态为 complete"
  cat "$OUT_FILE"
  exit 1
fi

echo "check-harness tests passed"
