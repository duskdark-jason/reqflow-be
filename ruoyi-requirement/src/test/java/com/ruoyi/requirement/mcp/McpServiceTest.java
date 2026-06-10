package com.ruoyi.requirement.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqMemoryIndex;
import com.ruoyi.requirement.domain.ReqPackageVersion;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqIndexImportResult;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqMemoryIndexMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;
import com.ruoyi.requirement.service.IReqPackageService;
import com.ruoyi.requirement.service.ReqActivityLogService;

class McpServiceTest
{
    @Test
    void successResponseSerializesWithoutNullError() throws Exception
    {
        String json = new ObjectMapper().writeValueAsString(McpResponse.success(1, Collections.emptyMap()));

        assertTrue(json.contains("\"result\":{}"), json);
        assertFalse(json.contains("\"error\""), json);
    }

    @Test
    void errorResponseSerializesJsonRpcErrorWithoutNullResult() throws Exception
    {
        String json = new ObjectMapper().writeValueAsString(McpResponse.error(7, "boom"));

        assertTrue(json.contains("\"code\":-32603"), json);
        assertTrue(json.contains("\"message\":\"boom\""), json);
        assertFalse(json.contains("\"result\""), json);
        assertFalse(json.contains("\"error\":null"), json);
    }

    @Test
    @SuppressWarnings("unchecked")
    void initializeDeclaresMcpCapabilities()
    {
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("name", "codex");
        clientInfo.put("version", "1.0.0");
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2025-11-25");
        params.put("capabilities", new HashMap<>());
        params.put("clientInfo", clientInfo);

        McpResponse response = new McpService().handle(request("initialize", params));

        Map<String, Object> result = (Map<String, Object>) response.getResult();
        assertEquals("2025-11-25", result.get("protocolVersion"));
        assertTrue(String.valueOf(result.get("capabilities")).contains("tools"));
        assertTrue(String.valueOf(result.get("capabilities")).contains("resources"));
        assertTrue(String.valueOf(result.get("capabilities")).contains("prompts"));
        assertTrue(String.valueOf(result.get("serverInfo")).contains("reqflow"));
    }

    @Test
    void toolListIncludesRepositoryIndexPublisher()
    {
        McpResponse response = new McpService().handle(request("tools/list", null));

        assertTrue(String.valueOf(response.getResult()).contains("publish_repository_index"));
        assertTrue(String.valueOf(response.getResult()).contains("get_harness_template"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toolListExposesRepositoryIndexPublisherSchema()
    {
        McpResponse response = new McpService().handle(request("tools/list", null));

        Map<String, Object> result = (Map<String, Object>) response.getResult();
        List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
        Map<String, Object> publisher = tools.stream()
                .filter(tool -> "publish_repository_index".equals(tool.get("name")))
                .findFirst()
                .orElseThrow();

        assertTrue(String.valueOf(publisher.get("description")).contains("发布"));
        assertTrue(String.valueOf(publisher.get("inputSchema")).contains("actionToken"));
        assertTrue(String.valueOf(publisher.get("inputSchema")).contains("remoteUrl"));
        assertTrue(String.valueOf(publisher.get("inputSchema")).contains("modules"));
    }

    @Test
    void resourceTemplatesListReturnsMcpTemplates()
    {
        McpResponse response = new McpService().handle(request("resources/templates/list", null));

        assertTrue(String.valueOf(response.getResult()).contains("resourceTemplates"));
        assertTrue(String.valueOf(response.getResult()).contains("requirement://{demandNo}"));
        assertTrue(String.valueOf(response.getResult()).contains("memory://{projectId}/modules"));
        assertTrue(String.valueOf(response.getResult()).contains("skill://reqflow/project-init"));
    }

    @Test
    void readsReqflowProjectInitSkillResource()
    {
        McpResponse response = new McpService().handle(request("resources/read", params("uri", "skill://reqflow/project-init")));

        String resultText = String.valueOf(response.getResult());
        assertTrue(resultText.contains("Reqflow MCP 项目接入初始化技能"), resultText);
        assertTrue(resultText.contains("mcp__reqflow.publish_repository_index"), resultText);
        assertTrue(resultText.contains("arguments.actionToken"), resultText);
        assertTrue(resultText.contains("get_harness_template"), resultText);
    }

    @Test
    void readsMaterializedProjectResource()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "projectMapper", projectMapper);
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);

        ReqProject project = new ReqProject();
        project.setProjectId(10L);
        project.setProjectCode("REQFLOW");
        project.setProjectName("需求平台");
        when(projectMapper.selectReqProjectByProjectId(10L)).thenReturn(project);
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(new ReqRepository()));
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.singletonList(new ReqVariant()));

        McpResponse response = service.handle(request("resources/read", params("uri", "project://10/overview")));

        assertTrue(String.valueOf(response.getResult()).contains("REQFLOW"));
        verify(projectMapper).selectReqProjectByProjectId(10L);
        verify(repositoryMapper).selectReqRepositoryList(any(ReqRepository.class));
        verify(variantMapper).selectReqVariantList(any(ReqVariant.class));
    }

    @Test
    void readsMemoryResourceByProjectAndVariant()
    {
        ReqMemoryIndexMapper memoryIndexMapper = mock(ReqMemoryIndexMapper.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "memoryIndexMapper", memoryIndexMapper);
        when(memoryIndexMapper.selectReqMemoryIndexList(any())).thenReturn(Collections.singletonList(new ReqMemoryIndex()));

        service.handle(request("resources/read", params("uri", "memory://10/modules?variantId=31")));

        ArgumentCaptor<ReqMemoryIndex> captor = forClass(ReqMemoryIndex.class);
        verify(memoryIndexMapper).selectReqMemoryIndexList(captor.capture());
        assertEquals(10L, captor.getValue().getProjectId());
        assertEquals(31L, captor.getValue().getVariantId());
        assertEquals("module", captor.getValue().getDocType());
    }

    @Test
    void readsLatestRequirementDraftPackage()
    {
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        IReqPackageService packageService = mock(IReqPackageService.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        when(demandMapper.selectReqDemandByDemandNo("REQ-20260609-001")).thenReturn(demand(5L));
        when(packageService.selectLatest(5L, "requirement_draft")).thenReturn(packageVersion("requirement_draft", "需求草稿内容"));

        McpResponse response = service.handle(request("resources/read", params("uri", "requirement://REQ-20260609-001/draft-package")));

        assertTrue(String.valueOf(response.getResult()).contains("需求草稿内容"));
        verify(packageService).selectLatest(5L, "requirement_draft");
    }

    @Test
    void readsLatestContextManifestPackage()
    {
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        IReqPackageService packageService = mock(IReqPackageService.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        when(demandMapper.selectReqDemandByDemandNo("REQ-20260609-001")).thenReturn(demand(6L));
        when(packageService.selectLatest(6L, "context_manifest")).thenReturn(packageVersion("context_manifest", "{\"demandNo\":\"REQ-20260609-001\"}"));

        McpResponse response = service.handle(request("resources/read", params("uri", "requirement://REQ-20260609-001/context-manifest")));

        assertTrue(String.valueOf(response.getResult()).contains("context_manifest"));
        verify(packageService).selectLatest(6L, "context_manifest");
    }

    @Test
    void publishRepositoryIndexToolDelegatesToIndexService()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        ReqIndexImportResult importResult = new ReqIndexImportResult();
        importResult.setBatchId(9L);
        importResult.setModuleCount(1);
        importResult.setImpactCount(2);
        when(indexService.importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any())).thenReturn(importResult);

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_abc");
        arguments.put("projectId", 1L);
        arguments.put("repoId", 2L);
        arguments.put("repoType", "FRONTEND");
        arguments.put("remoteUrl", "git@example.com:reqflow-ui.git");
        arguments.put("branchName", "main");
        arguments.put("commitHash", "abc123");
        arguments.put("indexVersion", "v1");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "publish_repository_index");
        params.put("arguments", arguments);

        service.handle(request("tools/call", params));

        ArgumentCaptor<ReqRepositoryIndexImportRequest> captor = forClass(ReqRepositoryIndexImportRequest.class);
        verify(indexService).importRepositoryIndex(captor.capture(), eq("mcp"), any(), any());
        assertEquals("reqflow_action_abc", captor.getValue().getActionToken());
    }

    @Test
    void publishRepositoryIndexToolReturnsMcpToolResultContent()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        ReqIndexImportResult importResult = new ReqIndexImportResult();
        importResult.setBatchId(9L);
        importResult.setModuleCount(1);
        importResult.setImpactCount(2);
        when(indexService.importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any())).thenReturn(importResult);

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_abc");
        arguments.put("remoteUrl", "git@example.com:reqflow-ui.git");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "publish_repository_index");
        params.put("arguments", arguments);

        McpResponse response = service.handle(request("tools/call", params));

        String responseText = String.valueOf(response.getError()) + " / " + String.valueOf(response.getResult());
        assertTrue(String.valueOf(response.getResult()).contains("content"), responseText);
        assertTrue(String.valueOf(response.getResult()).contains("structuredContent"), responseText);
        assertTrue(String.valueOf(response.getResult()).contains("isError=false"), responseText);
    }


    @Test
    void publishRepositoryIndexToolRequiresIndexImportPermission()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "publish_repository_index");
        params.put("arguments", new HashMap<>());

        McpResponse response = service.handle(request("tools/call", params));

        assertToolErrorResult(response, "req:index:import");
        verify(indexService, never()).importRepositoryIndex(any(), any(), any(), any());
    }

    @Test
    void publishRepositoryIndexToolBusinessErrorReturnsMcpToolErrorResult()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        when(indexService.importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any()))
                .thenThrow(new IllegalArgumentException("初始化指令不存在或已失效"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_invalid");
        arguments.put("remoteUrl", "git@example.com:reqflow-ui.git");
        arguments.put("branchName", "main");
        arguments.put("commitHash", "abc123");
        arguments.put("indexVersion", "v1");

        McpResponse response = service.handle(request("tools/call", toolParams("publish_repository_index", arguments)));

        assertToolErrorResult(response, "初始化指令不存在或已失效");
    }

    @Test
    void getHarnessTemplateToolReturnsWorkspaceAndRepositoryInstructions()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "projectMapper", projectMapper);
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);

        ReqProject project = new ReqProject();
        project.setProjectId(10L);
        project.setProjectCode("REQFLOW");
        project.setProjectName("需求平台");
        ReqRepository repository = new ReqRepository();
        repository.setRepoId(20L);
        repository.setRepoName("reqflow-ui");
        repository.setRepoType("FRONTEND");
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setDefaultBranch("main");
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(31L);
        variant.setVariantName("通用主线");
        variant.setBaselineBranch("main");

        when(projectMapper.selectReqProjectByProjectId(10L)).thenReturn(project);
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.singletonList(variant));

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectId", 10L);
        McpResponse response = service.handle(request("tools/call", toolParams("get_harness_template", arguments)));

        String resultText = String.valueOf(response.getResult());
        assertTrue(resultText.contains("# AGENTS.md"), resultText);
        assertTrue(resultText.contains("reqflow-ui"), resultText);
        assertTrue(resultText.contains("reqflowMcpSkill"), resultText);
        assertTrue(resultText.contains("Reqflow MCP 项目接入初始化技能"), resultText);
        assertTrue(resultText.contains("workspaceFiles"), resultText);
        assertTrue(resultText.contains("repositoryHarnessInstructions"), resultText);
        assertTrue(resultText.contains("files"), resultText);
        assertTrue(resultText.contains("docs/ai-harness/harness-index.json"), resultText);
        assertTrue(resultText.contains("\"template\": false"), resultText);
        assertTrue(resultText.contains("\"initialized\": true"), resultText);
        assertTrue(resultText.contains("docs/ai-harness/modules/reqflow-ui-overview.md"), resultText);
        assertTrue(resultText.contains("docs/process/agent-workflow.md"), resultText);
        assertTrue(resultText.contains("docs/process/new-requirement-flow.md"), resultText);
        assertTrue(resultText.contains("docs/templates/review-report-template.md"), resultText);
        assertTrue(resultText.contains("scripts/check-harness.sh"), resultText);
        assertTrue(resultText.contains("scripts/test-check-harness.sh"), resultText);
        assertTrue(resultText.contains("自动 Review、返修和复审循环"), resultText);
        assertTrue(resultText.contains("publish_repository_index"), resultText);
        assertTrue(resultText.contains("register_harness_init_result"), resultText);
        verify(projectMapper).selectReqProjectByProjectId(10L);
    }

    @Test
    void getHarnessTemplateToolRequiresProjectQueryPermission()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "projectMapper", projectMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("projectId", 10L);
        McpResponse response = service.handle(request("tools/call", toolParams("get_harness_template", arguments)));

        assertToolErrorResult(response, "req:project:query");
        verify(projectMapper, never()).selectReqProjectByProjectId(any());
    }

    @Test
    void registerHarnessInitResultRequiresPackageSavePermission()
    {
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("repoId", 2L);
        arguments.put("harnessStatus", "indexed");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "register_harness_init_result");
        params.put("arguments", arguments);

        McpResponse response = service.handle(request("tools/call", params));

        assertToolErrorResult(response, "req:package:save");
        verify(repositoryMapper, never()).updateHarnessInitResult(any(ReqRepository.class));
    }

    private McpRequest request(String method, Map<String, Object> params)
    {
        McpRequest request = new McpRequest();
        request.setJsonrpc("2.0");
        request.setId(1);
        request.setMethod(method);
        request.setParams(params);
        return request;
    }

    private Map<String, Object> params(String key, Object value)
    {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private Map<String, Object> toolParams(String name, Map<String, Object> arguments)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("arguments", arguments);
        return params;
    }

    private void assertToolErrorResult(McpResponse response, String expectedText)
    {
        String resultText = String.valueOf(response.getResult());
        assertNull(response.getError(), resultText);
        assertTrue(resultText.contains("content"), resultText);
        assertTrue(resultText.contains("isError=true"), resultText);
        assertTrue(resultText.contains(expectedText), resultText);
    }

    private ReqDemand demand(Long demandId)
    {
        ReqDemand demand = new ReqDemand();
        demand.setDemandId(demandId);
        demand.setProjectId(10L);
        demand.setDemandNo("REQ-20260609-001");
        return demand;
    }

    private ReqPackageVersion packageVersion(String artifactType, String content)
    {
        ReqPackageVersion version = new ReqPackageVersion();
        version.setArtifactType(artifactType);
        version.setContent(content);
        version.setVersionNo(2);
        return version;
    }

    private static class TestableMcpService extends McpService
    {
        private final boolean allowed;

        TestableMcpService(boolean allowed)
        {
            this.allowed = allowed;
        }

        @Override
        protected boolean hasPermission(String permission)
        {
            return allowed;
        }

        @Override
        protected String currentUsername()
        {
            return "mcp";
        }

        @Override
        protected Long currentUserId()
        {
            return 0L;
        }
    }
}
