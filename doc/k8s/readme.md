### 环境准备
需自行准备k8s集群k8s-master,k8s-worker1,k8s-worker2
用到域名服务，需要安装ingress-nginx、
用到存储卷，需要安装和配置nfs

### 执行步骤
各个中间的部署大体都是用有状态副本集来安装
1. 部署ConfigMap，Secret
2. 执行存储卷PersistentVolume和存储卷声明PersistentVolumeClaim
3. 部署服务，无头服务（None），集群服务(ClusterIp)，对外暴露服务(NodePort)
4. 部署容器组，无状态部署Deployment、有状态部署StatefulSet

### web微服务或者netty服务
1. 部署应用路由规则：Ingress（flash-im-gateway-tcp/flash-im-gateway-http）
2. 部署服务Service 
3. 部署无状态副本集Deployment

### 集群内的访问
1. 容器访问容器，访问地址规则：容器名-序号.服务名.命名空间.svc.cluster.local:容器端口
序号默认从0开始递增，看你部署时指定的副本数量，例如：  
nacos-0.nacos-headless.nacos.svc.cluster.local:28848  
nacos-1.nacos-headless.nacos.svc.cluster.local:28848

2. 容器访问服务：服务名:服务端口  
例如:http网关要访问认证中心：http://flash-im-auth:8080
通过服务访问，k8s会自动负载均衡路由到容器
