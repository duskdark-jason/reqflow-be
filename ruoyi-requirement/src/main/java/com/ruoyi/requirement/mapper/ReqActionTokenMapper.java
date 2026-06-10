package com.ruoyi.requirement.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.requirement.domain.ReqActionToken;

public interface ReqActionTokenMapper
{
    public ReqActionToken selectReqActionTokenByTokenId(Long tokenId);

    public ReqActionToken selectReqActionTokenByTokenHash(String tokenHash);

    public int insertReqActionToken(ReqActionToken reqActionToken);

    public int updateReqActionToken(ReqActionToken reqActionToken);

    public int updateLastUsed(@Param("tokenId") Long tokenId);
}
