package com.ruoyi.requirement.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReqPlatformRoleSqlTest
{
    private static final String ROLE_SQL = "docs/db/sql/req_platform_req016_role_permissions.sql";

    private static final String MENU_SQL = "docs/db/sql/req_platform_menu.sql";

    @Test
    void rolePermissionSqlDefinesRequirementAndDeveloperRoles() throws IOException
    {
        String sql = readRoleSql();

        assertTrue(sql.contains("'需求人员'"), sql);
        assertTrue(sql.contains("'requirement_user'"), sql);
        assertTrue(sql.contains("'开发人员'"), sql);
        assertTrue(sql.contains("'requirement_developer'"), sql);
        assertTrue(sql.contains("role_key = 'admin'"), sql);
    }

    @Test
    void requirementRoleOnlyGetsDemandAndStatisticsPermissions() throws IOException
    {
        String requirementSection = section(readRoleSql(), "-- 需求人员角色权限", "-- 开发人员角色权限");

        assertTrue(requirementSection.contains("req:demand:list"), requirementSection);
        assertTrue(requirementSection.contains("req:demand:query"), requirementSection);
        assertTrue(requirementSection.contains("req:demand:add"), requirementSection);
        assertTrue(requirementSection.contains("req:demand:edit"), requirementSection);
        assertTrue(requirementSection.contains("req:stats:view"), requirementSection);
        assertFalse(requirementSection.contains("req:demand:remove"), requirementSection);
        assertFalse(requirementSection.contains("req:project:"), requirementSection);
        assertFalse(requirementSection.contains("req:mcp:key"), requirementSection);
        assertFalse(requirementSection.contains("req:package:"), requirementSection);
        assertFalse(requirementSection.contains("req:index:"), requirementSection);
    }

    @Test
    void developerRoleGetsDemandMcpStatisticsAndHiddenPackageSavePermissions() throws IOException
    {
        String developerSection = section(readRoleSql(), "-- 开发人员角色权限", "-- 超级管理员说明");

        assertTrue(developerSection.contains("req:demand:list"), developerSection);
        assertTrue(developerSection.contains("req:demand:query"), developerSection);
        assertTrue(developerSection.contains("req:demand:edit"), developerSection);
        assertTrue(developerSection.contains("req:mcp:key:list"), developerSection);
        assertTrue(developerSection.contains("req:mcp:key:query"), developerSection);
        assertTrue(developerSection.contains("req:mcp:key:add"), developerSection);
        assertTrue(developerSection.contains("req:mcp:key:remove"), developerSection);
        assertTrue(developerSection.contains("req:package:save"), developerSection);
        assertTrue(developerSection.contains("req:stats:view"), developerSection);
        assertFalse(developerSection.contains("req:demand:add"), developerSection);
        assertFalse(developerSection.contains("req:demand:remove"), developerSection);
        assertFalse(developerSection.contains("req:mcp:key:edit"), developerSection);
        assertFalse(developerSection.contains("req:project:"), developerSection);
        assertFalse(developerSection.contains("req:index:"), developerSection);
    }

    @Test
    void menuSqlDefinesDemandRemovePermissionForAdminUse() throws IOException
    {
        String menuSql = readMenuSql();

        assertTrue(menuSql.contains("'需求删除'"), menuSql);
        assertTrue(menuSql.contains("'req:demand:remove'"), menuSql);
    }

    private String readRoleSql() throws IOException
    {
        return readSql(ROLE_SQL);
    }

    private String readMenuSql() throws IOException
    {
        return readSql(MENU_SQL);
    }

    private String readSql(String path) throws IOException
    {
        Path rootPath = Path.of(path);
        if (Files.exists(rootPath))
        {
            return Files.readString(rootPath);
        }
        return Files.readString(Path.of("..", path));
    }

    private String section(String content, String start, String end)
    {
        int startIndex = content.indexOf(start);
        int endIndex = content.indexOf(end);
        assertTrue(startIndex >= 0, content);
        assertTrue(endIndex > startIndex, content);
        return content.substring(startIndex, endIndex);
    }
}
