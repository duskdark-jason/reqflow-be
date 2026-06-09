package com.ruoyi.requirement.mcp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.dto.ReqIndexImpactPayload;
import com.ruoyi.requirement.dto.ReqIndexModulePayload;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.service.IReqPackageService;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class McpService
{
    @Autowired private ReqDemandMapper reqDemandMapper;
    @Autowired private ReqRepositoryMapper reqRepositoryMapper;
    @Autowired private IReqPackageService reqPackageService;
    @Autowired private IReqRepositoryIndexService repositoryIndexService;
    @Autowired private ReqActivityLogService activityLogService;

    public McpResponse handle(McpRequest request)
    {
        try
        {
            if ("resources/list".equals(request.getMethod())) return McpResponse.success(request.getId(), resourcesList());
            if ("resources/read".equals(request.getMethod())) return McpResponse.success(request.getId(), resourcesRead(stringParam(request, "uri")));
            if ("prompts/list".equals(request.getMethod())) return McpResponse.success(request.getId(), Collections.singletonMap("prompts", Arrays.asList(prompt("generate_agent_requirement_package"), prompt("generate_development_plan"), prompt("generate_execution_prompt"), prompt("generate_review_prompt"))));
            if ("prompts/get".equals(request.getMethod())) return McpResponse.success(request.getId(), promptsGet(stringParam(request, "name")));
            if ("tools/list".equals(request.getMethod())) return McpResponse.success(request.getId(), Collections.singletonMap("tools", Arrays.asList(tool("save_requirement_package"), tool("save_development_plan"), tool("upload_execution_report"), tool("upload_review_report"), tool("register_harness_init_result"), tool("publish_repository_index"))));
            if ("tools/call".equals(request.getMethod())) return McpResponse.success(request.getId(), toolsCall(request));
            return McpResponse.error(request.getId(), "不支持的MCP方法：" + request.getMethod());
        }
        catch (Exception e)
        {
            return McpResponse.error(request.getId(), e.getMessage());
        }
    }

    private Map<String, Object> resourcesList()
    {
        return Collections.singletonMap("resources", Arrays.asList(
                resource("requirement://REQ-20260609-001", "需求样例"),
                resource("requirement://REQ-20260609-001/context-manifest", "需求上下文清单"),
                resource("project://1/overview", "项目概览"),
                resource("variant://1/branch-policy", "客户线分支策略"),
                resource("workspace://1/agents", "工作空间AGENTS")));
    }

    private Map<String, Object> resourcesRead(String uri)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("uri", uri);
        if (uri != null && uri.startsWith("requirement://"))
        {
            String demandNo = uri.substring("requirement://".length()).replace("/context-manifest", "");
            ReqDemand demand = reqDemandMapper.selectReqDemandByDemandNo(demandNo);
            result.put("content", demand);
            if (demand != null)
            {
                activityLogService.record(currentUserId(), demand.getProjectId(), demand.getDemandId(), "mcp_read", "mcp", "读取需求资源：" + demandNo, null);
            }
        }
        else
        {
            result.put("content", "resource not materialized in MVP-lite");
        }
        return result;
    }

    private Map<String, Object> promptsGet(String name)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", "需求平台MVP-lite提示词：" + name);
        result.put("messages", Arrays.asList(Collections.singletonMap("content", "请按需求平台上下文执行：" + name)));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toolsCall(McpRequest request)
    {
        String name = stringParam(request, "name");
        Map<String, Object> arguments = request.getParams() == null ? Collections.emptyMap() : (Map<String, Object>) request.getParams().getOrDefault("arguments", Collections.emptyMap());
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
            return Collections.singletonMap("updated", rows);
        }
        if ("publish_repository_index".equals(name))
        {
            requirePermission("publish_repository_index", "req:index:import");
            return Collections.singletonMap("result", repositoryIndexService.importRepositoryIndex(toIndexRequest(arguments), "mcp", currentUsername(), currentUserId()));
        }
        requirePermission(name, "req:package:save");
        Long demandId = longArg(arguments, "demandId");
        String artifactType = artifactTypeForTool(name, stringArg(arguments, "artifactType"));
        return Collections.singletonMap("version", reqPackageService.saveVersion(demandId, artifactType, stringArg(arguments, "content"), name));
    }

    private ReqRepositoryIndexImportRequest toIndexRequest(Map<String, Object> arguments)
    {
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setProjectId(longArg(arguments, "projectId"));
        request.setRepoId(longArg(arguments, "repoId"));
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

    private Map<String, Object> prompt(String name) { return Collections.singletonMap("name", name); }
    private Map<String, Object> tool(String name) { return Collections.singletonMap("name", name); }
    private String stringParam(McpRequest request, String key) { return request.getParams() == null || request.getParams().get(key) == null ? null : String.valueOf(request.getParams().get(key)); }
    private String stringArg(Map<String, Object> arguments, String key) { Object value = arguments.get(key); return value == null ? null : String.valueOf(value); }
    private Long longArg(Map<String, Object> arguments, String key) { Object value = arguments.get(key); return value instanceof Number ? ((Number) value).longValue() : (value == null ? null : Long.valueOf(String.valueOf(value))); }
    private Integer intArg(Map<String, Object> arguments, String key) { Object value = arguments.get(key); return value instanceof Number ? ((Number) value).intValue() : (value == null ? null : Integer.valueOf(String.valueOf(value))); }
    private String currentUsername() { try { return SecurityUtils.getUsername(); } catch (Exception e) { return "mcp"; } }
    private Long currentUserId() { try { return SecurityUtils.getUserId(); } catch (Exception e) { return 0L; } }
}
