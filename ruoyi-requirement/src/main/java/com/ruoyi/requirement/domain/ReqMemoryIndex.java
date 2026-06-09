package com.ruoyi.requirement.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目记忆索引对象 req_memory_index
 */
public class ReqMemoryIndex extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记忆ID */
    private Long memoryId;

    /** 项目ID */
    private Long projectId;

    /** 仓库ID */
    private Long repoId;

    /** 定制线ID */
    private Long variantId;

    /** 文档类型 */
    private String docType;

    /** 文档路径 */
    private String docPath;

    /** 文档标题 */
    private String docTitle;

    /** 分支名称 */
    private String branchName;

    /** 提交哈希 */
    private String commitHash;

    /** 校验值 */
    private String checksum;

    /** 标签 */
    private String tags;

    /** 摘要 */
    private String summary;

    /** 索引时间 */
    private Date indexedAt;

    public Long getMemoryId()
    {
        return memoryId;
    }

    public void setMemoryId(Long memoryId)
    {
        this.memoryId = memoryId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getRepoId()
    {
        return repoId;
    }

    public void setRepoId(Long repoId)
    {
        this.repoId = repoId;
    }

    public Long getVariantId()
    {
        return variantId;
    }

    public void setVariantId(Long variantId)
    {
        this.variantId = variantId;
    }

    public String getDocType()
    {
        return docType;
    }

    public void setDocType(String docType)
    {
        this.docType = docType;
    }

    public String getDocPath()
    {
        return docPath;
    }

    public void setDocPath(String docPath)
    {
        this.docPath = docPath;
    }

    public String getDocTitle()
    {
        return docTitle;
    }

    public void setDocTitle(String docTitle)
    {
        this.docTitle = docTitle;
    }

    public String getBranchName()
    {
        return branchName;
    }

    public void setBranchName(String branchName)
    {
        this.branchName = branchName;
    }

    public String getCommitHash()
    {
        return commitHash;
    }

    public void setCommitHash(String commitHash)
    {
        this.commitHash = commitHash;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Date getIndexedAt()
    {
        return indexedAt;
    }

    public void setIndexedAt(Date indexedAt)
    {
        this.indexedAt = indexedAt;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("memoryId", getMemoryId())
            .append("projectId", getProjectId())
            .append("repoId", getRepoId())
            .append("variantId", getVariantId())
            .append("docType", getDocType())
            .append("docPath", getDocPath())
            .append("docTitle", getDocTitle())
            .append("branchName", getBranchName())
            .append("commitHash", getCommitHash())
            .append("checksum", getChecksum())
            .append("tags", getTags())
            .append("summary", getSummary())
            .append("indexedAt", getIndexedAt())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
