package com.ruoyi.requirement.template;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequirementTemplateServiceTest
{
    @Test
    void contextManifestEscapesRichTextHtml()
    {
        RequirementTemplateContext context = new RequirementTemplateContext();
        context.setDemandNo("REQ-001");
        context.setDemandTitle("带图片需求");
        context.setProjectName("需求平台");
        context.setVariantName("主线");
        context.setBaselineBranch("main");
        context.setTaskBranch("fix-demo");
        context.setModuleName("需求列表");
        context.setDemandSource("CUSTOMER");
        context.setBusinessBackground("<p><img src=\"/profile/upload/a.png\"></p>");
        context.setAttachments("/profile/upload/a.pdf");
        context.setAcceptanceText("验收");

        String result = new RequirementTemplateService().render("templates/requirement/context-manifest.json", context);

        assertTrue(result.contains("\\\"/profile/upload/a.png\\\""), result);
        assertTrue(result.contains("\"demandSource\": \"CUSTOMER\""), result);
    }
}
