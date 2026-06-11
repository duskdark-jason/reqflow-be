package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
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
    void rejectsUpdateWhenDemandIsNotDraft()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = new ReqDemand();
        current.setDemandId(1L);
        current.setCreatorId(7L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);

        ReqDemand update = new ReqDemand();
        update.setDemandId(1L);
        update.setCreatorId(7L);
        update.setTitle("修改需求");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateReqDemand(update));

        assertTrue(exception.getMessage().contains("只有未提交需求可以修改"));
        verify(reqDemandMapper, never()).updateReqDemand(any());
    }

    @Test
    void rejectsUpdateWhenEditorIsNotCreator()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = new ReqDemand();
        current.setDemandId(1L);
        current.setCreatorId(7L);
        current.setStatus("draft");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);

        ReqDemand update = new ReqDemand();
        update.setDemandId(1L);
        update.setCreatorId(8L);
        update.setTitle("修改需求");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateReqDemand(update));

        assertTrue(exception.getMessage().contains("只有需求创建人可以修改"));
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
        when(reqDemandMapper.selectDemandCount()).thenReturn(2L);
        when(reqDemandMapper.insertReqDemand(any())).thenReturn(1);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        ReqDemand demand = demand(10L, 31L);

        service.insertReqDemand(demand);

        assertEquals("REQ-003", demand.getDemandNo());
        assertEquals("draft", demand.getStatus());
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
        when(reqDemandMapper.selectDemandCount()).thenReturn(3L);
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

        assertEquals("REQ-004", demand.getDemandNo());
        assertEquals("draft", demand.getStatus());
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
        when(reqDemandMapper.selectDemandCount()).thenReturn(6L);
        when(reqDemandMapper.insertReqDemand(any())).thenReturn(1);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        ReqDemand demand = demand(10L, 31L);
        demand.setDemandNo("MANUAL-001");
        demand.setCreatorId(999L);

        service.insertReqDemand(demand);

        assertNotEquals("MANUAL-001", demand.getDemandNo());
        assertEquals("REQ-007", demand.getDemandNo());
        assertEquals(0L, demand.getCreatorId());
        verify(reqDemandMapper).insertReqDemand(demand);
    }

    @Test
    void createsRequirementPlanInstructionForDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqDemand demand = demand(10L, 31L);
        demand.setDemandId(5L);
        demand.setDemandNo("REQ-005");
        demand.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(demand);

        ReqActionInstruction created = new ReqActionInstruction();
        created.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        created.setTargetMethod("save_requirement_package");
        created.setToken("reqflow_action_demo");
        created.setPrompt("请基于需求上下文生成需求说明和执行计划。");
        created.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_PLAN),
                eq(10L),
                eq(31L),
                eq(5L),
                eq("save_requirement_package"),
                any(),
                eq("复制MCP编排指令"),
                eq("approver"))).thenReturn(created);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementPlanInstruction(5L, "approver");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_PLAN, instruction.getActionType());
        assertTrue(instruction.getContent().contains("save_requirement_package"));
        assertTrue(instruction.getContent().contains("save_development_plan"));
        assertTrue(instruction.getContent().contains("demandId: 5"));
        assertTrue(instruction.getContent().contains("demandNo: REQ-005"));
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
