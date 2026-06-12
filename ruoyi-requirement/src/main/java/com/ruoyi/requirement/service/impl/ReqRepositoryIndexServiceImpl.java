package com.ruoyi.requirement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqActionToken;
import com.ruoyi.requirement.domain.ReqDemand;
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
import com.ruoyi.requirement.mapper.ReqDemandMapper;
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
    @Autowired private ReqDemandMapper demandMapper;
    @Autowired private ReqActivityLogService activityLogService;
    @Autowired private IReqActionTokenService actionTokenService;

    @Override
    @Transactional
    public ReqIndexImportResult importRepositoryIndex(ReqRepositoryIndexImportRequest request, String sourceType, String username, Long userId)
    {
        validateRequired(request);
        validateNoPersonalAbsolutePath(request);
        // 发布索引是写路径，不能像页面查询那样降级为空；缺表时必须提示执行迁移，避免用户误以为初始化成功。
        assertIndexTablesReady();

        // actionToken/mcpKey 会把请求补齐到平台登记的项目分支，兼容路径仍支持显式 projectId/repoId。
        ResolvedRequestContext context = resolveRequestContext(request);
        ReqVariant branchVariant = context.branchVariant;
        ReqRepository repository = resolveRepository(request);
        validateCloseoutRepositoryScope(context.actionToken, repository);
        if (branchVariant == null)
        {
            branchVariant = resolveBranchVariant(request);
        }
        validateModuleKnowledgeForProjectInit(request);
        ReqRepositoryIndexBatch batch = buildBatch(request, sourceType, username, context.actionToken);
        batchMapper.insertReqRepositoryIndexBatch(batch);
        Long batchId = batch.getBatchId();

        deactivateExistingRepositoryKnowledge(request, branchVariant, username);

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

    private void assertIndexTablesReady()
    {
        assertIndexTableReady("req_repository_index_batch", () -> batchMapper.checkReqRepositoryIndexBatchTable());
        assertIndexTableReady("req_index_module", () -> moduleMapper.checkReqIndexModuleTable());
        assertIndexTableReady("req_impact_item", () -> impactMapper.checkReqImpactItemTable());
    }

    private void assertIndexTableReady(String tableName, Runnable checker)
    {
        try
        {
            checker.run();
        }
        catch (DataAccessException e)
        {
            if (ReqOptionalIndexTableGuard.isMissingTable(e, tableName))
            {
                throw ReqOptionalIndexTableGuard.missingIndexTable(tableName);
            }
            throw e;
        }
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
                // 只读列表允许旧环境降级为空，项目初始化页面会把它解释为“尚未发布索引”。
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
            if (ReqOptionalIndexTableGuard.isMissingTable(e, "req_index_module")
                    || ReqOptionalIndexTableGuard.isMissingTable(e, "req_repository_index_batch"))
            {
                // 模块索引表或批次表缺失时不吞掉写入失败；这里只服务查询页的兼容展示。
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
        // 影响面推荐必须锁定项目分支，否则不同客户线的模块、权限和表关系会互相污染。
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

    private void validateModuleKnowledgeForProjectInit(ReqRepositoryIndexImportRequest request)
    {
        if (StringUtils.isEmpty(request.getMcpKey()) && StringUtils.isEmpty(request.getActionToken()))
        {
            return;
        }
        // 初始化发布不是简单打点：必须带模块知识，后续需求设计才有可检索的页面/接口/权限归属。
        if (safeList(request.getModules()).isEmpty())
        {
            throw new ServiceException("项目初始化索引必须包含模块知识库，请先按前端页面、菜单或后端主能力分析生成 modules。");
        }
        Set<String> moduleCodes = new HashSet<>();
        for (ReqIndexModulePayload module : safeList(request.getModules()))
        {
            if (StringUtils.isEmpty(module.getModuleCode()) || StringUtils.isEmpty(module.getModuleName()))
            {
                throw new ServiceException("项目初始化索引的模块知识库编码和名称不能为空");
            }
            moduleCodes.add(module.getModuleCode());
        }
        for (ReqIndexImpactPayload impact : collectImpacts(request))
        {
            if (StringUtils.isEmpty(impact.getModuleCode()) || !moduleCodes.contains(impact.getModuleCode()))
            {
                // 影响面必须挂到本次模块，否则后续按模块查影响范围时会出现孤儿接口或孤儿权限。
                throw new ServiceException("项目初始化影响面必须归属到本次 modules 中的 moduleCode");
            }
        }
    }

    private void validateNoPersonalAbsolutePath(ReqRepositoryIndexImportRequest request)
    {
        // 索引会成为团队共享知识库，任何本机绝对路径都会让其他工作空间无法复用。
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

    private ReqRepositoryIndexBatch buildBatch(ReqRepositoryIndexImportRequest request, String sourceType,
            String username, ReqActionToken actionToken)
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
        if (isCloseoutActionToken(actionToken))
        {
            batch.setRemark(ReqCloseoutContext.batchRemark(actionToken.getDemandId(), request.getRepoId()));
        }
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
        module.setRepoScope(firstNotEmpty(payload.getRepoScope(), request.getRepoType()));
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

    private void deactivateExistingRepositoryKnowledge(ReqRepositoryIndexImportRequest request, ReqVariant branchVariant, String username)
    {
        for (Long variantId : collectSnapshotVariantIds(request, branchVariant))
        {
            ReqIndexModule module = new ReqIndexModule();
            module.setProjectId(request.getProjectId());
            module.setRepoId(request.getRepoId());
            module.setVariantId(variantId);
            module.setUpdateBy(username);
            moduleMapper.deactivateReqIndexModulesByRepositoryBranch(module);

            ReqImpactItem item = new ReqImpactItem();
            item.setProjectId(request.getProjectId());
            item.setRepoId(request.getRepoId());
            item.setVariantId(variantId);
            item.setBranchName(request.getBranchName());
            item.setUpdateBy(username);
            impactMapper.deactivateReqImpactItemsByRepositoryBranch(item);
        }
    }

    private Set<Long> collectSnapshotVariantIds(ReqRepositoryIndexImportRequest request, ReqVariant branchVariant)
    {
        Set<Long> variantIds = new HashSet<>();
        if (branchVariant != null)
        {
            variantIds.add(branchVariant.getVariantId());
        }
        for (ReqIndexModulePayload module : safeList(request.getModules()))
        {
            variantIds.add(module.getVariantId());
        }
        for (ReqIndexImpactPayload impact : collectImpacts(request))
        {
            variantIds.add(impact.getVariantId());
        }
        if (variantIds.isEmpty())
        {
            variantIds.add(null);
        }
        return variantIds;
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

    private ResolvedRequestContext resolveRequestContext(ReqRepositoryIndexImportRequest request)
    {
        if (StringUtils.isNotEmpty(request.getActionToken()))
        {
            // actionToken 优先级最高，它把一次初始化动作绑定到确定的项目、分支和目标 MCP 方法。
            return resolveBranchByActionToken(request.getActionToken(), request);
        }
        if (StringUtils.isEmpty(request.getMcpKey()))
        {
            return new ResolvedRequestContext(null, null);
        }
        ReqVariant branch = resolveBranchByMcpKey(request.getMcpKey());
        request.setProjectId(branch.getProjectId());
        request.setBranchName(branch.getBaselineBranch());
        return new ResolvedRequestContext(branch, null);
    }

    private ResolvedRequestContext resolveBranchByActionToken(String actionToken, ReqRepositoryIndexImportRequest request)
    {
        ReqActionToken token = actionTokenService.resolveToken(actionToken);
        if (!IReqActionTokenService.TARGET_PUBLISH_REPOSITORY_INDEX.equals(token.getTargetMethod()))
        {
            throw new ServiceException("动作Token不能用于仓库索引发布");
        }
        if (!IReqActionTokenService.ACTION_PROJECT_INIT.equals(token.getActionType())
                && !IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT.equals(token.getActionType()))
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
        if (IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT.equals(token.getActionType()))
        {
            validateCloseoutActionToken(token, branch);
        }
        request.setProjectId(branch.getProjectId());
        request.setBranchName(branch.getBaselineBranch());
        return new ResolvedRequestContext(branch, token);
    }

    private void validateCloseoutActionToken(ReqActionToken token, ReqVariant branch)
    {
        if (token.getDemandId() == null)
        {
            throw new ServiceException("归档动作Token未绑定需求");
        }
        ReqDemand demand = demandMapper.selectReqDemandByDemandId(token.getDemandId());
        if (demand == null)
        {
            throw new ServiceException("归档动作Token绑定的需求不存在");
        }
        if (!"closeout_pending".equals(demand.getStatus()))
        {
            throw new ServiceException("归档动作Token所属流程阶段已结束，请重新生成指令");
        }
        if (!branch.getProjectId().equals(demand.getProjectId())
                || !branch.getVariantId().equals(demand.getVariantId()))
        {
            throw new ServiceException("归档动作Token与需求项目分支不一致");
        }
    }

    private void validateCloseoutRepositoryScope(ReqActionToken token, ReqRepository repository)
    {
        if (!isCloseoutActionToken(token))
        {
            return;
        }
        Long expectedRepoId = ReqCloseoutContext.repoIdFromTokenRemark(token.getRemark());
        if (expectedRepoId == null)
        {
            throw new ServiceException("归档动作Token未绑定目标仓库");
        }
        if (repository == null || repository.getRepoId() == null || !expectedRepoId.equals(repository.getRepoId()))
        {
            throw new ServiceException("归档动作Token与目标仓库不一致");
        }
    }

    private boolean isCloseoutActionToken(ReqActionToken token)
    {
        return token != null && IReqActionTokenService.ACTION_REQUIREMENT_CLOSEOUT.equals(token.getActionType());
    }

    private static class ResolvedRequestContext
    {
        private final ReqVariant branchVariant;

        private final ReqActionToken actionToken;

        private ResolvedRequestContext(ReqVariant branchVariant, ReqActionToken actionToken)
        {
            this.branchVariant = branchVariant;
            this.actionToken = actionToken;
        }
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
            // 初始化指令侧只信任平台登记的远端地址，不允许调用方自己传 repoId 指向其他仓库。
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
