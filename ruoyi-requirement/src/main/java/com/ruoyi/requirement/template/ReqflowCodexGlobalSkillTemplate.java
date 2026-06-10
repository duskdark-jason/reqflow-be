package com.ruoyi.requirement.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reqflow 全局 Codex skill 包模板。
 */
public final class ReqflowCodexGlobalSkillTemplate
{
    public static final String SKILL_NAME = "reqflow-mcp";

    private ReqflowCodexGlobalSkillTemplate()
    {
    }

    public static Map<String, Object> globalSkillPackage()
    {
        Map<String, Object> skillPackage = new LinkedHashMap<>();
        skillPackage.put("skillName", SKILL_NAME);
        skillPackage.put("installScope", "global");
        skillPackage.put("installInstructions", installInstructions());
        skillPackage.put("files", skillFiles());
        return skillPackage;
    }

    private static List<Map<String, Object>> skillFiles()
    {
        List<Map<String, Object>> files = new ArrayList<>();
        Map<String, Object> skillFile = new LinkedHashMap<>();
        skillFile.put("path", SKILL_NAME + "/SKILL.md");
        skillFile.put("writeMode", "create-or-update");
        skillFile.put("content", skillContent());
        files.add(skillFile);
        return files;
    }

    private static String installInstructions()
    {
        return "Install this package as a global Codex skill named reqflow-mcp. "
                + "Let Codex resolve the correct global skills directory for the current operating system, "
                + "then create or update the files listed in files[]. Do not store personal MCP keys in the skill.";
    }

    public static String skillContent()
    {
        return "---\n"
                + "name: \"" + SKILL_NAME + "\"\n"
                + "description: \"Use when the user provides a Reqflow project-init instruction, actionToken, mcpServer reqflow, mcpTool reqflow.publish_repository_index, publish_repository_index, get_harness_template, harness initialization, or project onboarding request.\"\n"
                + "---\n\n"
                + "# Reqflow MCP Project Initialization\n\n"
                + "Use this skill when a request involves Reqflow MCP project onboarding, repository index publishing, or harness initialization.\n\n"
                + "## Trigger Signals\n\n"
                + "- `actionToken`\n"
                + "- `mcpServer: reqflow`\n"
                + "- `mcpTool: reqflow.publish_repository_index`\n"
                + "- `publish_repository_index`\n"
                + "- `get_harness_template`\n"
                + "- 项目接入初始化、harness 初始化、仓库索引发布\n\n"
                + "## Required Workflow\n\n"
                + "1. Confirm the current Codex session has loaded the reqflow MCP server and exposes `mcp__reqflow.get_harness_template`, `mcp__reqflow.publish_repository_index`, and `mcp__reqflow.register_harness_init_result`.\n"
                + "2. Call `mcp__reqflow.get_harness_template` first with the project ID from the instruction or platform context.\n"
                + "3. Write or merge the returned `workspaceFiles` and `repositoryHarnessInstructions[].files` into the target workspace and repositories.\n"
                + "4. In each target repository, run `sh scripts/check-docs.sh` and `sh scripts/check-harness.sh init` when those scripts are present.\n"
                + "5. Before publishing the index, inspect frontend routes, menus, page components, and API wrappers. Build `modules` as concrete frontend page business functions, menu entries, submenus, or hidden tabs. For backend-only repositories, map modules to companion frontend menus, MCP capabilities, or background jobs.\n"
                + "6. Publish each repository index with `mcp__reqflow.publish_repository_index`. `modules` must not be empty; each module should represent one frontend page business function or backend capability, and `pages/apis/tables/permissions/documents` must reference the matching `moduleCode`. Put the action token in `arguments.actionToken`; do not use it as `X-MCP-Key`.\n"
                + "7. Register the local harness result with `mcp__reqflow.register_harness_init_result`.\n\n"
                + "## Guardrails\n\n"
                + "- The reqflow MCP server stores platform context and repository indexes; it does not directly write files on behalf of another local workspace.\n"
                + "- For multi-repository workspaces, process each registered repository separately.\n"
                + "- Do not publish a repository-level overview as the only module. Project onboarding must persist a concrete business knowledge base derived from frontend pages or equivalent backend capabilities.\n"
                + "- If `publish_repository_index` reports missing index tables, surface the platform migration hint to the user instead of retrying blindly.\n";
    }
}
