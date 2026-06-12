package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReqDemandSchemaSqlTest
{
    private static final String SCHEMA_SQL = "docs/db/sql/req_platform_schema.sql";
    private static final String MIGRATION_SQL = "docs/db/sql/req_platform_req016_demand_form_fields.sql";

    @Test
    void demandSchemaContainsSourceAndAttachmentColumns() throws IOException
    {
        String sql = readSql(SCHEMA_SQL);

        assertTrue(sql.contains("demand_source"), sql);
        assertTrue(sql.contains("attachments"), sql);
    }

    @Test
    void migrationSqlAddsSourceAndAttachmentColumns() throws IOException
    {
        String sql = readSql(MIGRATION_SQL);

        assertTrue(sql.contains("demand_source"), sql);
        assertTrue(sql.contains("attachments"), sql);
        assertTrue(sql.contains("REQ-016"), sql);
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
