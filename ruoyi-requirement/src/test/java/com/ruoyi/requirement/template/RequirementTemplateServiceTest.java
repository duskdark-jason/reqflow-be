package com.ruoyi.requirement.template;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequirementTemplateServiceTest
{
    @Test
    void rendersKnownPlaceholdersDeterministically()
    {
        RequirementTemplateContext context = new RequirementTemplateContext();
        context.setProjectName("需求平台");
        context.setProjectCode("REQFLOW");
        context.setDemandNo("REQ-20260609-001");
        context.setDemandTitle("提交需求");
        context.setTaskBranch("feature/req-001-submit-demand");
        context.setAcceptanceText("可以生成执行包");

        String content = new RequirementTemplateService()
                .render("templates/requirement/requirement-draft.md", context);

        assertTrue(content.contains("需求平台"));
        assertTrue(content.contains("REQ-20260609-001"));
        assertTrue(content.contains("可以生成执行包"));
        assertFalse(content.contains("${"));
    }
}
