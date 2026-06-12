package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
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
import java.util.Date;
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
import com.ruoyi.requirement.domain.ReqPackageVersion;
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
                () -> service.updateReqDemandStatus(1L, "plan_pending", "req-user"));

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
        when(reqDemandMapper.updateReqDemandStatus(1L, "plan_pending", "admin")).thenReturn(1);
        mockLoginUser(1L, "admin");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(1L, "plan_pending", "admin"));
        verify(reqDemandMapper).updateReqDemandStatus(1L, "plan_pending", "admin");
    }

    @Test
    void selectedDeveloperCanExecuteDeveloperStatusAction()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(1L, "plan_pending", "developer")).thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        assertEquals(1, service.updateReqDemandStatus(1L, "plan_pending", "developer"));
        verify(reqDemandMapper).updateReqDemandStatus(1L, "plan_pending", "developer");
    }

    @Test
    void rejectsDesignCompletionWhenSupplementIsNewerThanRequirementDesign()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("plan_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(packageVersionMapper.selectLatestByDemandIdAndArtifactType(1L, "requirement"))
                .thenReturn(packageVersion(11L, "requirement", 1_000L));
        when(packageVersionMapper.selectLatestByDemandIdAndArtifactType(1L, "requirement_supplement"))
                .thenReturn(packageVersion(12L, "requirement_supplement", 2_000L));
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateReqDemandStatus(1L, "plan_ready", "developer"));

        assertTrue(exception.getMessage().contains("请先生成新的需求设计"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void selectedDeveloperCanCompleteDesignAfterNewRequirementDesign()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(1L);
        current.setStatus("plan_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);
        when(packageVersionMapper.selectLatestByDemandIdAndArtifactType(1L, "requirement"))
                .thenReturn(packageVersion(12L, "requirement", 2_000L));
        when(packageVersionMapper.selectLatestByDemandIdAndArtifactType(1L, "requirement_supplement"))
                .thenReturn(packageVersion(11L, "requirement_supplement", 1_000L));
        when(reqDemandMapper.updateReqDemandStatus(1L, "plan_ready", "developer")).thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);

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
                () -> service.updateReqDemandStatus(1L, "plan_pending", "developer"));

        assertTrue(exception.getMessage().contains("只有指定开发人员"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void submittingDemandCreatesDraftArtifactsForMcp()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setTitle("提交后生成草稿");
        current.setStatus("draft");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.countEnabledUserByRoleKey("requirement_developer", 8L)).thenReturn(1);
        when(reqDemandMapper.updateReqDemandStatus(5L, "submitted", "creator")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(5L, "submitted", "creator"));

        verify(packageVersionMapper).insertReqPackageVersion(argThat(packageVersion ->
                hasPackage(packageVersion, "requirement_draft", "提交后生成草稿", "REQ-005")
                        && packageVersion.getContent().contains("业务背景")
                        && packageVersion.getContent().contains("预期结果")
                        && packageVersion.getContent().contains("验收标准")));
        verify(packageVersionMapper).insertReqPackageVersion(argThat(packageVersion ->
                hasPackage(packageVersion, "context_manifest", "\"demandNo\": \"REQ-005\"", "\"projectId\": 10")));
    }

    @Test
    void selectedDeveloperCanReturnDemandForSupplement()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("plan_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "supplement_required", "developer")).thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(5L, "supplement_required", "developer"));
        verify(reqDemandMapper).updateReqDemandStatus(5L, "supplement_required", "developer");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_supplement_required"),
                eq("web"), contains("需要补充说明"), isNull());
    }

    @Test
    void selectedDeveloperCanRejectDemandAsUnfeasible()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "rejected", "developer")).thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(5L, "rejected", "developer"));
        verify(reqDemandMapper).updateReqDemandStatus(5L, "rejected", "developer");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_rejected"),
                eq("web"), contains("需求无法实现"), isNull());
    }

    @Test
    void creatorCanSubmitSupplementAndReturnDemandToDesignStage()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("supplement_required");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "plan_pending", "creator")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.submitDemandSupplement(5L, "补充业务规则和验收口径", "creator"));

        verify(packageVersionMapper).insertReqPackageVersion(argThat(packageVersion ->
                hasPackage(packageVersion, "requirement_supplement", "补充业务规则", "验收口径")));
        verify(reqDemandMapper).updateReqDemandStatus(5L, "plan_pending", "creator");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_supplement_submitted"),
                eq("web"), contains("提交补充说明"), isNull());
    }

    @Test
    void creatorCanSubmitDesignAdjustmentAndReturnDemandToDesignStage()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("plan_ready");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "plan_pending", "creator")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.submitDemandSupplement(5L, "请补充异常分支和验收样例", "creator"));

        verify(packageVersionMapper).insertReqPackageVersion(argThat(packageVersion ->
                hasPackage(packageVersion, "requirement_supplement", "请补充异常分支", "验收样例")
                        && "需求设计调整说明".equals(packageVersion.getVersionNote())));
        verify(reqDemandMapper).updateReqDemandStatus(5L, "plan_pending", "creator");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_design_adjustment_submitted"),
                eq("web"), contains("提交需求设计调整说明"), isNull());
    }

    @Test
    void rejectsSupplementWhenDemandIsNotWaitingForSupplement()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.submitDemandSupplement(5L, "补充说明", "creator"));

        assertTrue(exception.getMessage().contains("当前状态不需要补充说明"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void rejectsSupplementFromNonCreator()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqPackageVersionMapper packageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setStatus("supplement_required");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        mockLoginUser(9L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "packageVersionMapper", packageVersionMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.submitDemandSupplement(5L, "补充说明", "other"));

        assertTrue(exception.getMessage().contains("只有需求创建人可以补充说明"));
        verify(packageVersionMapper, never()).insertReqPackageVersion(any());
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
    void createsRequirementAnalysisInstructionForSubmittedDemand()
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
        assessmentInstruction.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS);
        assessmentInstruction.setToken("reqflow_action_assessment_token");
        assessmentInstruction.setPrompt("请先生成可行性评估。");
        assessmentInstruction.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_PLAN),
                eq(10L),
                eq(31L),
                eq(5L),
                eq(IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS),
                any(),
                eq("生成需求分析指令"),
                eq("approver"))).thenReturn(assessmentInstruction);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementPlanInstruction(5L, "approver");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_PLAN, instruction.getActionType());
        assertEquals(IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS, instruction.getTargetMethod());
        assertTrue(instruction.getContent().contains("upload_requirement_assessment"));
        assertFalse(instruction.getContent().contains("save_requirement_package"));
        assertFalse(instruction.getContent().contains("save_development_plan"));
        assertFalse(instruction.getContent().contains("执行计划"));
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_requirement_assessment"));
        assertFalse(instruction.getContent().contains("mcpTool: reqflow.save_requirement_package"));
        assertTrue(instruction.getContent().contains("arguments.actionToken"));
        assertTrue(instruction.getContent().contains("不是 X-MCP-Key"));
        assertTrue(instruction.getContent().contains("当前流程阶段内有效"));
        assertTrue(instruction.getContent().contains("流转到下一流程即失效"));
        assertTrue(instruction.getContent().contains("最长保留24小时"));
        assertTrue(instruction.getContent().contains("重新生成"));
        assertTrue(instruction.getContent().contains("需求分析阶段"));
        assertTrue(instruction.getContent().contains("需求分析 actionToken: reqflow_action_assessment_token"));
        assertFalse(instruction.getContent().contains("需求生成 actionToken"));
        assertTrue(instruction.getContent().contains("评估结论"));
        assertTrue(instruction.getContent().contains("需澄清"));
        assertTrue(instruction.getContent().contains("暂不可实现"));
        assertTrue(instruction.getContent().contains("停止生成 requirement.md"));
        assertTrue(instruction.getContent().contains("建议任务分支: feature/req-5-demand"));
        assertTrue(instruction.getContent().contains("git pull --ff-only"));
        assertTrue(instruction.getContent().contains("不生成 plan.md"));
        assertTrue(instruction.getContent().contains("不改业务代码"));
        assertTrue(instruction.getContent().contains("业务背景"));
        assertTrue(instruction.getContent().contains("附件"));
        assertTrue(instruction.getContent().contains("demandId: 5"));
        assertTrue(instruction.getContent().contains("demandNo: REQ-005"));
    }

    @Test
    void createsRequirementGenerateInstructionForPlanPendingDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqDemand demand = demand(10L, 31L);
        demand.setDemandId(5L);
        demand.setDemandNo("REQ-005");
        demand.setTitle("Demand");
        demand.setStatus("plan_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(demand);
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction designInstruction = new ReqActionInstruction();
        designInstruction.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_PLAN);
        designInstruction.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE);
        designInstruction.setToken("reqflow_action_generate_token");
        designInstruction.setPrompt("请根据评估结论生成详细需求设计。");
        designInstruction.setContent("design instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_PLAN),
                eq(10L),
                eq(31L),
                eq(5L),
                eq(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE),
                any(),
                eq("生成需求设计指令"),
                eq("approver"))).thenReturn(designInstruction);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementPlanInstruction(5L, "approver");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_PLAN, instruction.getActionType());
        assertEquals(IReqActionTokenService.TARGET_REQUIREMENT_GENERATE, instruction.getTargetMethod());
        assertTrue(instruction.getContent().contains("save_requirement_package"));
        assertFalse(instruction.getContent().contains("upload_requirement_assessment"));
        assertFalse(instruction.getContent().contains("save_development_plan"));
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.save_requirement_package"));
        assertTrue(instruction.getContent().contains("需求生成阶段"));
        assertTrue(instruction.getContent().contains("需求生成 actionToken: reqflow_action_generate_token"));
        assertTrue(instruction.getContent().contains("当前流程阶段内有效"));
        assertTrue(instruction.getContent().contains("流转到下一流程即失效"));
        assertTrue(instruction.getContent().contains("必须沿用需求分析阶段创建的任务分支"));
        assertTrue(instruction.getContent().contains("不生成 plan.md"));
        assertTrue(instruction.getContent().contains("不改业务代码"));
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
        demand.setStatus("developing");
        when(reqDemandMapper.selectReqDemandByDemandId(6L)).thenReturn(demand);
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction created = new ReqActionInstruction();
        created.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        created.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP);
        created.setToken("reqflow_action_develop_stage_token");
        created.setPrompt("请按需求设计和执行计划完成开发。");
        created.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP),
                eq(10L),
                eq(31L),
                eq(6L),
                eq(IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP),
                any(),
                eq("生成执行任务指令"),
                eq("developer"))).thenReturn(created);

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
        assertTrue(instruction.getContent().contains("开发阶段 actionToken: reqflow_action_develop_stage_token"));
        assertTrue(instruction.getContent().contains("arguments.actionToken"));
        assertTrue(instruction.getContent().contains("当前开发阶段内有效"));
        assertTrue(instruction.getContent().contains("流转到待验收后即失效"));
        assertTrue(instruction.getContent().contains("多次用于执行计划、执行报告和 Review 报告回写"));
        assertTrue(instruction.getContent().contains("不得重新生成不同任务分支"));
        assertTrue(instruction.getContent().contains("先分析需求是否可以拆分为多个 subagent"));
        assertTrue(instruction.getContent().contains("互不共享状态"));
        assertFalse(instruction.getContent().contains("返修要求"));
        assertTrue(instruction.getContent().contains("执行计划"));
        assertTrue(instruction.getContent().contains("执行报告"));
        assertTrue(instruction.getContent().contains("Review 报告"));
        assertTrue(instruction.getContent().contains("demandId: 6"));
        assertTrue(instruction.getContent().contains("demandNo: REQ-006"));
    }

    @Test
    void rejectsRequirementDevelopInstructionBeforeDeveloperStartsDevelopment()
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

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.createRequirementDevelopInstruction(6L, "developer"));

        assertTrue(exception.getMessage().contains("当前状态不能生成执行任务指令"));
        verify(actionTokenService, never()).createInstruction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createsRequirementRepairInstructionForRepairingDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqDemand demand = demand(10L, 31L);
        demand.setDemandId(6L);
        demand.setDemandNo("REQ-006");
        demand.setTitle("Demand");
        demand.setStatus("repairing");
        when(reqDemandMapper.selectReqDemandByDemandId(6L)).thenReturn(demand);
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction created = new ReqActionInstruction();
        created.setActionType(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP);
        created.setTargetMethod(IReqActionTokenService.TARGET_REQUIREMENT_REPAIR);
        created.setToken("reqflow_action_repair_stage_token");
        created.setPrompt("请按返修项完成修复和复审。");
        created.setContent("base instruction");
        when(actionTokenService.createInstruction(
                eq(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP),
                eq(10L),
                eq(31L),
                eq(6L),
                eq(IReqActionTokenService.TARGET_REQUIREMENT_REPAIR),
                any(),
                eq("生成返修任务指令"),
                eq("developer"))).thenReturn(created);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementDevelopInstruction(6L, "developer");

        assertEquals(IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP, instruction.getActionType());
        assertEquals(IReqActionTokenService.TARGET_REQUIREMENT_REPAIR, instruction.getTargetMethod());
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertFalse(instruction.getContent().contains("mcpTool: reqflow.save_development_plan"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_execution_report"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.upload_review_report"));
        assertTrue(instruction.getContent().contains("任务分支: feature/req-6-demand"));
        assertTrue(instruction.getContent().contains("返修阶段 actionToken: reqflow_action_repair_stage_token"));
        assertTrue(instruction.getContent().contains("当前返修阶段内有效"));
        assertTrue(instruction.getContent().contains("流转到待验收后即失效"));
        assertTrue(instruction.getContent().contains("多次用于执行报告和 Review 报告回写"));
        assertTrue(instruction.getContent().contains("不得重新生成不同任务分支"));
        assertTrue(instruction.getContent().contains("不重新生成需求设计或执行计划"));
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

    @Test
    void creatorAcceptanceMovesDemandToCloseoutPending()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("review");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "closeout_pending", "creator")).thenReturn(1);
        mockLoginUser(7L, "requirement_user");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(5L, "closeout_pending", "creator"));

        verify(reqDemandMapper).updateReqDemandStatus(5L, "closeout_pending", "creator");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_closeout_pending"),
                eq("web"), contains("进入合并归档"), isNull());
    }

    @Test
    void rejectsCompletingCloseoutUntilPlatformVerifiesPublishedIndexes()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActionTokenMapper actionTokenMapper = mock(ReqActionTokenMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("closeout_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(repository(21L), repository(22L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Collections.singletonList(batch(21L, "main")));
        when(actionTokenMapper.countUsedActionToken("requirement_closeout", "publish_repository_index", 10L, 31L, 5L))
                .thenReturn(1);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenMapper", actionTokenMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateReqDemandStatus(5L, "completed", "developer"));

        assertTrue(exception.getMessage().contains("归档结果未通过平台验证"));
        verify(reqDemandMapper, never()).updateReqDemandStatus(anyLong(), any(), any());
    }

    @Test
    void selectedDeveloperCanCompleteCloseoutAfterPlatformVerification()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqActionTokenMapper actionTokenMapper = mock(ReqActionTokenMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        ReqRepositoryIndexBatchMapper batchMapper = mock(ReqRepositoryIndexBatchMapper.class);
        ReqActivityLogService activityLogService = mock(ReqActivityLogService.class);
        ReqDemand current = demand(10L, 31L);
        current.setDemandId(5L);
        current.setDemandNo("REQ-005");
        current.setStatus("closeout_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(5L)).thenReturn(current);
        when(reqDemandMapper.updateReqDemandStatus(5L, "completed", "developer")).thenReturn(1);
        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "main"));
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(repository(21L), repository(22L)));
        when(batchMapper.selectReqRepositoryIndexBatchList(any())).thenReturn(Arrays.asList(batch(21L, "main"), batch(22L, "main")));
        when(actionTokenMapper.countUsedActionToken("requirement_closeout", "publish_repository_index", 10L, 31L, 5L))
                .thenReturn(2);
        mockLoginUser(8L, "requirement_developer");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "actionTokenMapper", actionTokenMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "activityLogService", activityLogService);

        assertEquals(1, service.updateReqDemandStatus(5L, "completed", "developer"));

        verify(reqDemandMapper).updateReqDemandStatus(5L, "completed", "developer");
        verify(activityLogService).record(anyLong(), eq(10L), eq(5L), eq("demand_completed"),
                eq("web"), contains("归档完成"), isNull());
    }

    @Test
    void createsRequirementCloseoutInstructionForCloseoutPendingDemand()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqVariantMapper variantMapper = mock(ReqVariantMapper.class);
        ReqRepositoryMapper repositoryMapper = mock(ReqRepositoryMapper.class);
        IReqActionTokenService actionTokenService = mock(IReqActionTokenService.class);
        ReqDemand demand = demand(10L, 31L);
        demand.setDemandId(6L);
        demand.setDemandNo("REQ-006");
        demand.setTitle("Demand");
        demand.setStatus("closeout_pending");
        when(reqDemandMapper.selectReqDemandByDemandId(6L)).thenReturn(demand);
        when(variantMapper.selectReqVariantByVariantId(31L)).thenReturn(variant(31L, 10L, "release/main"));
        ReqRepository backend = repository(21L);
        backend.setRepoName("后端仓库");
        ReqRepository frontend = repository(22L);
        frontend.setRepoName("前端仓库");
        when(repositoryMapper.selectReqRepositoryList(any())).thenReturn(Arrays.asList(backend, frontend));
        mockLoginUser(8L, "requirement_developer");

        ReqActionInstruction first = new ReqActionInstruction();
        first.setActionType("requirement_closeout");
        first.setTargetMethod("publish_repository_index");
        first.setToken("reqflow_action_closeout_backend");
        first.setPrompt("归档发布后端仓库索引。");
        ReqActionInstruction second = new ReqActionInstruction();
        second.setActionType("requirement_closeout");
        second.setTargetMethod("publish_repository_index");
        second.setToken("reqflow_action_closeout_frontend");
        second.setPrompt("归档发布前端仓库索引。");
        when(actionTokenService.createInstruction(eq("requirement_closeout"), eq(10L), eq(31L), eq(6L),
                eq("publish_repository_index"), any(), eq("生成合并归档指令"), eq("developer")))
                .thenReturn(first, second);

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);
        ReflectionTestUtils.setField(service, "variantMapper", variantMapper);
        ReflectionTestUtils.setField(service, "repositoryMapper", repositoryMapper);
        ReflectionTestUtils.setField(service, "actionTokenService", actionTokenService);

        ReqActionInstruction instruction = service.createRequirementDevelopInstruction(6L, "developer");

        assertEquals("requirement_closeout", instruction.getActionType());
        assertEquals("publish_repository_index", instruction.getTargetMethod());
        assertTrue(instruction.getContent().contains("请按全局 skill `reqflow-mcp`"));
        assertTrue(instruction.getContent().contains("需求分支: release/main"));
        assertTrue(instruction.getContent().contains("本地开发分支: feature/req-6-demand"));
        assertTrue(instruction.getContent().contains("git merge --squash feature/req-6-demand"));
        assertTrue(instruction.getContent().contains("git push"));
        assertTrue(instruction.getContent().contains("mcpTool: reqflow.publish_repository_index"));
        assertTrue(instruction.getContent().contains("后端仓库"));
        assertTrue(instruction.getContent().contains("reqflow_action_closeout_backend"));
        assertTrue(instruction.getContent().contains("前端仓库"));
        assertTrue(instruction.getContent().contains("reqflow_action_closeout_frontend"));
        assertTrue(instruction.getContent().contains("arguments.actionToken"));
        assertTrue(instruction.getContent().contains("一次性"));
        assertTrue(instruction.getContent().contains("平台验证"));
        assertTrue(instruction.getContent().contains("git branch -d feature/req-6-demand"));
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

    private boolean hasPackage(ReqPackageVersion packageVersion, String artifactType, String firstContent,
            String secondContent)
    {
        return packageVersion != null
                && artifactType.equals(packageVersion.getArtifactType())
                && packageVersion.getContent() != null
                && packageVersion.getContent().contains(firstContent)
                && packageVersion.getContent().contains(secondContent);
    }

    private ReqPackageVersion packageVersion(Long packageId, String artifactType, long createTime)
    {
        ReqPackageVersion version = new ReqPackageVersion();
        version.setPackageId(packageId);
        version.setDemandId(1L);
        version.setArtifactType(artifactType);
        version.setVersionNo(1);
        version.setContent(artifactType + " content");
        version.setCreateTime(new Date(createTime));
        return version;
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
