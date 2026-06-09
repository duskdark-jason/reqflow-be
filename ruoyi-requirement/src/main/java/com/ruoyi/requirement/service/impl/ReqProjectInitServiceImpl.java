package com.ruoyi.requirement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.requirement.domain.ReqIndexModule;
import com.ruoyi.requirement.domain.ReqModule;
import com.ruoyi.requirement.domain.ReqProject;
import com.ruoyi.requirement.domain.ReqRepository;
import com.ruoyi.requirement.domain.ReqRepositoryIndexBatch;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.dto.ReqProjectInitChecklist;
import com.ruoyi.requirement.dto.ReqProjectInitIndexSummary;
import com.ruoyi.requirement.dto.ReqProjectInitModuleSummary;
import com.ruoyi.requirement.dto.ReqProjectInitRepositoryItem;
import com.ruoyi.requirement.dto.ReqProjectInitRequest;
import com.ruoyi.requirement.dto.ReqProjectInitResponse;
import com.ruoyi.requirement.dto.ReqProjectInitVariantItem;
import com.ruoyi.requirement.mapper.ReqIndexModuleMapper;
import com.ruoyi.requirement.mapper.ReqModuleMapper;
import com.ruoyi.requirement.mapper.ReqProjectMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryIndexBatchMapper;
import com.ruoyi.requirement.mapper.ReqRepositoryMapper;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqProjectInitService;

@Service
public class ReqProjectInitServiceImpl implements IReqProjectInitService
{
    @Autowired private ReqProjectMapper projectMapper;
    @Autowired private ReqRepositoryMapper repositoryMapper;
    @Autowired private ReqVariantMapper variantMapper;
    @Autowired private ReqModuleMapper moduleMapper;
    @Autowired private ReqIndexModuleMapper indexModuleMapper;
    @Autowired private ReqRepositoryIndexBatchMapper batchMapper;

    @Override
    public ReqProjectInitResponse selectProjectInit(Long projectId)
    {
        if (projectId == null)
        {
            throw new ServiceException("项目不能为空");
        }
        ReqProject project = projectMapper.selectReqProjectByProjectId(projectId);
        if (project == null)
        {
            throw new ServiceException("项目不存在");
        }
        return buildResponse(project, loadRepositories(projectId), loadVariants(projectId), loadModules(projectId), loadIndexModules(projectId), loadBatches(projectId));
    }

    @Override
    @Transactional
    public ReqProjectInitResponse insertProjectInit(ReqProjectInitRequest request, String username)
    {
        validateRequest(request, false);

        ReqProject project = request.getProject();
        project.setCreateBy(username);
        projectMapper.insertReqProject(project);
        Long projectId = project.getProjectId();

        List<ReqRepository> repositories = insertRepositories(projectId, request.getRepositories(), username);
        List<ReqVariant> variants = insertVariants(projectId, request.getVariants(), username);
        return buildResponse(project, repositories, variants, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    @Transactional
    public ReqProjectInitResponse updateProjectInit(ReqProjectInitRequest request, String username)
    {
        validateRequest(request, true);

        ReqProject project = request.getProject();
        Long projectId = project.getProjectId();
        project.setUpdateBy(username);
        projectMapper.updateReqProject(project);

        List<ReqRepository> repositories = syncRepositories(projectId, request.getRepositories(), username);
        List<ReqVariant> variants = syncVariants(projectId, request.getVariants(), username);
        return buildResponse(project, repositories, variants, loadModules(projectId), loadIndexModules(projectId), loadBatches(projectId));
    }

    private ReqProjectInitResponse buildResponse(ReqProject project, List<ReqRepository> repositories, List<ReqVariant> variants,
            List<ReqModule> modules, List<ReqIndexModule> indexModules, List<ReqRepositoryIndexBatch> batches)
    {
        ReqProjectInitResponse response = new ReqProjectInitResponse();
        response.setProject(project);
        response.setRepositories(repositories.stream().map(this::toRepositoryItem).collect(Collectors.toList()));
        response.setVariants(variants.stream().map(this::toVariantItem).collect(Collectors.toList()));

        ReqProjectInitModuleSummary moduleSummary = buildModuleSummary(modules, indexModules);
        ReqProjectInitIndexSummary indexSummary = buildIndexSummary(repositories, batches);
        response.setModuleSummary(moduleSummary);
        response.setIndexSummary(indexSummary);
        response.setInitChecklist(buildChecklist(project, repositories, variants, moduleSummary, indexSummary));
        return response;
    }

    private ReqProjectInitModuleSummary buildModuleSummary(List<ReqModule> modules, List<ReqIndexModule> indexModules)
    {
        int manualModules = countDistinctModules(modules);
        int indexedModules = countDistinctIndexModules(indexModules);
        ReqProjectInitModuleSummary summary = new ReqProjectInitModuleSummary();
        summary.setManualModules(manualModules);
        summary.setIndexedModules(indexedModules);
        summary.setTotalModules(manualModules + indexedModules);
        return summary;
    }

    private ReqProjectInitIndexSummary buildIndexSummary(List<ReqRepository> repositories, List<ReqRepositoryIndexBatch> batches)
    {
        ReqProjectInitIndexSummary summary = new ReqProjectInitIndexSummary();
        Set<Long> indexedRepoIds = safeList(batches).stream()
                .filter(batch -> "imported".equals(batch.getStatus()) || StringUtils.isEmpty(batch.getStatus()))
                .map(ReqRepositoryIndexBatch::getRepoId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        int indexedRepositories = 0;
        for (ReqRepository repository : safeList(repositories))
        {
            if (repository.getRepoId() != null && indexedRepoIds.contains(repository.getRepoId()))
            {
                indexedRepositories++;
            }
        }
        summary.setIndexedRepositoryCount(indexedRepositories);
        summary.setUnindexedRepositoryCount(Math.max(safeList(repositories).size() - indexedRepositories, 0));
        if (!safeList(batches).isEmpty())
        {
            ReqRepositoryIndexBatch latest = batches.get(0);
            summary.setLatestCommit(latest.getCommitHash());
            summary.setLatestIndexedAt(latest.getCreateTime());
        }
        return summary;
    }

    private ReqProjectInitChecklist buildChecklist(ReqProject project, List<ReqRepository> repositories, List<ReqVariant> variants,
            ReqProjectInitModuleSummary moduleSummary, ReqProjectInitIndexSummary indexSummary)
    {
        ReqProjectInitChecklist checklist = new ReqProjectInitChecklist();
        boolean projectReady = project != null && StringUtils.isNotEmpty(project.getProjectName()) && StringUtils.isNotEmpty(project.getProjectCode());
        boolean repositoryReady = hasReadyRepositoryType(repositories, "FRONTEND") && hasReadyRepositoryType(repositories, "BACKEND");
        boolean variantReady = safeList(variants).stream().anyMatch(this::isReadyVariant);
        boolean moduleReady = moduleSummary.getTotalModules() != null && moduleSummary.getTotalModules() > 0;
        boolean indexReady = repositoryReady && indexSummary.getUnindexedRepositoryCount() != null && indexSummary.getUnindexedRepositoryCount() == 0;
        checklist.setProjectReady(projectReady);
        checklist.setRepositoryReady(repositoryReady);
        checklist.setVariantReady(variantReady);
        checklist.setModuleReady(moduleReady);
        checklist.setIndexReady(indexReady);
        return checklist;
    }

    private List<ReqRepository> insertRepositories(Long projectId, List<ReqProjectInitRepositoryItem> items, String username)
    {
        List<ReqRepository> repositories = new ArrayList<>();
        for (ReqProjectInitRepositoryItem item : safeList(items))
        {
            ReqRepository repository = toRepository(projectId, item);
            repository.setCreateBy(username);
            repositoryMapper.insertReqRepository(repository);
            repositories.add(repository);
        }
        return repositories;
    }

    private List<ReqVariant> insertVariants(Long projectId, List<ReqProjectInitVariantItem> items, String username)
    {
        List<ReqVariant> variants = new ArrayList<>();
        for (ReqProjectInitVariantItem item : safeList(items))
        {
            ReqVariant variant = toVariant(projectId, item);
            variant.setCreateBy(username);
            variantMapper.insertReqVariant(variant);
            variants.add(variant);
        }
        return variants;
    }

    private List<ReqRepository> syncRepositories(Long projectId, List<ReqProjectInitRepositoryItem> items, String username)
    {
        List<ReqRepository> repositories = new ArrayList<>();
        List<Long> keepIds = new ArrayList<>();
        for (ReqProjectInitRepositoryItem item : safeList(items))
        {
            ReqRepository repository = toRepository(projectId, item);
            if (repository.getRepoId() == null)
            {
                repository.setCreateBy(username);
                repositoryMapper.insertReqRepository(repository);
                addIdIfPresent(keepIds, repository.getRepoId());
            }
            else
            {
                repository.setUpdateBy(username);
                repositoryMapper.updateReqRepository(repository);
                addIdIfPresent(keepIds, repository.getRepoId());
            }
            repositories.add(repository);
        }
        if (keepIds.isEmpty())
        {
            repositoryMapper.deleteReqRepositoryByProjectId(projectId);
        }
        else
        {
            repositoryMapper.deleteReqRepositoryByProjectIdAndRepoIdsNotIn(projectId, keepIds.toArray(new Long[0]));
        }
        return repositories;
    }

    private List<ReqVariant> syncVariants(Long projectId, List<ReqProjectInitVariantItem> items, String username)
    {
        List<ReqVariant> variants = new ArrayList<>();
        List<Long> keepIds = new ArrayList<>();
        for (ReqProjectInitVariantItem item : safeList(items))
        {
            ReqVariant variant = toVariant(projectId, item);
            if (variant.getVariantId() == null)
            {
                variant.setCreateBy(username);
                variantMapper.insertReqVariant(variant);
                addIdIfPresent(keepIds, variant.getVariantId());
            }
            else
            {
                variant.setUpdateBy(username);
                variantMapper.updateReqVariant(variant);
                addIdIfPresent(keepIds, variant.getVariantId());
            }
            variants.add(variant);
        }
        if (keepIds.isEmpty())
        {
            variantMapper.deleteReqVariantByProjectId(projectId);
        }
        else
        {
            variantMapper.deleteReqVariantByProjectIdAndVariantIdsNotIn(projectId, keepIds.toArray(new Long[0]));
        }
        return variants;
    }

    private void addIdIfPresent(List<Long> ids, Long id)
    {
        if (id != null)
        {
            ids.add(id);
        }
    }

    private List<ReqRepository> loadRepositories(Long projectId)
    {
        ReqRepository query = new ReqRepository();
        query.setProjectId(projectId);
        return safeList(repositoryMapper.selectReqRepositoryList(query));
    }

    private List<ReqVariant> loadVariants(Long projectId)
    {
        ReqVariant query = new ReqVariant();
        query.setProjectId(projectId);
        return safeList(variantMapper.selectReqVariantList(query));
    }

    private List<ReqModule> loadModules(Long projectId)
    {
        ReqModule query = new ReqModule();
        query.setProjectId(projectId);
        query.setStatus("0");
        return safeList(moduleMapper.selectReqModuleList(query));
    }

    private List<ReqIndexModule> loadIndexModules(Long projectId)
    {
        ReqIndexModule query = new ReqIndexModule();
        query.setProjectId(projectId);
        query.setStatus("0");
        return safeList(indexModuleMapper.selectReqIndexModuleList(query));
    }

    private List<ReqRepositoryIndexBatch> loadBatches(Long projectId)
    {
        ReqRepositoryIndexBatch query = new ReqRepositoryIndexBatch();
        query.setProjectId(projectId);
        query.setStatus("imported");
        return safeList(batchMapper.selectReqRepositoryIndexBatchList(query));
    }

    private ReqRepository toRepository(Long projectId, ReqProjectInitRepositoryItem item)
    {
        ReqRepository repository = new ReqRepository();
        repository.setRepoId(item.getRepoId());
        repository.setProjectId(projectId);
        repository.setRepoName(item.getRepoName());
        repository.setRepoType(item.getRepoType());
        repository.setRepoUrl(item.getRepoUrl());
        repository.setLocalPathHint(null);
        repository.setDefaultBranch(item.getDefaultBranch());
        repository.setHarnessStatus(firstNotEmpty(item.getHarnessStatus(), "uninitialized"));
        repository.setHarnessCommit(item.getHarnessCommit());
        repository.setLastIndexedAt(item.getLastIndexedAt());
        repository.setStatus(firstNotEmpty(item.getStatus(), "0"));
        repository.setRemark(item.getRemark());
        return repository;
    }

    private ReqVariant toVariant(Long projectId, ReqProjectInitVariantItem item)
    {
        ReqVariant variant = new ReqVariant();
        variant.setVariantId(item.getVariantId());
        variant.setProjectId(projectId);
        variant.setVariantName(item.getVariantName());
        variant.setVariantCode(item.getVariantCode());
        variant.setCustomerName(item.getCustomerName());
        variant.setScopeType(firstNotEmpty(item.getScopeType(), "MAINLINE"));
        variant.setBaselineBranch(item.getBaselineBranch());
        variant.setBranchPolicy(firstNotEmpty(item.getBranchPolicy(), "shared_baseline"));
        variant.setDescription(item.getDescription());
        variant.setStatus(firstNotEmpty(item.getStatus(), "0"));
        variant.setRemark(item.getRemark());
        return variant;
    }

    private ReqProjectInitRepositoryItem toRepositoryItem(ReqRepository repository)
    {
        ReqProjectInitRepositoryItem item = new ReqProjectInitRepositoryItem();
        item.setRepoId(repository.getRepoId());
        item.setProjectId(repository.getProjectId());
        item.setRepoName(repository.getRepoName());
        item.setRepoType(repository.getRepoType());
        item.setRepoUrl(repository.getRepoUrl());
        item.setLocalPathHint(null);
        item.setDefaultBranch(repository.getDefaultBranch());
        item.setHarnessStatus(repository.getHarnessStatus());
        item.setHarnessCommit(repository.getHarnessCommit());
        item.setLastIndexedAt(repository.getLastIndexedAt());
        item.setStatus(repository.getStatus());
        item.setRemark(repository.getRemark());
        return item;
    }

    private ReqProjectInitVariantItem toVariantItem(ReqVariant variant)
    {
        ReqProjectInitVariantItem item = new ReqProjectInitVariantItem();
        item.setVariantId(variant.getVariantId());
        item.setProjectId(variant.getProjectId());
        item.setVariantName(variant.getVariantName());
        item.setVariantCode(variant.getVariantCode());
        item.setCustomerName(variant.getCustomerName());
        item.setScopeType(variant.getScopeType());
        item.setBaselineBranch(variant.getBaselineBranch());
        item.setBranchPolicy(variant.getBranchPolicy());
        item.setDescription(variant.getDescription());
        item.setStatus(variant.getStatus());
        item.setRemark(variant.getRemark());
        return item;
    }

    private void validateRequest(ReqProjectInitRequest request, boolean update)
    {
        if (request == null || request.getProject() == null)
        {
            throw new ServiceException("项目不能为空");
        }
        ReqProject project = request.getProject();
        if (update && project.getProjectId() == null)
        {
            throw new ServiceException("项目ID不能为空");
        }
        if (StringUtils.isEmpty(project.getProjectName()) || StringUtils.isEmpty(project.getProjectCode()))
        {
            throw new ServiceException("项目名称和编码不能为空");
        }
        validateText(project.getDescription());
        validateText(project.getRemark());
        validateText(request.getRemark());
        validateRepositories(request.getRepositories());
        validateVariants(request.getVariants());
    }

    private void validateRepositories(List<ReqProjectInitRepositoryItem> repositories)
    {
        if (safeList(repositories).isEmpty())
        {
            throw new ServiceException("前端仓库和后端仓库不能为空");
        }
        Set<String> repoTypes = new HashSet<>();
        for (ReqProjectInitRepositoryItem repository : safeList(repositories))
        {
            if (StringUtils.isEmpty(repository.getRepoName()) || StringUtils.isEmpty(repository.getRepoType())
                    || StringUtils.isEmpty(repository.getRepoUrl()) || StringUtils.isEmpty(repository.getDefaultBranch()))
            {
                throw new ServiceException("仓库名称、类型、Git远端和默认分支不能为空");
            }
            repoTypes.add(repository.getRepoType().toUpperCase());
            validateText(repository.getRepoUrl());
            validateText(repository.getDefaultBranch());
            validateText(repository.getRemark());
        }
        if (!repoTypes.contains("FRONTEND") || !repoTypes.contains("BACKEND"))
        {
            throw new ServiceException("项目初始化至少需要前端仓库和后端仓库");
        }
    }

    private void validateVariants(List<ReqProjectInitVariantItem> variants)
    {
        if (safeList(variants).isEmpty())
        {
            throw new ServiceException("客户基线不能为空");
        }
        for (ReqProjectInitVariantItem variant : safeList(variants))
        {
            if (StringUtils.isEmpty(variant.getVariantName()) || StringUtils.isEmpty(variant.getVariantCode())
                    || StringUtils.isEmpty(variant.getBaselineBranch()))
            {
                throw new ServiceException("客户线名称、编码和统一基线分支不能为空");
            }
            validateText(variant.getBaselineBranch());
            validateText(variant.getDescription());
            validateText(variant.getRemark());
        }
    }

    private void validateText(String value)
    {
        if (value == null) return;
        String normalized = value.replace('\\', '/').trim();
        if (normalized.startsWith("~")
                || normalized.startsWith("/Users/")
                || normalized.startsWith("/home/")
                || normalized.startsWith("file:/")
                || normalized.matches("^[A-Za-z]:/.*"))
        {
            throw new ServiceException("项目初始化不能保存个人本机绝对路径");
        }
    }

    private boolean hasReadyRepositoryType(List<ReqRepository> repositories, String repoType)
    {
        for (ReqRepository repository : safeList(repositories))
        {
            if (repoType.equalsIgnoreCase(repository.getRepoType())
                    && StringUtils.isNotEmpty(repository.getRepoUrl())
                    && StringUtils.isNotEmpty(repository.getDefaultBranch()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isReadyVariant(ReqVariant variant)
    {
        return StringUtils.isNotEmpty(variant.getVariantName())
                && StringUtils.isNotEmpty(variant.getVariantCode())
                && StringUtils.isNotEmpty(variant.getBaselineBranch());
    }

    private int countDistinctModules(List<ReqModule> modules)
    {
        Set<String> moduleKeys = new LinkedHashSet<>();
        for (ReqModule module : safeList(modules))
        {
            moduleKeys.add(firstNotEmpty(module.getModuleCode(), String.valueOf(module.getModuleId())));
        }
        return moduleKeys.size();
    }

    private int countDistinctIndexModules(List<ReqIndexModule> modules)
    {
        Set<String> moduleKeys = new LinkedHashSet<>();
        for (ReqIndexModule module : safeList(modules))
        {
            moduleKeys.add(firstNotEmpty(module.getModuleCode(), String.valueOf(module.getIndexModuleId())));
        }
        return moduleKeys.size();
    }

    private String firstNotEmpty(String... values)
    {
        for (String value : values)
        {
            if (StringUtils.isNotEmpty(value)) return value;
        }
        return "";
    }

    private <T> List<T> safeList(List<T> source)
    {
        return source == null ? Collections.emptyList() : source;
    }
}
