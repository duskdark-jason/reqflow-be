package com.ruoyi.requirement.dto;

public class ReqIndexImportResult
{
    private Long batchId;
    private Integer moduleCount;
    private Integer impactCount;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }
    public Integer getImpactCount() { return impactCount; }
    public void setImpactCount(Integer impactCount) { this.impactCount = impactCount; }
}
