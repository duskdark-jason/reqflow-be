package com.ruoyi.requirement.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.dto.ReqMcpUserOption;
import com.ruoyi.requirement.mapper.ReqActionTokenMapper;
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.IReqDemandService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class ReqDemandServiceImpl implements IReqDemandService
{
    private static final String BRANCH_NOT_INITIALIZED_MESSAGE = "项目分支尚未初始化完成，请先完成分支初始化后再提交需求";

    private static final String ACTION_TOKEN_USAGE_RULE = "有效期：当前流程阶段内有效，流转到下一流程即失效；最长保留24小时，过期或已使用后需重新生成。";

    private static final String DEVELOPMENT_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前开发阶段内有效，流转到待验收后即失效；最长保留24小时，可在本阶段多次用于执行计划、执行报告和 Review 报告回写。";

    private static final String ROLE_REQUIREMENT_USER = "requirement_user";

    private static final String ROLE_REQUIREMENT_DEVELOPER = "requirement_developer";

    @Autowired
    private ReqDemandMapper reqDemandMapper;

    @Autowired
    private ReqPackageVersionMapper packageVersionMapper;

    @Autowired
    private ReqActionTokenMapper actionTokenMapper;

    @Autowired
    private ReqVariantMapper variantMapper;

    @Autowired
    private ReqRepositoryMapper repositoryMapper;

    @Autowired
    private ReqRepositoryIndexBatchMapper batchMapper;

    @Autowired
    private ReqActivityLogService activityLogService;

    @Autowired
    private IReqActionTokenService actionTokenService;

    @Override
    public ReqDemand selectReqDemandByDemandId(Long demandId)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        validateDemandReadable(demand);
        return demand;
    }

    @Override
    public List<ReqDemand> selectReqDemandList(ReqDemand reqDemand)
    {
        if (reqDemand == null)
        {
            reqDemand = new ReqDemand();
        }
        if (!isCurrentAdmin())
        {
            reqDemand.setParticipantUserId(currentUserId());
        }
        return reqDemandMapper.selectReqDemandList(reqDemand);
    }

    @Override
    public List<ReqMcpUserOption> selectDeveloperOptions(String userName)
    {
        return safeList(reqDemandMapper.selectUserOptionsByRoleKey(ROLE_REQUIREMENT_DEVELOPER, userName));
    }

    @Override
    public int insertReqDemand(ReqDemand reqDemand)
    {
        validateDemandContent(reqDemand);
        validateDeveloperUser(reqDemand.getDeveloperUserId());
        validateDemandTargetInitialized(reqDemand.getProjectId(), reqDemand.getVariantId());
        reqDemand.setDemandNo(nextDemandNo());
        reqDemand.setStatus("draft");
        reqDemand.setCreatorId(currentUserId());
        int rows = reqDemandMapper.insertReqDemand(reqDemand);
        if (rows > 0)
        {
            activityLogService.record(reqDemand.getCreatorId(), reqDemand.getProjectId(), reqDemand.getDemandId(),
                    "demand_created", "web", "创建需求：" + reqDemand.getTitle(), null);
        }
        return rows;
    }

    @Override
    public int updateReqDemand(ReqDemand reqDemand)
    {
        if (reqDemand.getDemandId() == null)
        {
            throw new ServiceException("需求ID不能为空");
        }
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(reqDemand.getDemandId());
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (!"draft".equals(current.getStatus()))
        {
            throw new ServiceException("只有未提交需求可以修改");
        }
        if (!isCurrentAdmin() && (current.getCreatorId() == null || !current.getCreatorId().equals(currentUserId())))
        {
            throw new ServiceException("只有需求创建人可以修改");
        }
        if (reqDemand.getStatus() != null && !reqDemand.getStatus().isBlank()
                && !reqDemand.getStatus().equals(current.getStatus())
                && !ReqDemandStatusTransition.isAllowed(current.getStatus(), reqDemand.getStatus()))
        {
            throw new ServiceException("需求状态流转不允许");
        }
        reqDemand.setStatus(null);
        validateDemandContent(reqDemand);
        Long developerUserId = reqDemand.getDeveloperUserId() == null ? current.getDeveloperUserId() : reqDemand.getDeveloperUserId();
        validateDeveloperUser(developerUserId);
        Long projectId = reqDemand.getProjectId() == null ? current.getProjectId() : reqDemand.getProjectId();
        Long variantId = reqDemand.getVariantId() == null ? current.getVariantId() : reqDemand.getVariantId();
        validateDemandTargetInitialized(projectId, variantId);
        return reqDemandMapper.updateReqDemand(reqDemand);
    }

    @Override
    public int deleteReqDemandByDemandIds(Long[] demandIds, String operator)
    {
        Long[] normalizedIds = normalizeDemandIds(demandIds);
        if (normalizedIds.length == 0)
        {
            throw new ServiceException("需求ID不能为空");
        }
        List<ReqDemand> demands = Arrays.stream(normalizedIds)
                .map(reqDemandMapper::selectReqDemandByDemandId)
                .filter(demand -> demand != null)
                .collect(Collectors.toList());
        packageVersionMapper.deleteReqPackageVersionByDemandIds(normalizedIds);
        actionTokenMapper.deleteReqActionTokenByDemandIds(normalizedIds);
        int rows = reqDemandMapper.deleteReqDemandByDemandIds(normalizedIds);
        if (rows > 0)
        {
            Long operatorId = currentUserId();
            for (ReqDemand demand : demands)
            {
                activityLogService.record(operatorId, demand.getProjectId(), demand.getDemandId(),
                        "demand_deleted", "web", "删除需求：" + demand.getDemandNo(), null);
            }
        }
        return rows;
    }

    @Override
    public int updateReqDemandStatus(Long demandId, String status, String updateBy)
    {
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (!ReqDemandStatusTransition.isAllowed(current.getStatus(), status))
        {
            throw new ServiceException("需求状态流转不允许");
        }
        validateStatusActionRole(status);
        validateStatusActionParticipant(current, status);
        if ("submitted".equals(status))
        {
            validateDeveloperUser(current.getDeveloperUserId());
        }
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, status, updateBy);
        if (rows > 0 && "submitted".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_submitted", "web", "提交需求：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "completed".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_completed", "web", "办结需求：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "repairing".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_repairing", "web", "提交返修：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "review".equals(status) && "repairing".equals(current.getStatus()))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_repair_submitted", "web", "返修提交验收：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "archived".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_archived", "web", "归档需求：" + current.getDemandNo(), null);
        }
        return rows;
    }

    @Override
    public void validateDemandReadable(Long demandId)
    {
        validateDemandReadable(reqDemandMapper.selectReqDemandByDemandId(demandId));
    }

    @Override
    public void validateDemandPackageWritable(Long demandId, String artifactType)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (isCurrentAdmin())
        {
            return;
        }
        if (isCurrentDeveloper(demand))
        {
            return;
        }
        throw new ServiceException("只有指定开发人员可以回写该需求资料包");
    }

    @Override
    public ReqActionInstruction createRequirementPlanInstruction(Long demandId, String operator)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        validateDeveloperInstructionAccess(demand);
        if (!"submitted".equals(demand.getStatus()) && !"plan_pending".equals(demand.getStatus())
                && !"plan_ready".equals(demand.getStatus()))
        {
            throw new ServiceException("当前状态不能生成需求设计指令");
        }
        String taskBranch = suggestedTaskBranch(demand);
        String assessmentPrompt = "请先在需求设计阶段完成需求可行性评估和风险判断，通过 reqflow MCP 回写评估报告。";
        ReqActionInstruction assessmentInstruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                "upload_requirement_assessment",
                assessmentPrompt,
                "生成需求评估与设计",
                operator);
        String designPrompt = "请在需求评估允许继续后生成详细需求设计，通过 reqflow MCP 回写资料包。";
        ReqActionInstruction designInstruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                "save_requirement_package",
                designPrompt,
                "复制需求设计回写指令",
                operator);
        // 需求设计指令给开发人员复制到 Codex，动作 Token 仅定位需求上下文，不替代人员 MCP Key 鉴权。
        assessmentInstruction.setContent(requirementPlanInstructionContent(assessmentPrompt, assessmentInstruction.getToken(),
                designInstruction.getToken(), demand, taskBranch));
        return assessmentInstruction;
    }

    @Override
    public ReqActionInstruction createRequirementDevelopInstruction(Long demandId, String operator)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        validateDeveloperInstructionAccess(demand);
        if (!"confirmed".equals(demand.getStatus()) && !"developing".equals(demand.getStatus()))
        {
            throw new ServiceException("当前状态不能生成执行任务指令");
        }
        String developPrompt = "请基于已确认需求设计执行开发，并通过 reqflow MCP 回写执行计划、执行报告和 Review 报告。";
        ReqActionInstruction developInstruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                IReqActionTokenService.TARGET_REQUIREMENT_DEVELOP,
                developPrompt,
                "生成执行任务指令",
                operator);
        developInstruction.setContent(requirementDevelopInstructionContent(developPrompt, developInstruction.getToken(),
                demand, suggestedTaskBranch(demand)));
        return developInstruction;
    }

    private String nextDemandNo()
    {
        Long currentCount = reqDemandMapper.selectDemandCount();
        long sequence = (currentCount == null ? 0L : currentCount) + 1;
        return "REQ-" + String.format("%03d", sequence);
    }

    private String requirementPlanInstructionContent(String prompt, String assessmentActionToken, String designActionToken,
            ReqDemand demand, String taskBranch)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行 Reqflow 需求设计生成。"
                + "\nmcpServer: reqflow"
                + "\ntoolName: upload_requirement_assessment"
                + "\nmcpTool: reqflow.upload_requirement_assessment"
                + "\ntoolName: save_requirement_package"
                + "\nmcpTool: reqflow.save_requirement_package"
                + "\ntargetMethods: upload_requirement_assessment, save_requirement_package"
                + "\ndemandId: " + demand.getDemandId()
                + "\ndemandNo: " + demand.getDemandNo()
                + "\n建议任务分支: " + taskBranch
                + "\n可行性评估 actionToken: " + assessmentActionToken
                + "\n需求设计 actionToken: " + designActionToken
                + "\n" + ACTION_TOKEN_USAGE_RULE
                + "\n要求：先读取需求详情中的基础需求、业务背景、预期结果、验收标准、附件和历史需求设计版本。"
                + "\n分支要求：校验当前 workspace 仓库远端与平台项目一致，切换目标基线分支并 git pull --ff-only 后，创建或切换到上面的建议任务分支；需求人补充调整指令时继续使用同一任务分支。"
                + "\n评估要求：先形成需求可行性评估报告，必须给出评估结论、主要风险、阻断点、需要需求人补充或调整的内容、是否允许继续生成需求设计；结论类型从“可继续设计、需澄清、需调整、暂不可实现”中选择。"
                + "\n评估回写：先调用 upload_requirement_assessment，arguments.actionToken 填可行性评估 actionToken，content 填评估报告；如果结论是需澄清、需调整或暂不可实现，本轮停止生成 requirement.md，并把该结论作为反馈推送给需求人。"
                + "\n本地文件：评估通过或有条件允许继续后，只生成或更新 docs/specs/active/REQ-001-中文需求标题/meta.md 和 requirement.md，可在 requirement.md 顶部保留“可行性评估”小节；不生成 plan.md，不改业务代码。"
                + "\n保存要求：评估允许继续后调用 save_requirement_package，arguments.actionToken 填需求设计 actionToken，content 填详细需求设计；每次补充调整都再次回写评估和需求设计，平台按版本保存，最终版 requirement.md 保留在本地任务分支用于开发。"
                + "\n注意：两个 actionToken 分别是 upload_requirement_assessment 和 save_requirement_package 的 arguments.actionToken，不是 X-MCP-Key；MCP 鉴权仍使用人员 X-MCP-Key。";
    }

    private String requirementDevelopInstructionContent(String prompt, String developActionToken, ReqDemand demand,
            String taskBranch)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行 Reqflow 需求开发。"
                + "\nmcpServer: reqflow"
                + "\ntoolName: save_development_plan"
                + "\nmcpTool: reqflow.save_development_plan"
                + "\ntoolName: upload_execution_report"
                + "\nmcpTool: reqflow.upload_execution_report"
                + "\ntoolName: upload_review_report"
                + "\nmcpTool: reqflow.upload_review_report"
                + "\ntargetMethods: save_development_plan, upload_execution_report, upload_review_report"
                + "\ndemandId: " + demand.getDemandId()
                + "\ndemandNo: " + demand.getDemandNo()
                + "\n任务分支: " + taskBranch
                + "\n开发阶段 actionToken: " + developActionToken
                + "\n" + DEVELOPMENT_STAGE_TOKEN_USAGE_RULE
                + "\n分支要求：必须沿用需求设计阶段创建的任务分支，不得重新生成不同任务分支；如果本地不在该分支，先切换到该分支。"
                + "\n要求：先读取需求详情、最终需求设计和本地 requirement.md，生成或更新 plan.md；再按目标仓库规范完成实现、验证、自动 Review 和提交。"
                + "\n回写要求：本阶段三个 MCP 工具都使用同一个开发阶段 actionToken：先调用 save_development_plan 回写执行计划，开发验证完成后调用 upload_execution_report 回写执行报告，自动 Review 完成后调用 upload_review_report 回写 Review 报告。"
                + "\n注意：开发阶段 actionToken 是上述三个工具的 arguments.actionToken，不是 X-MCP-Key；MCP 鉴权仍使用人员 X-MCP-Key。";
    }

    private String suggestedTaskBranch(ReqDemand demand)
    {
        return "feature/" + requirementBranchToken(demand.getDemandNo()) + "-" + slug(demand.getTitle(), "demand");
    }

    private String requirementBranchToken(String demandNo)
    {
        String token = slug(demandNo, "requirement").toLowerCase();
        return token.replaceFirst("^req-0*([0-9]+)$", "req-$1");
    }

    private String slug(String value, String fallback)
    {
        if (StringUtils.isEmpty(value))
        {
            return fallback;
        }
        String slug = value.trim()
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .toLowerCase();
        return StringUtils.isEmpty(slug) ? fallback + "-" + Integer.toHexString(value.hashCode()) : slug;
    }

    private void validateDemandTargetInitialized(Long projectId, Long variantId)
    {
        if (projectId == null || variantId == null)
        {
            throw new ServiceException("项目和项目分支不能为空");
        }
        ReqVariant variant = variantMapper.selectReqVariantByVariantId(variantId);
        if (variant == null || !projectId.equals(variant.getProjectId()) || "1".equals(variant.getStatus()))
        {
            throw new ServiceException("项目分支不存在或不属于所选项目");
        }
        if (StringUtils.isEmpty(variant.getBaselineBranch()))
        {
            throw new ServiceException(BRANCH_NOT_INITIALIZED_MESSAGE);
        }

        // 新功能可以没有既有模块，但必须有仓库索引证据，避免需求落到未接入分支。
        List<ReqRepository> repositories = loadReadyRepositories(projectId);
        if (repositories.isEmpty())
        {
            throw new ServiceException(BRANCH_NOT_INITIALIZED_MESSAGE);
        }
        Set<Long> repositoryIds = repositories.stream()
                .map(ReqRepository::getRepoId)
                .collect(Collectors.toSet());
        Set<Long> indexedRepositoryIds = loadImportedBatches(projectId, variant.getBaselineBranch()).stream()
                .map(ReqRepositoryIndexBatch::getRepoId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (!indexedRepositoryIds.containsAll(repositoryIds))
        {
            throw new ServiceException(BRANCH_NOT_INITIALIZED_MESSAGE);
        }
    }

    private void validateStatusActionRole(String targetStatus)
    {
        if (isCurrentAdmin())
        {
            return;
        }
        String requiredRole = requiredRoleForStatusAction(targetStatus);
        if (requiredRole == null)
        {
            throw new ServiceException("当前角色不能执行该流程动作");
        }
        try
        {
            if (SecurityUtils.hasRole(requiredRole))
            {
                return;
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("当前角色不能执行该流程动作");
        }
        throw new ServiceException("当前角色不能执行该流程动作");
    }

    private boolean isCurrentAdmin()
    {
        try
        {
            return SecurityUtils.isAdmin() || SecurityUtils.hasRole("admin");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String requiredRoleForStatusAction(String targetStatus)
    {
        if ("submitted".equals(targetStatus) || "confirmed".equals(targetStatus)
                || "repairing".equals(targetStatus) || "completed".equals(targetStatus))
        {
            return ROLE_REQUIREMENT_USER;
        }
        if ("plan_ready".equals(targetStatus) || "plan_pending".equals(targetStatus)
                || "developing".equals(targetStatus) || "review".equals(targetStatus))
        {
            return ROLE_REQUIREMENT_DEVELOPER;
        }
        return null;
    }

    private void validateDemandReadable(ReqDemand demand)
    {
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (isCurrentAdmin() || isCurrentCreator(demand) || isCurrentDeveloper(demand))
        {
            return;
        }
        throw new ServiceException("当前用户不是该需求参与人");
    }

    private void validateStatusActionParticipant(ReqDemand demand, String targetStatus)
    {
        if (isCurrentAdmin())
        {
            return;
        }
        if ("submitted".equals(targetStatus) || "confirmed".equals(targetStatus)
                || "repairing".equals(targetStatus) || "completed".equals(targetStatus))
        {
            if (isCurrentCreator(demand))
            {
                return;
            }
            throw new ServiceException("只有需求创建人可以执行该流程动作");
        }
        if ("plan_ready".equals(targetStatus) || "plan_pending".equals(targetStatus)
                || "developing".equals(targetStatus) || "review".equals(targetStatus))
        {
            if (isCurrentDeveloper(demand))
            {
                return;
            }
            throw new ServiceException("只有指定开发人员可以执行该流程动作");
        }
    }

    private void validateDeveloperInstructionAccess(ReqDemand demand)
    {
        if (isCurrentAdmin() || isCurrentDeveloper(demand))
        {
            return;
        }
        throw new ServiceException("只有指定开发人员可以生成该需求指令");
    }

    private boolean isCurrentCreator(ReqDemand demand)
    {
        return demand != null && sameUser(demand.getCreatorId(), currentUserId());
    }

    private boolean isCurrentDeveloper(ReqDemand demand)
    {
        return demand != null && !"draft".equals(demand.getStatus())
                && sameUser(demand.getDeveloperUserId(), currentUserId());
    }

    private boolean sameUser(Long expectedUserId, Long actualUserId)
    {
        return expectedUserId != null && actualUserId != null && expectedUserId.equals(actualUserId);
    }

    private void validateDeveloperUser(Long developerUserId)
    {
        if (developerUserId == null)
        {
            throw new ServiceException("请选择指定开发人员");
        }
        if (reqDemandMapper.countEnabledUserByRoleKey(ROLE_REQUIREMENT_DEVELOPER, developerUserId) < 1)
        {
            throw new ServiceException("指定开发人员不存在或未启用开发人员角色");
        }
    }

    private Long[] normalizeDemandIds(Long[] demandIds)
    {
        if (demandIds == null)
        {
            return new Long[0];
        }
        return Arrays.stream(demandIds)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .toArray(new Long[0]);
    }

    private void validateDemandContent(ReqDemand reqDemand)
    {
        if (reqDemand == null)
        {
            throw new ServiceException("需求内容不能为空");
        }
        if (StringUtils.isBlank(reqDemand.getDemandSource()))
        {
            throw new ServiceException("需求来源不能为空");
        }
    }

    private List<ReqRepository> loadReadyRepositories(Long projectId)
    {
        ReqRepository query = new ReqRepository();
        query.setProjectId(projectId);
        return safeList(repositoryMapper.selectReqRepositoryList(query)).stream()
                .filter(this::isReadyRepository)
                .collect(Collectors.toList());
    }

    private List<ReqRepositoryIndexBatch> loadImportedBatches(Long projectId, String branchName)
    {
        ReqRepositoryIndexBatch query = new ReqRepositoryIndexBatch();
        query.setProjectId(projectId);
        query.setBranchName(branchName);
        query.setStatus("imported");
        try
        {
            return safeList(batchMapper.selectReqRepositoryIndexBatchList(query));
        }
        catch (DataAccessException e)
        {
            if (ReqOptionalIndexTableGuard.isMissingTable(e, "req_repository_index_batch"))
            {
                // 缺批次表时没有可靠证据证明仓库已索引，按未初始化处理。
                return Collections.emptyList();
            }
            throw e;
        }
    }

    private boolean isReadyRepository(ReqRepository repository)
    {
        return repository != null
                && repository.getRepoId() != null
                && StringUtils.isNotEmpty(repository.getRepoType())
                && StringUtils.isNotEmpty(repository.getRepoUrl())
                && StringUtils.isNotEmpty(repository.getDefaultBranch())
                && !"1".equals(repository.getStatus());
    }

    private <T> List<T> safeList(List<T> source)
    {
        return source == null ? Collections.emptyList() : source;
    }

    private Long currentUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return 0L;
        }
    }
}
