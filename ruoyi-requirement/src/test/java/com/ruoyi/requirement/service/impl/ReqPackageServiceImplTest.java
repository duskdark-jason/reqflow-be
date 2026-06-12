package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqModuleMapper;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.template.RequirementTemplateContext;

class ReqPackageServiceImplTest
{
    @Test
    void rejectsUnsupportedArtifactTypeBeforeSavingVersion()
    {
        ReqPackageVersionMapper reqPackageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqPackageServiceImpl service = new ReqPackageServiceImpl();
        ReflectionTestUtils.setField(service, "reqPackageVersionMapper", reqPackageVersionMapper);

        assertThrows(ServiceException.class, () -> service.saveVersion(1L, "unexpected_type", "content", null));
        verifyNoInteractions(reqPackageVersionMapper);
    }

    @Test
    void buildsFixTaskBranchFromNewFeatureNameDemandNoAndTitle()
    {
        ReqPackageServiceImpl service = new ReqPackageServiceImpl();
        ReflectionTestUtils.setField(service, "reqProjectMapper", projectMapper());
        ReflectionTestUtils.setField(service, "reqVariantMapper", variantMapper());
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper());
        ReflectionTestUtils.setField(service, "reqModuleMapper", mock(ReqModuleMapper.class));

        ReqDemand demand = demand();
        demand.setRemark("demand-list");
        demand.setTitle("new feature flow");

        RequirementTemplateContext context = ReflectionTestUtils.invokeMethod(service, "buildContext", demand);

        assertEquals("demand-list", context.getModuleName());
        assertEquals("fix-demand-list-REQ-007-new-feature-flow", context.getTaskBranch());
    }

    @Test
    void buildContextIncludesDemandSourceBackgroundAndAttachments()
    {
        ReqPackageServiceImpl service = new ReqPackageServiceImpl();
        ReflectionTestUtils.setField(service, "reqProjectMapper", projectMapper());
        ReflectionTestUtils.setField(service, "reqVariantMapper", variantMapper());
        ReflectionTestUtils.setField(service, "reqRepositoryMapper", repositoryMapper());
        ReflectionTestUtils.setField(service, "reqModuleMapper", mock(ReqModuleMapper.class));

        ReqDemand demand = demand();
        demand.setDemandSource("CUSTOMER");
        demand.setBusinessBackground("<p>客户反馈<img src=\"/profile/upload/demo.png\"></p>");
        demand.setAttachments("/profile/upload/demo.pdf");

        RequirementTemplateContext context = ReflectionTestUtils.invokeMethod(service, "buildContext", demand);

        assertEquals("CUSTOMER", context.getDemandSource());
        assertEquals("<p>客户反馈<img src=\"/profile/upload/demo.png\"></p>", context.getBusinessBackground());
        assertEquals("/profile/upload/demo.pdf", context.getAttachments());
    }

    private ReqDemand demand()
    {
        ReqDemand demand = new ReqDemand();
        demand.setDemandId(7L);
        demand.setDemandNo("REQ-20260611-007");
        demand.setTitle("需求列表新增功能");
        demand.setProjectId(1L);
        demand.setVariantId(2L);
        return demand;
    }

    private ReqProjectMapper projectMapper()
    {
        ReqProjectMapper mapper = mock(ReqProjectMapper.class);
        ReqProject project = new ReqProject();
        project.setProjectId(1L);
        project.setProjectName("需求平台");
        project.setProjectCode("REQFLOW");
        when(mapper.selectReqProjectByProjectId(1L)).thenReturn(project);
        return mapper;
    }

    private ReqVariantMapper variantMapper()
    {
        ReqVariantMapper mapper = mock(ReqVariantMapper.class);
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(2L);
        variant.setVariantName("主线");
        variant.setBaselineBranch("main");
        when(mapper.selectReqVariantByVariantId(2L)).thenReturn(variant);
        return mapper;
    }

    private ReqRepositoryMapper repositoryMapper()
    {
        ReqRepositoryMapper mapper = mock(ReqRepositoryMapper.class);
        ReqRepository repository = new ReqRepository();
        repository.setRepoId(3L);
        repository.setRepoName("reqflow-ui");
        repository.setRepoType("FRONTEND");
        when(mapper.selectReqRepositoryList(any())).thenReturn(Collections.singletonList(repository));
        return mapper;
    }
}
