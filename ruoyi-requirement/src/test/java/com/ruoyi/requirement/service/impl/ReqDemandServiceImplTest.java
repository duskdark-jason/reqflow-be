package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.requirement.domain.ReqDemand;
import com.ruoyi.requirement.mapper.ReqDemandMapper;

class ReqDemandServiceImplTest
{
    @Test
    void rejectsInvalidStatusChangeFromGenericUpdate()
    {
        ReqDemandMapper reqDemandMapper = mock(ReqDemandMapper.class);
        ReqDemand current = new ReqDemand();
        current.setDemandId(1L);
        current.setStatus("submitted");
        when(reqDemandMapper.selectReqDemandByDemandId(1L)).thenReturn(current);

        ReqDemand update = new ReqDemand();
        update.setDemandId(1L);
        update.setStatus("completed");

        ReqDemandServiceImpl service = new ReqDemandServiceImpl();
        ReflectionTestUtils.setField(service, "reqDemandMapper", reqDemandMapper);

        assertThrows(ServiceException.class, () -> service.updateReqDemand(update));
        verify(reqDemandMapper, never()).updateReqDemand(any());
    }
}
