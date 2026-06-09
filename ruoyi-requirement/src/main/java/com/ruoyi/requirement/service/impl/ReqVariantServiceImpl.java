package com.ruoyi.requirement.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.requirement.domain.ReqVariant;
import com.ruoyi.requirement.mapper.ReqVariantMapper;
import com.ruoyi.requirement.service.IReqVariantService;

/**
 * 需求平台项目分支Service业务层处理
 */
@Service
public class ReqVariantServiceImpl implements IReqVariantService
{
    @Autowired
    private ReqVariantMapper reqVariantMapper;

    @Override
    public ReqVariant selectReqVariantByVariantId(Long variantId)
    {
        return reqVariantMapper.selectReqVariantByVariantId(variantId);
    }

    @Override
    public List<ReqVariant> selectReqVariantList(ReqVariant reqVariant)
    {
        return reqVariantMapper.selectReqVariantList(reqVariant);
    }

    @Override
    public int insertReqVariant(ReqVariant reqVariant)
    {
        return reqVariantMapper.insertReqVariant(reqVariant);
    }

    @Override
    public int updateReqVariant(ReqVariant reqVariant)
    {
        return reqVariantMapper.updateReqVariant(reqVariant);
    }

    @Override
    public int deleteReqVariantByVariantIds(Long[] variantIds)
    {
        return reqVariantMapper.deleteReqVariantByVariantIds(variantIds);
    }

    @Override
    public int deleteReqVariantByVariantId(Long variantId)
    {
        return reqVariantMapper.deleteReqVariantByVariantId(variantId);
    }
}
