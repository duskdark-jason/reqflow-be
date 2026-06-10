package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.ruoyi.requirement.domain.ReqImpactItem;
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
        ReqRepositoryIndexServiceImpl service = new ReqRepositoryIndexServiceImpl();
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "moduleMapper", moduleMapper);
        ReflectionTestUtils.setField(service, "impactMapper", impactMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
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
