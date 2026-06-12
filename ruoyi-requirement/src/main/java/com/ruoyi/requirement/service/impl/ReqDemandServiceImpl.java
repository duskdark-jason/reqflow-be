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

    private static final String ACTION_TOKEN_USAGE_RULE = "有效期：24小时内有效，仅可使用一次；过期或已使用后需重新生成。";

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
        return reqDemandMapper.selectReqDemandByDemandId(demandId);
    }

    @Override
    public List<ReqDemand> selectReqDemandList(ReqDemand reqDemand)
    {
        return reqDemandMapper.selectReqDemandList(reqDemand);
    }

    @Override
    public int insertReqDemand(ReqDemand reqDemand)
    {
        validateDemandContent(reqDemand);
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
        if (reqDemand.getCreatorId() == null || current.getCreatorId() == null
                || !current.getCreatorId().equals(reqDemand.getCreatorId()))
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
    public ReqActionInstruction createRequirementPlanInstruction(Long demandId, String operator)
    {
        ReqDemand demand = reqDemandMapper.selectReqDemandByDemandId(demandId);
        if (demand == null)
        {
            throw new ServiceException("需求不存在");
        }
        String prompt = "请基于需求上下文生成需求设计，并通过 reqflow MCP 回写资料包。";
        ReqActionInstruction instruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_PLAN,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                "save_requirement_package",
                prompt,
                "复制生成需求设计指令",
                operator);
        // 编排指令给审批人员复制到 Codex，动作 Token 仅定位需求上下文，不替代人员 MCP Key 鉴权。
        instruction.setContent(requirementPlanInstructionContent(prompt, instruction.getToken(), demand));
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
        String planPrompt = "请基于已确认需求设计生成执行计划，并通过 reqflow MCP 回写执行计划。";
        ReqActionInstruction planInstruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                "save_development_plan",
                planPrompt,
                "复制执行任务指令",
                operator);
        String reportPrompt = "请根据已确认需求设计和执行计划完成开发、验证并通过 reqflow MCP 回写执行报告。";
        ReqActionInstruction reportInstruction = actionTokenService.createInstruction(
                IReqActionTokenService.ACTION_REQUIREMENT_DEVELOP,
                demand.getProjectId(),
                demand.getVariantId(),
                demand.getDemandId(),
                "upload_execution_report",
                reportPrompt,
                "复制执行报告指令",
                operator);
        planInstruction.setContent(requirementDevelopInstructionContent(planPrompt, planInstruction.getToken(),
                reportInstruction.getToken(), demand));
        return planInstruction;
    }

    private String nextDemandNo()
    {
        Long currentCount = reqDemandMapper.selectDemandCount();
        long sequence = (currentCount == null ? 0L : currentCount) + 1;
        return "REQ-" + String.format("%03d", sequence);
    }

    private String requirementPlanInstructionContent(String prompt, String actionToken, ReqDemand demand)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行 Reqflow 需求设计生成。"
                + "\nmcpServer: reqflow"
                + "\ntoolName: save_requirement_package"
                + "\nmcpTool: reqflow.save_requirement_package"
                + "\ntargetMethod: save_requirement_package"
                + "\ndemandId: " + demand.getDemandId()
                + "\ndemandNo: " + demand.getDemandNo()
                + "\nactionToken: " + actionToken
                + "\n" + ACTION_TOKEN_USAGE_RULE
                + "\n要求：先读取需求详情和现有资料包，迭代出完整需求设计。"
                + "\n保存要求：调用 save_requirement_package，arguments.actionToken 填上面的 actionToken，content 填完整需求设计。"
                + "\n注意：actionToken 是 save_requirement_package 的 arguments.actionToken，不是 X-MCP-Key；MCP 鉴权仍使用人员 X-MCP-Key。";
    }

    private String requirementDevelopInstructionContent(String prompt, String planActionToken, String reportActionToken, ReqDemand demand)
    {
        return prompt
                + "\n请按全局 skill `reqflow-mcp` 执行 Reqflow 需求开发。"
                + "\nmcpServer: reqflow"
                + "\ntoolName: save_development_plan"
                + "\nmcpTool: reqflow.save_development_plan"
                + "\ntoolName: upload_execution_report"
                + "\nmcpTool: reqflow.upload_execution_report"
                + "\ntargetMethods: save_development_plan, upload_execution_report"
                + "\ndemandId: " + demand.getDemandId()
                + "\ndemandNo: " + demand.getDemandNo()
                + "\n执行计划 actionToken: " + planActionToken
                + "\n执行报告 actionToken: " + reportActionToken
                + "\n" + ACTION_TOKEN_USAGE_RULE
                + "\n要求：先读取需求详情和需求设计，生成执行计划；再按目标仓库规范完成实现、验证和提交。"
                + "\n回写要求：先调用 save_development_plan，arguments.actionToken 填执行计划 actionToken；开发完成后调用 upload_execution_report，arguments.actionToken 填执行报告 actionToken。"
                + "\n注意：两个 actionToken 均只能成功使用一次，且都不是 X-MCP-Key；MCP 鉴权仍使用人员 X-MCP-Key。";
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
