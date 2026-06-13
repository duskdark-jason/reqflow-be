package com.ruoyi.requirement.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.domain.ReqPackageVersion;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqActionInstruction;
import com.ruoyi.requirement.dto.ReqCloseoutVerificationResult;
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

    private static final String REPAIR_STAGE_TOKEN_USAGE_RULE =
            "有效期：当前返修阶段内有效，流转到待验收后即失效；最长保留24小时，可在本阶段多次用于执行报告和 Review 报告回写。";

    private static final String CLOSEOUT_TOKEN_USAGE_RULE =
            "有效期：当前合并归档阶段内有效，最长保留24小时；每个仓库一枚一次性 actionToken，过期或已使用后需重新生成。";

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
    @Transactional
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
        if ("review".equals(current.getStatus()) && "repairing".equals(status))
        {
            throw new ServiceException("请先填写返修问题说明");
        }
        validateStatusActionRole(current.getStatus(), status);
        validateStatusActionParticipant(current, status);
        if ("submitted".equals(status))
        {
            validateDeveloperUser(current.getDeveloperUserId());
        }
        if ("plan_pending".equals(current.getStatus()) && "plan_ready".equals(status))
        {
            validateRequirementDesignGeneratedAfterLatestSupplement(current.getDemandId());
        }
        if ("repairing".equals(current.getStatus()) && "review".equals(status))
        {
            validateRepairArtifactsGeneratedAfterLatestRepairRequest(current.getDemandId());
        }
        if ("closeout_pending".equals(current.getStatus()) && "completed".equals(status))
        {
            validateCloseoutVerified(current);
        }
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, status, updateBy);
        if (rows > 0 && "submitted".equals(status))
        {
            saveInitialDraftPackage(current, updateBy);
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_submitted", "web", "提交需求：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "supplement_required".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_supplement_required", "web", "需要补充说明：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "rejected".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_rejected", "web", "需求无法实现：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "completed".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_completed", "web", "归档完成，办结需求：" + current.getDemandNo(), null);
        }
        else if (rows > 0 && "closeout_pending".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_closeout_pending", "web", "验收通过，进入合并归档：" + current.getDemandNo(), null);
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
    @Transactional
    public int submitDemandSupplement(Long demandId, String content, String updateBy)
    {
        if (StringUtils.isBlank(content))
        {
            throw new ServiceException("补充说明不能为空");
        }
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        boolean designAdjustment = "plan_ready".equals(current.getStatus());
        if (!"supplement_required".equals(current.getStatus()) && !designAdjustment)
        {
            throw new ServiceException("当前状态不需要补充说明");
        }
        if (!isCurrentAdmin() && !isCurrentCreator(current))
        {
            throw new ServiceException("只有需求创建人可以补充说明");
        }

        String versionNote = designAdjustment ? "需求设计调整说明" : "需求人补充说明";
        savePackageVersion(demandId, "requirement_supplement", content.trim(), versionNote, updateBy);
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, "plan_pending", updateBy);
        if (rows < 1)
        {
            throw new ServiceException("补充说明提交失败");
        }
        if (rows > 0)
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    designAdjustment ? "demand_design_adjustment_submitted" : "demand_supplement_submitted",
                    "web",
                    (designAdjustment ? "提交需求设计调整说明：" : "提交补充说明：") + current.getDemandNo(),
                    null);
        }
        return rows;
    }

    @Override
    @Transactional
    public int submitDemandRepair(Long demandId, String content, String updateBy)
    {
        if (StringUtils.isBlank(content))
        {
            throw new ServiceException("返修问题说明不能为空");
        }
        ReqDemand current = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (current == null)
        {
            throw new ServiceException("需求不存在");
        }
        if (!"review".equals(current.getStatus()))
        {
            throw new ServiceException("当前状态不能提交返修");
        }
        validateStatusActionRole(current.getStatus(), "repairing");
        validateStatusActionParticipant(current, "repairing");

        savePackageVersion(demandId, "requirement_supplement", content.trim(), "需求人返修问题说明", updateBy);
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, "repairing", updateBy);
        if (rows < 1)
        {
            throw new ServiceException("返修提交失败");
        }
        activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                "demand_repairing", "web", "提交返修问题说明：" + current.getDemandNo(), null);
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
        if (!"submitted".equals(demand.getStatus()) && !"plan_pending".equals(demand.getStatus()))
        {
            throw new ServiceException("当前状态不能生成需求设计指令");
        }
        String taskBranch = suggestedTaskBranch(demand);
        if ("submitted".equals(demand.getStatus()))
        {
            String prompt = "请在需求分析阶段完成需求可行性评估和风险判断，并通过 reqflow MCP 回写评估报告。";
            ReqActionInstruction instruction = actionTokenService.createInstruction(
                    IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                    demand.getProjectId(),
                    demand.getVariantId(),
                    demand.getDemandId(),
                    IReqActionTokenService.TARGET_REQUIREMENT_ANALYSIS,
                    prompt,
                    "生成需求分析指令",
                    operator);
            instruction.setContent(requirementAnalysisInstructionContent(prompt, instruction.getToken(), demand, taskBranch));
            return instruction;
        }
        String prompt = "请在需求生成阶段基于已确认可继续的评估结论生成详细需求设计，并通过 reqflow MCP 回写需求设计。";
        ReqActionInstruction instruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                IReqActionTokenService.TARGET_REQUIREMENT_GENERATE,
                prompt,
                "生成需求设计指令",
                operator);
        instruction.setContent(requirementGenerateInstructionContent(prompt, instruction.getToken(), demand, taskBranch));
        return instruction;
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
        if ("closeout_pending".equals(demand.getStatus()))
        {
            return createRequirementCloseoutInstruction(demand, operator);
        }
        if (!"developing".equals(demand.getStatus()) && !"repairing".equals(demand.getStatus()))
        {
            throw new ServiceException("当前状态不能生成执行任务指令");
        }
        if ("repairing".equals(demand.getStatus()))
        {
            String repairPrompt = "请在返修阶段根据 Review 返修项完成修复、验证和复审，并通过 reqflow MCP 回写返修执行报告和 Review 报告。";
            ReqActionInstruction repairInstruction = actionTokenService.createInstruction(
                    IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP,
                    demand.getProjectId(),
                    demand.getVariantId(),
                    demand.getDemandId(),
                    IReqActionTokenService.TARGET_REQUIREMENT_REPAIR,
                    repairPrompt,
                    "生成返修任务指令",
                    operator);
            repairInstruction.setContent(requirementRepairInstructionContent(repairPrompt, repairInstruction.getToken(),
                    demand, suggestedTaskBranch(demand)));
            return repairInstruction;
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

    @Override
    public ReqCloseoutVerificationResult verifyDemandCloseout(Long demandId)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        validateDeveloperInstructionAccess(demand);
        if (!"closeout_pending".equals(demand.getStatus()))
        {
            return new ReqCloseoutVerificationResult(false, "当前状态无需归档验证");
        }
        try
        {
            validateCloseoutVerified(demand);
            return new ReqCloseoutVerificationResult(true, "归档结果已通过平台验证");
        }
        catch (ServiceException e)
        {
            return new ReqCloseoutVerificationResult(false, e.getMessage());
        }
    }

    private String nextDemandNo()
    {
        Long currentCount = reqDemandMapper.selectDemandCount();
        long sequence = (currentCount == null ? 0L : currentCount) + 1;
        return "REQ-" + String.format("%03d", sequence);
    }

    private String requirementAnalysisInstructionContent(String prompt, String analysisActionToken, ReqDemand demand,
            String taskBranch)
    {
        return actionTokenInstructionContent(prompt, analysisActionToken);
    }

    private String requirementGenerateInstructionContent(String prompt, String generateActionToken, ReqDemand demand,
            String taskBranch)
    {
        return actionTokenInstructionContent(prompt, generateActionToken);
    }

    private String requirementDevelopInstructionContent(String prompt, String developActionToken, ReqDemand demand,
            String taskBranch)
    {
        return actionTokenInstructionContent(prompt, developActionToken);
    }

    private String requirementRepairInstructionContent(String prompt, String repairActionToken, ReqDemand demand,
            String taskBranch)
    {
        return actionTokenInstructionContent(prompt, repairActionToken);
    }

    private String actionTokenInstructionContent(String prompt, String actionToken)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行。"
                + "\nmcpServer: reqflow"
                + "\nactionToken: " + actionToken
                + "\n先调用 get_action_context 获取阶段上下文。";
    }

    private ReqActionInstruction createRequirementCloseoutInstruction(ReqDemand demand, String operator)
    {
        ReqVariant variant = variantMapper.selectReqVariantByVariantId(demand.getVariantId());
        if (variant == null || StringUtils.isEmpty(variant.getBaselineBranch()))
        {
            throw new ServiceException("项目分支不存在或未配置真实分支");
        }
        List<ReqRepository> repositories = loadReadyRepositories(demand.getProjectId());
        if (repositories.isEmpty())
        {
            throw new ServiceException("项目未配置有效代码仓库，不能生成合并归档指令");
        }

        String prompt = "请在需求验收通过后完成合并归档：将本地开发分支压缩合并到需求分支并推送，同时发布需求平台知识库索引。";
        List<ReqActionInstruction> repositoryInstructions = repositories.stream()
                .map(repository -> actionTokenService.createInstruction(
                        IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT,
                        demand.getProjectId(),
                        demand.getVariantId(),
                        demand.getDemandId(),
                        IReqActionTokenService.TARGET_PUBLISH_REPOSITORY_INDEX,
                        prompt + "目标仓库：" + repository.getRepoName(),
                        "生成合并归档指令",
                        operator,
                        ReqCloseoutContext.tokenRemark(repository.getRepoId())))
                .collect(Collectors.toList());
        ReqActionInstruction firstInstruction = repositoryInstructions.get(0);
        firstInstruction.setContent(requirementCloseoutInstructionContent(prompt, repositoryInstructions));
        return firstInstruction;
    }

    private String requirementCloseoutInstructionContent(String prompt, List<ReqActionInstruction> repositoryInstructions)
    {
        StringBuilder content = new StringBuilder();
        content.append(prompt)
                .append("\n请按全局 skill `reqflow-mcp` 执行。")
                .append("\nmcpServer: reqflow")
                .append("\nactionTokens:");
        for (ReqActionInstruction instruction : repositoryInstructions)
        {
            content.append("\n- ").append(instruction.getToken());
        }
        content.append("\n逐个 actionToken 调用 get_action_context 获取归档上下文。");
        return content.toString();
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

    private void validateStatusActionRole(String fromStatus, String targetStatus)
    {
        if (isCurrentAdmin())
        {
            return;
        }
        String requiredRole = requiredRoleForStatusAction(fromStatus, targetStatus);
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

    private String requiredRoleForStatusAction(String fromStatus, String targetStatus)
    {
        if ("closeout_pending".equals(fromStatus) && "completed".equals(targetStatus))
        {
            return ROLE_REQUIREMENT_DEVELOPER;
        }
        if ("submitted".equals(targetStatus) || "confirmed".equals(targetStatus)
                || "repairing".equals(targetStatus) || "closeout_pending".equals(targetStatus))
        {
            return ROLE_REQUIREMENT_USER;
        }
        if ("plan_ready".equals(fromStatus) && "plan_pending".equals(targetStatus))
        {
            return ROLE_REQUIREMENT_USER;
        }
        if ("plan_ready".equals(targetStatus) || "plan_pending".equals(targetStatus)
                || "supplement_required".equals(targetStatus) || "rejected".equals(targetStatus)
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
                || "repairing".equals(targetStatus) || "closeout_pending".equals(targetStatus)
                || ("plan_ready".equals(demand.getStatus()) && "plan_pending".equals(targetStatus)))
        {
            if (isCurrentCreator(demand))
            {
                return;
            }
            throw new ServiceException("只有需求创建人可以执行该流程动作");
        }
        if ("plan_ready".equals(targetStatus) || "plan_pending".equals(targetStatus)
                || "supplement_required".equals(targetStatus) || "rejected".equals(targetStatus)
                || "developing".equals(targetStatus) || "review".equals(targetStatus)
                || ("closeout_pending".equals(demand.getStatus()) && "completed".equals(targetStatus)))
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

    private void validateRequirementDesignGeneratedAfterLatestSupplement(Long demandId)
    {
        ReqPackageVersion requirement = packageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId, "requirement");
        if (requirement == null)
        {
            throw new ServiceException("请先生成新的需求设计后再提交需求人确认");
        }
        ReqPackageVersion supplement = packageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId,
                "requirement_supplement");
        if (supplement != null && !isPackageVersionNotEarlier(requirement, supplement))
        {
            throw new ServiceException("请先生成新的需求设计后再提交需求人确认");
        }
    }

    private void validateRepairArtifactsGeneratedAfterLatestRepairRequest(Long demandId)
    {
        ReqPackageVersion repairRequest = packageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId,
                "requirement_supplement");
        ReqPackageVersion executionReport = packageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId,
                "execution_report");
        ReqPackageVersion reviewReport = packageVersionMapper.selectLatestByDemandIdAndArtifactType(demandId,
                "review_report");
        if (executionReport == null || reviewReport == null)
        {
            throw new ServiceException("请先复制返修任务指令并回写返修执行报告和 Review 报告后再提交返修验收");
        }
        if (repairRequest != null && (!isPackageVersionNotEarlier(executionReport, repairRequest)
                || !isPackageVersionNotEarlier(reviewReport, repairRequest)))
        {
            throw new ServiceException("请先复制返修任务指令并回写返修执行报告和 Review 报告后再提交返修验收");
        }
    }

    private void validateCloseoutVerified(ReqDemand demand)
    {
        ReqVariant variant = variantMapper.selectReqVariantByVariantId(demand.getVariantId());
        if (variant == null || StringUtils.isEmpty(variant.getBaselineBranch()))
        {
            throw new ServiceException("归档结果未通过平台验证：项目分支不存在或未配置真实分支");
        }
        List<ReqRepository> repositories = loadReadyRepositories(demand.getProjectId());
        if (repositories.isEmpty())
        {
            throw new ServiceException("归档结果未通过平台验证：项目未配置有效代码仓库");
        }
        Set<Long> repositoryIds = repositories.stream()
                .map(ReqRepository::getRepoId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> closeoutIndexedRepositoryIds = loadCloseoutImportedBatches(demand, variant.getBaselineBranch(),
                repositoryIds).stream()
                .map(ReqRepositoryIndexBatch::getRepoId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (!closeoutIndexedRepositoryIds.containsAll(repositoryIds) || !hasUsedCloseoutTokensForAllRepositories(demand,
                repositoryIds))
        {
            throw new ServiceException("归档结果未通过平台验证：请先按合并归档指令完成全部仓库 push 和知识库发布");
        }
    }

    private List<ReqRepositoryIndexBatch> loadCloseoutImportedBatches(ReqDemand demand, String branchName,
            Set<Long> repositoryIds)
    {
        List<ReqRepositoryIndexBatch> batches = new ArrayList<>();
        for (Long repositoryId : repositoryIds)
        {
            ReqRepositoryIndexBatch query = new ReqRepositoryIndexBatch();
            query.setProjectId(demand.getProjectId());
            query.setRepoId(repositoryId);
            query.setBranchName(branchName);
            query.setStatus("imported");
            query.setRemark(ReqCloseoutContext.batchRemark(demand.getDemandId(), repositoryId));
            try
            {
                batches.addAll(safeList(batchMapper.selectReqRepositoryIndexBatchList(query)));
            }
            catch (DataAccessException e)
            {
                if (ReqOptionalIndexTableGuard.isMissingTable(e, "req_repository_index_batch"))
                {
                    return Collections.emptyList();
                }
                throw e;
            }
        }
        return batches;
    }

    private boolean hasUsedCloseoutTokensForAllRepositories(ReqDemand demand, Set<Long> repositoryIds)
    {
        for (Long repositoryId : repositoryIds)
        {
            int usedTokenCount = actionTokenMapper.countUsedActionTokenByRemark(
                    IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT,
                    IReqActionTokenService.TARGET_PUBLISH_REPOSITORY_INDEX,
                    demand.getProjectId(),
                    demand.getVariantId(),
                    demand.getDemandId(),
                    ReqCloseoutContext.tokenRemark(repositoryId));
            if (usedTokenCount < 1)
            {
                return false;
            }
        }
        return true;
    }

    private boolean isPackageVersionNotEarlier(ReqPackageVersion candidate, ReqPackageVersion baseline)
    {
        if (candidate.getCreateTime() != null && baseline.getCreateTime() != null)
        {
            return !candidate.getCreateTime().before(baseline.getCreateTime());
        }
        if (candidate.getPackageId() != null && baseline.getPackageId() != null)
        {
            return candidate.getPackageId() >= baseline.getPackageId();
        }
        return false;
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

    private void saveInitialDraftPackage(ReqDemand demand, String operator)
    {
        // 平台提交时生成的基础资料供 MCP 读取，不开放为需求人员的通用资料包写权限。
        savePackageVersion(demand.getDemandId(), "requirement_draft", buildRequirementDraftContent(demand),
                "提交需求自动生成草稿", operator);
        savePackageVersion(demand.getDemandId(), "context_manifest", buildContextManifestContent(demand),
                "提交需求自动生成上下文清单", operator);
    }

    private void savePackageVersion(Long demandId, String artifactType, String content, String versionNote,
            String operator)
    {
        Integer maxVersion = packageVersionMapper.selectMaxVersionNo(demandId, artifactType);
        ReqPackageVersion version = new ReqPackageVersion();
        version.setDemandId(demandId);
        version.setArtifactType(artifactType);
        version.setVersionNo((maxVersion == null ? 0 : maxVersion) + 1);
        version.setContent(content == null ? "" : content);
        version.setStatus("draft");
        version.setVersionNote(versionNote);
        version.setCreateBy(operator);
        packageVersionMapper.insertReqPackageVersion(version);
    }

    private String buildRequirementDraftContent(ReqDemand demand)
    {
        return "# 需求草稿\n\n"
                + "- 需求编号：" + text(demand.getDemandNo()) + "\n"
                + "- 需求标题：" + text(demand.getTitle()) + "\n"
                + "- 需求类型：" + text(demand.getDemandType()) + "\n"
                + "- 需求来源：" + text(demand.getDemandSource()) + "\n"
                + "- 项目ID：" + text(demand.getProjectId()) + "\n"
                + "- 项目分支ID：" + text(demand.getVariantId()) + "\n"
                + "- 指定开发人员ID：" + text(demand.getDeveloperUserId()) + "\n\n"
                + "## 业务背景\n\n" + text(demand.getBusinessBackground()) + "\n\n"
                + "## 预期结果\n\n" + text(demand.getExpectedResult()) + "\n\n"
                + "## 验收标准\n\n" + text(demand.getAcceptanceText()) + "\n\n"
                + "## 附件\n\n" + text(demand.getAttachments()) + "\n";
    }

    private String buildContextManifestContent(ReqDemand demand)
    {
        return "{\n"
                + "  \"demandId\": " + jsonNumber(demand.getDemandId()) + ",\n"
                + "  \"demandNo\": \"" + jsonText(demand.getDemandNo()) + "\",\n"
                + "  \"title\": \"" + jsonText(demand.getTitle()) + "\",\n"
                + "  \"projectId\": " + jsonNumber(demand.getProjectId()) + ",\n"
                + "  \"variantId\": " + jsonNumber(demand.getVariantId()) + ",\n"
                + "  \"moduleId\": " + jsonNumber(demand.getModuleId()) + ",\n"
                + "  \"creatorId\": " + jsonNumber(demand.getCreatorId()) + ",\n"
                + "  \"developerUserId\": " + jsonNumber(demand.getDeveloperUserId()) + "\n"
                + "}\n";
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private String jsonNumber(Number value)
    {
        return value == null ? "null" : String.valueOf(value);
    }

    private String jsonText(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String firstNotEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtils.isNotEmpty(value))
            {
                return value;
            }
        }
        return "";
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
