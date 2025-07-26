package com.szr.flashim.nacos.starter;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.function.Consumer;

public class NacosDiscovery {
    private final NamingService namingService;

    public NacosDiscovery(NamingService namingService) {
        this.namingService = namingService;
    }

    public void subscribe(String serviceName, Consumer<List<Instance>> listener) {
        try {
            namingService.subscribe(serviceName, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent) {
                        listener.accept(((NamingEvent) event).getInstances());
                    }
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException("Nacos订阅失败", e);
        }
    }

    public void registerInstance(String serviceName, String host, int port) throws NacosException {
        // 注册服务实例
        Instance instance = new Instance();
        instance.setIp(host);
        instance.setPort(port);
        instance.setServiceName(serviceName);
        instance.setEphemeral(true);
        namingService.registerInstance(serviceName, instance);//会自动开启心跳检测线程

    }

    public List<Instance> getInstances(String serviceName) {

        try {
            return namingService.getAllInstances(serviceName);
        } catch (NacosException e) {
            throw new RuntimeException("获取服务实例失败: " + serviceName, e);
        }
    }
}
