package com.ruoyi.requirement.template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class RequirementTemplateService
{
    public String render(String templatePath, RequirementTemplateContext context)
    {
        String template = load(templatePath);
        return template
                .replace("${projectNameJson}", jsonValue(context.getProjectName()))
                .replace("${variantNameJson}", jsonValue(context.getVariantName()))
                .replace("${baselineBranchJson}", jsonValue(context.getBaselineBranch()))
                .replace("${demandNoJson}", jsonValue(context.getDemandNo()))
                .replace("${demandTitleJson}", jsonValue(context.getDemandTitle()))
                .replace("${demandSourceJson}", jsonValue(context.getDemandSource()))
                .replace("${businessBackgroundJson}", jsonValue(context.getBusinessBackground()))
                .replace("${attachmentsJson}", jsonValue(context.getAttachments()))
                .replace("${taskBranchJson}", jsonValue(context.getTaskBranch()))
                .replace("${moduleNameJson}", jsonValue(context.getModuleName()))
                .replace("${acceptanceTextJson}", jsonValue(context.getAcceptanceText()))
                .replace("${projectName}", value(context.getProjectName()))
                .replace("${projectCode}", value(context.getProjectCode()))
                .replace("${repoName}", value(context.getRepoName()))
                .replace("${repoType}", value(context.getRepoType()))
                .replace("${variantName}", value(context.getVariantName()))
                .replace("${baselineBranch}", value(context.getBaselineBranch()))
                .replace("${demandNo}", value(context.getDemandNo()))
                .replace("${demandTitle}", value(context.getDemandTitle()))
                .replace("${demandSource}", value(context.getDemandSource()))
                .replace("${businessBackground}", value(context.getBusinessBackground()))
                .replace("${attachments}", value(context.getAttachments()))
                .replace("${taskBranch}", value(context.getTaskBranch()))
                .replace("${moduleName}", value(context.getModuleName()))
                .replace("${acceptanceText}", value(context.getAcceptanceText()));
    }

    private String load(String templatePath)
    {
        try
        {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("模板读取失败：" + templatePath, e);
        }
    }

    private String value(String value)
    {
        return value == null ? "" : value;
    }

    private String jsonValue(String value)
    {
        if (value == null)
        {
            return "\"\"";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t") + "\"";
    }
}
