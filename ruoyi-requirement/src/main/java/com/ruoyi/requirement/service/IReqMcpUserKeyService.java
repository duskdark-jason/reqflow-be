package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.requirement.domain.ReqMcpUserKey;
import com.ruoyi.requirement.dto.ReqMcpUserKeyCreateResult;
import com.ruoyi.requirement.dto.ReqMcpUserOption;

/**
 * 人员MCP访问KeyService接口
 */
public interface IReqMcpUserKeyService
{
    public List<ReqMcpUserKey> selectReqMcpUserKeyList(ReqMcpUserKey reqMcpUserKey);

    public ReqMcpUserKey selectReqMcpUserKeyByKeyId(Long keyId);

    public List<ReqMcpUserOption> selectUserOptions(String userName);

    public ReqMcpUserKeyCreateResult createKey(ReqMcpUserKey reqMcpUserKey, String operator, String mcpAddress);

    public int updateReqMcpUserKey(ReqMcpUserKey reqMcpUserKey);

    public int deleteReqMcpUserKeyByKeyIds(Long[] keyIds);

    public ReqMcpUserKeyCreateResult regenerateKey(Long keyId, String operator, String mcpAddress);

    public LoginUser authenticate(String plainKey, String ip);
}
