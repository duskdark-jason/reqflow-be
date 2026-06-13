package com.ruoyi.requirement.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReqflowCodexSetupPackageTemplateTest
{
    @Test
    @SuppressWarnings("unchecked")
    void setupPackageContainsMcpAndSkillInstructionsForSupportedClients()
    {
        Map<String, Object> setupPackage = ReqflowCodexSetupPackageTemplate.setupPackage("http://localhost:8080/requirement/mcp");

        assertEquals("reqflow-mcp-multi-client-setup", setupPackage.get("packageName"));
        assertEquals("global", setupPackage.get("installScope"));

        List<String> supportedClients = (List<String>) setupPackage.get("supportedClients");
        assertEquals(List.of("codex", "claude-code", "trae", "qoder", "codebuddy", "opencode"), supportedClients);

        List<Map<String, Object>> clientInstructions = (List<Map<String, Object>>) setupPackage.get("clientInstructions");
        assertNotNull(clientInstructions);
        assertEquals(6, clientInstructions.size());

        String packageText = String.valueOf(setupPackage);
        assertTrue(packageText.contains("[mcp_servers.reqflow]"), packageText);
        assertTrue(packageText.contains("\"type\": \"streamable-http\""), packageText);
        assertTrue(packageText.contains("\"type\": \"remote\""), packageText);
        assertTrue(packageText.contains("执行后选择要安装的工具"), packageText);
        assertTrue(packageText.contains("Manual MCP import required"), packageText);
        assertTrue(packageText.contains("写入或合并 OpenCode 全局 opencode.json"), packageText);
        assertTrue(packageText.contains("写入或合并 CodeBuddy 用户级 MCP 配置"), packageText);
        assertTrue(packageText.contains("Trae 可导入的 mcpServers JSON 片段"), packageText);
        assertTrue(packageText.contains("Qoder 可导入的 Streamable HTTP mcpServers JSON 片段"), packageText);
        assertFalse(packageText.contains("--client all"), packageText);
        assertTrue(packageText.contains("--client codex"), packageText);
        assertTrue(packageText.contains("--client claude-code"), packageText);
        assertTrue(packageText.contains("--client trae"), packageText);
        assertTrue(packageText.contains("--client qoder"), packageText);
        assertTrue(packageText.contains("--client codebuddy"), packageText);
        assertTrue(packageText.contains("--client opencode"), packageText);
        assertFalse(packageText.contains("-Client \"all\""), packageText);
        assertTrue(packageText.contains("-Client \"codex\""), packageText);
        assertTrue(packageText.contains("-Client \"claude-code\""), packageText);
        assertTrue(packageText.contains("-Client \"trae\""), packageText);
        assertTrue(packageText.contains("-Client \"qoder\""), packageText);
        assertTrue(packageText.contains("-Client \"codebuddy\""), packageText);
        assertTrue(packageText.contains("-Client \"opencode\""), packageText);
        assertTrue(packageText.contains("npx skills add"), packageText);
        assertTrue(packageText.contains("-a codex"), packageText);
        assertTrue(packageText.contains("-a claude-code"), packageText);
        assertTrue(packageText.contains("-a trae"), packageText);
        assertTrue(packageText.contains("-a qoder"), packageText);
        assertTrue(packageText.contains("-a codebuddy"), packageText);
        assertTrue(packageText.contains("-a opencode"), packageText);
        assertTrue(packageText.contains("${REQFLOW_MCP_KEY}"), packageText);
        assertFalse(packageText.contains("reqflow_mcp_test_secret"), packageText);
    }
}
