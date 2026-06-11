package com.ruoyi.requirement.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
import com.ruoyi.requirement.mapper.ReqDemandMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqDemandService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class ReqDemandServiceImpl implements IReqDemandService
{
    private static final String BRANCH_NOT_INITIALIZED_MESSAGE = "项目分支尚未初始化完成，请先完成分支初始化后再提交需求";

    @Autowired
    private ReqDemandMapper reqDemandMapper;

    @Autowired
    private ReqVariantMapper variantMapper;

    @Autowired
    private ReqRepositoryMapper repositoryMapper;

    @Autowired
    private ReqRepositoryIndexBatchMapper batchMapper;

    @Autowired
    private ReqActivityLogService activityLogService;

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
        validateDemandTargetInitialized(reqDemand.getProjectId(), reqDemand.getVariantId());
        reqDemand.setDemandNo(nextDemandNo());
        reqDemand.setStatus("submitted");
        if (reqDemand.getCreatorId() == null)
        {
            reqDemand.setCreatorId(currentUserId());
        }
        int rows = reqDemandMapper.insertReqDemand(reqDemand);
        if (rows > 0)
        {
            activityLogService.record(reqDemand.getCreatorId(), reqDemand.getProjectId(), reqDemand.getDemandId(),
                    "demand_submitted", "web", "提交需求：" + reqDemand.getTitle(), null);
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
        if (reqDemand.getStatus() != null && !reqDemand.getStatus().isBlank()
                && !reqDemand.getStatus().equals(current.getStatus())
                && !ReqDemandStatusTransition.isAllowed(current.getStatus(), reqDemand.getStatus()))
        {
            throw new ServiceException("需求状态流转不允许");
        }
        Long projectId = reqDemand.getProjectId() == null ? current.getProjectId() : reqDemand.getProjectId();
        Long variantId = reqDemand.getVariantId() == null ? current.getVariantId() : reqDemand.getVariantId();
        validateDemandTargetInitialized(projectId, variantId);
        return reqDemandMapper.updateReqDemand(reqDemand);
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
        int rows = reqDemandMapper.updateReqDemandStatus(demandId, status, updateBy);
        if (rows > 0 && "archived".equals(status))
        {
            activityLogService.record(currentUserId(), current.getProjectId(), current.getDemandId(),
                    "demand_archived", "web", "归档需求：" + current.getDemandNo(), null);
        }
        return rows;
    }

    private String nextDemandNo()
    {
        return "REQ-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%03d", reqDemandMapper.selectTodayDemandCount() + 1);
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
