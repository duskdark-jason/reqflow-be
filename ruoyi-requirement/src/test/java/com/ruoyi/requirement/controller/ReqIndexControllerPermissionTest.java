package com.ruoyi.requirement.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;

class ReqIndexControllerPermissionTest
{
    @Test
    void impactSuggestAllowsDemandFormContextPermissions() throws NoSuchMethodException
    {
        Method method = ReqIndexController.class.getDeclaredMethod("suggest", ReqImpactSuggestQuery.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        String value = preAuthorize.value();
        assertTrue(value.contains("hasAnyPermi"), value);
        assertTrue(value.contains("req:index:list"), value);
        assertTrue(value.contains("req:demand:add"), value);
        assertTrue(value.contains("req:demand:edit"), value);
        assertTrue(value.contains("req:demand:query"), value);
    }
}
