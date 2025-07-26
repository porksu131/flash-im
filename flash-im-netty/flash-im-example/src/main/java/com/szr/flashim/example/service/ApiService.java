package com.szr.flashim.example.service;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szr.flashim.example.model.Friend;
import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.general.model.ResponseResult;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ApiService {
    // 本地环境
//    public static String GATEWAY_HTTP_BASE_URL = "http://127.0.0.1:10921";
//    public static String GATEWAY_TCP_SERVER = "127.0.0.1:30420";

    // k8s环境
    public static String GATEWAY_HTTP_BASE_URL = "http://flash-im-gateway-http.com:32395";
    public static String GATEWAY_TCP_SERVER = "flash-im-gateway-tcp.com:31030";

    public static String LOGIN_URL = "/api/auth/login";
    public static String LOGOUT_URL = "/api/auth/logout";
    public static String REGISTER_URL = "/api/auth/register";
    public static String REFRESH_TOKEN_URL = "/api/auth/refresh";
    public static String ALL_USER_INFO_URL = "/api/user/queryAllUser";
    public static String QUERY_USER_BY_NAME_URL = "/api/user/queryByUserName";
    public static String QUERY_USER_BY_UID_URL = "/api/user/queryByUserId";
    public static String ALL_FRIEND_URL = "/api/friend/list";
    public static String ADD_FRIEND_URL = "/api/friend/add";

    // 登录
    public static ResponseResult<UserInfo> login(String userName, String password) throws Exception {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("userName", userName);
        reqMap.put("password", password);

        return sendPostRequest(LOGIN_URL, reqMap, UserInfo.class);
    }

    // 注册
    public static ResponseResult<UserInfo> register(String userName, String password) throws Exception {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("userName", userName);
        reqMap.put("password", password);

        return sendPostRequest(REGISTER_URL, reqMap, UserInfo.class);
    }

    // 登出
    public static ResponseResult<Void> logout(String accessToken) {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("accessToken", accessToken);
        try {
            return sendPostRequest(LOGOUT_URL, reqMap, Void.class);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 获取所有用户
    public static ResponseResult<List<UserInfo>> queryAllUser() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String result = sendPostRequest(ALL_USER_INFO_URL, null, "");
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class,
                    objectMapper.getTypeFactory().constructParametricType(List.class, UserInfo.class));
            return new ObjectMapper().readValue(result, type);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 根据用户名模糊查询用户
    public static ResponseResult<List<UserInfo>> queryByUserName(String input, String accessToken) {
        try {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("userName", input);
            ObjectMapper objectMapper = new ObjectMapper();
            String result = sendPostRequest(QUERY_USER_BY_NAME_URL, reqMap, accessToken);
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class,
                    objectMapper.getTypeFactory().constructParametricType(List.class, UserInfo.class));
            return new ObjectMapper().readValue(result, type);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 根据ID查询用户
    public static ResponseResult<List<UserInfo>> queryByUserId(Long input, String accessToken) {
        try {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("uid", input);
            ObjectMapper objectMapper = new ObjectMapper();
            String result = sendPostRequest(QUERY_USER_BY_UID_URL, reqMap, accessToken);
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class,
                    objectMapper.getTypeFactory().constructParametricType(List.class, UserInfo.class));
            return new ObjectMapper().readValue(result, type);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 获取好友列表
    public static ResponseResult<List<Friend>> getFriendList(long uid, String accessToken) {
        try {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("uid", uid);

            ObjectMapper objectMapper = new ObjectMapper();
            String result = sendPostRequest(ALL_FRIEND_URL, reqMap, accessToken);
            JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class,
                    objectMapper.getTypeFactory().constructParametricType(List.class, Friend.class));
            return new ObjectMapper().readValue(result, type);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 添加好友关系
    public static ResponseResult<Void> addFriendRelation(long uid, long friendId, String accessToken) {
        try {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("uid", uid);
            reqMap.put("friendId", friendId);
            return sendPostRequest(ADD_FRIEND_URL, reqMap, accessToken, Void.class);
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
    }


    private static <T> ResponseResult<T> sendPostRequest(String endpoint, Class<T> clazz) throws Exception {
        return sendPostRequest(endpoint, null, null, clazz);
    }

    private static <T> ResponseResult<T> sendPostRequest(String endpoint, Object reqBody, Class<T> clazz) throws Exception {
        return sendPostRequest(endpoint, reqBody, null, clazz);
    }

    private static <T> ResponseResult<T> sendPostRequest(String endpoint, Object reqBody, String accessToken, Class<T> clazz) throws Exception {
        String result = sendPostRequest(endpoint, reqBody, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ResponseResult.class, clazz);
        return objectMapper.readValue(result, type);
    }

    private static String sendPostRequest(String endpoint, Object reqBody, String accessToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(GATEWAY_HTTP_BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            if (StringUtils.isNotBlank(accessToken)) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (reqBody != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonData = objectMapper.writeValueAsString(reqBody);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    throw new RuntimeException("HTTP错误: " + responseCode + "\n" + errorResponse);
                }
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
