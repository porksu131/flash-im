package com.szr.flashim.core.starter;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoConfiguration
public class SpringBeanUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.applicationContext = applicationContext;
    }

    public static <T> Map<String, T> getMapBeansOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    public static <T> List<T> getBeansOfType(Class<T> type) {
        List<T> list = new ArrayList<>();
        Map<String, T> beansOfType = applicationContext.getBeansOfType(type);
        beansOfType.forEach((k, v) -> {
            list.add(v);
        });
        return list;
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> type) {
        return applicationContext.getBean(type);
    }
}
