package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqVariant;

/**
 * 需求平台客户定制线Mapper接口
 */
public interface ReqVariantMapper
{
    ReqVariant selectReqVariantByVariantId(Long variantId);

    List<ReqVariant> selectReqVariantList(ReqVariant reqVariant);

    int insertReqVariant(ReqVariant reqVariant);

    int updateReqVariant(ReqVariant reqVariant);

    int deleteReqVariantByVariantId(Long variantId);

    int deleteReqVariantByVariantIds(Long[] variantIds);

}
