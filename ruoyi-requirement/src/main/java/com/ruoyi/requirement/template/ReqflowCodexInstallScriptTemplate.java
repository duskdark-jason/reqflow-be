package com.ruoyi.requirement.template;

/**
 * Reqflow Codex 安装脚本模板。
 */
public final class ReqflowCodexInstallScriptTemplate
{
    private ReqflowCodexInstallScriptTemplate()
    {
    }

    public static String shellScript()
    {
        return normalizeSkillEmbedding("""
                #!/usr/bin/env bash
                set -euo pipefail

                SUPPORTED_CLIENTS="all codex claude-code trae qoder codebuddy opencode"
                INSTALLABLE_CLIENTS="codex claude-code trae qoder codebuddy opencode"
                CLIENT=""
                MCP_URL=""
                MCP_KEY_HEADER="X-MCP-Key"
                AUTOMATIC_MCP_CLIENTS=""
                MANUAL_MCP_IMPORTS=""
                SKILL_CLIENTS=""

                usage() {
                  cat <<'EOF'
                Usage: install.sh --url <reqflow-mcp-url> [--key <mcp-key>] [--client <client>]

                Clients: all, codex, claude-code, trae, qoder, codebuddy, opencode

                Omit --client to select target clients interactively after execution.
                The script installs the reqflow MCP configuration for all or the selected client and
                installs the reqflow-mcp global skill with:
                  npx skills add <local-skill-dir> -g -a <client> --copy -y
                EOF
                }

                while [ "$#" -gt 0 ]; do
                  case "$1" in
                    --client)
                      CLIENT="${2:-}"
                      shift 2
                      ;;
                    --url)
                      MCP_URL="${2:-}"
                      shift 2
                      ;;
                    --key)
                      REQFLOW_MCP_KEY="${2:-}"
                      shift 2
                      ;;
                    -h|--help)
                      usage
                      exit 0
                      ;;
                    *)
                      echo "Unknown argument: $1" >&2
                      exit 2
                      ;;
                  esac
                done

                MCP_KEY="${REQFLOW_MCP_KEY:-}"
                if [ -z "$MCP_URL" ]; then
                  echo "Missing --url <reqflow-mcp-url>" >&2
                  exit 2
                fi
                if [ -z "$MCP_KEY" ]; then
                  echo "Missing MCP key. Set REQFLOW_MCP_KEY or pass --key." >&2
                  exit 2
                fi

                SUPPORT_DIR="${REQFLOW_INSTALL_DIR:-$HOME/.reqflow-mcp}"
                mkdir -p "$SUPPORT_DIR"

                append_unique_word() {
                  local words="$1"
                  local word="$2"
                  case " $words " in
                    *" $word "*)
                      printf '%s' "$words"
                      ;;
                    *)
                      printf '%s %s' "$words" "$word"
                      ;;
                  esac
                }

                record_automatic_mcp() {
                  AUTOMATIC_MCP_CLIENTS=$(append_unique_word "$AUTOMATIC_MCP_CLIENTS" "$1")
                }

                record_manual_import() {
                  local client="$1"
                  local snippet_file="$2"
                  local instruction="$3"
                  MANUAL_MCP_IMPORTS="${MANUAL_MCP_IMPORTS}
                  - ${client}: ${snippet_file} (${instruction})"
                }

                record_skill_install() {
                  SKILL_CLIENTS=$(append_unique_word "$SKILL_CLIENTS" "$1")
                }

                sync_skill_to_dir() {
                  local source_file="$1"
                  local target_root="$2"
                  local target_dir="$target_root/reqflow-mcp"
                  mkdir -p "$target_dir"
                  cp "$source_file" "$target_dir/SKILL.md"
                }

                sync_known_skill_locations() {
                  local target_client="$1"
                  local source_file="$2"
                  sync_skill_to_dir "$source_file" "${AGENTS_HOME:-$HOME/.agents}/skills"
                  case "$target_client" in
                    codex)
                      sync_skill_to_dir "$source_file" "${CODEX_HOME:-$HOME/.codex}/skills"
                      ;;
                    claude-code)
                      sync_skill_to_dir "$source_file" "${CLAUDE_HOME:-$HOME/.claude}/skills"
                      ;;
                    trae)
                      sync_skill_to_dir "$source_file" "${TRAE_HOME:-$HOME/.trae}/skills"
                      ;;
                    qoder)
                      sync_skill_to_dir "$source_file" "${QODER_HOME:-$HOME/.qoder}/skills"
                      ;;
                    codebuddy)
                      sync_skill_to_dir "$source_file" "${CODEBUDDY_HOME:-$HOME/.codebuddy}/skills"
                      ;;
                    opencode)
                      sync_skill_to_dir "$source_file" "${OPENCODE_HOME:-${OPENCODE_CONFIG_DIR:-$HOME/.config/opencode}}/skills"
                      ;;
                  esac
                }

                toml_escape() {
                  printf '%s' "$1" | sed 's/\\\\/\\\\\\\\/g; s/"/\\\\"/g'
                }

                json_escape() {
                  printf '%s' "$1" | sed 's/\\\\/\\\\\\\\/g; s/"/\\\\"/g'
                }

                write_mcp_servers_snippet() {
                  local snippet_file="$1"
                  local server_type="$2"
                  local mcp_url_json
                  local mcp_key_json
                  mcp_url_json=$(json_escape "$MCP_URL")
                  mcp_key_json=$(json_escape "$MCP_KEY")
                  cat > "$snippet_file" <<EOF
                {
                  "mcpServers": {
                    "reqflow": {
                      "type": "$server_type",
                      "url": "$mcp_url_json",
                      "headers": {
                        "$MCP_KEY_HEADER": "$mcp_key_json"
                      }
                    }
                  }
                }
                EOF
                }

                write_opencode_config() {
                  local target_file="$1"
                  local mcp_url_json
                  local mcp_key_json
                  mcp_url_json=$(json_escape "$MCP_URL")
                  mcp_key_json=$(json_escape "$MCP_KEY")
                  cat > "$target_file" <<EOF
                {
                  "\\$schema": "https://opencode.ai/config.json",
                  "mcp": {
                    "reqflow": {
                      "type": "remote",
                      "url": "$mcp_url_json",
                      "enabled": true,
                      "headers": {
                        "$MCP_KEY_HEADER": "$mcp_key_json"
                      }
                    }
                  }
                }
                EOF
                }

                merge_json_config() {
                  local target_file="$1"
                  local root_key="$2"
                  local server_json="$3"
                  local schema="${4:-}"
                  if ! command -v node >/dev/null 2>&1; then
                    return 1
                  fi
                  mkdir -p "$(dirname "$target_file")"
                  CONFIG_FILE="$target_file" ROOT_KEY="$root_key" SERVER_NAME="reqflow" SERVER_JSON="$server_json" CONFIG_SCHEMA="$schema" node <<'NODE'
                const fs = require('fs');
                const file = process.env.CONFIG_FILE;
                const rootKey = process.env.ROOT_KEY;
                const serverName = process.env.SERVER_NAME || 'reqflow';
                const schema = process.env.CONFIG_SCHEMA;
                let config = {};
                if (fs.existsSync(file)) {
                  const text = fs.readFileSync(file, 'utf8').trim();
                  if (text) {
                    config = JSON.parse(text);
                  }
                }
                const server = JSON.parse(process.env.SERVER_JSON);
                if (!config || typeof config !== 'object' || Array.isArray(config)) {
                  config = {};
                }
                if (schema && !config.$schema) {
                  config.$schema = schema;
                }
                if (!config[rootKey] || typeof config[rootKey] !== 'object' || Array.isArray(config[rootKey])) {
                  config[rootKey] = {};
                }
                config[rootKey][serverName] = server;
                fs.writeFileSync(file, JSON.stringify(config, null, 2) + '\\n');
                NODE
                }

                resolve_opencode_config_file() {
                  local config_dir="$HOME/.config/opencode"
                  if [ -n "${OPENCODE_CONFIG:-}" ]; then
                    printf '%s\n' "$OPENCODE_CONFIG"
                  elif [ -e "$config_dir/opencode.json" ]; then
                    printf '%s\n' "$config_dir/opencode.json"
                  elif [ -e "$config_dir/opencode.jsonc" ]; then
                    printf '%s\n' "$config_dir/opencode.jsonc"
                  else
                    printf '%s\n' "$config_dir/opencode.json"
                  fi
                }

                resolve_codebuddy_config_file() {
                  local codebuddy_home="${CODEBUDDY_CONFIG_DIR:-${CODEBUDDY_HOME:-$HOME/.codebuddy}}"
                  if [ -e "$codebuddy_home/.mcp.json" ]; then
                    printf '%s\n' "$codebuddy_home/.mcp.json"
                  elif [ -e "$codebuddy_home/mcp.json" ]; then
                    printf '%s\n' "$codebuddy_home/mcp.json"
                  elif [ -e "$HOME/.codebuddy.json" ]; then
                    printf '%s\n' "$HOME/.codebuddy.json"
                  else
                    printf '%s\n' "$codebuddy_home/.mcp.json"
                  fi
                }

                install_codex_mcp() {
                  local codex_home
                  local config_file
                  local mcp_url_toml
                  local mcp_key_toml
                  local tmp_config
                  codex_home="${CODEX_HOME:-$HOME/.codex}"
                  config_file="$codex_home/config.toml"
                  mkdir -p "$codex_home"
                  touch "$config_file"

                  mcp_url_toml=$(toml_escape "$MCP_URL")
                  mcp_key_toml=$(toml_escape "$MCP_KEY")
                  tmp_config=$(mktemp)
                  awk '
                    /^\\[mcp_servers\\.reqflow\\]$/ { skip = 1; next }
                    /^\\[/ { skip = 0 }
                    !skip { print }
                  ' "$config_file" > "$tmp_config"
                  mv "$tmp_config" "$config_file"

                  cat >> "$config_file" <<EOF

                [mcp_servers.reqflow]
                url = "$mcp_url_toml"
                http_headers = { "$MCP_KEY_HEADER" = "$mcp_key_toml" }
                EOF
                  echo "Wrote Codex MCP config: $config_file"
                  record_automatic_mcp "codex"
                }

                install_claude_code_mcp() {
                  local snippet_file
                  if command -v claude >/dev/null 2>&1; then
                    if claude mcp add --transport http --scope user reqflow "$MCP_URL" --header "$MCP_KEY_HEADER: $MCP_KEY"; then
                      echo "Configured Claude Code MCP with claude mcp add."
                      record_automatic_mcp "claude-code"
                      return 0
                    fi
                    echo "claude mcp add failed; writing a .mcp.json snippet instead." >&2
                  fi
                  snippet_file="$SUPPORT_DIR/claude-code-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "http"
                  echo "Wrote Claude Code MCP snippet: $snippet_file"
                  echo "Import or merge the snippet into the Claude Code user/project MCP settings."
                  record_manual_import "claude-code" "$snippet_file" "merge into Claude Code user or project MCP settings"
                }

                install_trae_mcp() {
                  local snippet_file="$SUPPORT_DIR/trae-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "streamable-http"
                  echo "Wrote Trae MCP snippet: $snippet_file"
                  echo "Open Trae Settings > MCP and import or paste this mcpServers JSON."
                  record_manual_import "trae" "$snippet_file" "import in Trae Settings > MCP"
                }

                install_qoder_mcp() {
                  local snippet_file="$SUPPORT_DIR/qoder-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "streamable-http"
                  echo "Wrote Qoder MCP snippet: $snippet_file"
                  echo "Open Qoder Settings > MCP and import or paste this mcpServers JSON."
                  record_manual_import "qoder" "$snippet_file" "import in Qoder Settings > MCP"
                }

                install_codebuddy_mcp() {
                  local mcp_url_json
                  local mcp_key_json
                  local server_json
                  local config_file
                  local snippet_file
                  mcp_url_json=$(json_escape "$MCP_URL")
                  mcp_key_json=$(json_escape "$MCP_KEY")
                  server_json="{\\"type\\":\\"http\\",\\"url\\":\\"$mcp_url_json\\",\\"headers\\":{\\"$MCP_KEY_HEADER\\":\\"$mcp_key_json\\"}}"

                  if command -v codebuddy >/dev/null 2>&1; then
                    if codebuddy mcp add-json --scope user reqflow "$server_json"; then
                      echo "Configured CodeBuddy MCP with codebuddy mcp add-json."
                      record_automatic_mcp "codebuddy"
                      return 0
                    fi
                    echo "codebuddy mcp add-json failed; writing a JSON snippet instead." >&2
                  fi

                  config_file=$(resolve_codebuddy_config_file)
                  if [ ! -e "$config_file" ]; then
                    write_mcp_servers_snippet "$config_file" "http"
                    echo "Wrote CodeBuddy MCP config: $config_file"
                    record_automatic_mcp "codebuddy"
                  elif merge_json_config "$config_file" "mcpServers" "$server_json"; then
                    echo "Merged CodeBuddy MCP config: $config_file"
                    record_automatic_mcp "codebuddy"
                  else
                    snippet_file="$SUPPORT_DIR/codebuddy-reqflow-mcp.json"
                    write_mcp_servers_snippet "$snippet_file" "http"
                    echo "Existing CodeBuddy config could not be merged automatically. Merge snippet: $snippet_file"
                    record_manual_import "codebuddy" "$snippet_file" "merge into CodeBuddy MCP settings"
                  fi
                }

                install_opencode_mcp() {
                  local config_file
                  local snippet_file
                  local mcp_url_json
                  local mcp_key_json
                  local server_json
                  config_file=$(resolve_opencode_config_file)
                  mcp_url_json=$(json_escape "$MCP_URL")
                  mcp_key_json=$(json_escape "$MCP_KEY")
                  server_json="{\\"type\\":\\"remote\\",\\"url\\":\\"$mcp_url_json\\",\\"enabled\\":true,\\"headers\\":{\\"$MCP_KEY_HEADER\\":\\"$mcp_key_json\\"}}"
                  if [ ! -e "$config_file" ]; then
                    mkdir -p "$(dirname "$config_file")"
                    write_opencode_config "$config_file"
                    echo "Wrote OpenCode MCP config: $config_file"
                    record_automatic_mcp "opencode"
                  elif merge_json_config "$config_file" "mcp" "$server_json" "https://opencode.ai/config.json"; then
                    echo "Wrote OpenCode MCP config: $config_file"
                    record_automatic_mcp "opencode"
                  else
                    snippet_file="$SUPPORT_DIR/opencode-reqflow-mcp.json"
                    write_opencode_config "$snippet_file"
                    echo "Existing OpenCode config could not be merged automatically. Merge snippet into the mcp object: $snippet_file"
                    record_manual_import "opencode" "$snippet_file" "merge into OpenCode mcp object"
                  fi
                }

                install_global_skill() {
                  local target_client="$1"
                  local skill_parent
                  local skill_dir
                  local skill_file
                  if ! command -v npx >/dev/null 2>&1; then
                    echo "Missing npx. Install Node.js/npm, then rerun this script or run npx skills add manually." >&2
                    exit 3
                  fi
                  skill_parent=$(mktemp -d)
                  SKILL_DIR="$skill_parent/reqflow-mcp"
                  skill_dir="$SKILL_DIR"
                  skill_file="$skill_dir/SKILL.md"
                  mkdir -p "$skill_dir"
                  # Writes reqflow-mcp/SKILL.md before npx skills add copies it to the selected agent.
                  cat > "$skill_file" <<'REQFLOW_SKILL_EOF'
                {{REQFLOW_SKILL_CONTENT}}
                REQFLOW_SKILL_EOF
                  npx skills add "$SKILL_DIR" -g -a "$target_client" --copy -y
                  sync_known_skill_locations "$target_client" "$skill_file"
                  record_skill_install "$target_client"
                }

                install_selected_client() {
                  local target_client="$1"
                  case "$target_client" in
                    codex)
                      install_codex_mcp
                      ;;
                    claude-code)
                      install_claude_code_mcp
                      ;;
                    trae)
                      install_trae_mcp
                      ;;
                    qoder)
                      install_qoder_mcp
                      ;;
                    codebuddy)
                      install_codebuddy_mcp
                      ;;
                    opencode)
                      install_opencode_mcp
                      ;;
                  esac
                  install_global_skill "$target_client"
                }

                install_all_clients() {
                  local target_client
                  for target_client in codex claude-code trae qoder codebuddy opencode; do
                    install_selected_client "$target_client"
                  done
                }

                resolve_clients() {
                  local selection="$1"
                  local item
                  local selected=""
                  selection=$(printf '%s' "$selection" | tr '[:upper:]' '[:lower:]' | tr ',' ' ')
                  if [ -z "$selection" ]; then
                    selection="all"
                  fi
                  for item in $selection; do
                    case "$item" in
                      1|all)
                        printf '%s\n' "$INSTALLABLE_CLIENTS"
                        return 0
                        ;;
                      2|codex)
                        selected="$selected codex"
                        ;;
                      3|claude-code)
                        selected="$selected claude-code"
                        ;;
                      4|trae)
                        selected="$selected trae"
                        ;;
                      5|qoder)
                        selected="$selected qoder"
                        ;;
                      6|codebuddy)
                        selected="$selected codebuddy"
                        ;;
                      7|opencode)
                        selected="$selected opencode"
                        ;;
                      *)
                        echo "Unsupported client selection '$item'. Supported clients: $SUPPORTED_CLIENTS" >&2
                        exit 2
                        ;;
                    esac
                  done
                  printf '%s\n' "$selected"
                }

                select_clients_interactively() {
                  local selection
                  if [ ! -r /dev/tty ]; then
                    echo "No interactive terminal detected. Pass --client all or --client <client>." >&2
                    exit 2
                  fi
                  {
                    echo "Select reqflow clients to install:"
                    echo "  1) all"
                    echo "  2) codex"
                    echo "  3) claude-code"
                    echo "  4) trae"
                    echo "  5) qoder"
                    echo "  6) codebuddy"
                    echo "  7) opencode"
                    printf "Enter number(s) or client names, separated by comma or space [1]: "
                  } > /dev/tty
                  IFS= read -r selection < /dev/tty
                  resolve_clients "$selection"
                }

                SELECTED_CLIENTS="${CLIENT:-}"
                if [ -z "$SELECTED_CLIENTS" ]; then
                  SELECTED_CLIENTS=$(select_clients_interactively)
                else
                  SELECTED_CLIENTS=$(resolve_clients "$SELECTED_CLIENTS")
                fi

                for target_client in $SELECTED_CLIENTS; do
                  install_selected_client "$target_client"
                done

                if [ -n "${AUTOMATIC_MCP_CLIENTS// /}" ]; then
                  echo "Reqflow automatic MCP configuration completed for:$AUTOMATIC_MCP_CLIENTS."
                fi
                if [ -n "$MANUAL_MCP_IMPORTS" ]; then
                  echo "Manual MCP import required:$MANUAL_MCP_IMPORTS"
                fi
                if [ -n "${SKILL_CLIENTS// /}" ]; then
                  echo "reqflow-mcp global skill installed for:$SKILL_CLIENTS."
                fi
                echo "Do not call reqflow MCP tools automatically after installation."
                echo "Restart or refresh the selected client, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent()));
    }

    public static String powerShellScript()
    {
        return normalizeSkillEmbedding("""
                param(
                  [string]$Client = "",
                  [string]$McpUrl,
                  [string]$McpKey = $env:REQFLOW_MCP_KEY
                )

                $ErrorActionPreference = "Stop"
                $McpHeaderName = "X-MCP-Key"
                $InstallableClients = @("codex", "claude-code", "trae", "qoder", "codebuddy", "opencode")

                if ([string]::IsNullOrWhiteSpace($McpUrl)) {
                  throw "Missing -McpUrl <reqflow-mcp-url>"
                }
                if ([string]::IsNullOrWhiteSpace($McpKey)) {
                  throw "Missing MCP key. Set REQFLOW_MCP_KEY or pass -McpKey."
                }

                $SupportDir = if ($env:REQFLOW_INSTALL_DIR) { $env:REQFLOW_INSTALL_DIR } else { Join-Path $HOME ".reqflow-mcp" }
                New-Item -ItemType Directory -Force -Path $SupportDir | Out-Null
                $AutomaticMcpClients = New-Object System.Collections.Generic.List[string]
                $ManualMcpImports = New-Object System.Collections.Generic.List[string]
                $SkillClients = New-Object System.Collections.Generic.List[string]

                function Add-AutomaticMcpClient([string]$Client) {
                  if (-not $AutomaticMcpClients.Contains($Client)) {
                    [void]$AutomaticMcpClients.Add($Client)
                  }
                }

                function Add-ManualMcpImport([string]$Client, [string]$Path, [string]$Instruction) {
                  [void]$ManualMcpImports.Add("  - ${Client}: ${Path} (${Instruction})")
                }

                function Add-SkillClient([string]$Client) {
                  if (-not $SkillClients.Contains($Client)) {
                    [void]$SkillClients.Add($Client)
                  }
                }

                function Sync-SkillToDirectory([string]$SourceFile, [string]$TargetRoot) {
                  $targetDir = Join-Path $TargetRoot "reqflow-mcp"
                  New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
                  Copy-Item -Force -Path $SourceFile -Destination (Join-Path $targetDir "SKILL.md")
                }

                function Sync-KnownSkillLocations([string]$TargetClient, [string]$SourceFile) {
                  $agentsHome = if ($env:AGENTS_HOME) { $env:AGENTS_HOME } else { Join-Path $HOME ".agents" }
                  Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $agentsHome "skills")
                  switch ($TargetClient) {
                    "codex" {
                      $codexHome = if ($env:CODEX_HOME) { $env:CODEX_HOME } else { Join-Path $HOME ".codex" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $codexHome "skills")
                    }
                    "claude-code" {
                      $claudeHome = if ($env:CLAUDE_HOME) { $env:CLAUDE_HOME } else { Join-Path $HOME ".claude" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $claudeHome "skills")
                    }
                    "trae" {
                      $traeHome = if ($env:TRAE_HOME) { $env:TRAE_HOME } else { Join-Path $HOME ".trae" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $traeHome "skills")
                    }
                    "qoder" {
                      $qoderHome = if ($env:QODER_HOME) { $env:QODER_HOME } else { Join-Path $HOME ".qoder" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $qoderHome "skills")
                    }
                    "codebuddy" {
                      $codeBuddyHome = if ($env:CODEBUDDY_HOME) { $env:CODEBUDDY_HOME } else { Join-Path $HOME ".codebuddy" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $codeBuddyHome "skills")
                    }
                    "opencode" {
                      $openCodeHome = if ($env:OPENCODE_HOME) { $env:OPENCODE_HOME } elseif ($env:OPENCODE_CONFIG_DIR) { $env:OPENCODE_CONFIG_DIR } else { Join-Path (Join-Path $HOME ".config") "opencode" }
                      Sync-SkillToDirectory -SourceFile $SourceFile -TargetRoot (Join-Path $openCodeHome "skills")
                    }
                  }
                }

                function Escape-TomlString([string]$Value) {
                  return $Value.Replace("\\", "\\\\").Replace('"', '\\"')
                }

                function New-McpServersObject([string]$TransportType) {
                  $headers = [ordered]@{}
                  $headers[$McpHeaderName] = $McpKey
                  return [ordered]@{
                    mcpServers = [ordered]@{
                      reqflow = [ordered]@{
                        type = $TransportType
                        url = $McpUrl
                        headers = $headers
                      }
                    }
                  }
                }

                function Write-McpServersSnippet([string]$Path, [string]$TransportType) {
                  $parent = Split-Path -Parent $Path
                  if (-not [string]::IsNullOrWhiteSpace($parent)) {
                    New-Item -ItemType Directory -Force -Path $parent | Out-Null
                  }
                  New-McpServersObject $TransportType | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 -Path $Path
                }

                function New-OpenCodeObject {
                  $headers = [ordered]@{}
                  $headers[$McpHeaderName] = $McpKey
                  return [ordered]@{
                    '$schema' = "https://opencode.ai/config.json"
                    mcp = [ordered]@{
                      reqflow = [ordered]@{
                        type = "remote"
                        url = $McpUrl
                        enabled = $true
                        headers = $headers
                      }
                    }
                  }
                }

                function Write-OpenCodeConfig([string]$Path) {
                  $parent = Split-Path -Parent $Path
                  if (-not [string]::IsNullOrWhiteSpace($parent)) {
                    New-Item -ItemType Directory -Force -Path $parent | Out-Null
                  }
                  New-OpenCodeObject | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 -Path $Path
                }

                function Merge-JsonConfig([string]$Path, [string]$RootKey, [string]$ServerJson, [string]$Schema = "") {
                  if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
                    return $false
                  }
                  $parent = Split-Path -Parent $Path
                  if (-not [string]::IsNullOrWhiteSpace($parent)) {
                    New-Item -ItemType Directory -Force -Path $parent | Out-Null
                  }
                  $env:REQFLOW_MERGE_CONFIG_FILE = $Path
                  $env:REQFLOW_MERGE_ROOT_KEY = $RootKey
                  $env:REQFLOW_MERGE_SERVER_NAME = "reqflow"
                  $env:REQFLOW_MERGE_SERVER_JSON = $ServerJson
                  $env:REQFLOW_MERGE_SCHEMA = $Schema
                  $nodeScript = @'
                const fs = require('fs');
                const file = process.env.REQFLOW_MERGE_CONFIG_FILE;
                const rootKey = process.env.REQFLOW_MERGE_ROOT_KEY;
                const serverName = process.env.REQFLOW_MERGE_SERVER_NAME || 'reqflow';
                const schema = process.env.REQFLOW_MERGE_SCHEMA;
                let config = {};
                if (fs.existsSync(file)) {
                  const text = fs.readFileSync(file, 'utf8').trim();
                  if (text) {
                    config = JSON.parse(text);
                  }
                }
                const server = JSON.parse(process.env.REQFLOW_MERGE_SERVER_JSON);
                if (!config || typeof config !== 'object' || Array.isArray(config)) {
                  config = {};
                }
                if (schema && !config.$schema) {
                  config.$schema = schema;
                }
                if (!config[rootKey] || typeof config[rootKey] !== 'object' || Array.isArray(config[rootKey])) {
                  config[rootKey] = {};
                }
                config[rootKey][serverName] = server;
                fs.writeFileSync(file, JSON.stringify(config, null, 2) + '\\n');
                '@
                  $nodeScript | node
                  return $LASTEXITCODE -eq 0
                }

                function Resolve-OpenCodeConfigFile {
                  if ($env:OPENCODE_CONFIG) {
                    return $env:OPENCODE_CONFIG
                  }
                  $configDir = Join-Path $HOME ".config\\opencode"
                  $jsonPath = Join-Path $configDir "opencode.json"
                  $jsoncPath = Join-Path $configDir "opencode.jsonc"
                  if (Test-Path $jsonPath) {
                    return $jsonPath
                  }
                  if (Test-Path $jsoncPath) {
                    return $jsoncPath
                  }
                  return $jsonPath
                }

                function Resolve-CodeBuddyConfigFile {
                  $codeBuddyHome = if ($env:CODEBUDDY_CONFIG_DIR) {
                    $env:CODEBUDDY_CONFIG_DIR
                  } elseif ($env:CODEBUDDY_HOME) {
                    $env:CODEBUDDY_HOME
                  } else {
                    Join-Path $HOME ".codebuddy"
                  }
                  $primaryPath = Join-Path $codeBuddyHome ".mcp.json"
                  $legacyPath = Join-Path $codeBuddyHome "mcp.json"
                  $oldPath = Join-Path $HOME ".codebuddy.json"
                  if (Test-Path $primaryPath) {
                    return $primaryPath
                  }
                  if (Test-Path $legacyPath) {
                    return $legacyPath
                  }
                  if (Test-Path $oldPath) {
                    return $oldPath
                  }
                  return $primaryPath
                }

                function Install-CodexMcp {
                  $CodexHome = if ($env:CODEX_HOME) { $env:CODEX_HOME } else { Join-Path $HOME ".codex" }
                  $ConfigFile = Join-Path $CodexHome "config.toml"
                  New-Item -ItemType Directory -Force -Path $CodexHome | Out-Null
                  if (-not (Test-Path $ConfigFile)) {
                    New-Item -ItemType File -Force -Path $ConfigFile | Out-Null
                  }

                  $content = Get-Content -Raw -Path $ConfigFile
                  $content = [regex]::Replace($content, '(?ms)^\\[mcp_servers\\.reqflow\\]\\r?\\n.*?(?=^\\[|\\z)', '')
                  Set-Content -Encoding UTF8 -NoNewline -Path $ConfigFile -Value $content.TrimEnd()

                  $mcpUrlToml = Escape-TomlString $McpUrl
                  $mcpKeyToml = Escape-TomlString $McpKey
                  Add-Content -Encoding UTF8 -Path $ConfigFile -Value "`n`n[mcp_servers.reqflow]`nurl = `"$mcpUrlToml`"`nhttp_headers = { `"$McpHeaderName`" = `"$mcpKeyToml`" }"
                  Write-Host "Wrote Codex MCP config: $ConfigFile"
                  Add-AutomaticMcpClient -Client "codex"
                }

                function Install-ClaudeCodeMcp {
                  if (Get-Command claude -ErrorAction SilentlyContinue) {
                    & claude mcp add --transport http --scope user reqflow $McpUrl --header "$McpHeaderName: $McpKey"
                    if ($LASTEXITCODE -eq 0) {
                      Write-Host "Configured Claude Code MCP with claude mcp add."
                      Add-AutomaticMcpClient -Client "claude-code"
                      return
                    }
                    Write-Warning "claude mcp add failed; writing a .mcp.json snippet instead."
                  }
                  $snippetFile = Join-Path $SupportDir "claude-code-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "http"
                  Write-Host "Wrote Claude Code MCP snippet: $snippetFile"
                  Add-ManualMcpImport -Client "claude-code" -Path $snippetFile -Instruction "merge into Claude Code user or project MCP settings"
                }

                function Install-TraeMcp {
                  $snippetFile = Join-Path $SupportDir "trae-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "streamable-http"
                  Write-Host "Wrote Trae MCP snippet: $snippetFile"
                  Write-Host "Open Trae Settings > MCP and import or paste this mcpServers JSON."
                  Add-ManualMcpImport -Client "trae" -Path $snippetFile -Instruction "import in Trae Settings > MCP"
                }

                function Install-QoderMcp {
                  $snippetFile = Join-Path $SupportDir "qoder-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "streamable-http"
                  Write-Host "Wrote Qoder MCP snippet: $snippetFile"
                  Write-Host "Open Qoder Settings > MCP and import or paste this mcpServers JSON."
                  Add-ManualMcpImport -Client "qoder" -Path $snippetFile -Instruction "import in Qoder Settings > MCP"
                }

                function Install-CodeBuddyMcp {
                  $server = [ordered]@{
                    type = "http"
                    url = $McpUrl
                    headers = [ordered]@{}
                  }
                  $server.headers[$McpHeaderName] = $McpKey
                  $serverJson = $server | ConvertTo-Json -Depth 8 -Compress
                  if (Get-Command codebuddy -ErrorAction SilentlyContinue) {
                    & codebuddy mcp add-json --scope user reqflow $serverJson
                    if ($LASTEXITCODE -eq 0) {
                      Write-Host "Configured CodeBuddy MCP with codebuddy mcp add-json."
                      Add-AutomaticMcpClient -Client "codebuddy"
                      return
                    }
                    Write-Warning "codebuddy mcp add-json failed; writing a JSON snippet instead."
                  }

                  $configFile = Resolve-CodeBuddyConfigFile
                  if (-not (Test-Path $configFile)) {
                    Write-McpServersSnippet -Path $configFile -TransportType "http"
                    Write-Host "Wrote CodeBuddy MCP config: $configFile"
                    Add-AutomaticMcpClient -Client "codebuddy"
                  } elseif (Merge-JsonConfig -Path $configFile -RootKey "mcpServers" -ServerJson $serverJson) {
                    Write-Host "Merged CodeBuddy MCP config: $configFile"
                    Add-AutomaticMcpClient -Client "codebuddy"
                  } else {
                    $snippetFile = Join-Path $SupportDir "codebuddy-reqflow-mcp.json"
                    Write-McpServersSnippet -Path $snippetFile -TransportType "http"
                    Write-Host "Existing CodeBuddy config could not be merged automatically. Merge snippet: $snippetFile"
                    Add-ManualMcpImport -Client "codebuddy" -Path $snippetFile -Instruction "merge into CodeBuddy MCP settings"
                  }
                }

                function Install-OpenCodeMcp {
                  $configFile = Resolve-OpenCodeConfigFile
                  $headers = [ordered]@{}
                  $headers[$McpHeaderName] = $McpKey
                  $server = [ordered]@{
                    type = "remote"
                    url = $McpUrl
                    enabled = $true
                    headers = $headers
                  }
                  $serverJson = $server | ConvertTo-Json -Depth 8 -Compress
                  if (-not (Test-Path $configFile)) {
                    Write-OpenCodeConfig -Path $configFile
                    Write-Host "Wrote OpenCode MCP config: $configFile"
                    Add-AutomaticMcpClient -Client "opencode"
                  } elseif (Merge-JsonConfig -Path $configFile -RootKey "mcp" -ServerJson $serverJson -Schema "https://opencode.ai/config.json") {
                    Write-Host "Wrote OpenCode MCP config: $configFile"
                    Add-AutomaticMcpClient -Client "opencode"
                  } else {
                    $snippetFile = Join-Path $SupportDir "opencode-reqflow-mcp.json"
                    Write-OpenCodeConfig -Path $snippetFile
                    Write-Host "Existing OpenCode config could not be merged automatically. Merge snippet into the mcp object: $snippetFile"
                    Add-ManualMcpImport -Client "opencode" -Path $snippetFile -Instruction "merge into OpenCode mcp object"
                  }
                }

                function Install-GlobalSkill([string]$TargetClient) {
                  if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
                    throw "Missing npx. Install Node.js/npm, then rerun this script or run npx skills add manually."
                  }
                  $skillRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("reqflow-mcp-" + [guid]::NewGuid())
                  $SkillDir = Join-Path $skillRoot "reqflow-mcp"
                  New-Item -ItemType Directory -Force -Path $SkillDir | Out-Null
                  $SkillFile = Join-Path $SkillDir "SKILL.md"
                  # Writes reqflow-mcp/SKILL.md before npx skills add copies it to the selected agent.
                  $skillContent = @'
                {{REQFLOW_SKILL_CONTENT}}
                  '@
                  Set-Content -Encoding UTF8 -Path $SkillFile -Value $skillContent
                  npx skills add $SkillDir -g -a $TargetClient --copy -y
                  Sync-KnownSkillLocations -TargetClient $TargetClient -SourceFile $SkillFile
                  Add-SkillClient -Client $TargetClient
                }

                function Install-SelectedClient([string]$TargetClient) {
                  switch ($TargetClient) {
                    "codex" { Install-CodexMcp }
                    "claude-code" { Install-ClaudeCodeMcp }
                    "trae" { Install-TraeMcp }
                    "qoder" { Install-QoderMcp }
                    "codebuddy" { Install-CodeBuddyMcp }
                    "opencode" { Install-OpenCodeMcp }
                  }
                  Install-GlobalSkill -TargetClient $TargetClient
                }

                function Install-AllClients {
                  foreach ($targetClient in @("codex", "claude-code", "trae", "qoder", "codebuddy", "opencode")) {
                    Install-SelectedClient -TargetClient $targetClient
                  }
                }

                function Resolve-ReqflowClients([string]$Selection) {
                  if ([string]::IsNullOrWhiteSpace($Selection)) {
                    $Selection = "all"
                  }
                  $selected = New-Object System.Collections.Generic.List[string]
                  foreach ($item in ($Selection.ToLowerInvariant() -split '[,\\s]+' | Where-Object { $_ })) {
                    switch ($item) {
                      "1" { return $InstallableClients }
                      "all" { return $InstallableClients }
                      "2" { if (-not $selected.Contains("codex")) { [void]$selected.Add("codex") } }
                      "codex" { if (-not $selected.Contains("codex")) { [void]$selected.Add("codex") } }
                      "3" { if (-not $selected.Contains("claude-code")) { [void]$selected.Add("claude-code") } }
                      "claude-code" { if (-not $selected.Contains("claude-code")) { [void]$selected.Add("claude-code") } }
                      "4" { if (-not $selected.Contains("trae")) { [void]$selected.Add("trae") } }
                      "trae" { if (-not $selected.Contains("trae")) { [void]$selected.Add("trae") } }
                      "5" { if (-not $selected.Contains("qoder")) { [void]$selected.Add("qoder") } }
                      "qoder" { if (-not $selected.Contains("qoder")) { [void]$selected.Add("qoder") } }
                      "6" { if (-not $selected.Contains("codebuddy")) { [void]$selected.Add("codebuddy") } }
                      "codebuddy" { if (-not $selected.Contains("codebuddy")) { [void]$selected.Add("codebuddy") } }
                      "7" { if (-not $selected.Contains("opencode")) { [void]$selected.Add("opencode") } }
                      "opencode" { if (-not $selected.Contains("opencode")) { [void]$selected.Add("opencode") } }
                      default { throw "Unsupported client selection '$item'. Supported clients: all, codex, claude-code, trae, qoder, codebuddy, opencode." }
                    }
                  }
                  return $selected.ToArray()
                }

                function Select-ReqflowClientsInteractively {
                  Write-Host "Select reqflow clients to install:"
                  Write-Host "  1) all"
                  Write-Host "  2) codex"
                  Write-Host "  3) claude-code"
                  Write-Host "  4) trae"
                  Write-Host "  5) qoder"
                  Write-Host "  6) codebuddy"
                  Write-Host "  7) opencode"
                  $selection = Read-Host "Enter number(s) or client names, separated by comma or space [1]"
                  return Resolve-ReqflowClients $selection
                }

                if ([string]::IsNullOrWhiteSpace($Client)) {
                  $SelectedClients = Select-ReqflowClientsInteractively
                } else {
                  $SelectedClients = Resolve-ReqflowClients $Client
                }

                foreach ($targetClient in $SelectedClients) {
                  Install-SelectedClient -TargetClient $targetClient
                }

                if ($AutomaticMcpClients.Count -gt 0) {
                  Write-Host ("Reqflow automatic MCP configuration completed for: " + ($AutomaticMcpClients -join ", "))
                }
                if ($ManualMcpImports.Count -gt 0) {
                  Write-Host "Manual MCP import required:"
                  foreach ($manualImport in $ManualMcpImports) {
                    Write-Host $manualImport
                  }
                }
                if ($SkillClients.Count -gt 0) {
                  Write-Host ("reqflow-mcp global skill installed for: " + ($SkillClients -join ", "))
                }
                Write-Host "Do not call reqflow MCP tools automatically after installation."
                Write-Host "Restart or refresh the selected client, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent()));
    }

    private static String normalizeSkillEmbedding(String script)
    {
        return script.replaceAll("(?m)^\\s+---$", "---")
                .replaceAll("(?m)^\\s+REQFLOW_SKILL_EOF$", "REQFLOW_SKILL_EOF")
                .replaceAll("(?m)^\\s+'@$", "'@");
    }
}
