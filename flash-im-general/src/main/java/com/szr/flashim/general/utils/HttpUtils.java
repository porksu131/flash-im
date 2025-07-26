package com.szr.flashim.general.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public class HttpUtils {

    // 默认连接超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    // 默认读取超时时间（毫秒）
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    /**
     * 发送GET请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return request("GET", buildUrlWithParams(url, params), "", headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送POST请求
     * @param url 请求地址
     * @param body 请求体内容（JSON/Form）
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, String body, Map<String, String> headers) throws IOException {
        return request("POST", url, body, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 发送带文件的POST请求（multipart/form-data）
     * @param url 请求地址
     * @param formData 表单数据
     * @param fileFieldName 文件字段名
     * @param file 要上传的文件
     * @param headers 请求头
     * @return 响应内容
     */
    public static String postWithFile(String url, Map<String, String> formData, String fileFieldName, File file, Map<String, String> headers) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        if (headers != null) {
            headers.put("Content-Type", "multipart/form-data; boundary=" + boundary);
        } else {
            headers = Map.of("Content-Type", "multipart/form-data; boundary=" + boundary);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        // 添加表单字段
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            writer.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"")
                    .append(entry.getKey()).append("\"\r\n\r\n")
                    .append(entry.getValue()).append("\r\n");
        }

        // 添加文件
        writer.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(fileFieldName).append("\"; filename=\"")
                .append(file.getName()).append("\"\r\n")
                .append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(file.getName())).append("\r\n\r\n");
        writer.flush();

        // 写入文件内容
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        writer.append("\r\n").append("--").append(boundary).append("--\r\n");
        writer.close();

        return request("POST", url, outputStream.toByteArray(), headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 通用HTTP请求方法
     * @param method HTTP方法 (GET/POST/PUT/DELETE)
     * @param url 请求URL
     * @param content 请求内容（字节数组）
     * @param headers 请求头
     * @param connectTimeout 连接超时
     * @param readTimeout 读取超时
     * @return 响应内容
     */
    private static String request(String method, String url, byte[] content, Map<String, String> headers,
                                  int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 需要写入请求体
            if (content != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(content);
                }
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(connection.getInputStream());
            } else {
                throw new IOException("HTTP Error: " + responseCode + " - " + readResponse(connection.getErrorStream()));
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 重载方法：支持字符串内容
    private static String request(String method, String url, String content, Map<String, String> headers,
                                  int connectTimeout, int readTimeout) throws IOException {
        byte[] bytes = content != null && !content.isEmpty() ? content.getBytes(StandardCharsets.UTF_8) : null;
        return request(method, url, bytes, headers, connectTimeout, readTimeout);
    }

    // 读取响应内容
    private static String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    // 构建带查询参数的URL
    private static String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) return baseUrl;

        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            joiner.add(encodedKey + "=" + encodedValue);
        }

        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + joiner.toString();
    }
}