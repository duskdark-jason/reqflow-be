package com.ruoyi.requirement.dto;

import java.util.List;
import com.ruoyi.requirement.domain.ReqProject;

public class ReqProjectInitResponse
{
    private ReqProject project;
    private List<ReqProjectInitRepositoryItem> repositories;
    private List<ReqProjectInitVariantItem> variants;
    private ReqProjectInitModuleSummary moduleSummary;
    private ReqProjectInitIndexSummary indexSummary;
    private ReqProjectInitChecklist initChecklist;

    public ReqProject getProject() { return project; }
    public void setProject(ReqProject project) { this.project = project; }
    public List<ReqProjectInitRepositoryItem> getRepositories() { return repositories; }
    public void setRepositories(List<ReqProjectInitRepositoryItem> repositories) { this.repositories = repositories; }
    public List<ReqProjectInitVariantItem> getVariants() { return variants; }
    public void setVariants(List<ReqProjectInitVariantItem> variants) { this.variants = variants; }
    public ReqProjectInitModuleSummary getModuleSummary() { return moduleSummary; }
    public void setModuleSummary(ReqProjectInitModuleSummary moduleSummary) { this.moduleSummary = moduleSummary; }
    public ReqProjectInitIndexSummary getIndexSummary() { return indexSummary; }
    public void setIndexSummary(ReqProjectInitIndexSummary indexSummary) { this.indexSummary = indexSummary; }
    public ReqProjectInitChecklist getInitChecklist() { return initChecklist; }
    public void setInitChecklist(ReqProjectInitChecklist initChecklist) { this.initChecklist = initChecklist; }
}
