package com.ruoyi.web.controller.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.requirement.domain.ReqMcpUserKey;

class ReqMcpKeyControllerTest
{
    @Test
    void createAndRegenerateDoNotSavePlainKeyResponseInOperationLog() throws NoSuchMethodException
    {
        Method add = ReqMcpKeyController.class.getMethod("add", ReqMcpUserKey.class, HttpServletRequest.class);
        Method regenerate = ReqMcpKeyController.class.getMethod("regenerate", Long.class, HttpServletRequest.class);

        assertFalse(add.getAnnotation(Log.class).isSaveResponseData());
        assertFalse(regenerate.getAnnotation(Log.class).isSaveResponseData());
    }

    @Test
    void mcpAddressUsesConfiguredPublicUrlBeforeRequestHost()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ReflectionTestUtils.setField(controller, "mcpPublicUrl", " http://localhost:8080/requirement/mcp/ ");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("http");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("localhost:1024");

        assertEquals("http://localhost:8080/requirement/mcp",
                ReflectionTestUtils.invokeMethod(controller, "mcpAddress", request));
    }

    @Test
    void mcpAddressFallsBackToForwardedHostWhenPublicUrlIsBlank()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ReflectionTestUtils.setField(controller, "mcpPublicUrl", " ");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("reqflow.example.com");
        when(request.getContextPath()).thenReturn("/api");

        assertEquals("https://reqflow.example.com/api/requirement/mcp",
                ReflectionTestUtils.invokeMethod(controller, "mcpAddress", request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void configReturnsTheSameConfiguredAddressInAddressAndTemplate()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ReflectionTestUtils.setField(controller, "mcpPublicUrl", "https://reqflow.example.com/requirement/mcp");

        AjaxResult result = controller.config(mock(HttpServletRequest.class));
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);

        assertEquals("https://reqflow.example.com/requirement/mcp", data.get("mcpAddress"));
        assertTrue(((String) data.get("codexConfigTemplate"))
                .contains("\"url\": \"https://reqflow.example.com/requirement/mcp\""));
    }

    @Test
    @SuppressWarnings("unchecked")
    void configReturnsCrossPlatformGlobalSkillPackage()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ReflectionTestUtils.setField(controller, "mcpPublicUrl", "https://reqflow.example.com/requirement/mcp");

        AjaxResult result = controller.config(mock(HttpServletRequest.class));
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        Map<String, Object> skillPackage = (Map<String, Object>) data.get("codexGlobalSkillPackage");

        assertEquals("reqflow-mcp", skillPackage.get("skillName"));
        assertEquals("global", skillPackage.get("installScope"));
        assertTrue(String.valueOf(skillPackage.get("installInstructions")).contains("Codex skill"));
        assertFalse(String.valueOf(skillPackage.get("installInstructions")).contains("mkdir -p"));
        assertFalse(String.valueOf(skillPackage.get("installInstructions")).contains("$HOME/.codex"));

        String packageText = String.valueOf(skillPackage);
        assertTrue(packageText.contains("SKILL.md"), packageText);
        assertTrue(packageText.contains("name: reqflow-mcp"), packageText);
        assertTrue(packageText.contains("description:"), packageText);
        assertTrue(packageText.contains("actionToken"), packageText);
        assertTrue(packageText.contains("mcpServer: reqflow"), packageText);
        assertTrue(packageText.contains("mcpTool: reqflow.publish_repository_index"), packageText);
        assertTrue(packageText.contains("mcp__reqflow.get_harness_template"), packageText);
        assertTrue(packageText.contains("mcp__reqflow.publish_repository_index"), packageText);
        assertTrue(packageText.contains("mcp__reqflow.register_harness_init_result"), packageText);
    }

    @Test
    @SuppressWarnings("unchecked")
    void configReturnsCodexSetupPackage()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ReflectionTestUtils.setField(controller, "mcpPublicUrl", "https://reqflow.example.com/requirement/mcp");

        AjaxResult result = controller.config(mock(HttpServletRequest.class));
        Map<String, Object> data = (Map<String, Object>) result.get(AjaxResult.DATA_TAG);
        Map<String, Object> setupPackage = (Map<String, Object>) data.get("codexSetupPackage");

        assertEquals("reqflow-codex-setup", setupPackage.get("packageName"));
        assertEquals("global", setupPackage.get("installScope"));
        assertEquals(data.get("codexConfigTemplate"), setupPackage.get("codexConfigTemplate"));

        Map<String, Object> mcpServer = (Map<String, Object>) setupPackage.get("mcpServer");
        assertEquals("reqflow", mcpServer.get("name"));
        assertEquals("streamable-http", mcpServer.get("transport"));
        assertEquals("https://reqflow.example.com/requirement/mcp", mcpServer.get("url"));
        assertEquals("X-MCP-Key", mcpServer.get("headerName"));

        String setupText = String.valueOf(setupPackage);
        assertTrue(setupText.contains("skillPackage"), setupText);
        assertTrue(setupText.contains("请安装 reqflow MCP 配置"), setupText);
        assertTrue(setupText.contains("不要自动调用 publish_repository_index"), setupText);
        assertTrue(setupText.contains("serverMetadata"), setupText);
        assertTrue(setupText.contains("project-init"), setupText);
        assertTrue(setupText.contains("index-publish"), setupText);
        assertTrue(setupText.contains("package-handoff"), setupText);
    }
}
