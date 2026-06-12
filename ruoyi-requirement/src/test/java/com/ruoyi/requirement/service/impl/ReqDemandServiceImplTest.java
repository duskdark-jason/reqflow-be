package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.mapper.ReqActionTokenMapper;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.ReqActivityLogService;

class ReqDemandServiceImplTest
{
    @org.junit.jupiter.api.AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

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
        when(reqDemandMapper.countEnabledUserByRoleKey("requirement_developer", 8L)).thenReturn(1);

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
    void rejectsInsertWhenDeveloperIsMissing()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ReqDemand demand = demand(10L, 31L);
        demand.setDeveloperUserId(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertReqDemand(demand));

        assertTrue(exception.getMessage().contains("请选择指定开发人员"));
        verify(reqDemandMapper, never()).insertReqDemand(any());
    }

    @Test
    void rejectsInsertWhenDemandSourceIsBlank()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);

        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository(21L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Collections.singletonList(batch(21L, "main")));

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);

        ReqDemand demand = demand(10L, 31L);
        demand.setDemandSource(" ");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertReqDemand(demand));

        assertTrue(exception.getMessage().contains("需求来源不能为空"));
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
        when(reqDemandMapper.countEnabledUserByRoleKey("requirement_developer", 8L)).thenReturn(1);
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
        when(reqDemandMapper.countEnabledUserByRoleKey("requirement_developer", 8L)).thenReturn(1);
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
    void rejectsRequirementUserExecutingDeveloperStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateReqDemandStatus(1L, "plan_ready", "req-user"));

        assertTrue(exception.getMessage().contains("当前角色不能执行该流程动作"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void rejectsDeveloperExecutingRequirementUserStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("plan_ready");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateReqDemandStatus(1L, "confirmed", "developer"));

        assertTrue(exception.getMessage().contains("当前角色不能执行该流程动作"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void adminCanExecuteAnyValidStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(1L, "plan_ready", "admin")).thenReturn(1);
        mockLoginUser(1L, "admin");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(1L, "plan_ready", "admin"));
        verify(reqDemandMapper).updateReqDemandStatus(1L, "plan_ready", "admin");
    }

    @Test
    void selectedDeveloperCanExecuteDeveloperStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(1L, "plan_ready", "developer")).thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        assertEquals(1, service.updateReqDemandStatus(1L, "plan_ready", "developer"));
        verify(reqDemandMapper).updateReqDemandStatus(1L, "plan_ready", "developer");
    }

    @Test
    void rejectsOtherDeveloperExecutingDeveloperStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        mockLoginUser(9L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateReqDemandStatus(1L, "plan_ready", "developer"));

        assertTrue(exception.getMessage().contains("只有指定开发人员"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void creatorCanExecuteRequirementStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("plan_ready");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(1L, "confirmed", "creator")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(1L, "confirmed", "creator"));
        verify(reqDemandMapper).updateReqDemandStatus(1L, "confirmed", "creator");
    }

    @Test
    void rejectsNonParticipantReadingDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        mockLoginUser(9L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.selectReqDemandByDemandId(1L));

        assertTrue(exception.getMessage().contains("当前用户不是该需求参与人"));
    }

    @Test
    void deletesDemandAndRelatedGeneratedData()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqActionTokenMapper actionTokenMapper = mock(ReqActionTokenMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setDemandNo("REQ-001");
        current.setTitle("删除测试");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(reqDemandMapper.deleteReqDemandByDemandIds(aryEq(new Long[] {1L}))).thenReturn(1);
        mockLoginUser(1L, "admin");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);
        ReflectionTestUtils.setField(service, "actionTokenMapper", actionTokenMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.deleteReqDemandByDemandIds(new Long[] {1L}, "admin"));
        verify(packageVersionMapper).deleteReqPackageVersionByDemandIds(aryEq(new Long[] {1L}));
        verify(actionTokenMapper).deleteReqActionTokenByDemandIds(aryEq(new Long[] {1L}));
        verify(reqDemandMapper).deleteReqDemandByDemandIds(aryEq(new Long[] {1L}));
        verify(activityLogService).record(eq(1L), eq(10L), eq(1L), eq("demand_deleted"), eq("web"),
                contains("REQ-001"), isNull());
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
        when(reqDemandMapper.countEnabledUserByRoleKey("requirement_developer", 8L)).thenReturn(1);
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
        demand.setTitle("Demand");
        demand.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(demand);
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction assessmentInstruction = new ReqActionInstruction();
        assessmentInstruction.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        assessmentInstruction.setTargetMethod("upload_requirement_assessment");
        assessmentInstruction.setToken("reqflow_action_assessment_token");
        assessmentInstruction.setPrompt("请先生成可行性评估。");
        assessmentInstruction.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_PLAN),
                eq(10L),
                eq(31L),
                eq(5L),
                eq("upload_requirement_assessment"),
                any(),
                eq("生成需求评估与设计"),
                eq("approver"))).thenReturn(assessmentInstruction);
        ReqActionInstruction designInstruction = new ReqActionInstruction();
        designInstruction.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        designInstruction.setTargetMethod("save_requirement_package");
        designInstruction.setToken("reqflow_action_design_token");
        designInstruction.setPrompt("请根据基础需求生成详细需求设计。");
        designInstruction.setContent("design instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_PLAN),
                eq(10L),
                eq(31L),
                eq(5L),
                eq("save_requirement_package"),
                any(),
                eq("复制需求设计回写指令"),
                eq("approver"))).thenReturn(designInstruction);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementPlanInstruction(5L, "approver");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_PLAN, instruction.getActionType());
        assertEquals("upload_requirement_assessment", instruction.getTargetMethod());
        assertTrue(instruction.getContent().contains("upload_requirement_assessment"));
        assertTrue(instruction.getContent().contains("save_requirement_package"));
        assertFalse(instruction.getContent().contains("save_development_plan"));
        assertFalse(instruction.getContent().contains("执行计划"));
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_requirement_assessment"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.save_requirement_package"));
        assertTrue(instruction.getContent().contains("arguments.actionToken"));
        assertTrue(instruction.getContent().contains("不是 X-MCP-Key"));
        assertTrue(instruction.getContent().contains("24小时内有效"));
        assertTrue(instruction.getContent().contains("仅可使用一次"));
        assertTrue(instruction.getContent().contains("重新生成"));
        assertTrue(instruction.getContent().contains("需求设计阶段"));
        assertTrue(instruction.getContent().contains("可行性评估 actionToken: reqflow_action_assessment_token"));
        assertTrue(instruction.getContent().contains("需求设计 actionToken: reqflow_action_design_token"));
        assertTrue(instruction.getContent().contains("评估结论"));
        assertTrue(instruction.getContent().contains("需澄清"));
        assertTrue(instruction.getContent().contains("暂不可实现"));
        assertTrue(instruction.getContent().contains("停止生成 requirement.md"));
        assertTrue(instruction.getContent().contains("建议任务分支: feature/req-5-demand"));
        assertTrue(instruction.getContent().contains("git pull --ff-only"));
        assertTrue(instruction.getContent().contains("不生成 plan.md"));
        assertTrue(instruction.getContent().contains("最终版 requirement.md 保留在本地任务分支"));
        assertTrue(instruction.getContent().contains("业务背景"));
        assertTrue(instruction.getContent().contains("附件"));
        assertTrue(instruction.getContent().contains("demandId: 5"));
        assertTrue(instruction.getContent().contains("demandNo: REQ-005"));
    }

    @Test
    void createsRequirementDevelopInstructionForDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqDemand demand = demand(10L, 31L);
        demand.setDemandId(6L);
        demand.setDemandNo("REQ-006");
        demand.setTitle("Demand");
        demand.setStatus("confirmed");
        when(reqDemandMapper.selectReqDemandByDemandId(6L)).thenReturn(demand);
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction created = new ReqActionInstruction();
        created.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        created.setTargetMethod("save_development_plan");
        created.setToken("reqflow_action_plan_token");
        created.setPrompt("请按需求设计和执行计划完成开发。");
        created.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP),
                eq(10L),
                eq(31L),
                eq(6L),
                eq("save_development_plan"),
                any(),
                eq("生成执行任务指令"),
                eq("developer"))).thenReturn(created);
        ReqActionInstruction reportInstruction = new ReqActionInstruction();
        reportInstruction.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        reportInstruction.setTargetMethod("upload_execution_report");
        reportInstruction.setToken("reqflow_action_report_token");
        reportInstruction.setPrompt("请回写执行报告。");
        reportInstruction.setContent("report instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP),
                eq(10L),
                eq(31L),
                eq(6L),
                eq("upload_execution_report"),
                any(),
                eq("复制执行报告指令"),
                eq("developer"))).thenReturn(reportInstruction);
        ReqActionInstruction reviewInstruction = new ReqActionInstruction();
        reviewInstruction.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        reviewInstruction.setTargetMethod("upload_review_report");
        reviewInstruction.setToken("reqflow_action_review_token");
        reviewInstruction.setPrompt("请回写 Review 报告。");
        reviewInstruction.setContent("review instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP),
                eq(10L),
                eq(31L),
                eq(6L),
                eq("upload_review_report"),
                any(),
                eq("复制Review报告指令"),
                eq("developer"))).thenReturn(reviewInstruction);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementDevelopInstruction(6L, "developer");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP, instruction.getActionType());
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.save_development_plan"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_execution_report"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_review_report"));
        assertTrue(instruction.getContent().contains("任务分支: feature/req-6-demand"));
        assertTrue(instruction.getContent().contains("执行计划 actionToken: reqflow_action_plan_token"));
        assertTrue(instruction.getContent().contains("执行报告 actionToken: reqflow_action_report_token"));
        assertTrue(instruction.getContent().contains("Review报告 actionToken: reqflow_action_review_token"));
        assertTrue(instruction.getContent().contains("arguments.actionToken"));
        assertTrue(instruction.getContent().contains("24小时内有效"));
        assertTrue(instruction.getContent().contains("仅可使用一次"));
        assertTrue(instruction.getContent().contains("重新生成"));
        assertTrue(instruction.getContent().contains("不得重新生成不同任务分支"));
        assertTrue(instruction.getContent().contains("继续在同一任务分支补充 execution-report.md 和 review-report.md"));
        assertTrue(instruction.getContent().contains("执行计划"));
        assertTrue(instruction.getContent().contains("执行报告"));
        assertTrue(instruction.getContent().contains("Review 报告"));
        assertTrue(instruction.getContent().contains("demandId: 6"));
        assertTrue(instruction.getContent().contains("demandNo: REQ-006"));
    }

    @Test
    void recordsRepairingStatusTransition()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("review");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "repairing", "approver")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        service.updateReqDemandStatus(5L, "repairing", "approver");

        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_repairing"),
                eq("web"), contains("提交返修"), isNull());
    }

    private ReqDemand demand(Long projectId, Long variantId)
    {
        ReqDemand demand = new ReqDemand();
        demand.setProjectId(projectId);
        demand.setVariantId(variantId);
        demand.setTitle("新增需求");
        demand.setDemandType("FEATURE");
        demand.setDemandSource("BUSINESS");
        demand.setBusinessBackground("业务背景");
        demand.setExpectedResult("预期结果");
        demand.setAcceptanceText("验收标准");
        demand.setCreatorId(7L);
        demand.setDeveloperUserId(8L);
        return demand;
    }

    private void mockLoginUser(Long userId, String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName(roleKey);
        user.setRoles(List.of(role));
        LoginUser loginUser = new LoginUser(userId, 1L, user, Set.of("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
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
