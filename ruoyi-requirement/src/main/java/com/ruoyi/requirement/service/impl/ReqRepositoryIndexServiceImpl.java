package com.ruoyi.requirement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqImpactItem;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;
import com.ruoyi.requirement.dto.ReqImpactSuggestResult;
import com.ruoyi.requirement.dto.ReqIndexImpactPayload;
import com.ruoyi.requirement.dto.ReqIndexImportResult;
import com.ruoyi.requirement.dto.ReqIndexModulePayload;
import com.ruoyi.requirement.dto.ReqRepositoryIndexImportRequest;
import com.ruoyi.requirement.mapper.ReqImpactItemMapper;
import com.ruoyi.requirement.mapper.ReqIndexModuleMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqActionTokenService;
import com.ruoyi.requirement.service.IReqRepositoryIndexService;
import com.ruoyi.requirement.service.ReqActivityLogService;

@Service
public class ReqRepositoryIndexServiceImpl implements IReqRepositoryIndexService
{
    @Autowired private ReqRepositoryIndexBatchMapper batchMapper;
    @Autowired private ReqIndexModuleMapper moduleMapper;
    @Autowired private ReqImpactItemMapper impactMapper;
    @Autowired private ReqRepositoryMapper repositoryMapper;
    @Autowired private ReqVariantMapper variantMapper;
    @Autowired private ReqActivityLogService activityLogService;
    @Autowired private IReqActionTokenService actionTokenService;

    @Override
    @Transactional
    public ReqIndexImportResult importRepositoryIndex(ReqRepositoryIndexImportRequest request, String sourceType, String username, Long userId)
    {
        validateRequired(request);
        validateNoPersonalAbsolutePath(request);

        ReqVariant branchVariant = resolveRequestContext(request);
        resolveRepository(request);
        if (branchVariant == null)
        {
            branchVariant = resolveBranchVariant(request);
        }
        ReqRepositoryIndexBatch batch = buildBatch(request, sourceType, username);
        batchMapper.insertReqRepositoryIndexBatch(batch);
        Long batchId = batch.getBatchId();

        int moduleCount = 0;
        for (ReqIndexModulePayload payload : safeList(request.getModules()))
        {
            moduleMapper.insertReqIndexModule(buildModule(request, batchId, payload, branchVariant, username));
            moduleCount++;
        }

        int impactCount = 0;
        for (ReqIndexImpactPayload payload : collectImpacts(request))
        {
            impactMapper.insertReqImpactItem(buildImpact(request, batchId, payload, branchVariant, username));
            impactCount++;
        }

        ReqRepository update = new ReqRepository();
        update.setRepoId(request.getRepoId());
        update.setHarnessStatus("indexed");
        update.setHarnessCommit(request.getCommitHash());
        update.setUpdateBy(username);
        repositoryMapper.updateHarnessInitResult(update);

        activityLogService.record(userId, request.getProjectId(), null, "repository_index_published",
                sourceType == null ? "web" : sourceType, "仓库索引发布：" + request.getRemoteUrl(), null);

        ReqIndexImportResult result = new ReqIndexImportResult();
        result.setBatchId(batchId);
        result.setModuleCount(moduleCount);
        result.setImpactCount(impactCount);
        return result;
    }

    @Override
    public List<ReqRepositoryIndexBatch> selectBatchList(ReqRepositoryIndexBatch batch)
    {
        try
        {
            return batchMapper.selectReqRepositoryIndexBatchList(batch);
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

    @Override
    public List<ReqIndexModule> selectModuleList(ReqIndexModule module)
    {
        try
        {
            return moduleMapper.selectReqIndexModuleList(module);
        }
        catch (DataAccessException e)
        {
            if (ReqOptionalIndexTableGuard.isMissingTable(e, "req_index_module"))
            {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    @Override
    public ReqImpactSuggestResult suggestImpact(ReqImpactSuggestQuery query)
    {
        enrichSuggestQuery(query);
        ReqImpactSuggestResult result = new ReqImpactSuggestResult();
        for (ReqImpactItem item : impactMapper.selectLatestImpactItems(query))
        {
            if ("PAGE".equals(item.getItemType())) addUnique(result.getPages(), item);
            else if ("API".equals(item.getItemType())) addUnique(result.getApis(), item);
            else if ("TABLE".equals(item.getItemType())) addUnique(result.getTables(), item);
            else if ("PERMISSION".equals(item.getItemType())) addUnique(result.getPermissions(), item);
            else if ("DOCUMENT".equals(item.getItemType())) addUnique(result.getDocuments(), item);
        }
        return result;
    }

    private void enrichSuggestQuery(ReqImpactSuggestQuery query)
    {
        if (query == null || query.getVariantId() == null)
        {
            return;
        }
        ReqVariant variant = variantMapper.selectReqVariantByVariantId(query.getVariantId());
        if (variant == null)
        {
            throw new ServiceException("项目分支不存在");
        }
        if (query.getProjectId() != null && !query.getProjectId().equals(variant.getProjectId()))
        {
            throw new ServiceException("项目分支不属于当前项目");
        }
        query.setBranchName(variant.getBaselineBranch());
    }

    private void addUnique(List<ReqImpactItem> target, ReqImpactItem item)
    {
        String key = impactKey(item);
        for (ReqImpactItem exists : target)
        {
            if (key.equals(impactKey(exists)))
            {
                return;
            }
        }
        target.add(item);
    }

    private String impactKey(ReqImpactItem item)
    {
        String key = firstNotEmpty(item.getItemKey(), item.getApiPath(), item.getPermissionKey(), item.getTableName(), item.getRelativePath(), item.getItemName());
        return item.getItemType() + "|" + key;
    }

    private String firstNotEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtils.isNotEmpty(value)) return value;
        }
        return "";
    }

    private void validateRequired(ReqRepositoryIndexImportRequest request)
    {
        if (request == null)
        {
            throw new ServiceException("项目和仓库不能为空");
        }
        if (StringUtils.isNotEmpty(request.getMcpKey()) || StringUtils.isNotEmpty(request.getActionToken()))
        {
            if (StringUtils.isEmpty(request.getRemoteUrl()) || StringUtils.isEmpty(request.getCommitHash()) || StringUtils.isEmpty(request.getIndexVersion()))
            {
                throw new ServiceException("初始化指令、仓库远端、commit 和索引版本不能为空");
            }
            return;
        }
        if (request.getProjectId() == null || request.getRepoId() == null)
        {
            throw new ServiceException("项目和仓库不能为空");
        }
        if (StringUtils.isEmpty(request.getRemoteUrl()) || StringUtils.isEmpty(request.getBranchName())
                || StringUtils.isEmpty(request.getCommitHash()) || StringUtils.isEmpty(request.getIndexVersion()))
        {
            throw new ServiceException("仓库远端、分支、commit 和索引版本不能为空");
        }
    }

    private void validateNoPersonalAbsolutePath(ReqRepositoryIndexImportRequest request)
    {
        checkPath(request.getRemoteUrl());
        checkPath(request.getMcpKey());
        checkPath(request.getActionToken());
        checkPath(request.getBranchName());
        checkPath(request.getCommitHash());
        for (ReqIndexModulePayload module : safeList(request.getModules()))
        {
            checkPath(module.getRelativePath());
            checkPath(module.getSourceRef());
        }
        for (ReqIndexImpactPayload impact : collectImpacts(request))
        {
            checkPath(impact.getRelativePath());
            checkPath(impact.getItemKey());
            checkPath(impact.getApiPath());
            checkPath(impact.getPermissionKey());
            checkPath(impact.getTableName());
        }
    }

    private void checkPath(String value)
    {
        if (value == null) return;
        String normalized = value.replace('\\', '/');
        if (normalized.startsWith("/Users/") || normalized.startsWith("/home/") || normalized.matches("^[A-Za-z]:/Users/.*"))
        {
            throw new ServiceException("索引结果不能包含个人本机绝对路径");
        }
    }

    private ReqRepositoryIndexBatch buildBatch(ReqRepositoryIndexImportRequest request, String sourceType, String username)
    {
        ReqRepositoryIndexBatch batch = new ReqRepositoryIndexBatch();
        batch.setProjectId(request.getProjectId());
        batch.setRepoId(request.getRepoId());
        batch.setRepoType(request.getRepoType());
        batch.setRemoteUrl(request.getRemoteUrl());
        batch.setBranchName(request.getBranchName());
        batch.setCommitHash(request.getCommitHash());
        batch.setIndexVersion(request.getIndexVersion());
        batch.setSourceType(sourceType == null ? "web" : sourceType);
        batch.setModuleCount(safeList(request.getModules()).size());
        batch.setPageCount(safeList(request.getPages()).size());
        batch.setApiCount(safeList(request.getApis()).size());
        batch.setTableCount(safeList(request.getTables()).size());
        batch.setPermissionCount(safeList(request.getPermissions()).size());
        batch.setDocumentCount(safeList(request.getDocuments()).size());
        batch.setStatus("imported");
        batch.setCreateBy(username);
        return batch;
    }

    private ReqIndexModule buildModule(ReqRepositoryIndexImportRequest request, Long batchId, ReqIndexModulePayload payload, ReqVariant branchVariant, String username)
    {
        ReqIndexModule module = new ReqIndexModule();
        module.setBatchId(batchId);
        module.setProjectId(request.getProjectId());
        module.setRepoId(request.getRepoId());
        module.setVariantId(payload.getVariantId() == null && branchVariant != null ? branchVariant.getVariantId() : payload.getVariantId());
        module.setParentCode(payload.getParentCode());
        module.setModuleCode(payload.getModuleCode());
        module.setModuleName(payload.getModuleName());
        module.setModuleType(payload.getModuleType());
        module.setRepoScope(payload.getRepoScope());
        module.setRelativePath(payload.getRelativePath());
        module.setSourceRef(payload.getSourceRef());
        module.setSummary(payload.getSummary());
        module.setOrderNum(payload.getOrderNum());
        module.setStatus("0");
        module.setCreateBy(username);
        return module;
    }

    private ReqImpactItem buildImpact(ReqRepositoryIndexImportRequest request, Long batchId, ReqIndexImpactPayload payload, ReqVariant branchVariant, String username)
    {
        ReqImpactItem item = new ReqImpactItem();
        item.setBatchId(batchId);
        item.setProjectId(request.getProjectId());
        item.setRepoId(request.getRepoId());
        item.setVariantId(resolveImpactVariantId(request, payload, branchVariant));
        item.setBranchName(request.getBranchName());
        item.setModuleCode(payload.getModuleCode());
        item.setModuleId(payload.getModuleId());
        item.setItemType(payload.getItemType());
        item.setItemName(payload.getItemName());
        item.setItemKey(payload.getItemKey());
        item.setRelativePath(payload.getRelativePath());
        item.setHttpMethod(payload.getHttpMethod());
        item.setApiPath(payload.getApiPath());
        item.setPermissionKey(payload.getPermissionKey());
        item.setTableName(payload.getTableName());
        item.setSummary(payload.getSummary());
        item.setTags(payload.getTags());
        item.setStatus("0");
        item.setCreateBy(username);
        return item;
    }

    private Long resolveImpactVariantId(ReqRepositoryIndexImportRequest request, ReqIndexImpactPayload payload, ReqVariant branchVariant)
    {
        if (payload.getVariantId() != null)
        {
            return payload.getVariantId();
        }
        for (ReqIndexModulePayload module : safeList(request.getModules()))
        {
            if (StringUtils.isNotEmpty(payload.getModuleCode()) && payload.getModuleCode().equals(module.getModuleCode()) && module.getVariantId() != null)
            {
                return module.getVariantId();
            }
        }
        return branchVariant == null ? null : branchVariant.getVariantId();
    }

    private ReqVariant resolveBranchVariant(ReqRepositoryIndexImportRequest request)
    {
        if (StringUtils.isNotEmpty(request.getMcpKey()))
        {
            return resolveBranchByMcpKey(request.getMcpKey());
        }
        ReqVariant query = new ReqVariant();
        query.setProjectId(request.getProjectId());
        query.setBaselineBranch(request.getBranchName());
        query.setStatus("0");
        List<ReqVariant> variants = variantMapper.selectReqVariantList(query);
        return variants == null || variants.isEmpty() ? null : variants.get(0);
    }

    private ReqVariant resolveRequestContext(ReqRepositoryIndexImportRequest request)
    {
        if (StringUtils.isNotEmpty(request.getActionToken()))
        {
            return resolveBranchByActionToken(request.getActionToken(), request);
        }
        if (StringUtils.isEmpty(request.getMcpKey()))
        {
            return null;
        }
        ReqVariant branch = resolveBranchByMcpKey(request.getMcpKey());
        request.setProjectId(branch.getProjectId());
        request.setBranchName(branch.getBaselineBranch());
        return branch;
    }

    private ReqVariant resolveBranchByActionToken(String actionToken, ReqRepositoryIndexImportRequest request)
    {
        ReqActionToken token = actionTokenService.resolveToken(actionToken);
        if (!IReqActionTokenService.ACTION_PROJECT_INIT.equals(token.getActionType())
                || !"publish_repository_index".equals(token.getTargetMethod()))
        {
            throw new ServiceException("动作Token不能用于仓库索引发布");
        }
        if (token.getVariantId() == null)
        {
            throw new ServiceException("动作Token未绑定项目分支");
        }
        ReqVariant branch = variantMapper.selectReqVariantByVariantId(token.getVariantId());
        if (branch == null || !"0".equals(branch.getStatus()))
        {
            throw new ServiceException("动作Token对应的项目分支不存在");
        }
        if (token.getProjectId() != null && !token.getProjectId().equals(branch.getProjectId()))
        {
            throw new ServiceException("动作Token项目与分支不一致");
        }
        request.setProjectId(branch.getProjectId());
        request.setBranchName(branch.getBaselineBranch());
        return branch;
    }

    private ReqVariant resolveBranchByMcpKey(String mcpKey)
    {
        ReqVariant query = new ReqVariant();
        query.setMcpKey(mcpKey);
        query.setStatus("0");
        List<ReqVariant> variants = variantMapper.selectReqVariantList(query);
        if (variants == null || variants.isEmpty())
        {
            throw new ServiceException("MCP key 对应的项目分支不存在");
        }
        return variants.get(0);
    }

    private ReqRepository resolveRepository(ReqRepositoryIndexImportRequest request)
    {
        ReqRepository repository;
        if (StringUtils.isNotEmpty(request.getMcpKey()) || StringUtils.isNotEmpty(request.getActionToken()))
        {
            ReqRepository query = new ReqRepository();
            query.setProjectId(request.getProjectId());
            query.setRepoUrl(request.getRemoteUrl());
            List<ReqRepository> repositories = repositoryMapper.selectReqRepositoryList(query);
            repository = repositories == null || repositories.isEmpty() ? null : repositories.get(0);
            if (repository == null)
            {
                throw new ServiceException("初始化指令对应项目下不存在该仓库");
            }
            request.setRepoId(repository.getRepoId());
            request.setRepoType(firstNotEmpty(request.getRepoType(), repository.getRepoType()));
            return repository;
        }
        repository = repositoryMapper.selectReqRepositoryByRepoId(request.getRepoId());
        if (repository == null)
        {
            throw new ServiceException("仓库不存在");
        }
        if (!request.getProjectId().equals(repository.getProjectId()))
        {
            throw new ServiceException("仓库不属于当前项目");
        }
        if (StringUtils.isNotEmpty(repository.getRepoUrl()) && !repository.getRepoUrl().equals(request.getRemoteUrl()))
        {
            throw new ServiceException("仓库远端地址与平台登记不一致");
        }
        request.setRepoType(firstNotEmpty(request.getRepoType(), repository.getRepoType()));
        return repository;
    }

    private List<ReqIndexImpactPayload> collectImpacts(ReqRepositoryIndexImportRequest request)
    {
        List<ReqIndexImpactPayload> impacts = new ArrayList<>();
        impacts.addAll(typed(request.getPages(), "PAGE"));
        impacts.addAll(typed(request.getApis(), "API"));
        impacts.addAll(typed(request.getTables(), "TABLE"));
        impacts.addAll(typed(request.getPermissions(), "PERMISSION"));
        impacts.addAll(typed(request.getDocuments(), "DOCUMENT"));
        return impacts;
    }

    private List<ReqIndexImpactPayload> typed(List<ReqIndexImpactPayload> source, String itemType)
    {
        List<ReqIndexImpactPayload> result = new ArrayList<>();
        for (ReqIndexImpactPayload item : safeList(source))
        {
            if (StringUtils.isEmpty(item.getItemType()))
            {
                item.setItemType(itemType);
            }
            result.add(item);
        }
        return result;
    }

    private <T> List<T> safeList(List<T> source)
    {
        return source == null ? Collections.emptyList() : source;
    }
}
