#!/bin/sh

set -u

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd -P)
FAILED=0

list_files() {
  root=$1

  if [ -f "$root/AGENTS.md" ]; then
    printf '%s\n' "$root/AGENTS.md"
  fi

  if [ -d "$root/docs" ]; then
    find "$root/docs" -type f \( \
      -name '*.md' -o \
      -name '*.sql' -o \
      -name '*.txt' -o \
      -name '*.json' \
    \)
  fi
}

scan_pattern() {
  root=$1
  title=$2
  pattern=$3
  skip_templates=$4

  output=$(
    list_files "$root" | while IFS= read -r file; do
      if [ "$skip_templates" = "yes" ] && printf '%s\n' "$file" | grep -q '/templates/'; then
        continue
      fi
      if [ "$skip_templates" = "yes" ] && [ "$(basename "$file")" = "local-run-template.md" ]; then
        continue
      fi
      if [ "$skip_templates" = "yes" ] && [ "$(basename "$root")" = "harness-template" ]; then
        continue
      fi
      grep -nH -E "$pattern" "$file" 2>/dev/null || true
    done
  )

  if [ -n "$output" ]; then
    printf '%s\n' "检查失败：$title"
    printf '%s\n' "$output"
    FAILED=1
  fi
}

check_for_dstore() {
  root=$1

  output=$(find "$root" \( \
      -path "$root/.git" -o \
      -path "$root/.idea" -o \
      -path "$root/node_modules" -o \
      -path "$root/dist" -o \
      -path "$root/target" \
    \) -prune -o -name '.DS_Store' -type f -print 2>/dev/null || true)
  if [ -n "$output" ]; then
    printf '%s\n' "检查失败：发现 macOS 元数据文件 .DS_Store"
    printf '%s\n' "$output"
    FAILED=1
  fi
}

check_chinese_markdown_headings() {
  root=$1

  output=$(
    list_files "$root" | while IFS= read -r file; do
      if ! printf '%s\n' "$file" | grep -E '\.md$' >/dev/null 2>&1; then
        continue
      fi

      in_fence=0
      line_no=0
      while IFS= read -r line || [ -n "$line" ]; do
        line_no=$((line_no + 1))

        if printf '%s\n' "$line" | grep -E '^(```|~~~)' >/dev/null 2>&1; then
          if [ "$in_fence" -eq 0 ]; then
            in_fence=1
          else
            in_fence=0
          fi
          continue
        fi

        [ "$in_fence" -eq 0 ] || continue

        if printf '%s\n' "$line" | grep -E '^#{1,6}[[:space:]]+' >/dev/null 2>&1; then
          title=$(printf '%s\n' "$line" | sed -E 's/^#{1,6}[[:space:]]+//')
          if printf '%s\n' "$title" | grep -E '[A-Za-z]' >/dev/null 2>&1 &&
             ! printf '%s\n' "$title" | grep -E '[一-龥]' >/dev/null 2>&1; then
            printf '%s:%s:%s\n' "$file" "$line_no" "$line"
          fi
        fi
      done < "$file"
    done
  )

  if [ -n "$output" ]; then
    printf '%s\n' "检查失败：Markdown 文档存在纯英文标题，请使用中文描述"
    printf '%s\n' "$output"
    FAILED=1
  fi
}

check_root() {
  root=$1

  if [ ! -d "$root" ]; then
    printf '%s\n' "检查失败：路径不存在：$root"
    FAILED=1
    return
  fi

  check_for_dstore "$root"
  check_chinese_markdown_headings "$root"
  scan_pattern "$root" "本机绝对路径" '(/Users/|/home/[^/[:space:]]+|[A-Za-z]:\\Users\\)' "no"
  scan_pattern "$root" "非模板文档残留占位符" '【[^】]*(字段名|接口路径|接口方法名|需求名称|模块名称|页面/组件名称|填写|说明|目标|参数|类型|是/否|路径|命令|YYYY|占位)[^】]*】' "yes"
  scan_pattern "$root" "非模板文档残留 TODO/TBD" '(TODO|TBD)' "yes"
}

if [ "$#" -eq 0 ]; then
  check_root "$REPO_ROOT"
else
  for root in "$@"; do
    check_root "$root"
  done
fi

if [ "$FAILED" -ne 0 ]; then
  exit 1
fi

printf '%s\n' "文档检查通过"
