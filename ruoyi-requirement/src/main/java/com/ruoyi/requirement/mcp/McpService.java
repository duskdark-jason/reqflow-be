package com.ruoyi.requirement.mcp;

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
import com.ruoyi.requirement.service.IReqPackageService;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class McpService
{
    @Autowired private ReqDemandMapper reqDemandMapper;
    @Autowired private ReqProjectMapper projectMapper;
    @Autowired private ReqRepositoryMapper reqRepositoryMapper;
    @Autowired private ReqVariantMapper variantMapper;
    @Autowired private ReqMemoryIndexMapper memoryIndexMapper;
    @Autowired private IReqPackageService reqPackageService;
    @Autowired private IReqRepositoryIndexService repositoryIndexService;
    @Autowired private ReqActivityLogService activityLogService;

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
                    prompt("generate_agent_requirement_package", "生成需求说明包"),
                    prompt("generate_development_plan", "生成开发计划"),
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
        StringBuilder content = new StringBuilder();
        content.append("# AGENTS.md\n\n");
        content.append("## 项目\n");
        if (project != null)
        {
            content.append("- 项目：").append(project.getProjectName()).append("（").append(project.getProjectCode()).append("）\n");
        }
        content.append("- 仓库数量：").append(repositories == null ? 0 : repositories.size()).append("\n");
        content.append("- 分支数量：").append(variants == null ? 0 : variants.size()).append("\n\n");
        content.append("## 分支知识库\n");
        content.append("- 读取知识库时必须同时传入 projectId 与 variantId，避免不同分支的独有模块互相污染。\n");
        content.append("- 分支初始化、模块索引和需求影响推荐均以项目分支为粒度。\n");
        return content.toString();
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
                tool("save_requirement_package", "保存需求说明包", packageToolSchema(true)),
                tool("save_development_plan", "保存开发计划", packageToolSchema(false)),
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
            requirePermission("publish_repository_index", "req:index:import");
            return toolResult(Collections.singletonMap("result", repositoryIndexService.importRepositoryIndex(toIndexRequest(arguments), "mcp", currentUsername(), currentUserId())));
        }
        requirePermission(name, "req:package:save");
        Long demandId = longArg(arguments, "demandId");
        String artifactType = artifactTypeForTool(name, stringArg(arguments, "artifactType"));
        return toolResult(Collections.singletonMap("version", reqPackageService.saveVersion(demandId, artifactType, stringArg(arguments, "content"), name)));
    }

    private ReqRepositoryIndexImportRequest toIndexRequest(Map<String, Object> arguments)
    {
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
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
        for (ReqRepository repository : repositories)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("repository", repository);
            item.put("content", harnessInstructionContent(repository, variants));
            repositoryInstructions.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("repositories", repositories);
        result.put("variants", variants);
        result.put("workspaceAgents", workspaceAgentsContent(project, repositories, variants));
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
        content.append("3. 按项目分支分别初始化模块与知识库索引，不能用主线结果代替其他分支。\n");
        content.append("4. 运行 sh scripts/check-docs.sh 和 sh scripts/check-harness.sh init 后回写初始化结果。\n");
        return content.toString();
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

    private Map<String, Object> arrayProperty(String description)
    {
        Map<String, Object> property = property("array", description);
        property.put("items", Collections.singletonMap("type", "object"));
        return property;
    }

    private Map<String, Object> packageToolSchema(boolean allowArtifactType)
    {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("demandId", property("integer", "需求 ID"));
        properties.put("content", property("string", "要保存的交接资料正文"));
        if (allowArtifactType)
        {
            properties.put("artifactType", property("string", "产物类型，缺省为 requirement"));
        }
        return objectSchema(properties, Arrays.asList("demandId", "content"));
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
        properties.put("modules", arrayProperty("模块或功能点索引列表"));
        properties.put("pages", arrayProperty("页面影响面列表"));
        properties.put("apis", arrayProperty("接口影响面列表"));
        properties.put("tables", arrayProperty("数据表影响面列表"));
        properties.put("permissions", arrayProperty("权限影响面列表"));
        properties.put("documents", arrayProperty("文档影响面列表"));
        return objectSchema(properties, Collections.singletonList("remoteUrl"));
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
