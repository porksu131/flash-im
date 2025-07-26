#!/bin/bash
# 创建所有挂载目录并设置权限
base_dir="/usr/docker/data"
NACOS_IMAGE="nacos/nacos-server:v2.4.3"
mkdir -p $base_dir
chmod -R 777 $base_dir

# 创建子目录结构
dirs=(
  mysql/{conf,data,logs}
  nacos/{conf,data,logs}
  redis/{conf,data,logs}
  rocketmq/{namesrv/{conf,logs,store},broker/{conf,logs,store}}
)

for dir in "${dirs[@]}"; do
  full_path="$base_dir/$dir"
  mkdir -p "$full_path"
  chmod 777 "$full_path"
done

# 配置初始化（仅首次执行）
if [ ! -f "${base_dir}/nacos/conf/application.properties" ]; then
    docker run --rm -d --name nacos_init ${NACOS_IMAGE}
    sleep 10
    docker cp nacos_init:/home/nacos/conf ${base_dir}/nacos
    docker stop nacos_init >/dev/null
fi

# 创建RocketMQ Broker配置文件
BROKER_CONF="$base_dir/rocketmq/broker/conf/broker.conf"
if [ ! -f "$BROKER_CONF" ]; then
    cat > "$BROKER_CONF" <<EOF
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
namesrvAddr = rocketmq-namesrv:9876
EOF
    chmod 666 "$BROKER_CONF"
fi

# 创建RocketMQ Namesrv配置文件
NAMESRV_CONF="$base_dir/rocketmq/namesrv/conf/namesrv.properties"
if [ ! -f "$NAMESRV_CONF" ]; then
    touch "$NAMESRV_CONF"
    chmod 666 "$NAMESRV_CONF"
fi

# 停止并删除旧容器（如果存在）
docker compose down

# 启动所有服务
docker compose up -d

echo "所有服务已启动完成！"