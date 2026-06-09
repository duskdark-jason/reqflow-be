package com.ruoyi.requirement.dto;

import java.util.List;
import com.ruoyi.requirement.domain.ReqProject;

public class ReqProjectInitRequest
{
    private ReqProject project;
    private List<ReqProjectInitRepositoryItem> repositories;
    private List<ReqProjectInitVariantItem> variants;
    private String remark;

    public ReqProject getProject() { return project; }
    public void setProject(ReqProject project) { this.project = project; }
    public List<ReqProjectInitRepositoryItem> getRepositories() { return repositories; }
    public void setRepositories(List<ReqProjectInitRepositoryItem> repositories) { this.repositories = repositories; }
    public List<ReqProjectInitVariantItem> getVariants() { return variants; }
    public void setVariants(List<ReqProjectInitVariantItem> variants) { this.variants = variants; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
