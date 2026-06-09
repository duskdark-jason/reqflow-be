package com.ruoyi.requirement.mcp;

import java.util.Collections;

public class McpResponse
{
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
        McpResponse response = new McpResponse();
        response.setId(id);
        response.setError(Collections.singletonMap("message", message));
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
