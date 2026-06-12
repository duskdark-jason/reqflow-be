package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReqCodexInstallControllerTest
{
    @Test
    void shellInstallScriptWritesMcpConfigAndGlobalSkill()
    {
        String script = new ReqCodexInstallController().installShellScript();

        assertTrue(script.startsWith("#!/usr/bin/env bash"));
        assertTrue(script.contains("REQFLOW_MCP_KEY"));
        assertTrue(script.contains("[mcp_servers.reqflow]"));
        assertTrue(script.contains("X-MCP-Key"));
        assertTrue(script.contains("reqflow-mcp/SKILL.md"));
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
    }

    @Test
    void powershellInstallScriptWritesMcpConfigAndGlobalSkill()
    {
        String script = new ReqCodexInstallController().installPowerShellScript();

        assertTrue(script.contains("param("));
        assertTrue(script.contains("REQFLOW_MCP_KEY"));
        assertTrue(script.contains("[mcp_servers.reqflow]"));
        assertTrue(script.contains("X-MCP-Key"));
        assertTrue(script.contains("reqflow-mcp/SKILL.md"));
        assertTrue(script.contains("name: \"reqflow-mcp\""));
        assertTrue(script.contains("description: \"Use when"));
        assertTrue(script.contains("Do not call reqflow MCP tools automatically"));
        assertFalse(script.contains("reqflow_mcp_test_secret"));
    }
}
