package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqModule;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqProjectInitRepositoryItem;
import com.ruoyi.requirement.dto.ReqProjectInitRequest;
import com.ruoyi.requirement.dto.ReqProjectInitResponse;
import com.ruoyi.requirement.dto.ReqProjectInitVariantItem;
import com.ruoyi.requirement.mapper.ReqIndexModuleMapper;
import com.ruoyi.requirement.mapper.ReqModuleMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;

class ReqProjectInitServiceImplTest
{
    @Test
    void createsProjectRepositoriesAndVariantsInOneAggregate()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));

        doAnswer(invocation -> {
            ReqProject project = invocation.getArgument(0);
            project.setProjectId(10L);
            return 1;
        }).when(projectMapper).insertReqProject(any(ReqProject.class));
        doAnswer(invocation -> {
            ReqRepository repository = invocation.getArgument(0);
            repository.setRepoId("FRONTEND".equals(repository.getRepoType()) ? 21L : 22L);
            return 1;
        }).when(repositoryMapper).insertReqRepository(any(ReqRepository.class));
        doAnswer(invocation -> {
            ReqVariant variant = invocation.getArgument(0);
            variant.setVariantId(31L);
            return 1;
        }).when(variantMapper).insertReqVariant(any(ReqVariant.class));

        ReqProjectInitResponse response = service.insertProjectInit(baseRequest(), "admin");

        assertEquals(10L, response.getProject().getProjectId());
        assertEquals(2, response.getRepositories().size());
        assertEquals(1, response.getVariants().size());

        ArgumentCaptor<ReqRepository> repositoryCaptor = forClass(ReqRepository.class);
        verify(repositoryMapper, times(2)).insertReqRepository(repositoryCaptor.capture());
        ReqRepository firstRepository = repositoryCaptor.getAllValues().get(0);
        assertEquals(10L, firstRepository.getProjectId());
        assertEquals("admin", firstRepository.getCreateBy());
        assertEquals("uninitialized", firstRepository.getHarnessStatus());
        assertNull(firstRepository.getLocalPathHint());

        ArgumentCaptor<ReqVariant> variantCaptor = forClass(ReqVariant.class);
        verify(variantMapper).insertReqVariant(variantCaptor.capture());
        assertEquals(10L, variantCaptor.getValue().getProjectId());
        assertEquals("admin", variantCaptor.getValue().getCreateBy());
    }

    @Test
    void loadsInitContextWithModuleAndIndexSummaries()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqModuleMapper moduleMapper = mock(ReqModuleMapper.class);
        ReqIndexModuleMapper indexModuleMapper = mock(ReqIndexModuleMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                moduleMapper, indexModuleMapper, batchMapper);

        when(projectMapper.selectReqProjectByProjectId(10L)).thenReturn(project(10L));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(repository(21L, "FRONTEND"), repository(22L, "BACKEND")));
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.singletonList(variant(31L)));
        when(moduleMapper.selectReqModuleList(any())).thenReturn(Collections.singletonList(module("manual-demand")));
        when(indexModuleMapper.selectReqIndexModuleList(any())).thenReturn(Arrays.asList(indexModule("demand"), indexModule("demand"), indexModule("package")));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Arrays.asList(batch(99L, 22L, "def456"), batch(98L, 21L, "abc123")));

        ReqProjectInitResponse response = service.selectProjectInit(10L);

        assertEquals(1, response.getModuleSummary().getManualModules());
        assertEquals(2, response.getModuleSummary().getIndexedModules());
        assertEquals(3, response.getModuleSummary().getTotalModules());
        assertEquals("def456", response.getIndexSummary().getLatestCommit());
        assertEquals(2, response.getIndexSummary().getIndexedRepositoryCount());
        assertEquals(0, response.getIndexSummary().getUnindexedRepositoryCount());
        assertEquals("主线", response.getVariants().get(0).getBranchLabel());
        assertTrue(response.getInitChecklist().getProjectReady());
        assertTrue(response.getInitChecklist().getRepositoryReady());
        assertTrue(response.getInitChecklist().getVariantReady());
        assertTrue(response.getInitChecklist().getModuleReady());
        assertTrue(response.getInitChecklist().getIndexReady());
    }

    @Test
    void rejectsPersonalAbsolutePathBeforeWritingAggregate()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));
        ReqProjectInitRequest request = baseRequest();
        request.getRepositories().get(0).setRepoUrl("/Users/dusk/Projects/reqflow/reqflow-ui");

        assertThrows(ServiceException.class, () -> service.insertProjectInit(request, "admin"));

        verifyNoInteractions(projectMapper, repositoryMapper, variantMapper);
    }

    @Test
    void acceptsBranchLabelAndRealBranchNameWithoutManualVariantCode()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));

        doAnswer(invocation -> {
            ReqProject project = invocation.getArgument(0);
            project.setProjectId(10L);
            return 1;
        }).when(projectMapper).insertReqProject(any(ReqProject.class));

        ReqProjectInitRequest request = baseRequest();
        ReqProjectInitVariantItem branch = request.getVariants().get(0);
        branch.setBranchLabel("黑龙江医保");
        branch.setVariantName(null);
        branch.setVariantCode(null);
        branch.setCustomerName(null);
        branch.setBaselineBranch("release/hlj-main");

        service.insertProjectInit(request, "admin");

        ArgumentCaptor<ReqVariant> variantCaptor = forClass(ReqVariant.class);
        verify(variantMapper).insertReqVariant(variantCaptor.capture());
        ReqVariant savedBranch = variantCaptor.getValue();
        assertEquals("黑龙江医保", savedBranch.getVariantName());
        assertEquals("release/hlj-main", savedBranch.getBaselineBranch());
        assertEquals("RELEASE_HLJ_MAIN", savedBranch.getVariantCode());
    }

    @Test
    void generatesDistinctVariantCodeWhenBranchNameHasNoAsciiToken()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));

        doAnswer(invocation -> {
            ReqProject project = invocation.getArgument(0);
            project.setProjectId(10L);
            return 1;
        }).when(projectMapper).insertReqProject(any(ReqProject.class));

        ReqProjectInitRequest request = baseRequest();
        ReqProjectInitVariantItem branch = request.getVariants().get(0);
        branch.setBranchLabel("黑龙江医保");
        branch.setVariantName(null);
        branch.setVariantCode(null);
        branch.setBaselineBranch("黑龙江主线");

        service.insertProjectInit(request, "admin");

        ArgumentCaptor<ReqVariant> variantCaptor = forClass(ReqVariant.class);
        verify(variantMapper).insertReqVariant(variantCaptor.capture());
        assertTrue(variantCaptor.getValue().getVariantCode().startsWith("BRANCH_"));
    }

    @Test
    void updatesAggregateAndDeletesChildrenRemovedFromWizard()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));
        ReqProjectInitRequest request = baseRequest();
        request.getProject().setProjectId(10L);
        request.getRepositories().get(0).setRepoId(21L);
        request.getRepositories().get(1).setRepoId(22L);
        request.getVariants().get(0).setVariantId(31L);

        service.updateProjectInit(request, "admin");

        verify(projectMapper).updateReqProject(any(ReqProject.class));
        verify(repositoryMapper, times(2)).updateReqRepository(any(ReqRepository.class));
        verify(repositoryMapper).deleteReqRepositoryByProjectIdAndRepoIdsNotIn(eq(10L), argThat(ids -> Arrays.equals(ids, new Long[] {21L, 22L})));
        verify(repositoryMapper, never()).insertReqRepository(any(ReqRepository.class));
        verify(variantMapper).updateReqVariant(any(ReqVariant.class));
        verify(variantMapper).deleteReqVariantByProjectIdAndVariantIdsNotIn(eq(10L), argThat(ids -> Arrays.equals(ids, new Long[] {31L})));
    }

    @Test
    void keepsNewChildrenInsertedDuringAggregateUpdate()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));
        ReqProjectInitRequest request = baseRequest();
        request.getProject().setProjectId(10L);
        request.getRepositories().get(0).setRepoId(21L);

        doAnswer(invocation -> {
            ReqRepository repository = invocation.getArgument(0);
            repository.setRepoId(22L);
            return 1;
        }).when(repositoryMapper).insertReqRepository(any(ReqRepository.class));
        doAnswer(invocation -> {
            ReqVariant variant = invocation.getArgument(0);
            variant.setVariantId(31L);
            return 1;
        }).when(variantMapper).insertReqVariant(any(ReqVariant.class));

        service.updateProjectInit(request, "admin");

        verify(repositoryMapper).deleteReqRepositoryByProjectIdAndRepoIdsNotIn(eq(10L), argThat(ids -> Arrays.equals(ids, new Long[] {21L, 22L})));
        verify(variantMapper).deleteReqVariantByProjectIdAndVariantIdsNotIn(eq(10L), argThat(ids -> Arrays.equals(ids, new Long[] {31L})));
    }

    @Test
    void marksChecklistIncompleteWhenRequiredKnowledgeIsMissing()
    {
        ReqProjectMapper projectMapper = mock(ReqProjectMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqProjectInitServiceImpl service = newService(projectMapper, repositoryMapper, variantMapper,
                mock(ReqModuleMapper.class), mock(ReqIndexModuleMapper.class), mock(ReqRepositoryIndexBatchMapper.class));

        when(projectMapper.selectReqProjectByProjectId(10L)).thenReturn(project(10L));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository(21L, "FRONTEND")));
        when(variantMapper.selectReqVariantList(any())).thenReturn(Collections.emptyList());

        ReqProjectInitResponse response = service.selectProjectInit(10L);

        assertTrue(response.getInitChecklist().getProjectReady());
        assertFalse(response.getInitChecklist().getRepositoryReady());
        assertFalse(response.getInitChecklist().getVariantReady());
        assertFalse(response.getInitChecklist().getModuleReady());
        assertFalse(response.getInitChecklist().getIndexReady());
    }

    private ReqProjectInitServiceImpl newService(ReqProjectMapper projectMapper, ReqRepositoryMapper repositoryMapper,
            ReqVariantMapper variantMapper, ReqModuleMapper moduleMapper, ReqIndexModuleMapper indexModuleMapper,
            ReqRepositoryIndexBatchMapper batchMapper)
    {
        ReqProjectInitServiceImpl service = new ReqProjectInitServiceImpl();
        ReflectionTestUtils.setField(service, "projectMapper", projectMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "moduleMapper", moduleMapper);
        ReflectionTestUtils.setField(service, "indexModuleMapper", indexModuleMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        return service;
    }

    private ReqProjectInitRequest baseRequest()
    {
        ReqProject project = project(10L);
        project.setProjectId(null);
        project.setRemark("初始化项目");

        ReqProjectInitRequest request = new ReqProjectInitRequest();
        request.setProject(project);
        request.setRepositories(Arrays.asList(
                repositoryItem("REQFLOW-UI", "FRONTEND", "git@example.com:reqflow-ui.git", "main"),
                repositoryItem("REQFLOW-BE", "BACKEND", "git@example.com:reqflow-be.git", "main")));
        request.setVariants(Collections.singletonList(variantItem("主线", "MAIN", "main")));
        request.setRemark("只保存共享 Git 信息");
        return request;
    }

    private ReqProject project(Long projectId)
    {
        ReqProject project = new ReqProject();
        project.setProjectId(projectId);
        project.setProjectName("需求平台");
        project.setProjectCode("REQFLOW");
        project.setDescription("需求平台项目");
        project.setStatus("0");
        return project;
    }

    private ReqProjectInitRepositoryItem repositoryItem(String name, String type, String url, String branch)
    {
        ReqProjectInitRepositoryItem item = new ReqProjectInitRepositoryItem();
        item.setRepoName(name);
        item.setRepoType(type);
        item.setRepoUrl(url);
        item.setDefaultBranch(branch);
        item.setStatus("0");
        item.setLocalPathHint("/Users/should-not-be-saved");
        return item;
    }

    private ReqProjectInitVariantItem variantItem(String name, String code, String branch)
    {
        ReqProjectInitVariantItem item = new ReqProjectInitVariantItem();
        item.setVariantName(name);
        item.setVariantCode(code);
        item.setCustomerName("通用");
        item.setScopeType("MAINLINE");
        item.setBaselineBranch(branch);
        item.setBranchPolicy("shared_baseline");
        item.setStatus("0");
        return item;
    }

    private ReqRepository repository(Long repoId, String type)
    {
        ReqRepository repository = new ReqRepository();
        repository.setRepoId(repoId);
        repository.setProjectId(10L);
        repository.setRepoName(type + "仓库");
        repository.setRepoType(type);
        repository.setRepoUrl("git@example.com:reqflow.git");
        repository.setDefaultBranch("main");
        repository.setStatus("0");
        return repository;
    }

    private ReqVariant variant(Long variantId)
    {
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(variantId);
        variant.setProjectId(10L);
        variant.setVariantName("主线");
        variant.setVariantCode("MAIN");
        variant.setBaselineBranch("main");
        variant.setStatus("0");
        return variant;
    }

    private ReqModule module(String code)
    {
        ReqModule module = new ReqModule();
        module.setModuleCode(code);
        module.setStatus("0");
        return module;
    }

    private ReqIndexModule indexModule(String code)
    {
        ReqIndexModule module = new ReqIndexModule();
        module.setModuleCode(code);
        module.setStatus("0");
        return module;
    }

    private ReqRepositoryIndexBatch batch(Long batchId, Long repoId, String commit)
    {
        ReqRepositoryIndexBatch batch = new ReqRepositoryIndexBatch();
        batch.setBatchId(batchId);
        batch.setRepoId(repoId);
        batch.setCommitHash(commit);
        batch.setStatus("imported");
        batch.setCreateTime(new Date(batchId));
        return batch;
    }
}
