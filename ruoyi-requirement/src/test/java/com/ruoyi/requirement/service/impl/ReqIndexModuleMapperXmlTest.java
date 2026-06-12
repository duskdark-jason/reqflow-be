package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReqIndexModuleMapperXmlTest
{
    @Test
    void moduleTreeOnlyReadsLatestImportedBatchesAndSupportsFrontendScopeFilter() throws IOException
    {
        String xml = read("src/main/resources/mapper/requirement/ReqIndexModuleMapper.xml");

        assertTrue(xml.contains("select max(batch_id) as latest_batch_id"));
        assertTrue(xml.contains("group by repo_id, branch_name"));
        assertTrue(xml.contains("repo_scope = #{repoScope}"));
        assertTrue(xml.contains("deactivateReqIndexModulesByRepositoryBranch"));
    }

    private String read(String relativePath) throws IOException
    {
        Path modulePath = Path.of(relativePath);
        if (Files.exists(modulePath))
        {
            return Files.readString(modulePath);
        }
        return Files.readString(Path.of("ruoyi-requirement", relativePath));
    }
}
