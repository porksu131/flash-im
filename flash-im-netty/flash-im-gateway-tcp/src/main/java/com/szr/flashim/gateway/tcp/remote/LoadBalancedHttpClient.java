package com.szr.flashim.gateway.tcp.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szr.flashim.gateway.tcp.config.HttpLoadBalanceConfig;
import com.szr.flashim.general.exception.ServiceException;
import com.szr.flashim.general.model.ResponseResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class LoadBalancedHttpClient {

    private final LoadBalancerClient loadBalancerClient;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpLoadBalanceConfig httpLoadBalanceConfig;

    public LoadBalancedHttpClient(LoadBalancerClient loadBalancerClient,
                                  ObjectMapper objectMapper,
                                  HttpLoadBalanceConfig httpLoadBalanceConfig) {
        this.loadBalancerClient = loadBalancerClient;
        this.objectMapper = objectMapper;
        this.httpLoadBalanceConfig = httpLoadBalanceConfig;
        this.httpClient = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() {
        // 连接池配置
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(httpLoadBalanceConfig.getMaxTotalConnections());
        cm.setDefaultMaxPerRoute(httpLoadBalanceConfig.getDefaultMaxPerRoute());

        // 请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(httpLoadBalanceConfig.getDefaultConnectTimeout())
                .setSocketTimeout(httpLoadBalanceConfig.getDefaultSocketTimeout())
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(30, TimeUnit.SECONDS) // 清理空闲连接
                .build();
    }

    // ======================== 支持Class类型的请求 ========================

    public <T> ResponseResult<T> get(String serviceName, String path, Class<T> responseType) {
        return executeRequest(serviceName, path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseResult<T> get(String serviceName, String path, Map<String, Object> queryParams, Class<T> responseType) {
        String fullPath = buildPathWithQuery(path, queryParams);
        return executeRequest(serviceName, fullPath, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseResult<T> post(String serviceName, String path, Object requestBody, Class<T> responseType) {
        return executeRequest(serviceName, path, HttpMethod.POST, requestBody, responseType);
    }

    public <T> ResponseResult<T> put(String serviceName, String path, Object requestBody, Class<T> responseType) {
        return executeRequest(serviceName, path, HttpMethod.PUT, requestBody, responseType);
    }

    public <T> ResponseResult<T> delete(String serviceName, String path, Class<T> responseType) {
        return executeRequest(serviceName, path, HttpMethod.DELETE, null, responseType);
    }

    // ======================== 支持TypeReference的请求（嵌套泛型） ========================

    public <T> ResponseResult<T> get(String serviceName, String path, TypeReference<ResponseResult<T>> typeReference) {
        return executeRequest(serviceName, path, HttpMethod.GET, null, typeReference);
    }

    public <T> ResponseResult<T> get(String serviceName, String path, Map<String, Object> queryParams, TypeReference<ResponseResult<T>>typeReference) {
        String fullPath = buildPathWithQuery(path, queryParams);
        return executeRequest(serviceName, fullPath, HttpMethod.GET, null, typeReference);
    }

    public <T> ResponseResult<T> post(String serviceName, String path, Object requestBody, TypeReference<ResponseResult<T>> typeReference) {
        return executeRequest(serviceName, path, HttpMethod.POST, requestBody, typeReference);
    }

    public <T> ResponseResult<T> put(String serviceName, String path, Object requestBody, TypeReference<ResponseResult<T>> typeReference) {
        return executeRequest(serviceName, path, HttpMethod.PUT, requestBody, typeReference);
    }

    public <T> ResponseResult<T> delete(String serviceName, String path, TypeReference<ResponseResult<T>> typeReference) {
        return executeRequest(serviceName, path, HttpMethod.DELETE, null, typeReference);
    }

    // ======================== 核心请求方法 ========================

    private <T> ResponseResult<T> executeRequest(String serviceName, String path, HttpMethod method,
                                                 Object requestBody, Class<T> responseType) {
        try {
            ServiceInstance instance = getServiceInstance(serviceName);
            String url = buildUrl(instance, path);
            HttpRequestBase request = createRequest(url, method, requestBody);
            HttpResponse response = httpClient.execute(request);
            return handleResponse(response, responseType);
        } catch (Exception e) {
            return ResponseResult.fail(500, "请求异常: " + e.getMessage());
        }
    }

    private <T> ResponseResult<T> executeRequest(String serviceName, String path, HttpMethod method,
                                                 Object requestBody, TypeReference<ResponseResult<T>> typeReference) {
        try {
            ServiceInstance instance = getServiceInstance(serviceName);
            String url = buildUrl(instance, path);
            HttpRequestBase request = createRequest(url, method, requestBody);
            HttpResponse response = httpClient.execute(request);
            return handleResponse(response, typeReference);
        } catch (Exception e) {
            return ResponseResult.fail(500, "请求异常: " + e.getMessage());
        }
    }

    private ServiceInstance getServiceInstance(String serviceName) {
        ServiceInstance instance = loadBalancerClient.choose(serviceName);
        if (instance == null) {
            throw new ServiceException("服务不可用: " + serviceName);
        }
        return instance;
    }

    private String buildUrl(ServiceInstance instance, String path) {
        return "http://" + instance.getHost() + ":" + instance.getPort() +
                (path.startsWith("/") ? path : "/" + path);
    }

    private HttpRequestBase createRequest(String url, HttpMethod method, Object requestBody)
            throws JsonProcessingException, UnsupportedEncodingException {

        switch (method) {
            case GET:
                return new HttpGet(url);
            case POST:
                HttpPost post = new HttpPost(url);
                if (requestBody != null) {
                    post.setEntity(createStringEntity(requestBody));
                }
                return post;
            case PUT:
                HttpPut put = new HttpPut(url);
                if (requestBody != null) {
                    put.setEntity(createStringEntity(requestBody));
                }
                return put;
            case DELETE:
                return new HttpDelete(url);
            default:
                throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }
    }

    private StringEntity createStringEntity(Object obj) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(obj);
        return new StringEntity(json, ContentType.APPLICATION_JSON);
    }

    private String buildPathWithQuery(String path, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }

        StringBuilder sb = new StringBuilder(path);
        if (!path.contains("?")) {
            sb.append("?");
        } else if (!path.endsWith("?")) {
            sb.append("&");
        }

        boolean first = true;
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            first = false;

            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                sb.append(encode(key))
                        .append("=")
                        .append(encode(value.toString()));
            }
        }

        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ======================== 响应处理方法 ========================

    private <T> ResponseResult<T> handleResponse(HttpResponse response, Class<T> responseType)
            throws IOException {

        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());

        // 处理非200响应
        if (statusCode < 200 || statusCode >= 300) {
            return ResponseResult.fail(statusCode, "HTTP错误: " + responseBody);
        }

        // 处理空响应
        if (responseBody == null || responseBody.isEmpty()) {
            return ResponseResult.ok();
        }

        // 反序列化响应体
        try {
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class, responseType);
            return objectMapper.readValue(responseBody, type);
        } catch (Exception e) {
            return ResponseResult.fail(500, "响应解析失败: " + e.getMessage());
        }
    }

    private <T> ResponseResult<T> handleResponse(HttpResponse response, TypeReference<ResponseResult<T>> typeReference)
            throws IOException {

        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());

        // 处理非200响应
        if (statusCode < 200 || statusCode >= 300) {
            return ResponseResult.fail(statusCode, "HTTP错误: " + responseBody);
        }

        // 处理空响应
        if (responseBody == null || responseBody.isEmpty()) {
            return ResponseResult.ok();
        }

        // 反序列化响应体
        try {
            return objectMapper.readValue(responseBody, typeReference);
        } catch (Exception e) {
            return ResponseResult.fail(500, "响应解析失败: " + e.getMessage());
        }
    }


    private enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}
