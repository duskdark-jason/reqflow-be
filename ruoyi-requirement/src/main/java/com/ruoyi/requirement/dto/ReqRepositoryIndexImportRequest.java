package com.ruoyi.requirement.dto;

import java.util.ArrayList;
import java.util.List;

public class ReqRepositoryIndexImportRequest
{
    private Long projectId;
    private Long repoId;
    private String mcpKey;
    private String repoType;
    private String remoteUrl;
    private String branchName;
    private String commitHash;
    private String indexVersion;
    private List<ReqIndexModulePayload> modules = new ArrayList<>();
    private List<ReqIndexImpactPayload> pages = new ArrayList<>();
    private List<ReqIndexImpactPayload> apis = new ArrayList<>();
    private List<ReqIndexImpactPayload> tables = new ArrayList<>();
    private List<ReqIndexImpactPayload> permissions = new ArrayList<>();
    private List<ReqIndexImpactPayload> documents = new ArrayList<>();

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public String getMcpKey() { return mcpKey; }
    public void setMcpKey(String mcpKey) { this.mcpKey = mcpKey; }
    public String getRepoType() { return repoType; }
    public void setRepoType(String repoType) { this.repoType = repoType; }
    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public String getIndexVersion() { return indexVersion; }
    public void setIndexVersion(String indexVersion) { this.indexVersion = indexVersion; }
    public List<ReqIndexModulePayload> getModules() { return modules; }
    public void setModules(List<ReqIndexModulePayload> modules) { this.modules = modules; }
    public List<ReqIndexImpactPayload> getPages() { return pages; }
    public void setPages(List<ReqIndexImpactPayload> pages) { this.pages = pages; }
    public List<ReqIndexImpactPayload> getApis() { return apis; }
    public void setApis(List<ReqIndexImpactPayload> apis) { this.apis = apis; }
    public List<ReqIndexImpactPayload> getTables() { return tables; }
    public void setTables(List<ReqIndexImpactPayload> tables) { this.tables = tables; }
    public List<ReqIndexImpactPayload> getPermissions() { return permissions; }
    public void setPermissions(List<ReqIndexImpactPayload> permissions) { this.permissions = permissions; }
    public List<ReqIndexImpactPayload> getDocuments() { return documents; }
    public void setDocuments(List<ReqIndexImpactPayload> documents) { this.documents = documents; }
}
