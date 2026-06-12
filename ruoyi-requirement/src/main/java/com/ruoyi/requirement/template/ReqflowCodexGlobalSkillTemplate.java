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
                + "description: \"Use when the user provides a Reqflow project-init, requirement design, development, review, closeout, actionToken, mcpServer reqflow, get_harness_template, publish_repository_index, upload_requirement_assessment, save_requirement_package, upload_execution_report, or upload_review_report request.\"\n"
                + "---\n\n"
                + "# Reqflow MCP Workflow\n\n"
                + "Use this skill when a request involves Reqflow MCP project onboarding, requirement design, development handoff, repository index publishing, or harness initialization.\n\n"
                + "## Trigger Signals\n\n"
                + "- `actionToken`\n"
                + "- `mcpServer: reqflow`\n"
                + "- `mcpTool: reqflow.publish_repository_index`\n"
                + "- `mcpTool: reqflow.upload_requirement_assessment`\n"
                + "- `mcpTool: reqflow.save_requirement_package`\n"
                + "- `mcpTool: reqflow.save_development_plan`\n"
                + "- `mcpTool: reqflow.upload_execution_report`\n"
                + "- `mcpTool: reqflow.upload_review_report`\n"
                + "- `publish_repository_index`\n"
                + "- `get_harness_template`\n"
                + "- 项目接入初始化、harness 初始化、仓库索引发布、需求设计、执行计划、执行报告、Review 报告、合并归档\n\n"
                + "## Project Onboarding Workflow\n\n"
                + "1. Confirm the current Codex session has loaded the reqflow MCP server and exposes `mcp__reqflow.get_harness_template`, `mcp__reqflow.publish_repository_index`, and `mcp__reqflow.register_harness_init_result`.\n"
                + "2. Call `mcp__reqflow.get_harness_template` first with the project ID from the instruction or platform context.\n"
                + "3. In each target repository, verify the remote, switch to the registered default branch, and run `git pull --ff-only` before writing files.\n"
                + "4. Write or merge the returned `workspaceFiles` and `repositoryHarnessInstructions[].files` into the target workspace and repositories.\n"
                + "5. In each target repository, run `sh scripts/check-docs.sh` and `sh scripts/check-harness.sh init` when those scripts are present.\n"
                + "6. After init checks pass, commit and push the generated or upgraded `AGENTS.md`, `docs/`, and `scripts/` files.\n"
                + "7. Before publishing the index, inspect frontend routes, menus, page components, and API wrappers. Build `modules` as concrete frontend page business functions, menu entries, submenus, or hidden tabs. For backend-only repositories, map modules to companion frontend menus, MCP capabilities, or background jobs.\n"
                + "8. Publish each repository index with `mcp__reqflow.publish_repository_index`. `modules` must not be empty; each module should represent one frontend page business function or backend capability, and `pages/apis/tables/permissions/documents` must reference the matching `moduleCode`. Re-publishing the same repository branch is a snapshot sync; modules and impact items omitted from the new payload are no longer active in reqflow. Put the action token in `arguments.actionToken`; do not use it as `X-MCP-Key`.\n"
                + "9. Register the local harness result, commit, push result, and failure reason with `mcp__reqflow.register_harness_init_result`.\n\n"
                + "## Requirement Analysis Workflow\n\n"
                + "1. Read the demand detail, draft package, context manifest, repository remote, baseline branch, suggested task branch, historical design versions, and demander supplemental instructions from reqflow MCP resources or instruction content.\n"
                + "2. Verify the current workspace repository remote, switch to the target baseline branch, run `git pull --ff-only`, then create or switch to the suggested ASCII task branch.\n"
                + "3. Write the feasibility assessment first. The conclusion must be one of 可继续设计, 需澄清, 需调整, or 暂不可实现, with risks, blockers, and demander follow-up items.\n"
                + "4. Upload the assessment with `mcp__reqflow.upload_requirement_assessment`; pass the assessment action token as `arguments.actionToken`, never as `X-MCP-Key`.\n"
                + "5. Stop after uploading the assessment. If the conclusion is 需澄清, 需调整, or 暂不可实现, surface the feedback for the demander. If it is 可继续设计, wait for the platform to move to requirement generation and provide a new instruction.\n"
                + "6. Do not call `mcp__reqflow.save_requirement_package`, write final `requirement.md`, write `plan.md`, implement code, or write execution/review reports during requirement analysis.\n\n"
                + "## Requirement Generation Workflow\n\n"
                + "1. Read the approved assessment, demand detail, historical design versions, repository remote, baseline branch, suggested task branch, and demander supplemental instructions from reqflow.\n"
                + "2. Use the task branch created during requirement analysis. Do not create a different branch.\n"
                + "3. Update only `meta.md` and `requirement.md`, then call `mcp__reqflow.save_requirement_package` with the generation action token in `arguments.actionToken`.\n"
                + "4. Do not call `mcp__reqflow.upload_requirement_assessment`, write `plan.md`, implement code, or write execution/review reports during requirement generation.\n\n"
                + "## Requirement Development Workflow\n\n"
                + "1. Read the confirmed requirement design and task branch from reqflow, then switch to the task branch created during requirement design. Do not create a different branch.\n"
                + "2. Before writing `plan.md`, decide whether the work can be split across multiple subagents. Split only when boundaries are clear, shared state is avoided, and each subtask can be independently verified; otherwise keep a single execution path.\n"
                + "3. Generate or update `plan.md`, execute the work, and upload the development plan with `mcp__reqflow.save_development_plan`.\n"
                + "4. Upload the execution result with `mcp__reqflow.upload_execution_report`.\n"
                + "5. Run automatic review before moving the demand to acceptance, then upload the review result with `mcp__reqflow.upload_review_report`.\n"
                + "6. Use the same development-stage action token for the three development tools; put it in `arguments.actionToken`. The token is valid only while the demand stays in the current development stage, with a 24-hour maximum fallback. Personnel authentication still uses `X-MCP-Key`.\n\n"
                + "## Requirement Repair Workflow\n\n"
                + "1. Read the current repair instruction, review report, execution report, confirmed requirement design, and task branch from reqflow.\n"
                + "2. Switch to the existing task branch. Do not create a different branch and do not regenerate `requirement.md` or `plan.md`.\n"
                + "3. Fix only the listed RF items or demander repair notes, then update the same `execution-report.md` and upload it with `mcp__reqflow.upload_execution_report`.\n"
                + "4. Run or hand off re-review, update the same `review-report.md`, and upload it with `mcp__reqflow.upload_review_report`.\n"
                + "5. Use the same repair-stage action token for the two repair tools; put it in `arguments.actionToken`. The token is valid only while the demand stays in the current repair stage, with a 24-hour maximum fallback. Personnel authentication still uses `X-MCP-Key`.\n\n"
                + "## Requirement Closeout Workflow\n\n"
                + "1. Read the accepted demand, baseline branch, task branch, repository list, and closeout action tokens from reqflow.\n"
                + "2. In each repository, switch to the demand baseline branch, run `git pull --ff-only`, squash merge the local task branch into the baseline branch, and push the baseline branch.\n"
                + "3. Publish the current full repository index snapshot with `mcp__reqflow.publish_repository_index`; put the repository's closeout action token in `arguments.actionToken`, never as `X-MCP-Key`.\n"
                + "4. Wait for reqflow to verify that all active repositories on the demand baseline branch have fresh imported index batches and that the closeout tokens were used.\n"
                + "5. After platform verification passes, delete the local task branch. Do not mark the demand complete before platform verification succeeds.\n\n"
                + "## Guardrails\n\n"
                + "- The reqflow MCP server stores platform context and repository indexes; the local agent writes, commits, and pushes files in the target workspace.\n"
                + "- For multi-repository workspaces, process each registered repository separately.\n"
                + "- Do not publish a repository-level overview as the only module. Project onboarding must persist a concrete business knowledge base derived from frontend pages or equivalent backend capabilities.\n"
                + "- If `publish_repository_index` reports missing index tables, surface the platform migration hint to the user instead of retrying blindly.\n";
    }
}
