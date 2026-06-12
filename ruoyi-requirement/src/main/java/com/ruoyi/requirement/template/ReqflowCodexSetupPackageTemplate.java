package com.ruoyi.requirement.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reqflow Codex 安装指令包模板。
 */
public final class ReqflowCodexSetupPackageTemplate
{
    public static final String PACKAGE_NAME = "reqflow-codex-setup";

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
        setupPackage.put("mcpServer", mcpServer(mcpAddress));
        setupPackage.put("codexConfigTemplate", codexConfigTemplate);
        setupPackage.put("installScripts", installScripts(mcpAddress));
        setupPackage.put("installCommands", installCommands(mcpAddress));
        setupPackage.put("skillPackage", ReqflowCodexGlobalSkillTemplate.globalSkillPackage());
        setupPackage.put("installPrompt", installPrompt());
        setupPackage.put("serverMetadata", serverMetadata(mcpAddress));
        return setupPackage;
    }

    public static String codexConfigTemplate(String mcpAddress)
    {
        return "{\n"
                + "  \"mcpServers\": {\n"
                + "    \"reqflow\": {\n"
                + "      \"url\": \"" + escapeJson(mcpAddress) + "\",\n"
                + "      \"headers\": {\n"
                + "        \"" + MCP_KEY_HEADER + "\": \"创建后返回的Key\"\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private static String installInstructions()
    {
        return "Install the reqflow MCP configuration and the reqflow-mcp global Codex skill from this setup package. "
                + "Let Codex choose the correct configuration and skill locations for the current platform. "
                + "Do not call reqflow MCP tools automatically after installation.";
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
        return "请安装 reqflow MCP 配置和 reqflow-mcp 全局 skill。配置完成后只确认 MCP server 与 skill 已安装，"
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

    private static String escapeCommand(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
