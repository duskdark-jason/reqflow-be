package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.ReqActivityLogService;

class ReqDemandServiceImplTest
{
    @Test
    void rejectsInvalidStatusChangeFromGenericUpdate()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = new ReqDemand();
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);

        ReqDemand update = new ReqDemand();
        update.setDemandId(1L);
        update.setStatus("completed");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        assertThrows(ServiceException.class, () -> service.updateReqDemand(update));
        verify(reqDemandMapper, never()).updateReqDemand(any());
    }

    @Test
    void rejectsInsertWhenProjectBranchIsNotInitialized()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);

        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(repository(21L), repository(22L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Collections.singletonList(batch(21L, "main")));

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.insertReqDemand(demand(10L, 31L)));

        assertTrue(exception.getMessage().contains("项目分支尚未初始化完成"));
        verify(reqDemandMapper, never()).insertReqDemand(any());
    }

    @Test
    void insertsDemandWhenProjectBranchIsInitialized()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);

        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(repository(21L), repository(22L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Arrays.asList(
                batch(21L, "main"), batch(22L, "main")));
        when(reqDemandMapper.selectTodayDemandCount()).thenReturn(2);
        when(reqDemandMapper.insertReqDemand(any())).thenReturn(1);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        ReqDemand demand = demand(10L, 31L);

        service.insertReqDemand(demand);

        assertTrue(demand.getDemandNo().contains("REQ-"));
        verify(reqDemandMapper).insertReqDemand(demand);
    }

    @Test
    void insertsNewFeatureDemandWhenBranchHasIndexesWithoutExistingModules()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);

        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository(21L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Collections.singletonList(batch(21L, "main")));
        when(reqDemandMapper.selectTodayDemandCount()).thenReturn(3);
        when(reqDemandMapper.insertReqDemand(any())).thenReturn(1);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        ReqDemand demand = demand(10L, 31L);
        demand.setRemark("新增功能");

        service.insertReqDemand(demand);

        assertTrue(demand.getDemandNo().endsWith("-004"));
        verify(reqDemandMapper).insertReqDemand(demand);
    }

    @Test
    void overwritesClientProvidedDemandNoOnInsert()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);

        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository(21L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Collections.singletonList(batch(21L, "main")));
        when(reqDemandMapper.selectTodayDemandCount()).thenReturn(6);
        when(reqDemandMapper.insertReqDemand(any())).thenReturn(1);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        ReqDemand demand = demand(10L, 31L);
        demand.setDemandNo("MANUAL-001");

        service.insertReqDemand(demand);

        assertNotEquals("MANUAL-001", demand.getDemandNo());
        assertTrue(demand.getDemandNo().startsWith("REQ-"));
        assertTrue(demand.getDemandNo().endsWith("-007"));
        verify(reqDemandMapper).insertReqDemand(demand);
    }

    private ReqDemand demand(Long projectId, Long variantId)
    {
        ReqDemand demand = new ReqDemand();
        demand.setProjectId(projectId);
        demand.setVariantId(variantId);
        demand.setTitle("新增需求");
        demand.setDemandType("FEATURE");
        return demand;
    }

    private ReqVariant variant(Long variantId, Long projectId, String branchName)
    {
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(variantId);
        variant.setProjectId(projectId);
        variant.setVariantName("主线");
        variant.setVariantCode("MAIN");
        variant.setBaselineBranch(branchName);
        variant.setStatus("0");
        return variant;
    }

    private ReqRepository repository(Long repoId)
    {
        ReqRepository repository = new ReqRepository();
        repository.setRepoId(repoId);
        repository.setProjectId(10L);
        repository.setRepoName("仓库" + repoId);
        repository.setRepoType("BACKEND");
        repository.setRepoUrl("git@example.com:reqflow-" + repoId + ".git");
        repository.setDefaultBranch("main");
        repository.setStatus("0");
        return repository;
    }

    private ReqRepositoryIndexBatch batch(Long repoId, String branchName)
    {
        ReqRepositoryIndexBatch batch = new ReqRepositoryIndexBatch();
        batch.setProjectId(10L);
        batch.setRepoId(repoId);
        batch.setBranchName(branchName);
        batch.setStatus("imported");
        return batch;
    }

}
