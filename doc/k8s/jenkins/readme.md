#### 说明
- 由于本地资源不够，本示例中，只是通过k8s部署了一个jenkins副本，并非集群版本，其实k8s最终的部署结果跟在一台虚拟机上通过docker方式安装的结果没什么不同。  
- 通过挂载数据卷方式使用数据卷中的maven、jdk来进行Java程序的打包，而构建docker镜像则使用jenkins所在节点的docker。  
- 建议搭建k8s集群的jenkins，并且对jenkins镜像重新打包，把所需的mvn、jdk等拷贝进去，以及容器内再安装docker等。
- 另外，针对本示例，为了能够部署应用给到k8s集群，需要在k8s-master上执行部署命令，所以最后是jenkins是以无密码登录方式，登录k8s-master进行的kubectl部署
1. 对k8s的一个工作节点打标签，后续的部署文件deployment，通过定义nodeSelector来定位到该节点上部署。（其实跟通过docker直接在该节点上部署无两样。）
```shell 
  kubectl label node k8s-worker1 cname=jenkins
```

2. 创建nfs共享目录
```shell
## 如果没安装nfs，则安装
yum install nfs-utils -y
systemctl enable nfs --now

## 创建目录
mkdir /data/jenkins -p

vi /etc/exports
# 添加下面的内容
/data/jenkins *(rw,no_root_squash)
#保存、退出

## 使之生效
exportfs -arv
```

3. 部署jenkins
```shell
  kubectl apply -f jenkins-pv.yaml
  kubectl apply -f jenkins-pvc.yaml
  kubectl apply -f jenkins-service.yaml
  kubectl apply -f jenkins-deployment.yaml
```

4. 为了能让jenkins容器能使用宿主机上的docker，需要进入工作节点修改docker文件的权限
```shell
    chown root:root /var/run/docker.sock
    chmod 777 /var/run/docker.sock
    chmod 777 /etc/docker/daemon.json
```
或者使用 Systemd 的 ExecStartPost 指令，在docker启动后执行一些指定命令
```shell
## 创建目录，安装了docker一般已经存在
sudo mkdir -p /etc/systemd/system/docker.service.d

## 创建脚本文件
vi /etc/systemd/system/docker.service.d/chmod-docker-sock.sh
输入
#!/bin/bash
chown root:root /var/run/docker.sock
chmod 777 /var/run/docker.sock
chmod 777 /etc/docker/daemon.json
保存退出


## 脚本授权
chmod +x /etc/systemd/system/docker.service.d/chmod-docker-sock.sh

## 创建ExecStartPost
vi /etc/systemd/system/docker.service.d/post-start.conf
输入
[Service]
ExecStartPost=/bin/bash /etc/systemd/system/docker.service.d/chmod-docker-sock.sh
保存退出

## 重启
systemctl daemon-reload
systemctl restart docker

## 验证
journalctl -u docker.service | grep "ExecStartPost"

ls -ll /var/run/docker.sock 
```

5. 上传jdk、maven 到nfs服务所在节点，并将其解压到jenkins挂载nfs的目录
```shell
  # 将提前下载linux版本的jdk-17.0.15_linux-x64_bin.tar.gz上传，并解压
  tar -xzvf jdk-17.0.15_linux-x64_bin.tar.gz 
  
  # 将提前下载linux版本的apache-maven-3.9.9-bin.tar.gz上传到k8s中jenkins所在服务器
  tar -xzvf apache-maven-3.9.9-bin.tar.gz
  
  # 修改maven的远程仓库 mvn/conf/setting
  
  # 将二者解压后的文件移动到指定目录，jenkins挂载后，也能访问他们，在jenkins中配置好maven和java的路径即可
  mv jdk-17.0.15_linux-x64_bin/jdk /data/jenkins/jenkins-home
  mv apache-maven-3.9.9-bin/mvn /data/jenkins/jenkins-home
```


6. ssh无密码登录
- 客户端机器
```shell
  # 生成rsa公钥和私钥
  ssh-keygen -t rsa -b 2048
```
- 将公钥复制到服务器
```shell
  # 生成rsa公钥和私钥
  ssh-copy-id root@192.168.100.11
```