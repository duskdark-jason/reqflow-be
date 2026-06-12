#!/bin/sh

set -u

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
DEFAULT_REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd -P)
FAILED=0
MODE=complete
REPO_ROOT=$DEFAULT_REPO_ROOT
ROOT_SET=0
SPEC_PATH=
EXPECT_SPEC_VALUE=0
TARGET_SPEC_DIR=

usage() {
  printf '%s\n' "用法：sh scripts/check-harness.sh [init|review|complete] [repo-root] [--spec spec-path]"
}

for arg in "$@"; do
  if [ "$EXPECT_SPEC_VALUE" -eq 1 ]; then
    SPEC_PATH=$arg
    EXPECT_SPEC_VALUE=0
    continue
  fi

  case "$arg" in
    init|review|complete)
      MODE=$arg
      ;;
    --spec)
      EXPECT_SPEC_VALUE=1
      ;;
    --spec=*)
      SPEC_PATH=${arg#--spec=}
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [ "$ROOT_SET" -eq 0 ]; then
        REPO_ROOT=$arg
        ROOT_SET=1
      else
        usage
        exit 2
      fi
      ;;
  esac
done

if [ "$EXPECT_SPEC_VALUE" -eq 1 ]; then
  usage
  exit 2
fi

if [ -n "$SPEC_PATH" ] && [ "$SPEC_PATH" = "--spec" ]; then
  usage
  exit 2
fi

if [ -n "$SPEC_PATH" ]; then
  case "$SPEC_PATH" in
    /*)
      TARGET_SPEC_DIR=$SPEC_PATH
      ;;
    *)
      TARGET_SPEC_DIR="$REPO_ROOT/$SPEC_PATH"
      ;;
  esac
fi

fail() {
  printf '%s\n' "检查失败：$1"
  FAILED=1
}

check_file() {
  file=$1
  if [ ! -f "$file" ]; then
    fail "缺少文件：$file"
  fi
}

check_dir() {
  dir=$1
  if [ ! -d "$dir" ]; then
    fail "缺少目录：$dir"
  fi
}

check_spec_dir_name() {
  spec_dir=$1
  spec_name=$(basename "$spec_dir")

  if printf '%s\n' "$spec_name" | grep -E '^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]-' >/dev/null 2>&1; then
    fail "spec 目录名不得包含日期前缀，应使用 REQ-001-中文需求标题：$spec_dir"
    return
  fi

  if ! printf '%s\n' "$spec_name" | grep -E '^REQ-[0-9][0-9][0-9]-' >/dev/null 2>&1; then
    fail "spec 目录名必须使用 REQ-001-中文需求标题：$spec_dir"
    return
  fi

  if ! printf '%s' "$spec_name" | LC_ALL=C grep '[^ -~]' >/dev/null 2>&1; then
    fail "spec 目录名必须包含中文需求标题：$spec_dir"
  fi
}

check_pattern() {
  file=$1
  pattern=$2
  message=$3
  if [ -f "$file" ] && ! grep -E "$pattern" "$file" >/dev/null 2>&1; then
    fail "${message}：$file"
  fi
}

extract_json_string_field() {
  file=$1
  field=$2
  grep -E "\"$field\"[[:space:]]*:" "$file" 2>/dev/null |
    head -n 1 |
    sed -E "s/.*\"$field\"[[:space:]]*:[[:space:]]*\"([^\"]*)\".*/\\1/"
}

check_index_file_target() {
  index_file=$1
  field=$2
  value=$(extract_json_string_field "$index_file" "$field")

  if [ -z "$value" ]; then
    fail "harness 索引缺少有效路径字段 $field：$index_file"
    return
  fi

  case "$value" in
    /*)
      target=$value
      ;;
    *)
      target="$REPO_ROOT/$value"
      ;;
  esac

  if [ ! -f "$target" ]; then
    fail "harness 索引字段 $field 指向不存在文件：$value"
  fi
}

list_ac_ids() {
  file=$1
  grep -Eo 'AC-[A-Z0-9-]*[0-9][0-9][0-9]|AC-[0-9][0-9][0-9]' "$file" 2>/dev/null | sort -u || true
}

check_spec_meta() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"

  check_file "$meta_file"
  check_pattern "$meta_file" '状态[：:][[:space:]]*(planning|executing|review|repairing|complete)' "需求元信息缺少有效状态"
  check_pattern "$meta_file" '当前角色[：:][[:space:]]*(Plan Agent|Execution Agent|Review Agent|用户|人工)' "需求元信息缺少当前角色"
  check_pattern "$meta_file" '流程模式[：:][[:space:]]*(需求平台需求设计模式|需求平台编排模式|需求平台开发模式|项目接入初始化模式|平台自身建设模式)' "需求元信息缺少有效流程模式"
  check_pattern "$meta_file" '需求 Key[：:]' "需求元信息缺少需求 Key"
  check_pattern "$meta_file" '平台关联远端[：:]' "需求元信息缺少平台关联远端"
  check_pattern "$meta_file" '平台目标分支[：:]' "需求元信息缺少平台目标分支"
  check_pattern "$meta_file" '执行模式[：:][[:space:]]*(不适用|普通模式|任务分支模式)' "需求元信息缺少有效执行模式"
  check_pattern "$meta_file" '执行授权[：:][[:space:]]*(未授权|已授权|不适用)' "需求元信息缺少有效执行授权"
  check_pattern "$meta_file" 'Review 授权[：:][[:space:]]*(未授权|已授权|不适用)' "需求元信息缺少有效 Review 授权"
  check_pattern "$meta_file" '当前分支[：:]' "需求元信息缺少当前分支"
  check_pattern "$meta_file" 'companion 仓库[：:]' "需求元信息缺少 companion 仓库"
  check_pattern "$meta_file" '影响模块[：:]' "需求元信息缺少影响模块"
  check_pattern "$meta_file" '模块知识库动作[：:][[:space:]]*(新增|更新|无需更新)' "需求元信息缺少有效模块知识库动作"
  check_pattern "$meta_file" '模块知识库文档[：:]' "需求元信息缺少模块知识库文档"
  check_pattern "$meta_file" '无需更新原因[：:]' "需求元信息缺少模块知识库无需更新原因"
}

extract_spec_status() {
  meta_file=$1
  grep -E '状态[：:]' "$meta_file" 2>/dev/null |
    head -n 1 |
    sed 's/.*状态[：:]//; s/^[[:space:]]*//; s/[[:space:]].*$//'
}

extract_spec_role() {
  meta_file=$1
  grep -E '当前角色[：:]' "$meta_file" 2>/dev/null |
    head -n 1 |
    sed 's/.*当前角色[：:]//; s/^[[:space:]]*//; s/[[:space:]]*$//'
}

extract_meta_field() {
  meta_file=$1
  field=$2
  grep -E "$field[：:]" "$meta_file" 2>/dev/null |
    head -n 1 |
    sed "s/.*$field[：:]//; s/^[[:space:]]*//; s/[[:space:]]*$//"
}

check_spec_state() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"
  execution_file="$spec_dir/execution-report.md"
  review_file="$spec_dir/review-report.md"

  [ -f "$meta_file" ] || return

  status=$(extract_spec_status "$meta_file")
  role=$(extract_spec_role "$meta_file")
  execution_mode=$(extract_meta_field "$meta_file" "执行模式")
  branch=$(extract_meta_field "$meta_file" "当前分支")
  execution_auth=$(extract_meta_field "$meta_file" "执行授权")
  review_auth=$(extract_meta_field "$meta_file" "Review 授权")

  case "$status:$role" in
    "planning:Plan Agent"|"planning:用户"|"planning:人工"|"executing:Execution Agent"|"executing:用户"|"executing:人工"|"review:Review Agent"|"review:用户"|"review:人工"|"repairing:Execution Agent"|"repairing:用户"|"repairing:人工"|"complete:Execution Agent"|"complete:Review Agent"|"complete:用户"|"complete:人工")
      ;;
    *)
      fail "meta 状态和当前角色不匹配：$meta_file"
      ;;
  esac

  if [ "$MODE" = "complete" ] && [ -n "$TARGET_SPEC_DIR" ] && [ "$status" != "complete" ]; then
    fail "目标 spec 完成态要求 meta.md 状态为 complete：$meta_file"
    return
  fi

  case "$status" in
    planning)
      if [ -f "$execution_file" ]; then
        fail "meta 状态为 planning，但已有执行报告：$execution_file"
      fi
      if [ -f "$review_file" ]; then
        fail "meta 状态为 planning，但已有 Review 报告：$review_file"
      fi
      ;;
    executing)
      if [ -f "$review_file" ]; then
        fail "meta 状态为 executing，但已有 Review 报告：$review_file"
      fi
      ;;
    review)
      check_file "$execution_file"
      check_file "$review_file"
      ;;
    repairing|complete)
      check_file "$execution_file"
      check_file "$review_file"
      ;;
  esac

  case "$status" in
    executing|repairing|complete)
      if [ "$execution_auth" != "已授权" ]; then
        fail "meta 状态为 $status 时必须记录执行授权：已授权：$meta_file"
      fi
      if [ "$execution_mode" != "任务分支模式" ]; then
        fail "meta 状态为 $status 时必须记录执行模式：任务分支模式：$meta_file"
      fi
      if printf '%s\n' "$branch" | grep -E '^(main|master|未创建|无)?$' >/dev/null 2>&1; then
        fail "meta 状态为 $status 时当前分支必须是任务分支，不能是 $branch：$meta_file"
      fi
      if printf '%s' "$branch" | LC_ALL=C grep '[^ -~]' >/dev/null 2>&1; then
        fail "任务分支必须使用 ASCII：$meta_file"
      fi
      ;;
  esac

  case "$status" in
    review|complete)
      if [ "$review_auth" != "已授权" ]; then
        fail "meta 状态为 $status 时必须记录 Review 授权：已授权：$meta_file"
      fi
      ;;
  esac

}

check_ac_coverage() {
  spec_dir=$1
  requirement_file="$spec_dir/requirement.md"
  plan_file="$spec_dir/plan.md"
  execution_file="$spec_dir/execution-report.md"
  review_file="$spec_dir/review-report.md"

  ac_ids=$(list_ac_ids "$requirement_file")
  [ -n "$ac_ids" ] || return

  for ac_id in $ac_ids; do
    if ! grep -E "$ac_id" "$plan_file" >/dev/null 2>&1; then
      fail "计划缺少验收 ID 覆盖：$ac_id"
    fi
    if [ -f "$execution_file" ] && ! grep -E "$ac_id" "$execution_file" >/dev/null 2>&1; then
      fail "执行报告缺少验收 ID 覆盖：$ac_id"
    fi
    if [ -f "$review_file" ] && ! grep -E "$ac_id" "$review_file" >/dev/null 2>&1; then
      fail "Review 报告缺少验收 ID 覆盖：$ac_id"
    fi
  done
}

has_runtime_evidence() {
  file=$1
  grep -E '运行态证据' "$file" >/dev/null 2>&1 &&
    grep -E '执行目录' "$file" >/dev/null 2>&1 &&
    grep -E '启动命令' "$file" >/dev/null 2>&1 &&
    grep -E '(连通性检查命令|浏览器或 Playwright 命令|检查命令)' "$file" >/dev/null 2>&1 &&
    grep -E '(原始错误摘要|console/network 错误摘要|错误摘要)' "$file" >/dev/null 2>&1 &&
    grep -E '当前执行 agent 环境' "$file" >/dev/null 2>&1
}

check_repair_handoff() {
  spec_dir=$1
  review_file="$spec_dir/review-report.md"
  execution_file="$spec_dir/execution-report.md"
  [ -f "$review_file" ] || return

  check_pattern "$review_file" '结论：|Review 结论' "Review 报告缺少结论"
  if [ -f "$review_file" ] && ! grep -E '结论[：:][[:space:]]*(通过|有条件通过|阻断)' "$review_file" >/dev/null 2>&1; then
    fail "Review 结论必须是通过/有条件通过/阻断：$review_file"
  fi

  if grep -E '结论[：:].*(阻断|有条件通过)' "$review_file" >/dev/null 2>&1; then
    check_pattern "$review_file" '返修交接清单' "Review 报告缺少返修交接清单"
    check_pattern "$review_file" 'RF-[0-9][0-9][0-9]' "Review 报告缺少返修 ID"
  fi

  if [ "$MODE" = "review" ]; then
    return
  fi

  if grep -E '结论[：:].*(阻断|有条件通过)' "$review_file" >/dev/null 2>&1 &&
     ! grep -E '(复审结论|最终结论)[：:][[:space:]]*通过' "$review_file" >/dev/null 2>&1; then
    fail "Review 结论未最终通过，不能进入完成态：$review_file"
  fi

  repair_ids=$(grep -Eo 'RF-[0-9][0-9][0-9]' "$review_file" 2>/dev/null | sort -u || true)
  [ -n "$repair_ids" ] || return

  if [ ! -f "$execution_file" ]; then
    fail "Review 已产生返修项，但缺少执行报告：$execution_file"
    return
  fi

  check_pattern "$execution_file" 'Review 返修记录' "执行报告缺少 Review 返修记录"

  for repair_id in $repair_ids; do
    if ! grep -E "$repair_id.*(已修复|已处理|无需修复|用户接受|不修复)" "$execution_file" >/dev/null 2>&1; then
      fail "执行报告缺少返修项处理结果：$repair_id"
    fi
  done
}

check_task_branch_commit_record() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"
  execution_file="$spec_dir/execution-report.md"

  [ -f "$meta_file" ] || return
  [ -f "$execution_file" ] || return

  if ! grep -E '执行模式[：:][[:space:]]*任务分支模式' "$meta_file" >/dev/null 2>&1; then
    return
  fi

  if ! grep -E '(commit|提交)[：:][[:space:]]*[^[:space:]]+' "$execution_file" >/dev/null 2>&1 ||
     grep -E '(commit|提交)[：:][[:space:]]*(无|未提交|未执行|N/A|n/a|none|None)[[:space:]]*$' "$execution_file" >/dev/null 2>&1; then
    fail "任务分支模式必须在 execution-report.md 记录 commit：$execution_file"
  fi
}

check_module_knowledge_record() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"
  execution_file="$spec_dir/execution-report.md"

  [ -f "$meta_file" ] || return

  status=$(extract_spec_status "$meta_file")
  action=$(extract_meta_field "$meta_file" "模块知识库动作")
  module_docs_value=$(extract_meta_field "$meta_file" "模块知识库文档")
  no_update_reason=$(extract_meta_field "$meta_file" "无需更新原因")

  case "$action" in
    新增|更新)
      if ! printf '%s\n' "$module_docs_value" | grep -E 'docs/ai-harness/modules/[^[:space:]，,`]+\.md' >/dev/null 2>&1; then
        fail "模块知识库动作为 $action 时必须在 meta.md 记录 docs/ai-harness/modules/*.md：$meta_file"
      fi

      module_doc_paths=$(printf '%s\n' "$module_docs_value" | grep -Eo 'docs/ai-harness/modules/[^[:space:]，,`]+\.md' || true)
      for module_doc_path in $module_doc_paths; do
        if [ ! -f "$REPO_ROOT/$module_doc_path" ]; then
          fail "模块知识库文档不存在：$module_doc_path"
        fi
      done

      if [ "$MODE" = "complete" ]; then
        case "$status" in
          review|repairing|complete)
          if [ -f "$execution_file" ] &&
             ! grep -E 'docs/ai-harness/modules/[^[:space:]，,`]+\.md' "$execution_file" >/dev/null 2>&1; then
            fail "模块知识库动作为 $action 时执行报告必须记录模块文档路径：$execution_file"
          fi
            ;;
        esac
      fi
      ;;
    无需更新)
      if [ -z "$no_update_reason" ]; then
        fail "模块知识库动作为无需更新时必须记录具体原因：$meta_file"
      fi
      ;;
  esac
}

spec_mentions_db_change() {
  spec_dir=$1
  requirement_file="$spec_dir/requirement.md"
  plan_file="$spec_dir/plan.md"

  grep -E '(数据库/SQL[：:][[:space:]]*是|新增.*(数据库|数据表|表结构|表字段|索引|约束|SQL)|修改.*(数据库|数据表|表结构|表字段|索引|约束|SQL|join|JOIN|聚合|统计口径|分页粒度|Mapper|MyBatis)|数据迁移|数据清理|DDL|DML|库表|表关系|Mapper XML|MyBatis|docs/db/sql/|docs/db/)' "$requirement_file" "$plan_file" >/dev/null 2>&1
}

check_db_change_record() {
  spec_dir=$1
  execution_file="$spec_dir/execution-report.md"

  [ "$MODE" = "complete" ] || return
  [ -d "$REPO_ROOT/docs/db" ] || return
  [ -f "$execution_file" ] || return
  spec_mentions_db_change "$spec_dir" || return

  if ! grep -E '(docs/db/sql/|docs/db/)' "$execution_file" >/dev/null 2>&1; then
    fail "需求涉及数据库变更、SQL、Mapper 或数据口径时，execution-report.md 必须记录 docs/db/sql/ 或 docs/db/ 路径：$execution_file"
  fi
}

check_code_comment_record() {
  spec_dir=$1
  execution_file="$spec_dir/execution-report.md"

  [ "$MODE" = "complete" ] || return
  [ -f "$execution_file" ] || return

  check_pattern "$execution_file" '代码注释处理' "执行报告缺少代码注释处理"
  check_pattern "$execution_file" '注释动作[：:][[:space:]]*(新增|更新|无需新增)' "执行报告缺少有效代码注释动作"
  check_pattern "$execution_file" '处理说明[：:]' "执行报告缺少代码注释处理说明"
}

check_requirement_assessment_record() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"
  requirement_file="$spec_dir/requirement.md"
  assessment_file="$spec_dir/assessment.md"

  [ -f "$meta_file" ] || return
  flow_mode=$(extract_meta_field "$meta_file" "流程模式")
  [ "$flow_mode" = "需求平台需求设计模式" ] || [ "$flow_mode" = "需求平台编排模式" ] || return

  target_file="$requirement_file"
  [ -f "$assessment_file" ] && target_file="$assessment_file"
  check_pattern "$target_file" '可行性评估|需求可行性|风险评估' "需求平台需求设计模式缺少可行性评估记录"
  check_pattern "$target_file" '评估结论|结论' "需求可行性评估缺少评估结论"
  check_pattern "$target_file" '可继续设计|需澄清|需调整|暂不可实现' "需求可行性评估缺少有效结论类型"
}

check_one_spec() {
  spec_dir=$1
  meta_file="$spec_dir/meta.md"

  check_file "$spec_dir/requirement.md"
  check_spec_meta "$spec_dir"
  check_spec_state "$spec_dir"

  status=$(extract_spec_status "$meta_file")
  if [ "$status" != "planning" ]; then
    check_file "$spec_dir/plan.md"
  fi

  check_pattern "$spec_dir/requirement.md" 'AC-[A-Z0-9-]*[0-9][0-9][0-9]|AC-[0-9][0-9][0-9]' "需求说明缺少验收 ID"
  if [ -f "$spec_dir/plan.md" ]; then
    check_pattern "$spec_dir/plan.md" 'L0|L1|L2|L3|L4' "执行计划缺少分层验证"
    check_ac_coverage "$spec_dir"
  fi

  if [ -f "$spec_dir/execution-report.md" ]; then
    check_pattern "$spec_dir/execution-report.md" '命令|未开始|未执行' "执行报告缺少命令或状态说明"
    check_task_branch_commit_record "$spec_dir"
  fi

  check_module_knowledge_record "$spec_dir"
  check_db_change_record "$spec_dir"
  check_code_comment_record "$spec_dir"
  check_requirement_assessment_record "$spec_dir"

  if [ -f "$spec_dir/review-report.md" ] && [ ! -f "$spec_dir/execution-report.md" ]; then
    fail "存在 Review 报告但缺少执行报告：$spec_dir/execution-report.md"
  fi

  if [ "$MODE" = "complete" ] &&
     [ -f "$spec_dir/execution-report.md" ] &&
     [ ! -f "$spec_dir/review-report.md" ]; then
    fail "完成态存在执行报告但缺少 Review 报告：$spec_dir/review-report.md"
  fi

  check_repair_handoff "$spec_dir"
}

check_active_specs() {
  if [ -n "$TARGET_SPEC_DIR" ]; then
    if [ ! -d "$TARGET_SPEC_DIR" ]; then
      fail "指定 spec 目录不存在：$TARGET_SPEC_DIR"
      return
    fi
    check_spec_dir_name "$TARGET_SPEC_DIR"
    check_one_spec "$TARGET_SPEC_DIR"
    return
  fi

  active_dir="$REPO_ROOT/docs/specs/active"
  [ -d "$active_dir" ] || return

  for spec_dir in "$active_dir"/*; do
    [ -d "$spec_dir" ] || continue
    case "$(basename "$spec_dir")" in
      .* ) continue ;;
    esac

    check_spec_dir_name "$spec_dir"
    check_one_spec "$spec_dir"
  done
}

check_environment_claims() {
  unsafe_matches=""
  if [ -n "$TARGET_SPEC_DIR" ]; then
    for file in "$TARGET_SPEC_DIR/execution-report.md" "$TARGET_SPEC_DIR/review-report.md"; do
      [ -f "$file" ] || continue
      matches=$(
        grep -nH -E '(DB 不可达|数据库不可达|远程 DB 不可达|环境不可达|服务不可用|后端不可用|本地无网络)' "$file" 2>/dev/null |
          grep -v -E '(旧文|旧结论|错误结论|未做实际检查|无证据|不符合|不得|不能|禁止|替换为|改为|覆盖|示例)' || true
      )
      [ -n "$matches" ] || continue
      if [ "$(basename "$file")" = "execution-report.md" ] && has_runtime_evidence "$file"; then
        continue
      fi
      unsafe_matches="${unsafe_matches}
${matches}"
    done
  else
    active_dir="$REPO_ROOT/docs/specs/active"
    [ -d "$active_dir" ] || return
    for file in "$active_dir"/*/execution-report.md "$active_dir"/*/review-report.md; do
      [ -f "$file" ] || continue
      matches=$(
        grep -nH -E '(DB 不可达|数据库不可达|远程 DB 不可达|环境不可达|服务不可用|后端不可用|本地无网络)' "$file" 2>/dev/null |
          grep -v -E '(旧文|旧结论|错误结论|未做实际检查|无证据|不符合|不得|不能|禁止|替换为|改为|覆盖|示例)' || true
      )
      [ -n "$matches" ] || continue
      if [ "$(basename "$file")" = "execution-report.md" ] && has_runtime_evidence "$file"; then
        continue
      fi
      unsafe_matches="${unsafe_matches}
${matches}"
    done
  fi
  if [ -n "$unsafe_matches" ]; then
    printf '%s\n' "检查失败：发现可能缺少证据的环境断言"
    printf '%s\n' "$unsafe_matches"
    FAILED=1
  fi
}

check_required_docs() {
  index_file="$REPO_ROOT/docs/ai-harness/harness-index.json"
  repo_name=$(basename "$REPO_ROOT")

  check_file "$REPO_ROOT/docs/ai-harness/verification.md"
  check_file "$index_file"
  check_file "$REPO_ROOT/docs/process/agent-workflow.md"
  check_file "$REPO_ROOT/docs/process/platform-key-workflow.md"
  check_dir "$REPO_ROOT/docs/ai-harness/contracts"
  check_dir "$REPO_ROOT/docs/ai-harness/decisions"
  check_dir "$REPO_ROOT/docs/ai-harness/modules"
  check_pattern "$index_file" '"schemaVersion"[[:space:]]*:' "harness 索引缺少 schemaVersion"
  check_pattern "$index_file" '"template"[[:space:]]*:' "harness 索引缺少 template"
  check_pattern "$index_file" '"initialized"[[:space:]]*:' "harness 索引缺少 initialized"
  check_pattern "$index_file" '"repository"[[:space:]]*:' "harness 索引缺少 repository"
  check_pattern "$index_file" '"entrypoints"[[:space:]]*:' "harness 索引缺少 entrypoints"
  check_pattern "$index_file" '"platformKeyWorkflow"[[:space:]]*:' "harness 索引缺少 platformKeyWorkflow"
  check_pattern "$index_file" '"commands"[[:space:]]*:' "harness 索引缺少 commands"
  check_pattern "$index_file" '"customization"[[:space:]]*:' "harness 索引缺少 customization"
  check_pattern "$index_file" '"customerBranches"[[:space:]]*:' "harness 索引缺少 customerBranches"
  check_pattern "$index_file" '"taskBranchPrefix"[[:space:]]*:' "harness 索引缺少 taskBranchPrefix"
  check_pattern "$index_file" '"localRun"[[:space:]]*:' "harness 索引缺少 localRun"
  check_pattern "$index_file" '"localRunTemplate"[[:space:]]*:' "harness 索引缺少 localRunTemplate"
  check_pattern "$index_file" '"localRunDetected"[[:space:]]*:' "harness 索引缺少 localRunDetected"
  check_pattern "$index_file" '"localRunConfirmed"[[:space:]]*:' "harness 索引缺少 localRunConfirmed"
  check_index_file_target "$index_file" "localRun"
  check_index_file_target "$index_file" "platformKeyWorkflow"

  if grep -E '"template"[[:space:]]*:[[:space:]]*true' "$index_file" >/dev/null 2>&1; then
    if [ "$repo_name" != "harness-template" ]; then
      fail "harness 索引仍为模板索引，请复制后改为 template=false 并完成初始化：$index_file"
    fi
  else
    if ! grep -E '"initialized"[[:space:]]*:[[:space:]]*true' "$index_file" >/dev/null 2>&1; then
      fail "harness 索引尚未完成初始化，请填写仓库信息并设置 initialized=true：$index_file"
    fi
    if grep -E '"name"[[:space:]]*:[[:space:]]*""' "$index_file" >/dev/null 2>&1; then
      fail "harness 索引缺少仓库名称：$index_file"
    fi
    if grep -E '"role"[[:space:]]*:[[:space:]]*"(unknown)?"' "$index_file" >/dev/null 2>&1; then
      fail "harness 索引缺少有效仓库角色：$index_file"
    fi
    module_doc=$(find "$REPO_ROOT/docs/ai-harness/modules" -maxdepth 1 -type f -name '*.md' -print 2>/dev/null | head -n 1)
    if [ -z "$module_doc" ]; then
      fail "项目 harness 初始化必须至少包含一个模块知识库文档：$REPO_ROOT/docs/ai-harness/modules/*.md"
    fi
  fi

  if [ ! -f "$REPO_ROOT/docs/runbooks/local-run.md" ] &&
     [ ! -f "$REPO_ROOT/docs/runbooks/local-run.detected.md" ] &&
     [ ! -f "$REPO_ROOT/docs/runbooks/local-run-template.md" ]; then
    fail "缺少运行手册或运行手册模板：$REPO_ROOT/docs/runbooks/"
  fi

  if [ -d "$REPO_ROOT/docs/db" ]; then
    check_file "$REPO_ROOT/docs/db/README.md"
    check_file "$REPO_ROOT/docs/db/table-dictionary.md"
    check_file "$REPO_ROOT/docs/db/relationship.md"
  fi
}

check_required_docs
if [ "$MODE" != "init" ]; then
  check_active_specs
  check_environment_claims
fi

if [ "$FAILED" -ne 0 ]; then
  exit 1
fi

printf '%s\n' "Harness 检查通过（$MODE 模式）"
