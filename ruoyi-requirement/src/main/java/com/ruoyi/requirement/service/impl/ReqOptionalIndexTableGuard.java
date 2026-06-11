package com.ruoyi.requirement.service.impl;

import java.util.Locale;
import org.springframework.dao.DataAccessException;
import com.ruoyi.common.exception.ServiceException;

final class ReqOptionalIndexTableGuard
{
    static final String INDEX_TABLE_MIGRATION = "sql/req_platform_req007_index_tables.sql";

    private ReqOptionalIndexTableGuard()
    {
    }

    static boolean isMissingTable(DataAccessException e, String tableName)
    {
        // 旧环境可能尚未执行索引表迁移；只识别“目标表缺失”，避免把普通 SQL 错误误判成可降级。
        String message = collectExceptionMessage(e).toLowerCase(Locale.ROOT);
        String normalizedTable = tableName.toLowerCase(Locale.ROOT);
        return message.contains(normalizedTable)
                && (message.contains("doesn't exist") || message.contains("does not exist")
                || message.contains("not exist") || message.contains("not found") || message.contains("不存在"));
    }

    static ServiceException missingIndexTable(String tableName)
    {
        // 写入索引时必须显式失败并给出迁移路径，不能静默丢弃初始化结果。
        return new ServiceException("平台索引表未初始化：" + tableName
                + "。请先执行 " + INDEX_TABLE_MIGRATION
                + "，或执行 sql/req_platform_schema.sql 中 req_repository_index_batch、req_index_module、req_impact_item 的建表段。");
    }

    private static String collectExceptionMessage(Throwable throwable)
    {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null)
        {
            if (current.getMessage() != null)
            {
                builder.append(current.getMessage()).append('\n');
            }
            current = current.getCause();
        }
        return builder.toString();
    }
}
