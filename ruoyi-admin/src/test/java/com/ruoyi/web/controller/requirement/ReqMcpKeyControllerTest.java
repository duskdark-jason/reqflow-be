package com.ruoyi.web.controller.requirement;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Method;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
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
}
