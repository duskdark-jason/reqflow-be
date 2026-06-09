package com.ruoyi.requirement.dto;

public class ReqProjectInitChecklist
{
    private Boolean projectReady;
    private Boolean repositoryReady;
    private Boolean variantReady;
    private Boolean moduleReady;
    private Boolean indexReady;

    public Boolean getProjectReady() { return projectReady; }
    public void setProjectReady(Boolean projectReady) { this.projectReady = projectReady; }
    public Boolean getRepositoryReady() { return repositoryReady; }
    public void setRepositoryReady(Boolean repositoryReady) { this.repositoryReady = repositoryReady; }
    public Boolean getVariantReady() { return variantReady; }
    public void setVariantReady(Boolean variantReady) { this.variantReady = variantReady; }
    public Boolean getModuleReady() { return moduleReady; }
    public void setModuleReady(Boolean moduleReady) { this.moduleReady = moduleReady; }
    public Boolean getIndexReady() { return indexReady; }
    public void setIndexReady(Boolean indexReady) { this.indexReady = indexReady; }
}
