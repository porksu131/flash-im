package com.szr.flashim.remote.starter;

import com.szr.flashim.general.model.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
public class RestClient {
    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 通用请求方法
     */
    public <T> ResponseResult<T> exchange(String url, HttpMethod method, Object requestBody, Object... uriVariables) {
        try {
            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            // 2. 构造泛型类型引用
            ParameterizedTypeReference<ResponseResult<T>> typeRef = new ParameterizedTypeReference<>() {
            };

            // 3. 执行请求并处理响应
            log.debug("请求: {}, 参数: {}", url, requestBody);
            ResponseEntity<ResponseResult<T>> response = restTemplate.exchange(url, method, entity, typeRef, uriVariables);
            log.debug("响应: {}", response.getBody());

            // 4. 返回响应体（直接是ResponseResult<T>）
            return response.getBody();
        } catch (Exception e) {
            // 处理解析失败或网络错误
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request or parsing failed: " + e.getMessage());
        }
    }

    // GET请求
    public <T> ResponseResult<T> get(String url, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    // POST请求
    public <T> ResponseResult<T> post(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.POST, requestBody, responseType, uriVariables);
    }

    public <T> ResponseResult<T> post(String url, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.POST, null, responseType, uriVariables);
    }
}
