package com.ruoyi.requirement.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reqflow MCP 多客户端安装指令包模板。
 */
public final class ReqflowCodexSetupPackageTemplate
{
    public static final String PACKAGE_NAME = "reqflow-mcp-multi-client-setup";

    public static final String MCP_SERVER_NAME = "reqflow";

    public static final String MCP_KEY_HEADER = "X-MCP-Key";

    public static final String MCP_KEY_PLACEHOLDER = "${REQFLOW_MCP_KEY}";

    private ReqflowCodexSetupPackageTemplate()
    {
    }

    public static Map<String, Object> setupPackage(String mcpAddress)
    {
        String codexConfigTemplate = codexConfigTemplate(mcpAddress);
        Map<String, Object> setupPackage = new LinkedHashMap<>();
        setupPackage.put("packageName", PACKAGE_NAME);
        setupPackage.put("installScope", "global");
        setupPackage.put("installInstructions", installInstructions());
        setupPackage.put("supportedClients", supportedClients());
        setupPackage.put("mcpServer", mcpServer(mcpAddress));
        setupPackage.put("codexConfigTemplate", codexConfigTemplate);
        setupPackage.put("installScripts", installScripts(mcpAddress));
        setupPackage.put("installCommands", installCommands(mcpAddress));
        setupPackage.put("clientInstructions", clientInstructions(mcpAddress));
        setupPackage.put("skillPackage", ReqflowCodexGlobalSkillTemplate.globalSkillPackage());
        setupPackage.put("installPrompt", installPrompt());
        setupPackage.put("serverMetadata", serverMetadata(mcpAddress));
        return setupPackage;
    }

    public static String codexConfigTemplate(String mcpAddress)
    {
        return codexTomlConfig(mcpAddress, "创建后返回的Key");
    }

    private static String installInstructions()
    {
        return "Install the reqflow MCP configuration and the reqflow-mcp global skill for the selected AI client. "
                + "Use the clientInstructions[] entry matching Codex, Claude Code, Trae, Qoder, CodeBuddy, or OpenCode. "
                + "Prefer the provided npx skills add commands when the client supports Agent Skills. "
                + "Do not call reqflow MCP tools automatically after installation.";
    }

    private static List<String> supportedClients()
    {
        return List.of("codex", "claude-code", "trae", "qoder", "codebuddy", "opencode");
    }

    private static Map<String, Object> mcpServer(String mcpAddress)
    {
        Map<String, Object> mcpServer = new LinkedHashMap<>();
        mcpServer.put("name", MCP_SERVER_NAME);
        mcpServer.put("transport", "streamable-http");
        mcpServer.put("url", mcpAddress);
        mcpServer.put("headerName", MCP_KEY_HEADER);
        mcpServer.put("headerValue", "创建后返回的Key");
        return mcpServer;
    }

    private static String installPrompt()
    {
        return "请按目标客户端安装 reqflow MCP 配置和 reqflow-mcp 全局 skill。Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 优先使用 npx skills add。配置完成后只确认 MCP server 与 skill 已安装，"
                + "不要自动调用 publish_repository_index 或其他 reqflow MCP 工具；不要把 plainKey 或 actionToken 写入 skill 文件。";
    }

    private static List<Map<String, Object>> installScripts(String mcpAddress)
    {
        List<Map<String, Object>> scripts = new ArrayList<>();
        scripts.add(installScript("macos-linux", "macOS / Linux", "bash", installScriptUrl(mcpAddress, "install.sh")));
        scripts.add(installScript("windows-powershell", "Windows PowerShell", "powershell", installScriptUrl(mcpAddress, "install.ps1")));
        return scripts;
    }

    private static Map<String, Object> installScript(String platform, String label, String language, String url)
    {
        Map<String, Object> script = new LinkedHashMap<>();
        script.put("platform", platform);
        script.put("label", label);
        script.put("language", language);
        script.put("url", url);
        return script;
    }

    private static List<Map<String, Object>> installCommands(String mcpAddress)
    {
        List<Map<String, Object>> commands = new ArrayList<>();
        String shellUrl = installScriptUrl(mcpAddress, "install.sh");
        String powerShellUrl = installScriptUrl(mcpAddress, "install.ps1");
        commands.add(installCommand("macos-linux", "macOS / Linux", "bash",
                "export REQFLOW_MCP_KEY=\"" + MCP_KEY_PLACEHOLDER + "\"\n"
                        + "curl -fsSL \"" + escapeCommand(shellUrl) + "\" | bash -s -- --url \"" + escapeCommand(mcpAddress) + "\""));
        commands.add(installCommand("windows-powershell", "Windows PowerShell", "powershell",
                "$env:REQFLOW_MCP_KEY = \"" + MCP_KEY_PLACEHOLDER + "\"\n"
                        + "$script = irm \"" + escapeCommand(powerShellUrl) + "\"\n"
                        + "& ([scriptblock]::Create($script)) -McpUrl \"" + escapeCommand(mcpAddress) + "\""));
        return commands;
    }

    private static List<Map<String, Object>> clientInstructions(String mcpAddress)
    {
        List<Map<String, Object>> clients = new ArrayList<>();
        clients.add(codexClient(mcpAddress));
        clients.add(claudeCodeClient(mcpAddress));
        clients.add(traeClient(mcpAddress));
        clients.add(qoderClient(mcpAddress));
        clients.add(codeBuddyClient(mcpAddress));
        clients.add(openCodeClient(mcpAddress));
        return clients;
    }

    private static Map<String, Object> codexClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("codex", "Codex", "streamable-http", "~/.codex/config.toml");
        client.put("mcpConfigSnippet", codexTomlConfig(mcpAddress, MCP_KEY_PLACEHOLDER));
        client.put("commands", installCommands(mcpAddress));
        client.put("skillInstall", npxSkillInstall(mcpAddress, "codex", "Codex"));
        client.put("notes", List.of(
                "Codex MCP 配置写入 ~/.codex/config.toml。",
                "Codex skill 优先通过 npx skills add 安装到全局 skill 目录；安装后可重启 Codex 或刷新 skill 列表。"));
        return client;
    }

    private static Map<String, Object> claudeCodeClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("claude-code", "Claude Code", "streamable-http", "~/.claude.json 或项目 .mcp.json");
        client.put("mcpConfigSnippet", mcpServersJsonConfig(mcpAddress, "http"));
        client.put("commands", List.of(installCommand("claude-code-user", "Claude Code 全局 MCP", "bash",
                "claude mcp add --transport http --scope user reqflow \"" + escapeCommand(mcpAddress) + "\" --header \"" + MCP_KEY_HEADER + ": " + MCP_KEY_PLACEHOLDER + "\"")));
        client.put("skillInstall", npxSkillInstall(mcpAddress, "claude-code", "Claude Code"));
        client.put("notes", List.of(
                "Claude Code 可用 claude mcp add 写入用户级 MCP 配置，也可将 JSON 放入 .mcp.json。",
                "全局 skill 可通过 npx skills add 安装到 Claude Code 的用户级 skills 目录。"));
        return client;
    }

    private static Map<String, Object> traeClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("trae", "Trae", "streamable-http", "Trae 设置 > MCP > JSON 配置");
        client.put("mcpConfigSnippet", mcpServersJsonConfig(mcpAddress, "streamable-http"));
        client.put("commands", new ArrayList<Map<String, Object>>());
        client.put("skillInstall", npxSkillInstall(mcpAddress, "trae", "Trae"));
        client.put("notes", List.of(
                "Trae 的 MCP 配置可在 Settings > MCP 中粘贴 mcpServers JSON。",
                "Trae skill 可通过 npx skills add -a trae 全局安装，或在 Skills & Commands 中创建/导入 Global Skill。"));
        return client;
    }

    private static Map<String, Object> qoderClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("qoder", "Qoder", "streamable-http", "~/.qoder 或 Qoder Settings > MCP");
        client.put("mcpConfigSnippet", mcpServersJsonConfig(mcpAddress, "streamable-http"));
        client.put("commands", new ArrayList<Map<String, Object>>());
        client.put("skillInstall", npxSkillInstall(mcpAddress, "qoder", "Qoder"));
        client.put("notes", List.of(
                "Qoder 支持在 Connectors & MCP 中粘贴包含 headers 的 Streamable HTTP 配置。",
                "Qoder skill 可通过 npx skills add -a qoder 安装到用户级 skill 目录。"));
        return client;
    }

    private static Map<String, Object> codeBuddyClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("codebuddy", "CodeBuddy Code", "http", "~/.codebuddy/.mcp.json");
        client.put("mcpConfigSnippet", mcpServersJsonConfig(mcpAddress, "http"));
        client.put("commands", List.of(installCommand("codebuddy-user", "CodeBuddy 全局 MCP", "bash",
                "codebuddy mcp add-json --scope user reqflow '{\"type\":\"http\",\"url\":\"" + escapeSingleQuotedJson(mcpAddress)
                        + "\",\"headers\":{\"" + MCP_KEY_HEADER + "\":\"" + MCP_KEY_PLACEHOLDER + "\"}}'")));
        client.put("skillInstall", npxSkillInstall(mcpAddress, "codebuddy", "CodeBuddy Code"));
        client.put("notes", List.of(
                "CodeBuddy Code 的用户级 MCP 配置推荐写入 ~/.codebuddy/.mcp.json。",
                "CodeBuddy Code skill 可通过 npx skills add -a codebuddy 安装。"));
        return client;
    }

    private static Map<String, Object> openCodeClient(String mcpAddress)
    {
        Map<String, Object> client = baseClient("opencode", "OpenCode", "remote", "~/.config/opencode/opencode.json 或项目 opencode.json");
        client.put("mcpConfigSnippet", openCodeJsonConfig(mcpAddress));
        client.put("commands", List.of(installCommand("opencode-mcp-add", "OpenCode 交互式 MCP 添加", "bash",
                "opencode mcp add")));
        client.put("skillInstall", npxSkillInstall(mcpAddress, "opencode", "OpenCode"));
        client.put("notes", List.of(
                "OpenCode 的远程 MCP 配置写在 opencode.json 的 mcp.reqflow 下，type 使用 remote。",
                "OpenCode skill 可通过 npx skills add -a opencode 安装到用户级 skill 目录。"));
        return client;
    }

    private static Map<String, Object> baseClient(String clientId, String label, String transport, String configPath)
    {
        Map<String, Object> client = new LinkedHashMap<>();
        client.put("client", clientId);
        client.put("label", label);
        client.put("transport", transport);
        client.put("mcpConfigPath", configPath);
        client.put("mcpHeaderName", MCP_KEY_HEADER);
        return client;
    }

    private static Map<String, Object> npxSkillInstall(String mcpAddress, String agent, String label)
    {
        String skillUrl = installScriptUrl(mcpAddress, "skill/SKILL.md");
        Map<String, Object> skillInstall = new LinkedHashMap<>();
        skillInstall.put("method", "npx-skills-add");
        skillInstall.put("scope", "global");
        skillInstall.put("source", skillUrl);
        skillInstall.put("commands", List.of(
                installCommand(agent + "-skill-macos-linux", label + " 全局 skill（macOS / Linux）", "bash",
                        "SKILL_DIR=\"$(mktemp -d)/reqflow-mcp\"\n"
                                + "mkdir -p \"$SKILL_DIR\"\n"
                                + "curl -fsSL \"" + escapeCommand(skillUrl) + "\" -o \"$SKILL_DIR/SKILL.md\"\n"
                                + "npx skills add \"$SKILL_DIR\" -g -a " + agent + " --copy -y"),
                installCommand(agent + "-skill-windows-powershell", label + " 全局 skill（Windows PowerShell）", "powershell",
                        "$skillDir = Join-Path ([System.IO.Path]::GetTempPath()) (\"reqflow-mcp-\" + [guid]::NewGuid())\n"
                                + "New-Item -ItemType Directory -Force -Path $skillDir | Out-Null\n"
                                + "irm \"" + escapeCommand(skillUrl) + "\" | Set-Content -Encoding UTF8 -Path (Join-Path $skillDir \"SKILL.md\")\n"
                                + "npx skills add $skillDir -g -a " + agent + " --copy -y")));
        return skillInstall;
    }

    private static String codexTomlConfig(String mcpAddress, String keyValue)
    {
        return "[mcp_servers.reqflow]\n"
                + "url = \"" + escapeToml(mcpAddress) + "\"\n"
                + "http_headers = { \"" + MCP_KEY_HEADER + "\" = \"" + escapeToml(keyValue) + "\" }";
    }

    private static String mcpServersJsonConfig(String mcpAddress, String type)
    {
        return "{\n"
                + "  \"mcpServers\": {\n"
                + "    \"reqflow\": {\n"
                + "      \"type\": \"" + escapeJson(type) + "\",\n"
                + "      \"url\": \"" + escapeJson(mcpAddress) + "\",\n"
                + "      \"headers\": {\n"
                + "        \"" + MCP_KEY_HEADER + "\": \"" + MCP_KEY_PLACEHOLDER + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private static String openCodeJsonConfig(String mcpAddress)
    {
        return "{\n"
                + "  \"$schema\": \"https://opencode.ai/config.json\",\n"
                + "  \"mcp\": {\n"
                + "    \"reqflow\": {\n"
                + "      \"type\": \"remote\",\n"
                + "      \"url\": \"" + escapeJson(mcpAddress) + "\",\n"
                + "      \"enabled\": true,\n"
                + "      \"headers\": {\n"
                + "        \"" + MCP_KEY_HEADER + "\": \"" + MCP_KEY_PLACEHOLDER + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private static Map<String, Object> installCommand(String platform, String label, String language, String command)
    {
        Map<String, Object> installCommand = new LinkedHashMap<>();
        installCommand.put("platform", platform);
        installCommand.put("label", label);
        installCommand.put("language", language);
        installCommand.put("command", command);
        return installCommand;
    }

    private static String installScriptUrl(String mcpAddress, String fileName)
    {
        String address = trimTrailingSlash(mcpAddress);
        String marker = "/requirement/mcp";
        int markerIndex = address.indexOf(marker);
        if (markerIndex >= 0)
        {
            return address.substring(0, markerIndex) + "/requirement/codex/" + fileName;
        }
        return address + "/codex/" + fileName;
    }

    private static Map<String, Object> serverMetadata(String mcpAddress)
    {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("$schema", "https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json");
        metadata.put("name", "com.reqflow/reqflow-mcp");
        metadata.put("title", "Reqflow");
        metadata.put("description", "Reqflow requirement-platform MCP server for project onboarding, repository index publishing, requirement assessment, design, development, and review package persistence.");
        metadata.put("remotes", remotes(mcpAddress));
        metadata.put("toolsets", toolsets());
        metadata.put("securityNotes", securityNotes());
        return metadata;
    }

    private static List<Map<String, Object>> remotes(String mcpAddress)
    {
        List<Map<String, Object>> remotes = new ArrayList<>();
        Map<String, Object> remote = new LinkedHashMap<>();
        remote.put("type", "streamable-http");
        remote.put("url", mcpAddress);
        remote.put("headers", headers());
        remotes.add(remote);
        return remotes;
    }

    private static List<Map<String, Object>> headers()
    {
        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("name", MCP_KEY_HEADER);
        header.put("description", "Reqflow MCP user key returned once when creating a key.");
        header.put("isSecret", true);
        headers.add(header);
        return headers;
    }

    private static List<Map<String, Object>> toolsets()
    {
        List<Map<String, Object>> toolsets = new ArrayList<>();
        toolsets.add(toolset("project-init", "Project onboarding and harness initialization.", "get_harness_template", "register_harness_init_result"));
        toolsets.add(toolset("index-publish", "Repository knowledge index publishing.", "publish_repository_index"));
        toolsets.add(toolset("package-handoff", "Requirement assessment, design, plan, execution, and review package handoff.",
                "upload_requirement_assessment", "save_requirement_package", "save_development_plan", "upload_execution_report", "upload_review_report"));
        return toolsets;
    }

    private static Map<String, Object> toolset(String id, String description, String... tools)
    {
        Map<String, Object> toolset = new LinkedHashMap<>();
        toolset.put("id", id);
        toolset.put("description", description);
        toolset.put("tools", List.of(tools));
        return toolset;
    }

    private static List<String> securityNotes()
    {
        List<String> notes = new ArrayList<>();
        notes.add("The MCP user key is secret and must be configured as a header, not written into skill files.");
        notes.add("After setup, do not call publish_repository_index automatically; wait for an explicit project initialization instruction.");
        notes.add("The reqflow MCP server returns templates and stores indexes; the local agent writes, commits, and pushes harness initialization files in the target workspace.");
        return notes;
    }

    private static String escapeJson(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeToml(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeCommand(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeSingleQuotedJson(String value)
    {
        if (value == null)
        {
            return "";
        }
        return escapeJson(value).replace("'", "'\"'\"'");
    }

    private static String trimTrailingSlash(String value)
    {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/") && !result.endsWith("://"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
