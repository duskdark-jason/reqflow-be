package com.ruoyi.requirement.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(skillContent.contains("snapshot sync"), skillContent);
        assertTrue(skillContent.contains("closeout, archive, finish, or complete a local Harness task"), skillContent);
        assertTrue(skillContent.contains("before squash merge"), skillContent);
        assertTrue(skillContent.contains("git mv \"$SPEC_DIR\" docs/specs/done/"), skillContent);
        assertTrue(skillContent.contains("get_action_context"), skillContent);
        assertTrue(skillContent.contains("Call `mcp__reqflow.get_action_context` first"), skillContent);
        assertTrue(skillContent.contains("platformSync"), skillContent);
        assertTrue(skillContent.contains("Persist the copied `actionToken` in local `meta.md`"), skillContent);
        assertTrue(skillContent.contains("Do not call writeback tools until the user explicitly confirms"), skillContent);
        assertTrue(skillContent.contains("resume after context compaction"), skillContent);
        assertTrue(skillContent.contains("Stop after local requirement design draft"), skillContent);
        assertTrue(skillContent.contains("present the generated requirement design to the user for confirmation or edits"), skillContent);
        assertFalse(skillContent.contains("Stop after requirement package upload"), skillContent);
        assertTrue(skillContent.contains("stage: requirement_analysis"), skillContent);
        assertTrue(skillContent.contains("stage: requirement_generate"), skillContent);
        assertTrue(skillContent.contains("stage: requirement_develop"), skillContent);
        assertTrue(skillContent.contains("stage: requirement_repair"), skillContent);
        assertTrue(skillContent.contains("stage: requirement_closeout"), skillContent);
        assertTrue(skillContent.contains("requirement_analysis -> upload_requirement_assessment"), skillContent);
        assertTrue(skillContent.contains("requirement_generate -> save_requirement_package"), skillContent);
        assertTrue(skillContent.contains("requirement_develop -> save_development_plan, upload_execution_report, upload_review_report"), skillContent);
        assertTrue(skillContent.contains("requirement_repair -> upload_execution_report, upload_review_report"), skillContent);
        assertTrue(skillContent.contains("requirement_closeout -> publish_repository_index"), skillContent);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseFrontmatter(String skillContent)
    {
        String[] parts = skillContent.split("---", 3);
        return new Yaml().loadAs(parts[1], Map.class);
    }
}
