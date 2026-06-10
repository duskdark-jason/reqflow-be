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

                MCP_URL=""
                while [ "$#" -gt 0 ]; do
                  case "$1" in
                    --url)
                      MCP_URL="${2:-}"
                      shift 2
                      ;;
                    --key)
                      REQFLOW_MCP_KEY="${2:-}"
                      shift 2
                      ;;
                    -h|--help)
                      echo "Usage: install.sh --url <reqflow-mcp-url> [--key <mcp-key>]"
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

                CODEX_HOME="${CODEX_HOME:-$HOME/.codex}"
                SKILLS_ROOT="${CODEX_SKILLS_DIR:-$CODEX_HOME/skills}"
                CONFIG_FILE="$CODEX_HOME/config.toml"
                SKILL_DIR="$SKILLS_ROOT/reqflow-mcp"
                SKILL_FILE="$SKILL_DIR/SKILL.md"

                mkdir -p "$CODEX_HOME" "$SKILL_DIR"
                touch "$CONFIG_FILE"

                toml_escape() {
                  printf '%s' "$1" | sed 's/\\\\/\\\\\\\\/g; s/"/\\\\"/g'
                }

                MCP_URL_TOML=$(toml_escape "$MCP_URL")
                MCP_KEY_TOML=$(toml_escape "$MCP_KEY")
                TMP_CONFIG=$(mktemp)
                awk '
                  /^\\[mcp_servers\\.reqflow\\]$/ { skip = 1; next }
                  /^\\[/ { skip = 0 }
                  !skip { print }
                ' "$CONFIG_FILE" > "$TMP_CONFIG"
                mv "$TMP_CONFIG" "$CONFIG_FILE"

                cat >> "$CONFIG_FILE" <<EOF

                [mcp_servers.reqflow]
                url = "$MCP_URL_TOML"
                http_headers = { "X-MCP-Key" = "$MCP_KEY_TOML" }
                EOF

                # Writes reqflow-mcp/SKILL.md.
                cat > "$SKILL_FILE" <<'REQFLOW_SKILL_EOF'
                {{REQFLOW_SKILL_CONTENT}}
                REQFLOW_SKILL_EOF

                echo "Reqflow Codex MCP and reqflow-mcp skill installed."
                echo "Do not call reqflow MCP tools automatically after installation."
                echo "Restart or refresh Codex, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent());
    }

    public static String powerShellScript()
    {
        return """
                param(
                  [string]$McpUrl,
                  [string]$McpKey = $env:REQFLOW_MCP_KEY
                )

                $ErrorActionPreference = "Stop"

                if ([string]::IsNullOrWhiteSpace($McpUrl)) {
                  throw "Missing -McpUrl <reqflow-mcp-url>"
                }
                if ([string]::IsNullOrWhiteSpace($McpKey)) {
                  throw "Missing MCP key. Set REQFLOW_MCP_KEY or pass -McpKey."
                }

                $CodexHome = if ($env:CODEX_HOME) { $env:CODEX_HOME } else { Join-Path $HOME ".codex" }
                $SkillsRoot = if ($env:CODEX_SKILLS_DIR) { $env:CODEX_SKILLS_DIR } else { Join-Path $CodexHome "skills" }
                $ConfigFile = Join-Path $CodexHome "config.toml"
                $SkillDir = Join-Path $SkillsRoot "reqflow-mcp"
                $SkillFile = Join-Path $SkillDir "SKILL.md"

                New-Item -ItemType Directory -Force -Path $CodexHome, $SkillDir | Out-Null
                if (-not (Test-Path $ConfigFile)) {
                  New-Item -ItemType File -Force -Path $ConfigFile | Out-Null
                }

                function Escape-TomlString([string]$Value) {
                  return $Value.Replace("\\", "\\\\").Replace('"', '\\"')
                }

                $content = Get-Content -Raw -Path $ConfigFile
                $content = [regex]::Replace($content, '(?ms)^\\[mcp_servers\\.reqflow\\]\\r?\\n.*?(?=^\\[|\\z)', '')
                Set-Content -Encoding UTF8 -NoNewline -Path $ConfigFile -Value $content.TrimEnd()

                $mcpUrlToml = Escape-TomlString $McpUrl
                $mcpKeyToml = Escape-TomlString $McpKey
                Add-Content -Encoding UTF8 -Path $ConfigFile -Value "`n`n[mcp_servers.reqflow]`nurl = `"$mcpUrlToml`"`nhttp_headers = { `"X-MCP-Key`" = `"$mcpKeyToml`" }"

                # Writes reqflow-mcp/SKILL.md.
                $skillContent = @'
                {{REQFLOW_SKILL_CONTENT}}
                '@
                Set-Content -Encoding UTF8 -Path $SkillFile -Value $skillContent

                Write-Host "Reqflow Codex MCP and reqflow-mcp skill installed."
                Write-Host "Do not call reqflow MCP tools automatically after installation."
                Write-Host "Restart or refresh Codex, then verify the reqflow MCP server is loaded."
                """.replace("{{REQFLOW_SKILL_CONTENT}}", ReqflowCodexGlobalSkillTemplate.skillContent());
    }
}
