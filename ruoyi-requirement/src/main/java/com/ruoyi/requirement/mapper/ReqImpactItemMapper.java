package com.ruoyi.requirement.mapper;

import java.util.List;
import com.ruoyi.requirement.domain.ReqImpactItem;
import com.ruoyi.requirement.dto.ReqImpactSuggestQuery;

public interface ReqImpactItemMapper
{
    List<ReqImpactItem> selectReqImpactItemList(ReqImpactItem item);

    List<ReqImpactItem> selectLatestImpactItems(ReqImpactSuggestQuery query);

    int insertReqImpactItem(ReqImpactItem item);
}
