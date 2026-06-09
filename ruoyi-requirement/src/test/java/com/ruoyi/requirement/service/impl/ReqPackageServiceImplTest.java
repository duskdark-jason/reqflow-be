package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.mapper.ReqPackageVersionMapper;

class ReqPackageServiceImplTest
{
    @Test
    void rejectsUnsupportedArtifactTypeBeforeSavingVersion()
    {
        ReqPackageVersionMapper reqPackageVersionMapper = mock(ReqPackageVersionMapper.class);
        ReqPackageServiceImpl service = new ReqPackageServiceImpl();
        ReflectionTestUtils.setField(service, "reqPackageVersionMapper", reqPackageVersionMapper);

        assertThrows(ServiceException.class, () -> service.saveVersion(1L, "unexpected_type", "content", null));
        verifyNoInteractions(reqPackageVersionMapper);
    }
}
