package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReqCodexInstallControllerTest
{
    @Test
    void shellInstallScriptInstallsSelectedClientMcpAndGlobalSkill()
    {
        String script = new ReqCodexInstallController().installShellScript();

        assertTrue(script.startsWith("#!/usr/bin/env bash"));
        assertTrue(script.contains("--client"));
        assertTrue(script.contains("all"));
        assertTrue(script.contains("SUPPORTED_CLIENTS"));
        assertTrue(script.contains("select_clients_interactively"));
        assertTrue(script.contains("Select reqflow clients to install"));
        assertTrue(script.contains("REQFLOW_MCP_KEY"));
        assertTrue(script.contains("[mcp_servers.reqflow]"));
        assertTrue(script.contains("X-MCP-Key"));
        assertTrue(script.contains("install_codex_mcp"));
        assertTrue(script.contains("install_claude_code_mcp"));
        assertTrue(script.contains("install_trae_mcp"));
        assertTrue(script.contains("install_qoder_mcp"));
        assertTrue(script.contains("install_codebuddy_mcp"));
        assertTrue(script.contains("install_opencode_mcp"));
        assertTrue(script.contains("install_all_clients"));
        assertTrue(script.contains("reqflow-mcp/SKILL.md"));
        assertTrue(script.contains("npx skills add \"$SKILL_DIR\" -g -a \"$target_client\" --copy -y"));
        assertTrue(script.contains("merge_json_config"));
        assertTrue(script.contains("record_manual_import \"trae\""));
        assertTrue(script.contains("record_manual_import \"qoder\""));
        assertTrue(script.contains("Reqflow automatic MCP configuration completed"));
        assertTrue(script.contains("Manual MCP import required"));
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("Reqflow MCP and reqflow-mcp global skill installed for:$SELECTED_CLIENTS."));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
    }

    @Test
    void powershellInstallScriptInstallsSelectedClientMcpAndGlobalSkill()
    {
        String script = new ReqCodexInstallController().installPowerShellScript();

        assertTrue(script.contains("param("));
        assertTrue(script.contains("$InstallableClients = @(\"codex\", \"claude-code\", \"trae\", \"qoder\", \"codebuddy\", \"opencode\")"));
        assertTrue(script.contains("Select-ReqflowClientsInteractively"));
        assertTrue(script.contains("Select reqflow clients to install"));
        assertTrue(script.contains("REQFLOW_MCP_KEY"));
        assertTrue(script.contains("[mcp_servers.reqflow]"));
        assertTrue(script.contains("X-MCP-Key"));
        assertTrue(script.contains("Install-CodexMcp"));
        assertTrue(script.contains("Install-ClaudeCodeMcp"));
        assertTrue(script.contains("Install-TraeMcp"));
        assertTrue(script.contains("Install-QoderMcp"));
        assertTrue(script.contains("Install-CodeBuddyMcp"));
        assertTrue(script.contains("Install-OpenCodeMcp"));
        assertTrue(script.contains("Install-AllClients"));
        assertTrue(script.contains("reqflow-mcp/SKILL.md"));
        assertTrue(script.contains("npx skills add $SkillDir -g -a $TargetClient --copy -y"));
        assertTrue(script.contains("Merge-JsonConfig"));
        assertTrue(script.contains("Add-ManualMcpImport -Client \"trae\""));
        assertTrue(script.contains("Add-ManualMcpImport -Client \"qoder\""));
        assertTrue(script.contains("Reqflow automatic MCP configuration completed"));
        assertTrue(script.contains("Manual MCP import required"));
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("Reqflow MCP and reqflow-mcp global skill installed for: "));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
    }

    @Test
    void shellInstallScriptMergesExistingOpenCodeConfig(@TempDir Path tempDir) throws Exception
    {
        Path home = tempDir.resolve("home");
        Path openCodeConfig = home.resolve(".config/opencode/opencode.json");
        Files.createDirectories(openCodeConfig.getParent());
        Files.writeString(openCodeConfig, """
                {
                  "$schema": "https://opencode.ai/config.json",
                  "model": "anthropic/test",
                  "mcp": {
                    "other": {
                      "type": "remote",
                      "url": "https://example.com/mcp",
                      "enabled": true
                    }
                  }
                }
                """);

        Path bin = tempDir.resolve("bin");
        Files.createDirectories(bin);
        Path npx = bin.resolve("npx");
        Files.writeString(npx, "#!/usr/bin/env bash\nexit 0\n", StandardCharsets.UTF_8);
        npx.toFile().setExecutable(true);

        Path installScript = tempDir.resolve("install.sh");
        Files.writeString(installScript, new ReqCodexInstallController().installShellScript(), StandardCharsets.UTF_8);
        installScript.toFile().setExecutable(true);

        ProcessBuilder processBuilder = new ProcessBuilder("bash", installScript.toString(),
                "--client", "opencode",
                "--url", "http://127.0.0.1:8080/requirement/mcp",
                "--key", "reqflow_secret");
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("HOME", home.toString());
        processBuilder.environment().put("REQFLOW_INSTALL_DIR", tempDir.resolve("support").toString());
        processBuilder.environment().put("PATH", bin + File.pathSeparator + System.getenv("PATH"));

        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, output);
        String mergedConfig = Files.readString(openCodeConfig);
        assertTrue(mergedConfig.contains("\"model\": \"anthropic/test\""), mergedConfig);
        assertTrue(mergedConfig.contains("\"other\""), mergedConfig);
        assertTrue(mergedConfig.contains("\"reqflow\""), mergedConfig);
        assertTrue(mergedConfig.contains("\"type\": \"remote\""), mergedConfig);
        assertTrue(mergedConfig.contains("\"X-MCP-Key\": \"reqflow_secret\""), mergedConfig);
        assertTrue(output.contains("Wrote OpenCode MCP config"), output);
        assertFalse(Files.exists(tempDir.resolve("support/opencode-reqflow-mcp.json")));
    }

    @Test
    void skillFileEndpointReturnsGlobalSkillContent()
    {
        String skill = new ReqCodexInstallController().skillFile();

        assertTrue(skill.startsWith("---"));
        assertTrue(skill.contains("name: \"reqflow-mcp\""));
        assertTrue(skill.contains("mcp__reqflow.get_harness_template"));
        assertTrue(skill.contains("actionToken"));
        assertFalse(skill.contains("reqflow_mcp_test_secret"));
    }
}
