package com.ruoyi.requirement.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
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

    int deleteReqVariantByProjectId(Long projectId);

    int deleteReqVariantByProjectIdAndVariantIdsNotIn(@Param("projectId") Long projectId, @Param("variantIds") Long[] variantIds);

}
