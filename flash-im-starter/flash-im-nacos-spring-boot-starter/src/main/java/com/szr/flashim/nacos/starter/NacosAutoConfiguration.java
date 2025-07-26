package com.szr.flashim.nacos.starter;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@AutoConfiguration
@EnableConfigurationProperties(NacosProperties.class)
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.server-addr")
public class NacosAutoConfiguration {
    @Bean
    public NamingService namingService(NacosProperties nacosProperties) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosProperties.getServerAddr());

        if (nacosProperties.getUsername() != null) {
            properties.put(PropertyKeyConst.USERNAME, nacosProperties.getUsername());
        }

        if (nacosProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.PASSWORD, nacosProperties.getPassword());
        }

        if (nacosProperties.getNamespace() != null) {
            properties.put(PropertyKeyConst.NAMESPACE, nacosProperties.getNamespace());
        }

        return NacosFactory.createNamingService(properties);
    }

    @Bean
    public NacosDiscovery nacosDiscovery(NamingService namingService) {
        return new NacosDiscovery(namingService);
    }
}
