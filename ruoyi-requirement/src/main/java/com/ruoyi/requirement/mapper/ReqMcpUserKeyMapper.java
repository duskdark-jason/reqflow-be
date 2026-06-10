package com.ruoyi.requirement.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.requirement.domain.ReqMcpUserKey;

/**
 * 人员MCP访问KeyMapper接口
 */
public interface ReqMcpUserKeyMapper
{
    public List<ReqMcpUserKey> selectReqMcpUserKeyList(ReqMcpUserKey reqMcpUserKey);

    public ReqMcpUserKey selectReqMcpUserKeyByKeyId(Long keyId);

    public ReqMcpUserKey selectReqMcpUserKeyByKeyHash(String keyHash);

    public int insertReqMcpUserKey(ReqMcpUserKey reqMcpUserKey);

    public int updateReqMcpUserKey(ReqMcpUserKey reqMcpUserKey);

    public int deleteReqMcpUserKeyByKeyId(Long keyId);

    public int deleteReqMcpUserKeyByKeyIds(Long[] keyIds);

    public int updateLastUsed(@Param("keyId") Long keyId, @Param("lastUsedIp") String lastUsedIp);
}
