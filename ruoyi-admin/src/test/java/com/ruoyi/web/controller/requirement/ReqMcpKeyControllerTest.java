package com.ruoyi.web.controller.requirement;

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
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.annotation.Log;
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
    void controllerDoesNotExposeConfigEndpoint()
    {
        boolean hasConfigMapping = Arrays.stream(ReqMcpKeyController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .flatMap(method -> Arrays.stream(method.getAnnotation(GetMapping.class).value()))
                .anyMatch("/config"::equals);

        assertFalse(hasConfigMapping);
    }
}
