package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpPublicHostConfig;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

class ReqMcpKeyControllerTest
{
    @Test
    void createDoesNotSavePlainKeyResponseInOperationLog() throws NoSuchMethodException
    {
        Method add = ReqMcpKeyController.class.getMethod("add", ReqMcpUserKey.class, HttpServletRequest.class);

        assertFalse(add.getAnnotation(Log.class).isSaveResponseData());
    }

    @Test
    void mcpAddressUsesSystemConfiguredHostPortBeforeRequestHost()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        when(configService.selectConfigByKey("reqflow.mcp.public-host")).thenReturn(" 10.0.0.12:18080 ");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("http");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("localhost:1024");

        assertEquals("http://10.0.0.12:18080/requirement/mcp",
                ReflectionTestUtils.invokeMethod(controller, "mcpAddress", request));
    }

    @Test
    void mcpAddressKeepsBackendContextPathWithConfiguredHost()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        when(configService.selectConfigByKey("reqflow.mcp.public-host")).thenReturn("reqflow.example.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getContextPath()).thenReturn("/reqflow-api");

        assertEquals("https://reqflow.example.com/reqflow-api/requirement/mcp",
                ReflectionTestUtils.invokeMethod(controller, "mcpAddress", request));
    }

    @Test
    void mcpAddressFallsBackToForwardedHostWhenPublicUrlIsBlank()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        when(configService.selectConfigByKey("reqflow.mcp.public-host")).thenReturn(" ");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("reqflow.example.com");
        when(request.getContextPath()).thenReturn("/api");

        assertEquals("https://reqflow.example.com/api/requirement/mcp",
                ReflectionTestUtils.invokeMethod(controller, "mcpAddress", request));
    }

    @Test
    void configEndpointsAreAdminOnly() throws NoSuchMethodException
    {
        Method query = ReqMcpKeyController.class.getMethod("config", HttpServletRequest.class);
        Method update = ReqMcpKeyController.class.getMethod("updateConfig", ReqMcpPublicHostConfig.class,
                HttpServletRequest.class);

        assertEquals("/config", query.getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/config", update.getAnnotation(PutMapping.class).value()[0]);
        assertEquals("@ss.hasRole('admin')", query.getAnnotation(PreAuthorize.class).value());
        assertEquals("@ss.hasRole('admin')", update.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void configEndpointReturnsConfiguredPublicHostAndResolvedMcpAddress()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        when(configService.selectConfigByKey("reqflow.mcp.public-host")).thenReturn("mcp.example.com:8443");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getContextPath()).thenReturn("/reqflow-api");

        ReqMcpPublicHostConfig data = (ReqMcpPublicHostConfig) controller.config(request).get("data");

        assertEquals("reqflow.mcp.public-host", data.getConfigKey());
        assertEquals("mcp.example.com:8443", data.getPublicHost());
        assertEquals("https://mcp.example.com:8443/reqflow-api/requirement/mcp", data.getMcpAddress());
    }

    @Test
    void updateConfigInsertsPublicHostWhenMissing()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        when(configService.selectConfigList(any(SysConfig.class))).thenReturn(Collections.emptyList());
        mockLoginUser(1L, "admin");

        ReqMcpPublicHostConfig request = new ReqMcpPublicHostConfig();
        request.setPublicHost("mcp.example.com:8443");
        controller.updateConfig(request, mock(HttpServletRequest.class));

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(configService).insertConfig(captor.capture());
        assertEquals("MCP服务对外访问地址", captor.getValue().getConfigName());
        assertEquals("reqflow.mcp.public-host", captor.getValue().getConfigKey());
        assertEquals("mcp.example.com:8443", captor.getValue().getConfigValue());
        assertEquals("Y", captor.getValue().getConfigType());
    }

    @Test
    void updateConfigRejectsProtocolOrPath()
    {
        ReqMcpKeyController controller = new ReqMcpKeyController();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(controller, "configService", configService);
        mockLoginUser(1L, "admin");

        ReqMcpPublicHostConfig request = new ReqMcpPublicHostConfig();
        request.setPublicHost("https://mcp.example.com/reqflow-api/requirement/mcp");

        assertEquals(500, controller.updateConfig(request, mock(HttpServletRequest.class)).get("code"));
    }

    @Test
    void controllerDoesNotExposeEditOrRegenerateEndpoint()
    {
        boolean hasUnexpectedPutMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PutMapping.class))
                .flatMap(method -> Arrays.stream(method.getAnnotation(PutMapping.class).value()))
                .anyMatch(value -> !"/config".equals(value));
        boolean hasRegenerateMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostMapping.class))
                .flatMap(method -> Arrays.stream(method.getAnnotation(PostMapping.class).value()))
                .anyMatch(value -> value.contains("regenerate"));

        assertFalse(hasUnexpectedPutMapping);
        assertFalse(hasRegenerateMapping);
    }

    private void mockLoginUser(Long userId, String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName(roleKey);
        user.setRoles(List.of(role));
        LoginUser loginUser = new LoginUser(userId, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }
}
