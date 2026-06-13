package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
    }

    @Test
    void powershellInstallScriptInstallsSelectedClientMcpAndGlobalSkill()
    {
        String script = new ReqCodexInstallController().installPowerShellScript();

        assertTrue(script.contains("param("));
        assertTrue(script.contains("[ValidateSet(\"all\", \"codex\", \"claude-code\", \"trae\", \"qoder\", \"codebuddy\", \"opencode\")]"));
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
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
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
