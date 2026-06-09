package com.ruoyi.requirement.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqModule;
import com.ruoyi.requirement.domain.ReqPackageVersion;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqModuleMapper;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqPackageService;
import com.ruoyi.requirement.service.ReqActivityLogService;
import com.ruoyi.requirement.template.RequirementTemplateContext;
import com.ruoyi.requirement.template.RequirementTemplateService;

@Service
public class ReqPackageServiceImpl implements IReqPackageService
{
    private static final Set<String> SUPPORTED_ARTIFACT_TYPES = Set.of(
            "requirement_draft",
            "requirement",
            "plan",
            "context_manifest",
            "branch_execution_brief",
            "execution_prompt",
            "review_prompt",
            "execution_report",
            "review_report");

    @Autowired private ReqPackageVersionMapper reqPackageVersionMapper;
    @Autowired private ReqDemandMapper reqDemandMapper;
    @Autowired private ReqProjectMapper reqProjectMapper;
    @Autowired private ReqRepositoryMapper reqRepositoryMapper;
    @Autowired private ReqVariantMapper reqVariantMapper;
    @Autowired private ReqModuleMapper reqModuleMapper;
    @Autowired private RequirementTemplateService templateService;
    @Autowired private ReqActivityLogService activityLogService;

    @Override
    public List<ReqPackageVersion> selectReqPackageVersionListByDemandId(Long demandId)
    {
        return reqPackageVersionMapper.selectReqPackageVersionListByDemandId(demandId);
    }

    @Override
    public ReqPackageVersion selectLatest(Long demandId, String artifactType)
    {
        validateArtifactType(artifactType);
        return reqPackageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId, artifactType);
    }

    @Override
    public ReqPackageVersion saveVersion(Long demandId, String artifactType, String content, String versionNote)
    {
        validateArtifactType(artifactType);
        Integer maxVersion = reqPackageVersionMapper.selectMaxVersionNo(demandId, artifactType);
        ReqPackageVersion version = new ReqPackageVersion();
        version.setDemandId(demandId);
        version.setArtifactType(artifactType);
        version.setVersionNo((maxVersion == null ? 0 : maxVersion) + 1);
        version.setContent(content == null ? "" : content);
        version.setStatus("draft");
        version.setVersionNote(versionNote);
        version.setCreateBy(currentUsername());
        reqPackageVersionMapper.insertReqPackageVersion(version);
        recordPackageWrite(demandId, artifactType, version.getVersionNo());
        return version;
    }

    @Override
    public List<ReqPackageVersion> generateDraftPackage(Long demandId)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        RequirementTemplateContext context = buildContext(demand);
        List<ReqPackageVersion> versions = new ArrayList<>();
        versions.add(saveVersion(demandId, "requirement_draft", templateService.render("templates/requirement/requirement-draft.md", context), "生成需求草稿"));
        versions.add(saveVersion(demandId, "context_manifest", templateService.render("templates/requirement/context-manifest.json", context), "生成上下文清单"));
        versions.add(saveVersion(demandId, "branch_execution_brief", templateService.render("templates/requirement/harness-init-instruction.md", context), "生成仓库初始化指令"));
        activityLogService.record(currentUserId(), demand.getProjectId(), demandId, "package_generated", "web", "需求执行包草稿生成：" + demand.getDemandNo(), null);
        return versions;
    }

    private void validateArtifactType(String artifactType)
    {
        if (artifactType == null || !SUPPORTED_ARTIFACT_TYPES.contains(artifactType))
        {
            throw new ServiceException("不支持的执行包产物类型");
        }
    }

    private RequirementTemplateContext buildContext(ReqDemand demand)
    {
        ReqProject project = reqProjectMapper.selectReqProjectByProjectId(demand.getProjectId());
        ReqVariant variant = reqVariantMapper.selectReqVariantByVariantId(demand.getVariantId());
        ReqModule module = demand.getModuleId() == null ? null : reqModuleMapper.selectReqModuleByModuleId(demand.getModuleId());
        ReqRepository query = new ReqRepository();
        query.setProjectId(demand.getProjectId());
        List<ReqRepository> repos = reqRepositoryMapper.selectReqRepositoryList(query);
        ReqRepository repo = repos.isEmpty() ? null : repos.get(0);

        RequirementTemplateContext context = new RequirementTemplateContext();
        context.setProjectName(project == null ? "" : project.getProjectName());
        context.setProjectCode(project == null ? "" : project.getProjectCode());
        context.setRepoName(repo == null ? "" : repo.getRepoName());
        context.setRepoType(repo == null ? "" : repo.getRepoType());
        context.setVariantName(variant == null ? "" : variant.getVariantName());
        context.setBaselineBranch(variant == null ? "main" : variant.getBaselineBranch());
        context.setDemandNo(demand.getDemandNo());
        context.setDemandTitle(demand.getTitle());
        context.setTaskBranch("feature/" + demand.getDemandNo());
        context.setModuleName(module == null ? "" : module.getModuleName());
        context.setAcceptanceText(demand.getAcceptanceText());
        return context;
    }

    private void recordPackageWrite(Long demandId, String artifactType, Integer versionNo)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        String eventType = "mcp_write";
        if ("plan".equals(artifactType)) eventType = "plan_saved";
        else if ("execution_report".equals(artifactType)) eventType = "execution_report_uploaded";
        else if ("review_report".equals(artifactType)) eventType = "review_report_uploaded";
        activityLogService.record(currentUserId(), demand == null ? null : demand.getProjectId(), demandId, eventType,
                "web", "保存执行包产物：" + artifactType + " v" + versionNo, null);
    }

    private String currentUsername()
    {
        try { return SecurityUtils.getUsername(); }
        catch (Exception e) { return "system"; }
    }

    private Long currentUserId()
    {
        try { return SecurityUtils.getUserId(); }
        catch (Exception e) { return 0L; }
    }
}
