package com.ruoyi.requirement.dto;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.requirement.domain.ReqImpactItem;

public class ReqImpactSuggestResult
{
    private List<ReqImpactItem> pages = new ArrayList<>();
    private List<ReqImpactItem> apis = new ArrayList<>();
    private List<ReqImpactItem> tables = new ArrayList<>();
    private List<ReqImpactItem> permissions = new ArrayList<>();
    private List<ReqImpactItem> documents = new ArrayList<>();

    public List<ReqImpactItem> getPages() { return pages; }
    public void setPages(List<ReqImpactItem> pages) { this.pages = pages; }
    public List<ReqImpactItem> getApis() { return apis; }
    public void setApis(List<ReqImpactItem> apis) { this.apis = apis; }
    public List<ReqImpactItem> getTables() { return tables; }
    public void setTables(List<ReqImpactItem> tables) { this.tables = tables; }
    public List<ReqImpactItem> getPermissions() { return permissions; }
    public void setPermissions(List<ReqImpactItem> permissions) { this.permissions = permissions; }
    public List<ReqImpactItem> getDocuments() { return documents; }
    public void setDocuments(List<ReqImpactItem> documents) { this.documents = documents; }
}
