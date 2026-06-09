package com.ruoyi.requirement.service.impl;

import java.util.Locale;
import org.springframework.dao.DataAccessException;

final class ReqOptionalIndexTableGuard
{
    private ReqOptionalIndexTableGuard()
    {
    }

    static boolean isMissingTable(DataAccessException e, String tableName)
    {
        String message = collectExceptionMessage(e).toLowerCase(Locale.ROOT);
        String normalizedTable = tableName.toLowerCase(Locale.ROOT);
        return message.contains(normalizedTable)
                && (message.contains("doesn't exist") || message.contains("does not exist")
                || message.contains("not exist") || message.contains("not found") || message.contains("不存在"));
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
