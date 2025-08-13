1. 脚本上传
```text
  将docker-compose.yaml和startup.sh上传到同一目录，startup.sh是支持重复运行的
```

2. 赋予执行权限
```bash
  sudo chmod +x startup.sh
```

3. 执行部署
```bash
  sudo ./startup.sh
```

4. 检查所有容器状态
```bash
  docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

5. 错误排查：一般的nacos会失败，no datasource set
```shell
  docker logs -f nacos # 查看nacos容器启动日志，初始化nacos数据库表mysql-schema.sql后，等他自动重启，或手动重启
  docker restart nacos # 手动重启
```

6. 错误排查：rocketmq-broker启动成功，但是实际应用过程无法连接，启动rocketmq-broker后默认的配置文件已挂载出来，修改ip,namesrvAddr等
```shell
# broker.conf文件内容
namesrvAddr = rocketmq-namesrv:9876
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
brokerIP1 = 192.168.100.130
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
deleteWhen = 04
fileReservedTime = 72
autoCreateTopicEnable=true
autoCreateSubscriptionGroup=true
tlsTestModeEnable = false


## 重新启动broker
docker compose restart rocketmq-broker
```
