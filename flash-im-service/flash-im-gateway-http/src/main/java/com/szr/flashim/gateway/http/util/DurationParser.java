package com.szr.flashim.gateway.http.util;

import java.time.Duration;

public class DurationParser {
    public static Duration parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        input = input.trim();
        int lastDigitIndex = -1;

        // 查找最后一个数字字符的位置
        for (int i = 0; i < input.length(); i++) {
            if (Character.isDigit(input.charAt(i))) {
                lastDigitIndex = i;
            } else {
                break;
            }
        }

        if (lastDigitIndex == -1) {
            throw new IllegalArgumentException("No numeric value found: " + input);
        }

        try {
            // 分割数字部分和单位部分
            long value = Long.parseLong(input.substring(0, lastDigitIndex + 1));
            String unit = input.substring(lastDigitIndex + 1).trim().toLowerCase();

            // 根据单位创建Duration
            return switch (unit) {
                case "ns" -> Duration.ofNanos(value);
                case "us" -> Duration.ofNanos(value * 1000); // 微秒转纳秒
                case "ms" -> Duration.ofMillis(value);
                case "s"  -> Duration.ofSeconds(value);
                case "m"  -> Duration.ofMinutes(value);
                case "h"  -> Duration.ofHours(value);
                case "d"  -> Duration.ofDays(value);
                default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric format: " + input, e);
        }
    }

    public static void main(String[] args) {
        // 示例用法
        String input = "10s";
        Duration duration = parse(input);
        System.out.println(duration);  // 输出: PT10S
    }
}