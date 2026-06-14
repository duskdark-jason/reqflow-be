package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReqDemandSchemaSqlTest
{
    private static final String INIT_SQL = "docs/db/sql/req_platform_init.sql";

    @Test
    void demandSchemaContainsSourceAndAttachmentColumns() throws IOException
    {
        String sql = readSql(INIT_SQL);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS req_demand"), sql);
        assertTrue(sql.contains("demand_source"), sql);
        assertTrue(sql.contains("attachments"), sql);
        assertTrue(sql.contains("developer_user_id"), sql);
        assertTrue(sql.contains("idx_req_demand_developer"), sql);
    }

    @Test
    void cleanInitSqlContainsSourceAndAttachmentColumns() throws IOException
    {
        String sql = readSql(INIT_SQL);

        assertTrue(sql.contains("demand_source VARCHAR(64) NOT NULL DEFAULT 'BUSINESS'"), sql);
        assertTrue(sql.contains("attachments TEXT"), sql);
    }

    @Test
    void cleanInitSqlContainsDeveloperLockColumnAndIndex() throws IOException
    {
        String sql = readSql(INIT_SQL);

        assertTrue(sql.contains("developer_user_id BIGINT DEFAULT NULL"), sql);
        assertTrue(sql.contains("KEY idx_req_demand_developer (developer_user_id)"), sql);
    }

    private String readSql(String relativePath) throws IOException
    {
        Path rootPath = Path.of(relativePath);
        if (Files.exists(rootPath))
        {
            return Files.readString(rootPath);
        }
        return Files.readString(Path.of("..", relativePath));
    }
}
