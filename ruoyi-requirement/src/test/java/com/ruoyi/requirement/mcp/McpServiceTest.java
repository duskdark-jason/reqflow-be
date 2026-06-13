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
import com.ruoyi.requirement.domain.ReqActionToken;
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
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.IReqDemandService;
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
        assertTrue(String.valueOf(response.getResult()).contains("get_action_context"));
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
        Map<String, Object> schema = (Map<String, Object>) publisher.get("inputSchema");
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertTrue(properties.containsKey("actionToken"));
        assertTrue(properties.containsKey("remoteUrl"));
        assertTrue(properties.containsKey("modules"));
        assertTrue(((List<String>) schema.get("required")).contains("commitHash"), String.valueOf(schema));
        assertTrue(((List<String>) schema.get("required")).contains("indexVersion"), String.valueOf(schema));

        Map<String, Object> modules = (Map<String, Object>) properties.get("modules");
        Map<String, Object> moduleItems = (Map<String, Object>) modules.get("items");
        Map<String, Object> moduleItemProperties = (Map<String, Object>) moduleItems.get("properties");
        assertTrue(String.valueOf(modules.get("description")).contains("前端页面业务功能"), String.valueOf(modules));
        assertTrue(moduleItemProperties.containsKey("moduleCode"), String.valueOf(moduleItems));
        assertTrue(moduleItemProperties.containsKey("moduleName"), String.valueOf(moduleItems));
        assertTrue(((List<String>) moduleItems.get("required")).contains("moduleCode"), String.valueOf(moduleItems));
        assertTrue(((List<String>) moduleItems.get("required")).contains("moduleName"), String.valueOf(moduleItems));

        Map<String, Object> pages = (Map<String, Object>) properties.get("pages");
        Map<String, Object> pageItems = (Map<String, Object>) pages.get("items");
        Map<String, Object> pageItemProperties = (Map<String, Object>) pageItems.get("properties");
        assertTrue(pageItemProperties.containsKey("moduleCode"), String.valueOf(pageItems));
        assertTrue(String.valueOf(pageItemProperties.get("moduleCode")).contains("modules[].moduleCode"), String.valueOf(pageItemProperties));
        Map<String, Object> actionContext = tools.stream()
                .filter(tool -> "get_action_context".equals(tool.get("name")))
                .findFirst()
                .orElseThrow();
        Map<String, Object> actionContextSchema = (Map<String, Object>) actionContext.get("inputSchema");
        Map<String, Object> actionContextProperties = (Map<String, Object>) actionContextSchema.get("properties");
        assertTrue(actionContextProperties.containsKey("actionToken"), String.valueOf(actionContextSchema));
        assertTrue(((List<String>) actionContextSchema.get("required")).contains("actionToken"),
                String.valueOf(actionContextSchema));
    }

    @Test
    @SuppressWarnings("unchecked")
    void actionContextReturnsLightweightStageAndVersionSummaryWithoutConsumingToken()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP);
        token.setProjectId(10L);
        token.setVariantId(31L);
        token.setDemandId(9L);
        when(actionTokenService.resolveTokenForContext("reqflow_action_develop")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        ReqDemand demand = demand(9L, "developing");
        demand.setTitle("缩短指令");
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand);
        ReqPackageVersion requirement = packageVersion("requirement", "需求设计正文不应直接进入上下文");
        requirement.setVersionNo(3);
        requirement.setVersionNote("需求设计");
        when(packageService.selectReqPackageVersionListByDemandId(9L)).thenReturn(Collections.singletonList(requirement));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", mock(ReqVariantMapper.class));

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_develop");

        McpResponse response = service.handle(request("tools/call", toolParams("get_action_context", arguments)));

        Map<String, Object> result = (Map<String, Object>) response.getResult();
        Map<String, Object> content = (Map<String, Object>) result.get("structuredContent");
        assertEquals("requirement_develop", content.get("stage"));
        assertEquals("requirement_develop", content.get("targetMethod"));
        assertTrue(String.valueOf(content.get("allowedTools")).contains("save_development_plan"));
        assertTrue(String.valueOf(content.get("resources")).contains("requirement://REQ-20260609-001"));
        assertTrue(String.valueOf(content.get("packageVersions")).contains("versionNo=3"),
                String.valueOf(content.get("packageVersions")));
        assertFalse(String.valueOf(content).contains("需求设计正文不应直接进入上下文"), String.valueOf(content));
        verify(actionTokenService).resolveTokenForContext("reqflow_action_develop");
        verify(actionTokenService, never()).resolveToken("reqflow_action_develop");
    }

    @Test
    @SuppressWarnings("unchecked")
    void closeoutActionContextReturnsRepositoryListAndWritebackGate()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT);
        token.setTargetMethod(IReqActionTokenService.TARGET_PUBLISH_REPOSITORY_INDEX);
        token.setProjectId(10L);
        token.setVariantId(31L);
        token.setDemandId(9L);
        when(actionTokenService.resolveTokenForContext("reqflow_action_closeout")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "closeout_pending"));
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(31L);
        variant.setProjectId(10L);
        variant.setBaselineBranch("release/main");
        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepository backend = new ReqRepository();
        backend.setRepoId(21L);
        backend.setProjectId(10L);
        backend.setRepoName("后端");
        backend.setRepoUrl("git@example.com:reqflow-be.git");
        ReqRepository frontend = new ReqRepository();
        frontend.setRepoId(22L);
        frontend.setProjectId(10L);
        frontend.setRepoName("前端");
        frontend.setRepoUrl("git@example.com:reqflow-ui.git");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(List.of(backend, frontend));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_closeout");

        McpResponse response = service.handle(request("tools/call", toolParams("get_action_context", arguments)));

        Map<String, Object> result = (Map<String, Object>) response.getResult();
        Map<String, Object> content = (Map<String, Object>) result.get("structuredContent");
        assertEquals("requirement_closeout", content.get("stage"));
        assertTrue(String.valueOf(content.get("repositories")).contains("reqflow-be.git"));
        assertTrue(String.valueOf(content.get("repositories")).contains("reqflow-ui.git"));
        assertFalse(content.containsKey("targetRepository"), String.valueOf(content));
        assertTrue(String.valueOf(content.get("writebackPolicy")).contains("requiresExplicitUserConfirmation=true"),
                String.valueOf(content.get("writebackPolicy")));
        assertTrue(String.valueOf(content.get("tokenPersistence")).contains("meta.md"),
                String.valueOf(content.get("tokenPersistence")));
        verify(actionTokenService).resolveTokenForContext("reqflow_action_closeout");
        verify(actionTokenService, never()).resolveToken("reqflow_action_closeout");
    }

    @Test
    void resourceTemplatesListReturnsMcpTemplates()
    {
        McpResponse response = new McpService().handle(request("resources/templates/list", null));

        assertTrue(String.valueOf(response.getResult()).contains("resourceTemplates"));
        assertTrue(String.valueOf(response.getResult()).contains("requirement://{demandNo}"));
        assertTrue(String.valueOf(response.getResult()).contains("requirement://{demandNo}/supplement"));
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
        assertTrue(resultText.contains("前端路由"), resultText);
        assertTrue(resultText.contains("页面组件"), resultText);
        assertTrue(resultText.contains("前端页面业务功能"), resultText);
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
        IReqDemandService demandService = mock(IReqDemandService.class);
        IReqPackageService packageService = mock(IReqPackageService.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "reqDemandService", demandService);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        when(demandMapper.selectReqDemandByDemandNo("REQ-20260609-001")).thenReturn(demand(5L));
        when(packageService.selectLatest(5L, "requirement_draft")).thenReturn(packageVersion("requirement_draft", "需求草稿内容"));

        McpResponse response = service.handle(request("resources/read", params("uri", "requirement://REQ-20260609-001/draft-package")));

        assertTrue(String.valueOf(response.getResult()).contains("需求草稿内容"));
        verify(demandService).validateDemandReadable(5L);
        verify(packageService).selectLatest(5L, "requirement_draft");
    }

    @Test
    void readsLatestRequirementSupplementPackage()
    {
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        IReqDemandService demandService = mock(IReqDemandService.class);
        IReqPackageService packageService = mock(IReqPackageService.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "reqDemandService", demandService);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        when(demandMapper.selectReqDemandByDemandNo("REQ-20260609-001")).thenReturn(demand(7L));
        when(packageService.selectLatest(7L, "requirement_supplement"))
                .thenReturn(packageVersion("requirement_supplement", "需求人补充说明"));

        McpResponse response = service.handle(request("resources/read",
                params("uri", "requirement://REQ-20260609-001/supplement")));

        assertTrue(String.valueOf(response.getResult()).contains("需求人补充说明"));
        verify(demandService).validateDemandReadable(7L);
        verify(packageService).selectLatest(7L, "requirement_supplement");
    }

    @Test
    void readsLatestContextManifestPackage()
    {
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        IReqDemandService demandService = mock(IReqDemandService.class);
        IReqPackageService packageService = mock(IReqPackageService.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "reqDemandService", demandService);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        when(demandMapper.selectReqDemandByDemandNo("REQ-20260609-001")).thenReturn(demand(6L));
        when(packageService.selectLatest(6L, "context_manifest")).thenReturn(packageVersion("context_manifest", "{\"demandNo\":\"REQ-20260609-001\"}"));

        McpResponse response = service.handle(request("resources/read", params("uri", "requirement://REQ-20260609-001/context-manifest")));

        assertTrue(String.valueOf(response.getResult()).contains("context_manifest"));
        verify(demandService).validateDemandReadable(6L);
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
    void publishRepositoryIndexToolAllowsActionTokenWithoutGlobalIndexPermission()
    {
        IReqRepositoryIndexService indexService = mock(IReqRepositoryIndexService.class);
        ReqIndexImportResult importResult = new ReqIndexImportResult();
        importResult.setBatchId(10L);
        when(indexService.importRepositoryIndex(any(ReqRepositoryIndexImportRequest.class), eq("mcp"), any(), any()))
                .thenReturn(importResult);
        McpService service = new TestableMcpService(false);
        ReflectionTestUtils.setField(service, "repositoryIndexService", indexService);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_closeout");
        arguments.put("remoteUrl", "git@example.com:reqflow-ui.git");
        arguments.put("commitHash", "abc123");
        arguments.put("indexVersion", "v1");

        service.handle(request("tools/call", toolParams("publish_repository_index", arguments)));

        ArgumentCaptor<ReqRepositoryIndexImportRequest> captor = forClass(ReqRepositoryIndexImportRequest.class);
        verify(indexService).importRepositoryIndex(captor.capture(), eq("mcp"), any(), any());
        assertEquals("reqflow_action_closeout", captor.getValue().getActionToken());
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
    void requirementDesignToolCanResolveDemandByPlanActionToken()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_design")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "plan_pending"));
        when(packageService.saveVersion(9L, "requirement", "需求设计", "save_requirement_package"))
                .thenReturn(packageVersion("requirement", "需求设计"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_design");
        arguments.put("content", "需求设计");

        service.handle(request("tools/call", toolParams("save_requirement_package", arguments)));

        verify(actionTokenService).resolveToken("reqflow_action_design");
        verify(packageService).saveVersion(9L, "requirement", "需求设计", "save_requirement_package");
    }

    @Test
    void requirementAssessmentToolCanResolveDemandByPlanActionToken()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_assessment")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "submitted"));
        when(packageService.saveVersion(9L, "requirement_assessment", "可行性评估", "upload_requirement_assessment"))
                .thenReturn(packageVersion("requirement_assessment", "可行性评估"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_assessment");
        arguments.put("content", "可行性评估");

        service.handle(request("tools/call", toolParams("upload_requirement_assessment", arguments)));

        verify(actionTokenService).resolveToken("reqflow_action_assessment");
        verify(packageService).saveVersion(9L, "requirement_assessment", "可行性评估", "upload_requirement_assessment");
    }

    @Test
    void requirementPlanActionTokenDoesNotAllowDevelopmentPlanTool()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_design")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "submitted"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_design");
        arguments.put("content", "执行计划");

        McpResponse response = service.handle(request("tools/call", toolParams("save_development_plan", arguments)));

        assertToolErrorResult(response, "动作Token不支持当前MCP工具");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
    }

    @Test
    void requirementGenerateTokenExpiresAfterDemandIsConfirmed()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_generate")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "confirmed"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_generate");
        arguments.put("content", "需求设计");

        McpResponse response = service.handle(request("tools/call", toolParams("save_requirement_package", arguments)));

        assertToolErrorResult(response, "动作Token所属流程阶段已结束");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
    }

    @Test
    void requirementGenerateTokenExpiresAfterDemandIsReadyForRequesterConfirmation()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_generate")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "plan_ready"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_generate");
        arguments.put("content", "需求设计");

        McpResponse response = service.handle(request("tools/call", toolParams("save_requirement_package", arguments)));

        assertToolErrorResult(response, "动作Token所属流程阶段已结束");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
    }

    @Test
    void developmentToolsCanResolveDemandBySameDevelopStageActionToken()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_develop_stage")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "developing"));
        when(packageService.saveVersion(9L, "plan", "执行计划", "save_development_plan"))
                .thenReturn(packageVersion("plan", "执行计划"));
        when(packageService.saveVersion(9L, "execution_report", "执行报告", "upload_execution_report"))
                .thenReturn(packageVersion("execution_report", "执行报告"));
        when(packageService.saveVersion(9L, "review_report", "Review 报告", "upload_review_report"))
                .thenReturn(packageVersion("review_report", "Review 报告"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> planArguments = new HashMap<>();
        planArguments.put("actionToken", "reqflow_action_develop_stage");
        planArguments.put("content", "执行计划");
        Map<String, Object> reportArguments = new HashMap<>();
        reportArguments.put("actionToken", "reqflow_action_develop_stage");
        reportArguments.put("content", "执行报告");
        Map<String, Object> reviewArguments = new HashMap<>();
        reviewArguments.put("actionToken", "reqflow_action_develop_stage");
        reviewArguments.put("content", "Review 报告");

        service.handle(request("tools/call", toolParams("save_development_plan", planArguments)));
        service.handle(request("tools/call", toolParams("upload_execution_report", reportArguments)));
        service.handle(request("tools/call", toolParams("upload_review_report", reviewArguments)));

        verify(actionTokenService, org.mockito.Mockito.times(3)).resolveToken("reqflow_action_develop_stage");
        verify(packageService).saveVersion(9L, "plan", "执行计划", "save_development_plan");
        verify(packageService).saveVersion(9L, "execution_report", "执行报告", "upload_execution_report");
        verify(packageService).saveVersion(9L, "review_report", "Review 报告", "upload_review_report");
    }

    @Test
    void developStageActionTokenCannotBeUsedBeforeDeveloperStartsDevelopment()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_develop_stage")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "confirmed"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_develop_stage");
        arguments.put("content", "执行计划");

        McpResponse response = service.handle(request("tools/call", toolParams("save_development_plan", arguments)));

        assertToolErrorResult(response, "动作Token所属流程阶段已结束");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
    }

    @Test
    void developStageActionTokenExpiresAfterDemandMovesToReview()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_develop_stage")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "review"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_develop_stage");
        arguments.put("content", "执行报告");

        McpResponse response = service.handle(request("tools/call", toolParams("upload_execution_report", arguments)));

        assertToolErrorResult(response, "动作Token所属流程阶段已结束");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
    }

    @Test
    void repairToolsCanResolveDemandBySameRepairStageActionToken()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqDemandService demandService = mock(IReqDemandService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_REPAIR);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_repair_stage")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "repairing"));
        when(packageService.selectLatest(9L, "execution_report"))
                .thenReturn(packageVersion("execution_report", "# 执行报告\n\n原始执行内容"));
        when(packageService.selectLatest(9L, "review_report"))
                .thenReturn(packageVersion("review_report", "# Review 报告\n\n原始 Review 内容"));
        when(packageService.saveVersion(eq(9L), eq("execution_report"), any(), eq("upload_execution_report")))
                .thenReturn(packageVersion("execution_report", "合并后的返修执行报告"));
        when(packageService.saveVersion(eq(9L), eq("review_report"), any(), eq("upload_review_report")))
                .thenReturn(packageVersion("review_report", "合并后的返修 Review 报告"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "reqDemandService", demandService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> reportArguments = new HashMap<>();
        reportArguments.put("actionToken", "reqflow_action_repair_stage");
        reportArguments.put("content", "返修执行报告");
        Map<String, Object> reviewArguments = new HashMap<>();
        reviewArguments.put("actionToken", "reqflow_action_repair_stage");
        reviewArguments.put("content", "返修 Review 报告");

        service.handle(request("tools/call", toolParams("upload_execution_report", reportArguments)));
        service.handle(request("tools/call", toolParams("upload_review_report", reviewArguments)));

        verify(actionTokenService, org.mockito.Mockito.times(2)).resolveToken("reqflow_action_repair_stage");
        ArgumentCaptor<String> executionContent = forClass(String.class);
        verify(packageService).saveVersion(eq(9L), eq("execution_report"), executionContent.capture(), eq("upload_execution_report"));
        assertTrue(executionContent.getValue().contains("原始执行内容"));
        assertTrue(executionContent.getValue().contains("## 返修执行记录"));
        assertTrue(executionContent.getValue().contains("返修执行报告"));
        ArgumentCaptor<String> reviewContent = forClass(String.class);
        verify(packageService).saveVersion(eq(9L), eq("review_report"), reviewContent.capture(), eq("upload_review_report"));
        assertTrue(reviewContent.getValue().contains("原始 Review 内容"));
        assertTrue(reviewContent.getValue().contains("## 返修 Review 记录"));
        assertTrue(reviewContent.getValue().contains("返修 Review 报告"));
        verify(demandService).updateReqDemandStatus(9L, "review", "mcp");
    }

    @Test
    void repairStageActionTokenDoesNotAllowDevelopmentPlanTool()
    {
        IReqPackageService packageService = mock(IReqPackageService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        token.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_REPAIR);
        token.setDemandId(9L);
        when(actionTokenService.resolveToken("reqflow_action_repair_stage")).thenReturn(token);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        when(demandMapper.selectReqDemandByDemandId(9L)).thenReturn(demand(9L, "repairing"));

        McpService service = new TestableMcpService(true);
        ReflectionTestUtils.setField(service, "reqPackageService", packageService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        ReflectionTestUtils.setField(service, "reqDemandMapper", demandMapper);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("actionToken", "reqflow_action_repair_stage");
        arguments.put("content", "执行计划");

        McpResponse response = service.handle(request("tools/call", toolParams("save_development_plan", arguments)));

        assertToolErrorResult(response, "动作Token不支持当前MCP工具");
        verify(packageService, never()).saveVersion(any(), any(), any(), any());
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
        assertTrue(resultText.contains("docs/ai-harness/modules/reqflow-ui-page-functions.md"), resultText);
        assertTrue(resultText.contains("前端页面功能索引"), resultText);
        assertTrue(resultText.contains("菜单目录、子菜单、隐藏页签"), resultText);
        assertTrue(resultText.contains("页面组件和 API 封装"), resultText);
        assertTrue(resultText.contains("docs/process/agent-workflow.md"), resultText);
        assertTrue(resultText.contains("docs/process/new-requirement-flow.md"), resultText);
        assertTrue(resultText.contains("docs/templates/review-report-template.md"), resultText);
        assertTrue(resultText.contains("scripts/check-harness.sh"), resultText);
        assertTrue(resultText.contains("scripts/test-check-harness.sh"), resultText);
        assertTrue(resultText.contains("指定 spec 必须位于 docs/specs/active"), resultText);
        assertTrue(resultText.contains("expected target spec under done directory to fail"), resultText);
        assertTrue(resultText.contains("不要在 `done/` 中边执行边修改"), resultText);
        assertTrue(resultText.contains("收到归档、办结或结束任务指令"), resultText);
        assertTrue(resultText.contains("git mv \"$SPEC_DIR\" docs/specs/done/"), resultText);
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
        return demand(demandId, null);
    }

    private ReqDemand demand(Long demandId, String status)
    {
        ReqDemand demand = new ReqDemand();
        demand.setDemandId(demandId);
        demand.setProjectId(10L);
        demand.setDemandNo("REQ-20260609-001");
        demand.setStatus(status);
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
