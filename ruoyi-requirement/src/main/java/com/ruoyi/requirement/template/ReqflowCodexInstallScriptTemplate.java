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
        return """
                #!/usr/bin/env bash
                set -euo pipefail

                SUPPORTED_CLIENTS="codex claude-code trae qoder codebuddy opencode"
                CLIENT="codex"
                MCP_URL=""
                MCP_KEY_HEADER="X-MCP-Key"

                usage() {
                  cat <<'EOF'
                Usage: install.sh --url <reqflow-mcp-url> [--key <mcp-key>] [--client <client>]

                Clients: codex, claude-code, trae, qoder, codebuddy, opencode

                The script installs the reqflow MCP configuration for the selected client and
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
                case "$CLIENT" in
                  codex|claude-code|trae|qoder|codebuddy|opencode)
                    ;;
                  *)
                    echo "Unsupported --client '$CLIENT'. Supported clients: $SUPPORTED_CLIENTS" >&2
                    exit 2
                    ;;
                esac
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
                }

                install_claude_code_mcp() {
                  local snippet_file
                  if command -v claude >/dev/null 2>&1; then
                    if claude mcp add --transport http --scope user reqflow "$MCP_URL" --header "$MCP_KEY_HEADER: $MCP_KEY"; then
                      echo "Configured Claude Code MCP with claude mcp add."
                      return 0
                    fi
                    echo "claude mcp add failed; writing a .mcp.json snippet instead." >&2
                  fi
                  snippet_file="$SUPPORT_DIR/claude-code-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "http"
                  echo "Wrote Claude Code MCP snippet: $snippet_file"
                  echo "Import or merge the snippet into the Claude Code user/project MCP settings."
                }

                install_trae_mcp() {
                  local snippet_file="$SUPPORT_DIR/trae-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "streamable-http"
                  echo "Wrote Trae MCP snippet: $snippet_file"
                  echo "Open Trae Settings > MCP and import or paste this mcpServers JSON."
                }

                install_qoder_mcp() {
                  local snippet_file="$SUPPORT_DIR/qoder-reqflow-mcp.json"
                  write_mcp_servers_snippet "$snippet_file" "streamable-http"
                  echo "Wrote Qoder MCP snippet: $snippet_file"
                  echo "Open Qoder Settings > MCP and import or paste this mcpServers JSON."
                }

                install_codebuddy_mcp() {
                  local mcp_url_json
                  local mcp_key_json
                  local server_json
                  local codebuddy_home
                  local config_file
                  local snippet_file
                  mcp_url_json=$(json_escape "$MCP_URL")
                  mcp_key_json=$(json_escape "$MCP_KEY")
                  server_json="{\\"type\\":\\"http\\",\\"url\\":\\"$mcp_url_json\\",\\"headers\\":{\\"$MCP_KEY_HEADER\\":\\"$mcp_key_json\\"}}"

                  if command -v codebuddy >/dev/null 2>&1; then
                    if codebuddy mcp add-json --scope user reqflow "$server_json"; then
                      echo "Configured CodeBuddy MCP with codebuddy mcp add-json."
                      return 0
                    fi
                    echo "codebuddy mcp add-json failed; writing a JSON snippet instead." >&2
                  fi

                  codebuddy_home="${CODEBUDDY_HOME:-$HOME/.codebuddy}"
                  config_file="$codebuddy_home/.mcp.json"
                  if [ ! -e "$config_file" ]; then
                    mkdir -p "$codebuddy_home"
                    write_mcp_servers_snippet "$config_file" "http"
                    echo "Wrote CodeBuddy MCP config: $config_file"
                  else
                    snippet_file="$SUPPORT_DIR/codebuddy-reqflow-mcp.json"
                    write_mcp_servers_snippet "$snippet_file" "http"
                    echo "Existing CodeBuddy config found. Merge snippet: $snippet_file"
                  fi
                }

                install_opencode_mcp() {
                  local config_file
                  local snippet_file
                  config_file="${OPENCODE_CONFIG:-$HOME/.config/opencode/opencode.json}"
                  if [ ! -e "$config_file" ]; then
                    mkdir -p "$(dirname "$config_file")"
                    write_opencode_config "$config_file"
                    echo "Wrote OpenCode MCP config: $config_file"
                  else
                    snippet_file="$SUPPORT_DIR/opencode-reqflow-mcp.json"
                    write_opencode_config "$snippet_file"
                    echo "Existing OpenCode config found. Merge snippet into the mcp object: $snippet_file"
                  fi
                }

                install_global_skill() {
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
                  npx skills add "$SKILL_DIR" -g -a "$CLIENT" --copy -y
                }

                case "$CLIENT" in
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

                install_global_skill

                echo "Reqflow $CLIENT MCP and reqflow-mcp global skill installed."
                echo "Do not call reqflow MCP tools automatically after installation."
                echo "Restart or refresh the selected client, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent());
    }

    public static String powerShellScript()
    {
        return """
                param(
                  [ValidateSet("codex", "claude-code", "trae", "qoder", "codebuddy", "opencode")]
                  [string]$Client = "codex",
                  [string]$McpUrl,
                  [string]$McpKey = $env:REQFLOW_MCP_KEY
                )

                $ErrorActionPreference = "Stop"
                $McpHeaderName = "X-MCP-Key"

                if ([string]::IsNullOrWhiteSpace($McpUrl)) {
                  throw "Missing -McpUrl <reqflow-mcp-url>"
                }
                if ([string]::IsNullOrWhiteSpace($McpKey)) {
                  throw "Missing MCP key. Set REQFLOW_MCP_KEY or pass -McpKey."
                }

                $SupportDir = if ($env:REQFLOW_INSTALL_DIR) { $env:REQFLOW_INSTALL_DIR } else { Join-Path $HOME ".reqflow-mcp" }
                New-Item -ItemType Directory -Force -Path $SupportDir | Out-Null

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
                }

                function Install-ClaudeCodeMcp {
                  if (Get-Command claude -ErrorAction SilentlyContinue) {
                    & claude mcp add --transport http --scope user reqflow $McpUrl --header "$McpHeaderName: $McpKey"
                    if ($LASTEXITCODE -eq 0) {
                      Write-Host "Configured Claude Code MCP with claude mcp add."
                      return
                    }
                    Write-Warning "claude mcp add failed; writing a .mcp.json snippet instead."
                  }
                  $snippetFile = Join-Path $SupportDir "claude-code-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "http"
                  Write-Host "Wrote Claude Code MCP snippet: $snippetFile"
                }

                function Install-TraeMcp {
                  $snippetFile = Join-Path $SupportDir "trae-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "streamable-http"
                  Write-Host "Wrote Trae MCP snippet: $snippetFile"
                  Write-Host "Open Trae Settings > MCP and import or paste this mcpServers JSON."
                }

                function Install-QoderMcp {
                  $snippetFile = Join-Path $SupportDir "qoder-reqflow-mcp.json"
                  Write-McpServersSnippet -Path $snippetFile -TransportType "streamable-http"
                  Write-Host "Wrote Qoder MCP snippet: $snippetFile"
                  Write-Host "Open Qoder Settings > MCP and import or paste this mcpServers JSON."
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
                      return
                    }
                    Write-Warning "codebuddy mcp add-json failed; writing a JSON snippet instead."
                  }

                  $codeBuddyHome = if ($env:CODEBUDDY_HOME) { $env:CODEBUDDY_HOME } else { Join-Path $HOME ".codebuddy" }
                  $configFile = Join-Path $codeBuddyHome ".mcp.json"
                  if (-not (Test-Path $configFile)) {
                    Write-McpServersSnippet -Path $configFile -TransportType "http"
                    Write-Host "Wrote CodeBuddy MCP config: $configFile"
                  } else {
                    $snippetFile = Join-Path $SupportDir "codebuddy-reqflow-mcp.json"
                    Write-McpServersSnippet -Path $snippetFile -TransportType "http"
                    Write-Host "Existing CodeBuddy config found. Merge snippet: $snippetFile"
                  }
                }

                function Install-OpenCodeMcp {
                  $configFile = if ($env:OPENCODE_CONFIG) { $env:OPENCODE_CONFIG } else { Join-Path $HOME ".config\\opencode\\opencode.json" }
                  if (-not (Test-Path $configFile)) {
                    Write-OpenCodeConfig -Path $configFile
                    Write-Host "Wrote OpenCode MCP config: $configFile"
                  } else {
                    $snippetFile = Join-Path $SupportDir "opencode-reqflow-mcp.json"
                    Write-OpenCodeConfig -Path $snippetFile
                    Write-Host "Existing OpenCode config found. Merge snippet into the mcp object: $snippetFile"
                  }
                }

                function Install-GlobalSkill {
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
                  npx skills add $SkillDir -g -a $Client --copy -y
                }

                switch ($Client) {
                  "codex" { Install-CodexMcp }
                  "claude-code" { Install-ClaudeCodeMcp }
                  "trae" { Install-TraeMcp }
                  "qoder" { Install-QoderMcp }
                  "codebuddy" { Install-CodeBuddyMcp }
                  "opencode" { Install-OpenCodeMcp }
                }

                Install-GlobalSkill

                Write-Host "Reqflow $Client MCP and reqflow-mcp global skill installed."
                Write-Host "Do not call reqflow MCP tools automatically after installation."
                Write-Host "Restart or refresh the selected client, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent());
    }
}
