package com.ruoyi.requirement.service;

import java.util.List;
import com.ruoyi.requirement.domain.ReqVariant;

/**
 * 需求平台项目分支Service接口
 */
public interface IReqVariantService
{
    ReqVariant selectReqVariantByVariantId(Long variantId);
    List<ReqVariant> selectReqVariantList(ReqVariant reqVariant);
    int insertReqVariant(ReqVariant reqVariant);
    int updateReqVariant(ReqVariant reqVariant);
    int deleteReqVariantByVariantIds(Long[] variantIds);
    int deleteReqVariantByVariantId(Long variantId);
}
