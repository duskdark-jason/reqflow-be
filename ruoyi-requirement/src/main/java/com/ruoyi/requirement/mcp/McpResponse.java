package com.ruoyi.requirement.mcp;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse
{
    private static final int INTERNAL_ERROR = -32603;

    private String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private Object error;

    public static McpResponse success(Object id, Object result)
    {
        McpResponse response = new McpResponse();
        response.setId(id);
        response.setResult(result);
        return response;
    }

    public static McpResponse error(Object id, String message)
    {
        return error(id, INTERNAL_ERROR, message);
    }

    public static McpResponse methodNotFound(Object id, String message)
    {
        return error(id, -32601, message);
    }

    private static McpResponse error(Object id, int code, String message)
    {
        McpResponse response = new McpResponse();
        response.setId(id);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message == null || message.isEmpty() ? "MCP调用失败" : message);
        response.setError(error);
        return response;
    }

    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }
}
