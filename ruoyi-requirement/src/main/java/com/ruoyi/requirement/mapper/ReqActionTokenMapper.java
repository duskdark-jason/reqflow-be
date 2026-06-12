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

    public int touchLastUsed(@Param("tokenId") Long tokenId);

    public int countUsedActionToken(@Param("actionType") String actionType,
                                    @Param("targetMethod") String targetMethod,
                                    @Param("projectId") Long projectId,
                                    @Param("variantId") Long variantId,
                                    @Param("demandId") Long demandId);

    public int deleteReqActionTokenByDemandIds(Long[] demandIds);
}
