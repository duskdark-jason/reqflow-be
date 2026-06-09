#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CHECK_SCRIPT="$SCRIPT_DIR/check-docs.sh"

TMP_DIR=$(mktemp -d "${TMPDIR:-/tmp}/check-docs-test.XXXXXX")
OUT_FILE="$TMP_DIR/check-docs.out"
trap 'rm -rf "$TMP_DIR"' EXIT HUP INT TERM

for script_name in check-docs test-check-docs; do
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

mkdir -p "$TMP_DIR/docs/templates" "$TMP_DIR/docs/process"

printf '%s\n' 'metadata' > "$TMP_DIR/docs/.DS_Store"

if sh "$CHECK_SCRIPT" "$TMP_DIR" >"$OUT_FILE" 2>&1; then
  echo "expected .DS_Store check to fail"
  exit 1
fi

if ! grep -q ".DS_Store" "$OUT_FILE"; then
  echo "expected failure output to mention .DS_Store"
  cat "$OUT_FILE"
  exit 1
fi

rm "$TMP_DIR/docs/.DS_Store"

printf '%s\n' '# English Only' 'This heading should be rejected.' > "$TMP_DIR/docs/process/english.md"

if sh "$CHECK_SCRIPT" "$TMP_DIR" >"$OUT_FILE" 2>&1; then
  echo "expected English-only heading check to fail"
  exit 1
fi

if ! grep -q "英文标题" "$OUT_FILE"; then
  echo "expected failure output to mention English-only headings"
  cat "$OUT_FILE"
  exit 1
fi

rm "$TMP_DIR/docs/process/english.md"

printf '%s\n' '# 文档' '这里包含 /Users/example/project，应被识别。' > "$TMP_DIR/docs/process/bad.md"

if sh "$CHECK_SCRIPT" "$TMP_DIR" >"$OUT_FILE" 2>&1; then
  echo "expected personal path check to fail"
  exit 1
fi

if ! grep -q "本机绝对路径" "$OUT_FILE"; then
  echo "expected failure output to mention personal absolute path"
  cat "$OUT_FILE"
  exit 1
fi

rm "$TMP_DIR/docs/process/bad.md"
printf '%s\n' '# 模板' '【字段名】允许出现在模板中。' > "$TMP_DIR/docs/templates/template.md"
printf '%s\n' '# 正常文档' '团队文档不包含个人路径。' > "$TMP_DIR/docs/process/good.md"

sh "$CHECK_SCRIPT" "$TMP_DIR" >"$OUT_FILE" 2>&1

TEMPLATE_ROOT="$TMP_DIR/harness-template"
mkdir -p "$TEMPLATE_ROOT/docs/ai-harness"
printf '%s\n' '# 模板验证' '【填写编译或构建命令】允许保留在模板包内。' > "$TEMPLATE_ROOT/docs/ai-harness/verification.md"

sh "$CHECK_SCRIPT" "$TEMPLATE_ROOT" >"$OUT_FILE" 2>&1

echo "check-docs tests passed"
