package com.ruoyi.requirement.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class ReqflowCodexGlobalSkillTemplateTest
{
    @Test
    void skillFrontmatterIsValidYamlAndKeepsTriggerKeywords()
    {
        Map<String, Object> frontmatter = parseFrontmatter(ReqflowCodexGlobalSkillTemplate.skillContent());

        assertEquals("reqflow-mcp", frontmatter.get("name"));
        String description = String.valueOf(frontmatter.get("description"));
        assertTrue(description.contains("actionToken"));
        assertTrue(description.contains("publish_repository_index"));
        assertTrue(description.contains("get_harness_template"));
        String skillContent = ReqflowCodexGlobalSkillTemplate.skillContent();
        assertTrue(skillContent.contains("frontend routes"), skillContent);
        assertTrue(skillContent.contains("frontend page business function"), skillContent);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseFrontmatter(String skillContent)
    {
        String[] parts = skillContent.split("---", 3);
        return new Yaml().loadAs(parts[1], Map.class);
    }
}
