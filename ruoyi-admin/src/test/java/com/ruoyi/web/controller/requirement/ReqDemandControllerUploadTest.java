package com.ruoyi.web.controller.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;

class ReqDemandControllerUploadTest
{
    @Test
    void uploadRejectsFilesOverTwoMb() throws Exception
    {
        ReqDemandController controller = new ReqDemandController();
        byte[] content = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", content);

        AjaxResult result = controller.uploadFile(file);

        assertEquals(500, result.get("code"));
        assertTrue(String.valueOf(result.get("msg")).contains("2MB"));
    }
}
