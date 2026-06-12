package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
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
    void controllerDoesNotExposeConfigEndpoint()
    {
        boolean hasConfigMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .flatMap(method -> Arrays.stream(method.getAnnotation(GetMapping.class).value()))
                .anyMatch("/config"::equals);

        assertFalse(hasConfigMapping);
    }

    @Test
    void controllerDoesNotExposeEditOrRegenerateEndpoint()
    {
        boolean hasPutMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(PutMapping.class));
        boolean hasRegenerateMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostMapping.class))
                .flatMap(method -> Arrays.stream(method.getAnnotation(PostMapping.class).value()))
                .anyMatch(value -> value.contains("regenerate"));

        assertFalse(hasPutMapping);
        assertFalse(hasRegenerateMapping);
    }
}
