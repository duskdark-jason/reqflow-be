package com.ruoyi.requirement.mcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqMemoryIndex;
import com.ruoyi.requirement.domain.ReqPackageVersion;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqIndexImpactPayload;
import com.ruoyi.requirement.dto.ReqIndexModulePayload;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqMemoryIndexMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.IReqDemandService;
import com.ruoyi.requirement.service.IReqPackageService;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class McpService
{
    private static final String HARNESS_TEMPLATE_ROOT = "harness-template/";
    private static final String HARNESS_TEMPLATE_FILE_INDEX = HARNESS_TEMPLATE_ROOT + "files.txt";

    @Autowired private ReqDemandMapper reqDemandMapper;
    @Autowired private ReqProjectMapper projectMapper;
    @Autowired private ReqRepositoryMapper reqRepositoryMapper;
    @Autowired private ReqVariantMapper variantMapper;
    @Autowired private ReqMemoryIndexMapper memoryIndexMapper;
    @Autowired private IReqDemandService reqDemandService;
    @Autowired private IReqPackageService reqPackageService;
    @Autowired private IReqRepositoryIndexService repositoryIndexService;
    @Autowired private ReqActivityLogService activityLogService;
    @Autowired private IReqActionTokenService actionTokenService;

    public McpResponse handle(McpRequest request)
    {
        if (request == null)
        {
            return McpResponse.error(null, "MCP请求不能为空");
        }
        try
        {
            if ("initialize".equals(request.getMethod())) return McpResponse.success(request.getId(), initialize(request));
            if ("notifications/initialized".equals(request.getMethod())) return McpResponse.success(request.getId(), Collections.emptyMap());
            if ("ping".equals(request.getMethod())) return McpResponse.success(request.getId(), Collections.emptyMap());
            if ("resources/list".equals(request.getMethod())) return McpResponse.success(request.getId(), resourcesList());
            if ("resources/read".equals(request.getMethod())) return McpResponse.success(request.getId(), resourcesRead(stringParam(request, "uri")));
            if ("resources/templates/list".equals(request.getMethod())) return McpResponse.success(request.getId(), resourceTemplatesList());
            if ("prompts/list".equals(request.getMethod())) return McpResponse.success(request.getId(), Collections.singletonMap("prompts", Arrays.asList(
                    prompt("generate_requirement_design", "生成需求设计"),
                    prompt("generate_development_plan", "生成执行计划"),
                    prompt("generate_execution_prompt", "生成执行提示"),
                    prompt("generate_review_prompt", "生成 Review 提示"))));
            if ("prompts/get".equals(request.getMethod())) return McpResponse.success(request.getId(), promptsGet(stringParam(request, "name")));
            if ("tools/list".equals(request.getMethod())) return McpResponse.success(request.getId(), toolsList());
            if ("tools/call".equals(request.getMethod())) return McpResponse.success(request.getId(), toolsCallResult(request));
            return McpResponse.methodNotFound(request.getId(), "不支持的MCP方法：" + request.getMethod());
        }
        catch (Exception e)
        {
            return McpResponse.error(request.getId(), e.getMessage());
        }
    }

    private Map<String, Object> toolsCallResult(McpRequest request)
    {
        try
        {
            return toolsCall(request);
        }
        catch (Exception e)
        {
            return toolError(e.getMessage());
        }
    }

    private Map<String, Object> initialize(McpRequest request)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", negotiatedProtocolVersion(stringParam(request, "protocolVersion")));
        result.put("capabilities", serverCapabilities());

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", "reqflow");
        serverInfo.put("title", "统一需求流转平台 MCP 服务");
        serverInfo.put("version", "2026-06-10");
        serverInfo.put("description", "提供需求平台上下文、项目接入初始化和仓库索引发布能力。");
        result.put("serverInfo", serverInfo);
        result.put("instructions", "通过 tools/list 发现能力；项目接入初始化使用 publish_repository_index 发布仓库索引。");
        return result;
    }

    private String negotiatedProtocolVersion(String requested)
    {
        if (requested != null && requested.startsWith("2025-"))
        {
            return requested;
        }
        return "2025-11-25";
    }

    private Map<String, Object> serverCapabilities()
    {
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("resources", capability("listChanged", true));
        capabilities.put("tools", capability("listChanged", true));
        capabilities.put("prompts", capability("listChanged", true));
        return capabilities;
    }

    private Map<String, Object> capability(String key, Object value)
    {
        Map<String, Object> capability = new LinkedHashMap<>();
        capability.put(key, value);
        return capability;
    }

    private Map<String, Object> resourcesList()
    {
        return Collections.singletonMap("resources", Arrays.asList(
                resource("requirement://{demandNo}", "需求详情"),
                resource("requirement://{demandNo}/draft-package", "需求草稿包"),
                resource("requirement://{demandNo}/context-manifest", "需求上下文清单"),
                resource("project://{projectId}/overview", "项目概览"),
                resource("project://{projectId}/repositories", "项目仓库清单"),
                resource("variant://{variantId}/overview", "项目分支概览"),
                resource("variant://{variantId}/branch-policy", "项目分支策略"),
                resource("memory://{projectId}/modules?variantId={variantId}", "分支模块知识库"),
                resource("memory://{projectId}/contracts?variantId={variantId}", "分支接口契约知识库"),
                resource("memory://{projectId}/decisions?variantId={variantId}", "分支决策知识库"),
                resource("memory://{projectId}/runbooks?variantId={variantId}", "分支运行手册知识库"),
                resource("memory://{projectId}/specs/done?variantId={variantId}", "分支已完成需求知识库"),
                resource("skill://reqflow/project-init", "Reqflow 项目接入初始化技能"),
                resource("workspace://{projectId}/agents", "工作空间AGENTS")));
    }

    private Map<String, Object> resourceTemplatesList()
    {
        return Collections.singletonMap("resourceTemplates", Arrays.asList(
                resourceTemplate("requirement://{demandNo}", "需求详情", "按稳定需求编号读取需求详情"),
                resourceTemplate("requirement://{demandNo}/draft-package", "需求草稿包", "读取最新需求草稿包"),
                resourceTemplate("requirement://{demandNo}/context-manifest", "上下文清单", "读取最新需求上下文清单"),
                resourceTemplate("project://{projectId}/overview", "项目概览", "读取项目、仓库和项目分支"),
                resourceTemplate("project://{projectId}/repositories", "项目仓库清单", "读取项目下代码仓库"),
                resourceTemplate("variant://{variantId}/overview", "项目分支概览", "读取项目分支详情"),
                resourceTemplate("variant://{variantId}/branch-policy", "项目分支策略", "读取项目分支策略"),
                resourceTemplate("memory://{projectId}/modules?variantId={variantId}", "分支模块知识库", "读取指定项目分支的模块知识"),
                resourceTemplate("memory://{projectId}/contracts?variantId={variantId}", "分支接口契约知识库", "读取指定项目分支的接口契约"),
                resourceTemplate("memory://{projectId}/decisions?variantId={variantId}", "分支决策知识库", "读取指定项目分支的决策记录"),
                resourceTemplate("memory://{projectId}/runbooks?variantId={variantId}", "分支运行手册知识库", "读取指定项目分支的运行手册"),
                resourceTemplate("memory://{projectId}/specs/done?variantId={variantId}", "分支已完成需求知识库", "读取指定项目分支的已完成需求"),
                resourceTemplate("skill://reqflow/project-init", "Reqflow 项目接入初始化技能", "读取 agent 识别 reqflow MCP 初始化流程的技能说明"),
                resourceTemplate("workspace://{projectId}/agents", "工作空间AGENTS", "生成工作空间 AGENTS 内容")));
    }

    private Map<String, Object> resourcesRead(String uri)
    {
        Object content;
        if (uri != null && uri.startsWith("requirement://"))
        {
            content = readRequirementResource(uri);
        }
        else if (uri != null && uri.startsWith("project://"))
        {
            content = readProjectResource(uri);
        }
        else if (uri != null && uri.startsWith("variant://"))
        {
            content = readVariantResource(uri);
        }
        else if (uri != null && uri.startsWith("memory://"))
        {
            content = readMemoryResource(uri);
        }
        else if (uri != null && uri.startsWith("workspace://"))
        {
            content = readWorkspaceResource(uri);
        }
        else if ("skill://reqflow/project-init".equals(uri))
        {
            content = reqflowProjectInitSkillContent();
        }
        else
        {
            content = "resource not materialized in MVP-lite";
        }
        return resourceReadResult(uri, content);
    }

    private Object readRequirementResource(String uri)
    {
        String body = uri.substring("requirement://".length());
        String demandNo = body.contains("/") ? body.substring(0, body.indexOf('/')) : body;
        String path = body.contains("/") ? body.substring(body.indexOf('/') + 1) : "";
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandNo(demandNo);
        if (demand == null)
        {
            return null;
        }
        reqDemandService.validateDemandReadable(demand.getDemandId());
        activityLogService.record(currentUserId(), demand.getProjectId(), demand.getDemandId(), "mcp_read", "mcp", "读取需求资源：" + demandNo, null);

        String artifactType = requirementArtifactType(path);
        if (artifactType == null)
        {
            return demand;
        }
        ReqPackageVersion packageVersion = reqPackageService.selectLatest(demand.getDemandId(), artifactType);
        Map<String, Object> content = new HashMap<>();
        content.put("demand", demand);
        content.put("artifactType", artifactType);
        content.put("packageVersion", packageVersion);
        return content;
    }

    private Map<String, Object> readProjectResource(String uri)
    {
        String body = stripQuery(uri.substring("project://".length()));
        Long projectId = leadingLong(body);
        String path = pathAfterLeadingId(body);
        ReqProject project = projectMapper.selectReqProjectByProjectId(projectId);

        Map<String, Object> content = new HashMap<>();
        content.put("project", project);
        content.put("repositories", repositories(projectId));
        if (!"repositories".equals(path))
        {
            content.put("variants", variants(projectId));
        }
        return content;
    }

    private Map<String, Object> readVariantResource(String uri)
    {
        String body = stripQuery(uri.substring("variant://".length()));
        Long variantId = leadingLong(body);
        String path = pathAfterLeadingId(body);
        ReqVariant variant = variantMapper.selectReqVariantByVariantId(variantId);

        Map<String, Object> content = new HashMap<>();
        content.put("variant", variant);
        if ("branch-policy".equals(path) && variant != null)
        {
            content.put("branchPolicy", variant.getBranchPolicy());
        }
        return content;
    }

    private Map<String, Object> readMemoryResource(String uri)
    {
        String body = uri.substring("memory://".length());
        String pathBody = stripQuery(body);
        Long projectId = leadingLong(pathBody);
        String memoryPath = pathAfterLeadingId(pathBody);

        ReqMemoryIndex query = new ReqMemoryIndex();
        query.setProjectId(projectId);
        query.setVariantId(longQueryParam(uri, "variantId"));
        query.setDocType(memoryDocType(memoryPath));

        Map<String, Object> content = new HashMap<>();
        content.put("projectId", projectId);
        content.put("variantId", query.getVariantId());
        content.put("docType", query.getDocType());
        content.put("documents", memoryIndexMapper.selectReqMemoryIndexList(query));
        return content;
    }

    private Map<String, Object> readWorkspaceResource(String uri)
    {
        String body = stripQuery(uri.substring("workspace://".length()));
        Long projectId = leadingLong(body);
        ReqProject project = projectMapper.selectReqProjectByProjectId(projectId);
        List<ReqRepository> repositories = repositories(projectId);
        List<ReqVariant> variants = variants(projectId);

        Map<String, Object> content = new HashMap<>();
        content.put("project", project);
        content.put("repositories", repositories);
        content.put("variants", variants);
        content.put("agents", workspaceAgentsContent(project, repositories, variants));
        return content;
    }

    private List<ReqRepository> repositories(Long projectId)
    {
        ReqRepository query = new ReqRepository();
        query.setProjectId(projectId);
        return reqRepositoryMapper.selectReqRepositoryList(query);
    }

    private List<ReqVariant> variants(Long projectId)
    {
        ReqVariant query = new ReqVariant();
        query.setProjectId(projectId);
        return variantMapper.selectReqVariantList(query);
    }

    private String workspaceAgentsContent(ReqProject project, List<ReqRepository> repositories, List<ReqVariant> variants)
    {
        String content = readTemplateResource("WORKSPACE_AGENTS.snippet.md");
        // workspace AGENTS 只负责入口导航，仓库清单必须来自平台登记信息，避免初始化 agent 按本机目录猜测仓库结构。
        content = content.replaceAll("(?s)```text\\n.*?```",
                java.util.regex.Matcher.quoteReplacement("```text\n" + workspaceRepositoryBlock(repositories) + "```"));
        content = removeTemplatePlaceholders(content);
        StringBuilder header = new StringBuilder();
        header.append("# AGENTS.md\n\n");
        header.append("## 项目信息\n");
        if (project != null)
        {
            header.append("- 项目：").append(project.getProjectName()).append("（").append(project.getProjectCode()).append("）\n");
        }
        header.append("- 仓库数量：").append(repositories == null ? 0 : repositories.size()).append("\n");
        header.append("- 分支数量：").append(variants == null ? 0 : variants.size()).append("\n\n");
        header.append("## 分支知识库\n");
        header.append("- 读取知识库时必须同时传入 projectId 与 variantId，避免不同分支的独有模块互相污染。\n");
        header.append("- 分支初始化、模块索引和需求影响推荐均以项目分支为粒度。\n\n");
        String body = content.replaceFirst("^# AGENTS\\.md 工作空间入口\\n\\n", "");
        return header.append(body).toString();
    }

    private String workspaceRepositoryBlock(List<ReqRepository> repositories)
    {
        StringBuilder builder = new StringBuilder();
        for (ReqRepository repository : safeList(repositories))
        {
            builder.append(firstNotEmpty(repository.getRepoName(), "repository"))
                    .append("/   # ")
                    .append(firstNotEmpty(repository.getRepoType(), "UNKNOWN"))
                    .append("，默认分支 ")
                    .append(firstNotEmpty(repository.getDefaultBranch(), "main"))
                    .append("\n");
        }
        if (builder.length() == 0)
        {
            builder.append("repository/   # 待平台配置仓库\n");
        }
        return builder.toString();
    }

    private Map<String, Object> promptsGet(String name)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", "需求平台MVP-lite提示词：" + name);
        result.put("messages", Arrays.asList(promptMessage("请按需求平台上下文执行：" + name)));
        return result;
    }

    private Map<String, Object> toolsList()
    {
        return Collections.singletonMap("tools", Arrays.asList(
                tool("save_requirement_package", "保存需求设计", packageToolSchema(true)),
                tool("save_development_plan", "保存执行计划", packageToolSchema(false)),
                tool("upload_execution_report", "上传执行报告", packageToolSchema(false)),
                tool("upload_review_report", "上传 Review 报告", packageToolSchema(false)),
                tool("register_harness_init_result", "登记项目 harness 初始化结果", registerHarnessSchema()),
                tool("get_harness_template", "读取项目 harness 初始化模板包", getHarnessTemplateSchema()),
                tool("publish_repository_index", "发布当前仓库索引到需求平台项目分支知识库", publishRepositoryIndexSchema())));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toolsCall(McpRequest request)
    {
        String name = stringParam(request, "name");
        Map<String, Object> arguments = request.getParams() == null ? Collections.emptyMap() : (Map<String, Object>) request.getParams().getOrDefault("arguments", Collections.emptyMap());
        if ("get_harness_template".equals(name))
        {
            // 模板读取是只读能力，只允许具备项目查看权限的用户拿到初始化包，不在平台侧替用户写仓库文件。
            requirePermission("get_harness_template", "req:project:query");
            return toolResult(getHarnessTemplate(longArg(arguments, "projectId")));
        }
        if ("register_harness_init_result".equals(name))
        {
            requirePermission("register_harness_init_result", "req:package:save");
            ReqRepository repository = new ReqRepository();
            repository.setRepoId(longArg(arguments, "repoId"));
            repository.setHarnessStatus(stringArg(arguments, "harnessStatus"));
            repository.setHarnessCommit(stringArg(arguments, "harnessCommit"));
            repository.setUpdateBy(currentUsername());
            int rows = reqRepositoryMapper.updateHarnessInitResult(repository);
            activityLogService.record(currentUserId(), null, null, "harness_init_registered", "mcp", "登记Harness初始化结果", null);
            return toolResult(Collections.singletonMap("updated", rows));
        }
        if ("publish_repository_index".equals(name))
        {
            // 仓库索引发布会写入模块/影响面知识库，权限必须独立于普通文档保存能力校验。
            requirePermission("publish_repository_index", "req:index:import");
            return toolResult(Collections.singletonMap("result", repositoryIndexService.importRepositoryIndex(toIndexRequest(arguments), "mcp", currentUsername(), currentUserId())));
        }
        requirePermission(name, "req:package:save");
        Long demandId = resolvePackageDemandId(name, arguments);
        String artifactType = artifactTypeForTool(name, stringArg(arguments, "artifactType"));
        return toolResult(Collections.singletonMap("version", reqPackageService.saveVersion(demandId, artifactType, stringArg(arguments, "content"), name)));
    }

    private ReqRepositoryIndexImportRequest toIndexRequest(Map<String, Object> arguments)
    {
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        // actionToken 是新的安全入口；projectId/repoId/mcpKey 保留为兼容字段，由服务层统一解析归属。
        request.setProjectId(longArg(arguments, "projectId"));
        request.setRepoId(longArg(arguments, "repoId"));
        request.setMcpKey(stringArg(arguments, "mcpKey"));
        request.setActionToken(stringArg(arguments, "actionToken"));
        request.setRepoType(stringArg(arguments, "repoType"));
        request.setRemoteUrl(stringArg(arguments, "remoteUrl"));
        request.setBranchName(stringArg(arguments, "branchName"));
        request.setCommitHash(stringArg(arguments, "commitHash"));
        request.setIndexVersion(stringArg(arguments, "indexVersion"));
        request.setModules(moduleListArg(arguments, "modules"));
        request.setPages(impactListArg(arguments, "pages"));
        request.setApis(impactListArg(arguments, "apis"));
        request.setTables(impactListArg(arguments, "tables"));
        request.setPermissions(impactListArg(arguments, "permissions"));
        request.setDocuments(impactListArg(arguments, "documents"));
        return request;
    }

    private Map<String, Object> getHarnessTemplate(Long projectId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("项目不能为空");
        }
        ReqProject project = projectMapper.selectReqProjectByProjectId(projectId);
        if (project == null)
        {
            throw new IllegalArgumentException("项目不存在");
        }
        List<ReqRepository> repositories = repositories(projectId);
        List<ReqVariant> variants = variants(projectId);
        List<Map<String, Object>> repositoryInstructions = new ArrayList<>();
        // 返回 workspace 入口和每个仓库的文件包，调用方负责在真实工作空间合并，平台不执行 shell 或 Git 操作。
        for (ReqRepository repository : repositories)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("repository", repository);
            item.put("content", harnessInstructionContent(repository, variants));
            item.put("files", repositoryHarnessFiles(repository, variants));
            repositoryInstructions.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("repositories", repositories);
        result.put("variants", variants);
        result.put("reqflowMcpSkill", reqflowProjectInitSkillContent());
        result.put("workspaceAgents", workspaceAgentsContent(project, repositories, variants));
        result.put("workspaceFiles", workspaceHarnessFiles(project, repositories, variants));
        result.put("repositoryHarnessInstructions", repositoryInstructions);
        return result;
    }

    private String harnessInstructionContent(ReqRepository repository, List<ReqVariant> variants)
    {
        StringBuilder content = new StringBuilder();
        content.append("# Harness 初始化指令\n\n");
        content.append("## 目标仓库\n");
        content.append("- 仓库：").append(repository.getRepoName()).append("\n");
        content.append("- 类型：").append(repository.getRepoType()).append("\n");
        content.append("- 远端：").append(repository.getRepoUrl()).append("\n");
        content.append("- 默认分支：").append(repository.getDefaultBranch()).append("\n\n");
        content.append("## 项目分支\n");
        for (ReqVariant variant : safeList(variants))
        {
            content.append("- ").append(variant.getVariantName()).append("：").append(variant.getBaselineBranch()).append("\n");
        }
        content.append("\n## 初始化要求\n");
        content.append("1. 进入目标仓库后先校验远端和当前分支。\n");
        content.append("2. 下发或更新仓库 AGENTS.md、docs/ai-harness、docs/process、docs/templates 和 scripts。\n");
        content.append("3. 先分析前端路由、菜单、页面组件和 API 封装，按菜单目录、子菜单、隐藏页签或页面业务功能生成模块知识库；纯后端仓库按 companion 前端菜单、MCP 能力或后台任务生成模块。\n");
        content.append("4. 运行 sh scripts/check-docs.sh 和 sh scripts/check-harness.sh init。\n");
        content.append("5. 通过 mcp__reqflow.publish_repository_index 发布结构化索引，modules 必须是一行一个前端页面业务功能或后端主能力，pages/apis/permissions/tables/documents 通过 moduleCode 归属。\n");
        content.append("6. 通过 mcp__reqflow.register_harness_init_result 回写初始化结果。\n");
        return content.toString();
    }

    private List<Map<String, Object>> workspaceHarnessFiles(ReqProject project, List<ReqRepository> repositories, List<ReqVariant> variants)
    {
        return Collections.singletonList(harnessFile("AGENTS.md", workspaceAgentsContent(project, repositories, variants), "merge-if-exists"));
    }

    private List<Map<String, Object>> repositoryHarnessFiles(ReqRepository repository, List<ReqVariant> variants)
    {
        String slug = repositorySlug(repository);
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(harnessFile("AGENTS.md", repositoryAgentsContent(repository), "merge-if-exists"));
        for (String path : harnessTemplateFilePaths())
        {
            // 初始化包只下发 docs/ 与 scripts/，避免把模板根目录的说明文件覆盖到业务仓库。
            if (!path.startsWith("docs/") && !path.startsWith("scripts/"))
            {
                continue;
            }
            if ("docs/ai-harness/harness-index.json".equals(path))
            {
                // harness-index 必须按目标仓库重写 template/initialized/远端/分支信息，不能直接复制模板索引。
                files.add(harnessFile(path, repositoryHarnessIndexContent(repository, variants), "create-or-merge"));
                continue;
            }
            files.add(harnessFile(path, repositoryTemplateContent(path), templateWriteMode(path)));
        }
        files.add(harnessFile("docs/ai-harness/modules/" + slug + "-page-functions.md", repositoryModuleContent(repository), "create-if-missing"));
        return files;
    }

    private Map<String, Object> harnessFile(String path, String content, String writeMode)
    {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("path", path);
        file.put("writeMode", writeMode);
        file.put("content", content);
        return file;
    }

    private String repositorySlug(ReqRepository repository)
    {
        String name = repository == null ? "" : firstNotEmpty(repository.getRepoName(), repository.getRepoType(), "repository");
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "repository" : slug;
    }

    private String repositoryAgentsContent(ReqRepository repository)
    {
        String content = readTemplateResource("AGENTS.snippet.md");
        content = content.replace("【用 3 到 5 句话说明项目类型、主要用户、核心业务和技术栈。】",
                "本仓库已接入需求平台 harness，由需求平台下发流程、模板和检查脚本。"
                        + "当前仓库类型为 " + firstNotEmpty(repository.getRepoType(), "UNKNOWN")
                        + "，默认分支为 " + firstNotEmpty(repository.getDefaultBranch(), "main")
                        + "。请以平台登记远端和当前仓库事实为准补充业务上下文。");
        content = content.replace("`【构建文件路径，如 pom.xml / package.json / pyproject.toml】`", "按仓库实际构建文件补充");
        content = content.replace("`【配置文件路径】`", "按仓库实际配置文件补充");
        content = removeTemplatePlaceholders(content);
        return content
                + "\n## Reqflow MCP 项目接入初始化\n\n"
                + reqflowProjectInitSkillContent()
                + "\n## 当前仓库\n\n"
                + "- 仓库：" + firstNotEmpty(repository.getRepoName(), "未命名仓库") + "\n"
                + "- 类型：" + firstNotEmpty(repository.getRepoType(), "UNKNOWN") + "\n"
                + "- 远端：" + firstNotEmpty(repository.getRepoUrl(), "未配置") + "\n"
                + "- 默认分支：" + firstNotEmpty(repository.getDefaultBranch(), "main") + "\n";
    }

    private List<String> harnessTemplateFilePaths()
    {
        String index = readTemplateResource(HARNESS_TEMPLATE_FILE_INDEX);
        List<String> paths = new ArrayList<>();
        for (String line : index.split("\\R"))
        {
            String path = line.trim();
            if (!path.isEmpty() && !path.startsWith("#"))
            {
                paths.add(path);
            }
        }
        return paths;
    }

    private String repositoryTemplateContent(String path)
    {
        String content = readTemplateResource(HARNESS_TEMPLATE_ROOT + path);
        if (isRuntimeInitializedDoc(path))
        {
            // 初始化后的运行文档要去掉占位符；模板目录自身仍保留占位符，供后续需求复制填写。
            return removeTemplatePlaceholders(content);
        }
        return content;
    }

    private boolean isRuntimeInitializedDoc(String path)
    {
        return !path.contains("/templates/")
                && !"docs/runbooks/local-run-template.md".equals(path)
                && (path.endsWith(".md") || path.endsWith(".json"));
    }

    private String templateWriteMode(String path)
    {
        if (path.endsWith("/.gitkeep") || path.endsWith(".gitkeep"))
        {
            return "create-if-missing";
        }
        return "create-or-merge";
    }

    private String removeTemplatePlaceholders(String content)
    {
        return content == null ? "" : content.replaceAll("【[^】]+】", "按项目实际补充");
    }

    private String repositoryHarnessIndexContent(ReqRepository repository, List<ReqVariant> variants)
    {
        List<ReqVariant> safeVariants = safeList(variants);
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"schemaVersion\": 1,\n");
        builder.append("  \"template\": false,\n");
        builder.append("  \"harnessVersion\": \"2026-06-09\",\n");
        builder.append("  \"initialized\": true,\n");
        builder.append("  \"repository\": {\n");
        builder.append("    \"name\": \"").append(escapeJson(firstNotEmpty(repository.getRepoName(), ""))).append("\",\n");
        builder.append("    \"role\": \"").append(escapeJson(firstNotEmpty(repository.getRepoType(), "repository"))).append("\",\n");
        builder.append("    \"workspace\": \"\",\n");
        builder.append("    \"remoteUrl\": \"").append(escapeJson(firstNotEmpty(repository.getRepoUrl(), ""))).append("\",\n");
        builder.append("    \"companionRepositories\": []\n");
        builder.append("  },\n");
        builder.append("  \"entrypoints\": {\n");
        builder.append("    \"workflow\": \"docs/process/agent-workflow.md\",\n");
        builder.append("    \"platformKeyWorkflow\": \"docs/process/platform-key-workflow.md\",\n");
        builder.append("    \"verification\": \"docs/ai-harness/verification.md\",\n");
        builder.append("    \"localRun\": \"docs/runbooks/local-run-template.md\",\n");
        builder.append("    \"localRunTemplate\": \"docs/runbooks/local-run-template.md\",\n");
        builder.append("    \"localRunDetected\": \"docs/runbooks/local-run.detected.md\",\n");
        builder.append("    \"localRunConfirmed\": \"docs/runbooks/local-run.md\",\n");
        builder.append("    \"activeSpecs\": \"docs/specs/active\"\n");
        builder.append("  },\n");
        builder.append("  \"commands\": {\n");
        builder.append("    \"init\": \"sh scripts/check-docs.sh && sh scripts/check-harness.sh init\",\n");
        builder.append("    \"review\": \"sh scripts/check-harness.sh review\",\n");
        builder.append("    \"complete\": \"sh scripts/check-harness.sh complete\"\n");
        builder.append("  },\n");
        builder.append("  \"customization\": {\n");
        builder.append("    \"customerBranches\": [],\n");
        builder.append("    \"taskBranchPrefix\": \"feature\"\n");
        builder.append("  },\n");
        builder.append("  \"variants\": [");
        for (int i = 0; i < safeVariants.size(); i++)
        {
            ReqVariant variant = safeVariants.get(i);
            if (i > 0) builder.append(", ");
            builder.append("{\"variantId\": ").append(variant.getVariantId() == null ? "null" : variant.getVariantId())
                    .append(", \"baselineBranch\": \"").append(escapeJson(firstNotEmpty(variant.getBaselineBranch(), ""))).append("\"}");
        }
        builder.append("]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private String repositoryModuleContent(ReqRepository repository)
    {
        String repoName = firstNotEmpty(repository.getRepoName(), "仓库");
        String repoType = firstNotEmpty(repository.getRepoType(), "UNKNOWN");
        // 模块骨架必须是非模板文件，确保项目初始化后至少有一个可继续细化的模块知识库入口。
        return "# " + repoName + "前端页面功能索引\n\n"
                + "## 业务目的\n\n"
                + "本文件是项目接入初始化阶段生成的非模板模块知识库骨架。初始化 agent 必须先扫描当前仓库的前端路由、菜单配置、页面组件和 API 封装，"
                + "再把每个菜单目录、子菜单、隐藏页签或页面业务功能沉淀为具体模块；纯后端仓库应按 companion 前端菜单、MCP 能力或后台任务建立同等粒度的业务模块。\n\n"
                + "## 初始化分析入口\n\n"
                + "| 类型 | 优先扫描路径 | 沉淀要求 |\n"
                + "|---|---|---|\n"
                + "| 仓库信息 | `" + repoName + "` / `" + repoType + "` | 只作为范围说明，不得把整个仓库当成唯一业务模块。 |\n"
                + "| 前端路由与菜单 | `src/router/**`、`src/store/modules/permission.*`、后端菜单 SQL 或动态路由接口 | 提取一级菜单、子菜单、隐藏页签和权限标识。 |\n"
                + "| 页面组件 | `src/views/**`、`pages/**`、`app/**` | 一行一个可被用户理解的页面业务功能，记录页面文件和关键用户动作。 |\n"
                + "| API 封装 | `src/api/**`、请求 hooks 或 service 文件 | 记录页面功能调用的接口路径、方法和状态字段。 |\n"
                + "| 后端主能力 | Controller、MCP tool、后台任务、Mapper | 无前端页面时，说明它服务的菜单、隐藏页签、MCP 能力或后台流程。 |\n\n"
                + "## 菜单与功能入口\n\n"
                + "| 菜单目录 | 子菜单/页面 | 功能说明 | 前端文件 | API 封装 | 后端接口与权限 | 后端核心文件 |\n"
                + "|---|---|---|---|---|---|---|\n"
                + "| 初始化待补齐 | 初始化待补齐 | 初始化 agent 发布 `publish_repository_index` 前必须替换为真实页面业务功能。 | `初始化待补齐` | `初始化待补齐` | `初始化待补齐` | `初始化待补齐` |\n\n"
                + "## 发布索引要求\n\n"
                + "- `publish_repository_index.modules` 必须按前端页面业务功能或后端主能力生成，不能只提交仓库概览、技术层目录或空数组。\n"
                + "- `moduleCode` 使用稳定 ASCII 编码，`moduleName` 使用业务中文名称，`moduleType` 推荐使用 `PAGE_FUNCTION`、`BUSINESS` 或 `BACKEND_CAPABILITY`。\n"
                + "- `pages`、`apis`、`tables`、`permissions` 和 `documents` 必须通过 `moduleCode` 归属到对应业务模块，并只写相对路径或结构化标识。\n";
    }

    private String reqflowProjectInitSkillContent()
    {
        return "# Reqflow MCP 项目接入初始化技能\n\n"
                + "## 触发条件\n\n"
                + "- 用户要求项目接入初始化、harness 初始化或发布项目索引。\n"
                + "- 指令中出现 `actionToken`、`mcpServer: reqflow`、`mcpTool: reqflow.publish_repository_index` 或 `publish_repository_index`。\n\n"
                + "## 必须使用的 MCP 工具\n\n"
                + "- 读取模板：`mcp__reqflow.get_harness_template`。\n"
                + "- 发布索引：`mcp__reqflow.publish_repository_index`。\n"
                + "- 回写结果：`mcp__reqflow.register_harness_init_result`。\n\n"
                + "## 执行顺序\n\n"
                + "1. 确认当前会话已加载 reqflow MCP server，且工具名是 `mcp__reqflow.publish_repository_index`。\n"
                + "2. 调用 `get_harness_template` 获取 `workspaceFiles` 和 `repositoryHarnessInstructions[].files`。\n"
                + "3. 在目标 workspace 写入或合并本地 harness 文件，不允许只调用发布索引工具。\n"
                + "4. 在每个子仓库运行 `sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh init`。\n"
                + "5. 发布索引前，先扫描前端路由、菜单、页面组件和 API 封装，按菜单目录、子菜单、隐藏页签或前端页面业务功能生成 `modules`；纯后端仓库按 companion 前端菜单、MCP 能力或后台任务生成模块。\n"
                + "6. 调用 `publish_repository_index`，`modules` 不能为空，且必须是一行一个前端页面业务功能或后端主能力；`pages/apis/tables/permissions/documents` 通过 `moduleCode` 归属。`actionToken` 必须作为 `arguments.actionToken`，不能作为 `X-MCP-Key`。\n"
                + "7. 调用 `register_harness_init_result` 回写 harness 初始化状态和 commit。\n"
                + "8. 多仓 workspace 必须分别处理 BACKEND 和 FRONTEND 仓库，不能用一个仓库的索引代替另一个仓库。\n";
    }

    private String escapeJson(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String readTemplateResource(String path)
    {
        String resourcePath = path.startsWith(HARNESS_TEMPLATE_ROOT) ? path : HARNESS_TEMPLATE_ROOT + path;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalStateException("Harness 模板资源不存在：" + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("读取 Harness 模板资源失败：" + resourcePath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ReqIndexModulePayload> moduleListArg(Map<String, Object> arguments, String key)
    {
        Object value = arguments.get(key);
        if (!(value instanceof List)) return Collections.emptyList();
        List<ReqIndexModulePayload> result = new ArrayList<>();
        for (Object item : (List<Object>) value)
        {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            ReqIndexModulePayload payload = new ReqIndexModulePayload();
            payload.setVariantId(longArg(map, "variantId"));
            payload.setParentCode(stringArg(map, "parentCode"));
            payload.setModuleCode(stringArg(map, "moduleCode"));
            payload.setModuleName(stringArg(map, "moduleName"));
            payload.setModuleType(stringArg(map, "moduleType"));
            payload.setRepoScope(stringArg(map, "repoScope"));
            payload.setRelativePath(stringArg(map, "relativePath"));
            payload.setSourceRef(stringArg(map, "sourceRef"));
            payload.setSummary(stringArg(map, "summary"));
            payload.setOrderNum(intArg(map, "orderNum"));
            result.add(payload);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ReqIndexImpactPayload> impactListArg(Map<String, Object> arguments, String key)
    {
        Object value = arguments.get(key);
        if (!(value instanceof List)) return Collections.emptyList();
        List<ReqIndexImpactPayload> result = new ArrayList<>();
        for (Object item : (List<Object>) value)
        {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            ReqIndexImpactPayload payload = new ReqIndexImpactPayload();
            payload.setItemType(stringArg(map, "itemType"));
            payload.setModuleCode(stringArg(map, "moduleCode"));
            payload.setModuleId(longArg(map, "moduleId"));
            payload.setVariantId(longArg(map, "variantId"));
            payload.setItemName(stringArg(map, "itemName"));
            payload.setItemKey(stringArg(map, "itemKey"));
            payload.setRelativePath(stringArg(map, "relativePath"));
            payload.setHttpMethod(stringArg(map, "httpMethod"));
            payload.setApiPath(stringArg(map, "apiPath"));
            payload.setPermissionKey(stringArg(map, "permissionKey"));
            payload.setTableName(stringArg(map, "tableName"));
            payload.setSummary(stringArg(map, "summary"));
            payload.setTags(stringArg(map, "tags"));
            result.add(payload);
        }
        return result;
    }

    private void requirePermission(String toolName, String permission)
    {
        if (!hasPermission(permission))
        {
            throw new IllegalArgumentException("调用MCP工具 " + toolName + " 需要权限：" + permission);
        }
    }

    protected boolean hasPermission(String permission)
    {
        try { return SecurityUtils.hasPermi(permission); }
        catch (Exception e) { return false; }
    }

    private String artifactTypeForTool(String toolName, String requested)
    {
        if ("save_requirement_package".equals(toolName)) return requested == null || requested.isEmpty() ? "requirement" : requested;
        if ("save_development_plan".equals(toolName)) return "plan";
        if ("upload_execution_report".equals(toolName)) return "execution_report";
        if ("upload_review_report".equals(toolName)) return "review_report";
        throw new IllegalArgumentException("不支持的MCP工具：" + toolName);
    }

    private Long resolvePackageDemandId(String toolName, Map<String, Object> arguments)
    {
        String actionToken = stringArg(arguments, "actionToken");
        if (actionToken == null || actionToken.isEmpty())
        {
            Long demandId = longArg(arguments, "demandId");
            if (demandId == null)
            {
                throw new IllegalArgumentException("需求ID或actionToken不能为空");
            }
            return demandId;
        }
        ReqActionToken token = actionTokenService.resolveToken(actionToken);
        if (IReqActionTokenService.ACTION_REQUIREMENT_PLAN.equals(token.getActionType()))
        {
            if (!"save_requirement_package".equals(toolName))
            {
                throw new IllegalArgumentException("动作Token不支持当前MCP工具：" + toolName);
            }
            return token.getDemandId();
        }
        if (!toolName.equals(token.getTargetMethod()))
        {
            throw new IllegalArgumentException("动作Token不支持当前MCP工具：" + toolName);
        }
        return token.getDemandId();
    }

    private Map<String, Object> resource(String uri, String name)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("uri", uri);
        map.put("name", name);
        return map;
    }

    private Map<String, Object> resourceTemplate(String uriTemplate, String name, String description)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uriTemplate", uriTemplate);
        map.put("name", name);
        map.put("description", description);
        map.put("mimeType", "application/json");
        return map;
    }

    private Map<String, Object> resourceReadResult(String uri, Object content)
    {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("uri", uri);
        text.put("mimeType", "application/json");
        text.put("text", JSON.toJSONString(content));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contents", Collections.singletonList(text));
        result.put("structuredContent", content);
        return result;
    }

    private Map<String, Object> promptMessage(String text)
    {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", text);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", content);
        return message;
    }

    private Map<String, Object> toolResult(Object structuredContent)
    {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("type", "text");
        text.put("text", JSON.toJSONString(structuredContent));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(text));
        result.put("structuredContent", structuredContent);
        result.put("isError", false);
        return result;
    }

    private Map<String, Object> toolError(String message)
    {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("type", "text");
        text.put("text", message == null || message.isEmpty() ? "MCP工具调用失败" : message);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(text));
        result.put("isError", true);
        return result;
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> inputSchema)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("inputSchema", inputSchema);
        return map;
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required)
    {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (required != null && !required.isEmpty())
        {
            schema.put("required", required);
        }
        schema.put("additionalProperties", true);
        return schema;
    }

    private Map<String, Object> property(String type, String description)
    {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    private Map<String, Object> arrayObjectProperty(String description, Map<String, Object> itemProperties, List<String> required)
    {
        Map<String, Object> property = property("array", description);
        property.put("items", objectSchema(itemProperties, required));
        return property;
    }

    private Map<String, Object> moduleArrayProperty()
    {
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("variantId", property("integer", "项目分支 ID，可省略；省略时服务端按 actionToken、mcpKey 或 branchName 归属"));
        itemProperties.put("parentCode", property("string", "父级模块编码，适用于菜单目录和子页面层级"));
        itemProperties.put("moduleCode", property("string", "稳定 ASCII 模块编码，项目初始化时必填"));
        itemProperties.put("moduleName", property("string", "业务模块中文名称，项目初始化时必填"));
        itemProperties.put("moduleType", property("string", "模块类型，推荐 PAGE_FUNCTION、BUSINESS 或 BACKEND_CAPABILITY"));
        itemProperties.put("repoScope", property("string", "仓库范围，例如 FRONTEND、BACKEND 或 FULLSTACK"));
        itemProperties.put("relativePath", property("string", "模块主入口相对路径，不得使用个人本机绝对路径"));
        itemProperties.put("sourceRef", property("string", "模块来源说明，例如路由、菜单、页面组件或后台任务"));
        itemProperties.put("summary", property("string", "模块业务摘要"));
        itemProperties.put("orderNum", property("integer", "排序号"));
        return arrayObjectProperty("模块或功能点索引列表；项目初始化时不能为空，优先按前端页面业务功能、菜单目录、子菜单或隐藏页签生成，一行代表一个具体业务知识库模块",
                itemProperties, Arrays.asList("moduleCode", "moduleName"));
    }

    private Map<String, Object> impactArrayProperty(String description)
    {
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("moduleCode", property("string", "必须匹配本次 modules[].moduleCode，用于把影响面归属到具体业务知识库模块"));
        itemProperties.put("moduleId", property("integer", "兼容字段：人工模块 ID"));
        itemProperties.put("variantId", property("integer", "项目分支 ID，可省略；省略时服务端按模块或 actionToken 归属"));
        itemProperties.put("itemName", property("string", "影响面名称"));
        itemProperties.put("itemKey", property("string", "影响面稳定标识，例如路由名、组件名或文档标识"));
        itemProperties.put("relativePath", property("string", "相对路径，不得使用个人本机绝对路径"));
        itemProperties.put("httpMethod", property("string", "接口请求方法，接口影响面使用"));
        itemProperties.put("apiPath", property("string", "接口路径，接口影响面使用"));
        itemProperties.put("permissionKey", property("string", "权限标识，权限影响面使用"));
        itemProperties.put("tableName", property("string", "数据表名，数据表影响面使用"));
        itemProperties.put("summary", property("string", "影响面摘要"));
        itemProperties.put("tags", property("string", "标签，逗号分隔"));
        return arrayObjectProperty(description, itemProperties, Collections.singletonList("moduleCode"));
    }

    private Map<String, Object> packageToolSchema(boolean allowArtifactType)
    {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("demandId", property("integer", "需求 ID；未传时可用 actionToken 定位"));
        properties.put("actionToken", property("string", "生成需求设计或执行任务指令中的动作 token，可用于定位需求上下文"));
        properties.put("content", property("string", "要保存的交接资料正文"));
        if (allowArtifactType)
        {
            properties.put("artifactType", property("string", "产物类型，缺省为 requirement"));
        }
        return objectSchema(properties, Collections.singletonList("content"));
    }

    private Map<String, Object> registerHarnessSchema()
    {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("repoId", property("integer", "仓库 ID"));
        properties.put("harnessStatus", property("string", "初始化状态"));
        properties.put("harnessCommit", property("string", "初始化结果 commit"));
        return objectSchema(properties, Arrays.asList("repoId", "harnessStatus"));
    }

    private Map<String, Object> getHarnessTemplateSchema()
    {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("projectId", property("integer", "需求平台项目 ID"));
        return objectSchema(properties, Collections.singletonList("projectId"));
    }

    private Map<String, Object> publishRepositoryIndexSchema()
    {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("actionToken", property("string", "项目分支初始化指令中的动作 token，优先用于定位项目、分支和目标方法"));
        properties.put("remoteUrl", property("string", "当前仓库 Git 远端地址"));
        properties.put("projectId", property("integer", "兼容路径：需求平台项目 ID"));
        properties.put("repoId", property("integer", "兼容路径：需求平台仓库 ID"));
        properties.put("mcpKey", property("string", "兼容路径：项目分支识别 key"));
        properties.put("repoType", property("string", "仓库类型，例如 FRONTEND 或 BACKEND"));
        properties.put("branchName", property("string", "真实 Git 分支名"));
        properties.put("commitHash", property("string", "当前索引 commit"));
        properties.put("indexVersion", property("string", "索引数据格式版本"));
        properties.put("modules", moduleArrayProperty());
        properties.put("pages", impactArrayProperty("页面影响面列表；每项 moduleCode 必须匹配 modules[].moduleCode"));
        properties.put("apis", impactArrayProperty("接口影响面列表；每项 moduleCode 必须匹配 modules[].moduleCode"));
        properties.put("tables", impactArrayProperty("数据表影响面列表；每项 moduleCode 必须匹配 modules[].moduleCode"));
        properties.put("permissions", impactArrayProperty("权限影响面列表；每项 moduleCode 必须匹配 modules[].moduleCode"));
        properties.put("documents", impactArrayProperty("文档影响面列表；每项 moduleCode 必须匹配 modules[].moduleCode"));
        return objectSchema(properties, Arrays.asList("remoteUrl", "commitHash", "indexVersion"));
    }

    private String stripQuery(String text)
    {
        if (text == null) return "";
        int index = text.indexOf('?');
        return index < 0 ? text : text.substring(0, index);
    }

    private Long leadingLong(String text)
    {
        if (text == null || text.isEmpty())
        {
            throw new IllegalArgumentException("资源URI缺少ID");
        }
        int index = text.indexOf('/');
        String value = index < 0 ? text : text.substring(0, index);
        return Long.valueOf(value);
    }

    private String pathAfterLeadingId(String text)
    {
        if (text == null) return "";
        int index = text.indexOf('/');
        return index < 0 || index + 1 >= text.length() ? "" : text.substring(index + 1);
    }

    private Long longQueryParam(String uri, String key)
    {
        String value = queryParam(uri, key);
        return value == null || value.isEmpty() ? null : Long.valueOf(value);
    }

    private String queryParam(String uri, String key)
    {
        if (uri == null) return null;
        int index = uri.indexOf('?');
        if (index < 0 || index + 1 >= uri.length()) return null;
        String[] pairs = uri.substring(index + 1).split("&");
        for (String pair : pairs)
        {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && key.equals(parts[0])) return parts[1];
        }
        return null;
    }

    private String memoryDocType(String path)
    {
        if ("modules".equals(path)) return "module";
        if ("contracts".equals(path)) return "contract";
        if ("decisions".equals(path)) return "decision";
        if ("runbooks".equals(path)) return "runbook";
        if ("specs/done".equals(path)) return "spec";
        if ("domains".equals(path) || "domain".equals(path)) return "domain";
        return path;
    }

    private String requirementArtifactType(String path)
    {
        if ("draft-package".equals(path)) return "requirement_draft";
        if ("context-manifest".equals(path)) return "context_manifest";
        return null;
    }

    private <T> List<T> safeList(List<T> list)
    {
        return list == null ? Collections.emptyList() : list;
    }

    private String firstNotEmpty(String... values)
    {
        if (values == null) return "";
        for (String value : values)
        {
            if (value != null && !value.isEmpty()) return value;
        }
        return "";
    }

    private Map<String, Object> prompt(String name, String description) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("description", description);
        return map;
    }
    private String stringParam(McpRequest request, String key) { return request.getParams() == null || request.getParams().get(key) == null ? null : String.valueOf(request.getParams().get(key)); }
    private String stringArg(Map<String, Object> arguments, String key) { Object value = arguments.get(key); return value == null ? null : String.valueOf(value); }
    private Long longArg(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) return null;
        if (value instanceof Number) return Long.valueOf(((Number) value).longValue());
        return Long.valueOf(String.valueOf(value));
    }
    private Integer intArg(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) return null;
        if (value instanceof Number) return Integer.valueOf(((Number) value).intValue());
        return Integer.valueOf(String.valueOf(value));
    }
    protected String currentUsername() { try { return SecurityUtils.getUsername(); } catch (Exception e) { return "mcp"; } }
    protected Long currentUserId() {
        try {
            Long userId = SecurityUtils.getUserId();
            return userId == null ? 0L : userId;
        } catch (Exception e) {
            return 0L;
        }
    }
}
