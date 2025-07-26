### 通过helm部署
helm 是 Kubernetes 的包管理器。类似于yum安装指令，统一对安装服务进行管理，使得用户不需要关心服务之间的依赖关系。Helm 由客户端组件 helm 和服务端组件 Tiller 组成，能够将一组K8S资源打包统一管理，是查找、共享和使用为Kubernetes构建的软件的最佳方式。

- 简化部署
- 高度可配置：Helm Charts提供了高度可配置的选项，可以轻松自定义和修改应用程序的部署配置；
- 版本控制
- 模板化：Helm Charts使用YAML模板来定义Kubernetes对象的配置，从而简化了配置过程，并提高了可重复性和可扩展性；
- 应用程序库：Helm具有应用程序库的概念，可以轻松地共享和重用Helm Charts，从而简化了多个应用程序的部署和管理；
- 插件系统：Helm拥有一个强大的插件系统，允许您扩展和定制Helm的功能，以满足特定的需求和要求。


建议：其他组件也用helm部署，相对简单

### 离线部署
官网：https://github.com/itboon/rocketmq-helm  
本示例是离线部署，先下载chart到本地，再根据需要修改内部的template中的yaml文件资源清单
```shell
## 添加 helm 仓库
helm repo add rocketmq-repo https://helm-charts.itboon.top/rocketmq
helm repo update rocketmq-repo

## 拉取远程 chart 到本地
helm search repo rocketmq
helm pull rocketmq-repo/rocketmq-cluster --version=12.4.0

## 解压
tar -xzvf rocketmq-cluster-12.4.0.tgz

## 进入解压后文件目录
cd /root/rocketmq-cluster/ 
```

```shell
## 部署高可用集群模式, 多 Master 多 Slave
## 本示例，开启持久化存储，存储卷类，需提前创建
# 1个 master 1个副节点，镜像：apache/rocketmq:5.2.0
## JVM堆初始内存-Xms512，JVM堆最大内存-Xmx512，JVM堆新生代内存-Xmn256
## 禁用proxy
## dashboard组件启用ingress外部域名访问，本示例ingressClassName是提前准备
helm install rocketmq . \
  --namespace rocketmq \
  --create-namespace \
  --set broker.persistence.enabled="true" \
  --set broker.persistence.size="10Gi" \
  --set broker.persistence.storageClass="nfs-storage" \
  --set broker.size.master="1" \
  --set broker.size.replica="1" \
  --set broker.master.jvm.maxHeapSize="2048M" \
  --set broker.master.resources.requests.cpu="100m" \
  --set broker.master.resources.requests.memory="1Gi" \
  --set broker.replica.jvm.maxHeapSize="1300M" \
  --set broker.replica.resources.requests.cpu="50m" \
  --set broker.replica.resources.requests.memory="1Gi" \
  --set proxy.enabled="false" \
  --set nameserver.replicaCount="1" \
  --set nameserver.jvm.maxHeapSize="512M" \
  --set nameserver.resources.requests.cpu="100m" \
  --set nameserver.resources.requests.memory="1Gi" \
  --set dashboard.enabled="true" \
  --set dashboard.image.repository="apacherocketmq/rocketmq-dashboard" \
  --set dashboard.image.tag="2.0.1" \
  --set dashboard.auth.enabled="true" \
  --set dashboard.auth.users[0].name="admin" \
  --set dashboard.auth.users[0].password="admin" \
  --set dashboard.auth.users[0].isAdmin="true" \
  --set dashboard.auth.users[1].name="user01" \
  --set dashboard.auth.users[1].password="user01" \
  --set dashboard.ingress.enabled="true" \
  --set dashboard.ingress.className="ingress" \
  --set dashboard.ingress.hosts[0].host="rocketmq-dashboard.flash-im.com" \
  --set image.repository="apache/rocketmq" \
  --set image.tag="5.2.0"
```

```shell
卸载
helm uninstall rocketmq --namespace rocketmq
```

