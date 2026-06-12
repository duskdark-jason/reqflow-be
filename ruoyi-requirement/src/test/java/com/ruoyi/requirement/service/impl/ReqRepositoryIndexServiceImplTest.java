package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.sql.SQLException;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqImpactItem;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;
import com.ruoyi.requirement.dto.ReqImpactSuggestResult;
import com.ruoyi.requirement.dto.ReqIndexImpactPayload;
import com.ruoyi.requirement.dto.ReqIndexImportResult;
import com.ruoyi.requirement.dto.ReqIndexModulePayload;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqImpactItemMapper;
import com.ruoyi.requirement.mapper.ReqIndexModuleMapper;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.ReqActivityLogService;

class ReqRepositoryIndexServiceImplTest
{
    @Test
    void rejectsPersonalAbsolutePathBeforeWritingIndex()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, mock(ReqRepositoryMapper.class), mock(ReqVariantMapper.class), mock(ReqActivityLogService.class));

        ReqRepositoryIndexImportRequest request = baseRequest();
        ReqIndexImpactPayload page = impact("PAGE", "系统首页");
        page.setRelativePath("/home/developer/project/src/views/index.vue");
        request.setPages(Collections.singletonList(page));

        assertThrows(ServiceException.class, () -> service.importRepositoryIndex(request, "mcp", "tester", 7L));
        verifyNoInteractions(batchMapper, moduleMapper, impactMapper);
    }

    @Test
    void importsBatchModulesAndImpactItems()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper, mock(ReqVariantMapper.class), activityLogService);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        when(repositoryMapper.selectReqRepositoryByRepoId(2L)).thenReturn(repository);
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(99L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = baseRequest();
        request.setModules(Collections.singletonList(module("demand", "需求管理")));
        request.setPages(Collections.singletonList(impact("PAGE", "需求列表")));
        request.setApis(Collections.singletonList(impact("API", "需求列表接口")));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "mcp", "tester", 7L);

        assertEquals(99L, result.getBatchId());
        assertEquals(1, result.getModuleCount());
        assertEquals(2, result.getImpactCount());
        verify(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));
        verify(moduleMapper).insertReqIndexModule(any());
        verify(impactMapper, times(2)).insertReqImpactItem(any());
        verify(repositoryMapper).updateHarnessInitResult(any(ReqRepository.class));
        verify(activityLogService).record(7L, 1L, null, "repository_index_published", "mcp", "仓库索引发布：git@example.com:reqflow-ui.git", null);
    }

    @Test
    void importsIndexAsSnapshotAndDeactivatesPreviousRepositoryKnowledge()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper,
                repositoryMapper, variantMapper, mock(ReqActivityLogService.class));

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryByRepoId(2L)).thenReturn(repository);
        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("customer/hlj");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.singletonList(branch));
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(104L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = baseRequest();
        request.setModules(Collections.singletonList(module("demand", "需求管理")));
        request.setPages(Collections.singletonList(impact("PAGE", "需求列表")));

        service.importRepositoryIndex(request, "mcp", "tester", 7L);

        verify(moduleMapper).deactivateReqIndexModulesByRepositoryBranch(argThat(module ->
                Long.valueOf(1L).equals(module.getProjectId())
                        && Long.valueOf(2L).equals(module.getRepoId())
                        && Long.valueOf(8L).equals(module.getVariantId())
                        && "tester".equals(module.getUpdateBy())));
        verify(impactMapper).deactivateReqImpactItemsByRepositoryBranch(argThat(item ->
                Long.valueOf(1L).equals(item.getProjectId())
                        && Long.valueOf(2L).equals(item.getRepoId())
                        && Long.valueOf(8L).equals(item.getVariantId())
                        && "customer/hlj".equals(item.getBranchName())
                        && "tester".equals(item.getUpdateBy())));
        verify(moduleMapper).insertReqIndexModule(any());
        verify(impactMapper).insertReqImpactItem(any());
    }

    @Test
    void allowsNonProjectInitIndexWithoutModules()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper, mock(ReqVariantMapper.class), activityLogService);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryByRepoId(2L)).thenReturn(repository);
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(100L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = baseRequest();
        request.setPages(Collections.singletonList(impact("PAGE", "需求列表")));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "web", "tester", 7L);

        assertEquals(100L, result.getBatchId());
        assertEquals(0, result.getModuleCount());
        assertEquals(1, result.getImpactCount());
        verify(moduleMapper, never()).insertReqIndexModule(any());
        verify(impactMapper).insertReqImpactItem(any());
    }

    @Test
    void rejectsImportWithFriendlyMessageWhenIndexBatchTableIsMissing()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper,
                repositoryMapper, mock(ReqVariantMapper.class), mock(ReqActivityLogService.class));
        when(batchMapper.checkReqRepositoryIndexBatchTable()).thenThrow(missingTable("req_repository_index_batch"));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.importRepositoryIndex(baseRequest(), "mcp", "tester", 7L));

        assertTrue(exception.getMessage().contains("平台索引表未初始化"));
        assertTrue(exception.getMessage().contains("req_repository_index_batch"));
        assertTrue(exception.getMessage().contains("docs/db/sql/req_platform_req007_index_tables.sql"));
        verify(repositoryMapper, never()).selectReqRepositoryByRepoId(any());
        verify(batchMapper, never()).insertReqRepositoryIndexBatch(any());
        verifyNoInteractions(moduleMapper, impactMapper);
    }

    @Test
    void rejectsImportWithFriendlyMessageWhenIndexModuleTableIsMissing()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper,
                repositoryMapper, mock(ReqVariantMapper.class), mock(ReqActivityLogService.class));
        when(moduleMapper.checkReqIndexModuleTable()).thenThrow(missingTable("req_index_module"));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.importRepositoryIndex(baseRequest(), "mcp", "tester", 7L));

        assertTrue(exception.getMessage().contains("平台索引表未初始化"));
        assertTrue(exception.getMessage().contains("req_index_module"));
        verify(repositoryMapper, never()).selectReqRepositoryByRepoId(any());
        verify(batchMapper, never()).insertReqRepositoryIndexBatch(any());
        verifyNoInteractions(impactMapper);
    }

    @Test
    void rejectsImportWithFriendlyMessageWhenImpactItemTableIsMissing()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper,
                repositoryMapper, mock(ReqVariantMapper.class), mock(ReqActivityLogService.class));
        when(impactMapper.checkReqImpactItemTable()).thenThrow(missingTable("req_impact_item"));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.importRepositoryIndex(baseRequest(), "mcp", "tester", 7L));

        assertTrue(exception.getMessage().contains("平台索引表未初始化"));
        assertTrue(exception.getMessage().contains("req_impact_item"));
        verify(repositoryMapper, never()).selectReqRepositoryByRepoId(any());
        verify(batchMapper, never()).insertReqRepositoryIndexBatch(any());
    }

    @Test
    void importsIndexByMcpKeyAndRemoteUrl()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper, variantMapper, activityLogService);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("release/hlj-main");
        branch.setMcpKey("REQFLOW:HLJ");
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.singletonList(branch));

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-be.git");
        repository.setRepoType("BACKEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(101L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setMcpKey("REQFLOW:HLJ");
        request.setRemoteUrl("git@example.com:reqflow-be.git");
        request.setCommitHash("def456");
        request.setIndexVersion("v1");
        request.setModules(Collections.singletonList(module("backend-task", "后端任务")));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "mcp", "tester", 7L);

        assertEquals(101L, result.getBatchId());
        ArgumentCaptor<ReqRepositoryIndexBatch> batchCaptor = forClass(ReqRepositoryIndexBatch.class);
        verify(batchMapper).insertReqRepositoryIndexBatch(batchCaptor.capture());
        assertEquals(1L, batchCaptor.getValue().getProjectId());
        assertEquals(2L, batchCaptor.getValue().getRepoId());
        assertEquals("BACKEND", batchCaptor.getValue().getRepoType());
        assertEquals("release/hlj-main", batchCaptor.getValue().getBranchName());
        verify(repositoryMapper, never()).selectReqRepositoryByRepoId(any());
        verify(activityLogService).record(7L, 1L, null, "repository_index_published", "mcp", "仓库索引发布：git@example.com:reqflow-be.git", null);
    }

    @Test
    void importsIndexByActionTokenAndRemoteUrl()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper,
                variantMapper, activityLogService, actionTokenService);

        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_PROJECT_INIT);
        token.setProjectId(1L);
        token.setVariantId(8L);
        token.setTargetMethod("publish_repository_index");
        when(actionTokenService.resolveToken("reqflow_action_branch_init")).thenReturn(token);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("release/hlj-main");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(branch);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-be.git");
        repository.setRepoType("BACKEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(102L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setActionToken("reqflow_action_branch_init");
        request.setRemoteUrl("git@example.com:reqflow-be.git");
        request.setCommitHash("def456");
        request.setIndexVersion("v1");
        request.setModules(Collections.singletonList(module("backend-task", "后端任务")));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "mcp", "tester", 7L);

        assertEquals(102L, result.getBatchId());
        ArgumentCaptor<ReqRepositoryIndexBatch> batchCaptor = forClass(ReqRepositoryIndexBatch.class);
        verify(batchMapper).insertReqRepositoryIndexBatch(batchCaptor.capture());
        assertEquals(1L, batchCaptor.getValue().getProjectId());
        assertEquals(2L, batchCaptor.getValue().getRepoId());
        assertEquals("BACKEND", batchCaptor.getValue().getRepoType());
        assertEquals("release/hlj-main", batchCaptor.getValue().getBranchName());
        verify(actionTokenService).resolveToken("reqflow_action_branch_init");
    }

    @Test
    void rejectsProjectInitIndexWithoutModuleKnowledge()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper,
                variantMapper, mock(ReqActivityLogService.class), actionTokenService);

        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_PROJECT_INIT);
        token.setProjectId(1L);
        token.setVariantId(8L);
        token.setTargetMethod("publish_repository_index");
        when(actionTokenService.resolveToken("reqflow_action_branch_init")).thenReturn(token);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("main");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(branch);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));

        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setActionToken("reqflow_action_branch_init");
        request.setRemoteUrl("git@example.com:reqflow-ui.git");
        request.setCommitHash("abc123");
        request.setIndexVersion("v1");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.importRepositoryIndex(request, "mcp", "tester", 7L));

        assertTrue(exception.getMessage().contains("项目初始化索引必须包含模块知识库"), exception.getMessage());
        verify(batchMapper, never()).insertReqRepositoryIndexBatch(any());
        verify(moduleMapper, never()).insertReqIndexModule(any());
        verify(impactMapper, never()).insertReqImpactItem(any());
    }

    @Test
    void importsProjectInitPageFunctionModulesAndImpacts()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper,
                variantMapper, activityLogService, actionTokenService);

        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_PROJECT_INIT);
        token.setProjectId(1L);
        token.setVariantId(8L);
        token.setTargetMethod("publish_repository_index");
        when(actionTokenService.resolveToken("reqflow_action_branch_init")).thenReturn(token);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("main");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(branch);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(103L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqIndexModulePayload demandModule = module("requirement-demand", "需求提交");
        demandModule.setModuleType("PAGE_FUNCTION");
        demandModule.setRepoScope("FRONTEND");
        demandModule.setSummary("从前端需求列表页面、详情隐藏页签和 API 封装分析出的业务功能。");
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setActionToken("reqflow_action_branch_init");
        request.setRemoteUrl("git@example.com:reqflow-ui.git");
        request.setCommitHash("abc123");
        request.setIndexVersion("v1");
        request.setModules(Collections.singletonList(demandModule));
        ReqIndexImpactPayload page = impact("PAGE", "需求列表页");
        page.setModuleCode("requirement-demand");
        ReqIndexImpactPayload api = impact("API", "提交需求接口");
        api.setModuleCode("requirement-demand");
        ReqIndexImpactPayload permission = impact("PERMISSION", "需求新增权限");
        permission.setModuleCode("requirement-demand");
        request.setPages(Collections.singletonList(page));
        request.setApis(Collections.singletonList(api));
        request.setPermissions(Collections.singletonList(permission));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "mcp", "tester", 7L);

        assertEquals(103L, result.getBatchId());
        assertEquals(1, result.getModuleCount());
        assertEquals(3, result.getImpactCount());
        ArgumentCaptor<ReqIndexModule> moduleCaptor = forClass(ReqIndexModule.class);
        verify(moduleMapper).insertReqIndexModule(moduleCaptor.capture());
        assertEquals("requirement-demand", moduleCaptor.getValue().getModuleCode());
        assertEquals("需求提交", moduleCaptor.getValue().getModuleName());
        assertEquals("PAGE_FUNCTION", moduleCaptor.getValue().getModuleType());
        assertEquals(8L, moduleCaptor.getValue().getVariantId());

        ArgumentCaptor<ReqImpactItem> impactCaptor = forClass(ReqImpactItem.class);
        verify(impactMapper, times(3)).insertReqImpactItem(impactCaptor.capture());
        for (ReqImpactItem item : impactCaptor.getAllValues())
        {
            assertEquals("requirement-demand", item.getModuleCode());
            assertEquals(8L, item.getVariantId());
        }
    }

    @Test
    void importsCloseoutIndexByDemandScopedActionToken()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqDemandMapper demandMapper = mock(ReqDemandMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper,
                variantMapper, mock(ReqActivityLogService.class), actionTokenService, demandMapper);

        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT);
        token.setProjectId(1L);
        token.setVariantId(8L);
        token.setDemandId(6L);
        token.setTargetMethod(IReqActionTokenService.TARGET_PUBLISH_REPOSITORY_INDEX);
        when(actionTokenService.resolveToken("reqflow_action_closeout")).thenReturn(token);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("release/main");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(branch);
        ReqDemand demand = new ReqDemand();
        demand.setDemandId(6L);
        demand.setProjectId(1L);
        demand.setVariantId(8L);
        demand.setStatus("closeout_pending");
        when(demandMapper.selectReqDemandByDemandId(6L)).thenReturn(demand);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        doAnswer(invocation -> {
            ReqRepositoryIndexBatch batch = invocation.getArgument(0);
            batch.setBatchId(105L);
            return 1;
        }).when(batchMapper).insertReqRepositoryIndexBatch(any(ReqRepositoryIndexBatch.class));

        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setActionToken("reqflow_action_closeout");
        request.setRemoteUrl("git@example.com:reqflow-ui.git");
        request.setCommitHash("abc123");
        request.setIndexVersion("v1");
        request.setModules(Collections.singletonList(module("requirement-demand", "需求提交")));

        ReqIndexImportResult result = service.importRepositoryIndex(request, "mcp", "tester", 7L);

        assertEquals(105L, result.getBatchId());
        ArgumentCaptor<ReqRepositoryIndexBatch> batchCaptor = forClass(ReqRepositoryIndexBatch.class);
        verify(batchMapper).insertReqRepositoryIndexBatch(batchCaptor.capture());
        assertEquals("release/main", batchCaptor.getValue().getBranchName());
        assertEquals(1L, batchCaptor.getValue().getProjectId());
        verify(actionTokenService).resolveToken("reqflow_action_closeout");
    }

    @Test
    void rejectsProjectInitImpactWhenModuleCodeIsNotInPublishedModules()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, moduleMapper, impactMapper, repositoryMapper,
                variantMapper, mock(ReqActivityLogService.class), actionTokenService);

        ReqActionToken token = new ReqActionToken();
        token.setActionType(IReqActionTokenService.ACTION_PROJECT_INIT);
        token.setProjectId(1L);
        token.setVariantId(8L);
        token.setTargetMethod("publish_repository_index");
        when(actionTokenService.resolveToken("reqflow_action_branch_init")).thenReturn(token);

        ReqVariant branch = new ReqVariant();
        branch.setVariantId(8L);
        branch.setProjectId(1L);
        branch.setBaselineBranch("main");
        branch.setStatus("0");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(branch);

        ReqRepository repository = new ReqRepository();
        repository.setRepoId(2L);
        repository.setProjectId(1L);
        repository.setRepoUrl("git@example.com:reqflow-ui.git");
        repository.setRepoType("FRONTEND");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));

        ReqIndexModulePayload demandModule = module("requirement-demand", "需求提交");
        demandModule.setModuleType("PAGE_FUNCTION");
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setActionToken("reqflow_action_branch_init");
        request.setRemoteUrl("git@example.com:reqflow-ui.git");
        request.setCommitHash("abc123");
        request.setIndexVersion("v1");
        request.setModules(Collections.singletonList(demandModule));
        request.setPages(Collections.singletonList(impact("PAGE", "需求列表页")));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.importRepositoryIndex(request, "mcp", "tester", 7L));

        assertTrue(exception.getMessage().contains("影响面必须归属到本次 modules"), exception.getMessage());
        verify(batchMapper, never()).insertReqRepositoryIndexBatch(any());
        verify(moduleMapper, never()).insertReqIndexModule(any());
        verify(impactMapper, never()).insertReqImpactItem(any());
    }

    @Test
    void suggestsImpactItemsGroupedByType()
    {
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(mock(ReqRepositoryIndexBatchMapper.class), mock(ReqIndexModuleMapper.class), impactMapper, mock(ReqRepositoryMapper.class), mock(ReqVariantMapper.class), mock(ReqActivityLogService.class));
        when(impactMapper.selectLatestImpactItems(any())).thenReturn(Arrays.asList(
                impactItem("PAGE", "需求列表"),
                impactItem("API", "需求接口"),
                impactItem("TABLE", "req_demand"),
                impactItem("PERMISSION", "req:demand:list"),
                impactItem("DOCUMENT", "需求平台文档")));

        ReqImpactSuggestQuery query = new ReqImpactSuggestQuery();
        query.setProjectId(1L);
        query.setModuleCode("demand");

        ReqImpactSuggestResult result = service.suggestImpact(query);

        assertEquals(1, result.getPages().size());
        assertEquals(1, result.getApis().size());
        assertEquals(1, result.getTables().size());
        assertEquals(1, result.getPermissions().size());
        assertEquals(1, result.getDocuments().size());
    }

    @Test
    void suggestsImpactItemsForSelectedVariantBaseline()
    {
        ReqImpactItemMapper impactMapper = mock(ReqImpactItemMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(mock(ReqRepositoryIndexBatchMapper.class), mock(ReqIndexModuleMapper.class), impactMapper, mock(ReqRepositoryMapper.class), variantMapper, mock(ReqActivityLogService.class));
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(8L);
        variant.setProjectId(1L);
        variant.setBaselineBranch("customer/hlj");
        when(variantMapper.selectReqVariantByVariantId(8L)).thenReturn(variant);
        when(impactMapper.selectLatestImpactItems(any())).thenReturn(Collections.emptyList());

        ReqImpactSuggestQuery query = new ReqImpactSuggestQuery();
        query.setProjectId(1L);
        query.setVariantId(8L);
        query.setModuleCode("demand");

        service.suggestImpact(query);

        ArgumentCaptor<ReqImpactSuggestQuery> captor = forClass(ReqImpactSuggestQuery.class);
        verify(impactMapper).selectLatestImpactItems(captor.capture());
        assertEquals("customer/hlj", captor.getValue().getBranchName());
        assertEquals(8L, captor.getValue().getVariantId());
    }

    @Test
    void returnsEmptyBatchListWhenIndexBatchTableIsMissing()
    {
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(batchMapper, mock(ReqIndexModuleMapper.class),
                mock(ReqImpactItemMapper.class), mock(ReqRepositoryMapper.class), mock(ReqVariantMapper.class),
                mock(ReqActivityLogService.class));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenThrow(new BadSqlGrammarException(
                "selectReqRepositoryIndexBatchList",
                "select * from req_repository_index_batch",
                new SQLException("Table 'ry-vue.req_repository_index_batch' doesn't exist", "42S02")));

        List<ReqRepositoryIndexBatch> result = service.selectBatchList(new ReqRepositoryIndexBatch());

        assertEquals(0, result.size());
    }

    @Test
    void returnsEmptyModuleListWhenIndexModuleTableIsMissing()
    {
        ReqIndexModuleMapper moduleMapper = mock(ReqIndexModuleMapper.class);
        ReqRepositoryIndexServiceImpl service = newService(mock(ReqRepositoryIndexBatchMapper.class), moduleMapper,
                mock(ReqImpactItemMapper.class), mock(ReqRepositoryMapper.class), mock(ReqVariantMapper.class),
                mock(ReqActivityLogService.class));
        when(moduleMapper.selectReqIndexModuleList(any())).thenThrow(new BadSqlGrammarException(
                "selectReqIndexModuleList",
                "select * from req_index_module",
                new SQLException("Table 'ry-vue.req_index_module' doesn't exist", "42S02")));

        List<?> result = service.selectModuleList(new com.ruoyi.requirement.domain.ReqIndexModule());

        assertEquals(0, result.size());
    }

    private ReqRepositoryIndexServiceImpl newService(ReqRepositoryIndexBatchMapper batchMapper, ReqIndexModuleMapper moduleMapper,
            ReqImpactItemMapper impactMapper, ReqRepositoryMapper repositoryMapper, ReqVariantMapper variantMapper, ReqActivityLogService activityLogService)
    {
        return newService(batchMapper, moduleMapper, impactMapper, repositoryMapper, variantMapper, activityLogService,
                mock(IReqActionTokenService.class));
    }

    private ReqRepositoryIndexServiceImpl newService(ReqRepositoryIndexBatchMapper batchMapper, ReqIndexModuleMapper moduleMapper,
            ReqImpactItemMapper impactMapper, ReqRepositoryMapper repositoryMapper, ReqVariantMapper variantMapper,
            ReqActivityLogService activityLogService, IReqActionTokenService actionTokenService)
    {
        return newService(batchMapper, moduleMapper, impactMapper, repositoryMapper, variantMapper, activityLogService,
                actionTokenService, mock(ReqDemandMapper.class));
    }

    private ReqRepositoryIndexServiceImpl newService(ReqRepositoryIndexBatchMapper batchMapper, ReqIndexModuleMapper moduleMapper,
            ReqImpactItemMapper impactMapper, ReqRepositoryMapper repositoryMapper, ReqVariantMapper variantMapper,
            ReqActivityLogService activityLogService, IReqActionTokenService actionTokenService,
            ReqDemandMapper demandMapper)
    {
        ReqRepositoryIndexServiceImpl service = new ReqRepositoryIndexServiceImpl();
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "moduleMapper", moduleMapper);
        ReflectionTestUtils.setField(service, "impactMapper", impactMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "demandMapper", demandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);
        return service;
    }

    private ReqRepositoryIndexImportRequest baseRequest()
    {
        ReqRepositoryIndexImportRequest request = new ReqRepositoryIndexImportRequest();
        request.setProjectId(1L);
        request.setRepoId(2L);
        request.setRepoType("FRONTEND");
        request.setRemoteUrl("git@example.com:reqflow-ui.git");
        request.setBranchName("customer/hlj");
        request.setCommitHash("abc123");
        request.setIndexVersion("v1");
        return request;
    }

    private BadSqlGrammarException missingTable(String tableName)
    {
        return new BadSqlGrammarException("checkTable", "select count(1) from " + tableName,
                new SQLException("Table 'ry-vue." + tableName + "' doesn't exist", "42S02"));
    }

    private ReqIndexModulePayload module(String code, String name)
    {
        ReqIndexModulePayload module = new ReqIndexModulePayload();
        module.setModuleCode(code);
        module.setModuleName(name);
        module.setModuleType("BUSINESS");
        module.setRepoScope("FRONTEND");
        module.setRelativePath("src/views/requirement/demand/index.vue");
        return module;
    }

    private ReqIndexImpactPayload impact(String type, String name)
    {
        ReqIndexImpactPayload impact = new ReqIndexImpactPayload();
        impact.setItemType(type);
        impact.setModuleCode("demand");
        impact.setItemName(name);
        impact.setItemKey(name);
        impact.setRelativePath("src/views/requirement/demand/index.vue");
        return impact;
    }

    private ReqImpactItem impactItem(String type, String name)
    {
        ReqImpactItem item = new ReqImpactItem();
        item.setItemType(type);
        item.setItemName(name);
        return item;
    }
}
