package com.ruoyi.requirement.dto;

import java.util.Date;

public class ReqProjectInitIndexSummary
{
    private Date latestIndexedAt;
    private String latestCommit;
    private Integer indexedRepositoryCount;
    private Integer unindexedRepositoryCount;

    public Date getLatestIndexedAt() { return latestIndexedAt; }
    public void setLatestIndexedAt(Date latestIndexedAt) { this.latestIndexedAt = latestIndexedAt; }
    public String getLatestCommit() { return latestCommit; }
    public void setLatestCommit(String latestCommit) { this.latestCommit = latestCommit; }
    public Integer getIndexedRepositoryCount() { return indexedRepositoryCount; }
    public void setIndexedRepositoryCount(Integer indexedRepositoryCount) { this.indexedRepositoryCount = indexedRepositoryCount; }
    public Integer getUnindexedRepositoryCount() { return unindexedRepositoryCount; }
    public void setUnindexedRepositoryCount(Integer unindexedRepositoryCount) { this.unindexedRepositoryCount = unindexedRepositoryCount; }
}
