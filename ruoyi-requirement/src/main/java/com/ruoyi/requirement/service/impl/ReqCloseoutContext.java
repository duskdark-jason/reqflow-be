package com.ruoyi.requirement.service.impl;

import com.ruoyi.common.utils.StringUtils;

final class ReqCloseoutContext
{
    private static final String TOKEN_REPO_PREFIX = "closeoutRepoId=";

    private static final String BATCH_PREFIX = "closeoutDemandId=";

    private static final String REPO_PART = ";repoId=";

    private ReqCloseoutContext()
    {
    }

    static String tokenRemark(Long repoId)
    {
        return TOKEN_REPO_PREFIX + repoId;
    }

    static Long repoIdFromTokenRemark(String remark)
    {
        if (StringUtils.isEmpty(remark) || !remark.startsWith(TOKEN_REPO_PREFIX))
        {
            return null;
        }
        try
        {
            return Long.valueOf(remark.substring(TOKEN_REPO_PREFIX.length()));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    static String batchRemark(Long demandId, Long repoId)
    {
        return BATCH_PREFIX + demandId + REPO_PART + repoId;
    }
}
