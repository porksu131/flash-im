单独一台虚拟机机器安装harbor私有仓库

1. 下载离线安装包
```shell
wget https://github.com/goharbor/harbor/releases/download/v2.13.0/harbor-offline-installer-v2.13.1.tgz -O harbor-offline-installer-v2.13.1.tgz
```
2. 解压
```shell
tar xzvf harbor-offline-installer-v2.13.1.tgz
```
3. 进入harbor文件夹，拷贝一份模板harbor.yml，并修改内容改hostname的ip地址，改下端口，注释https相关内容，修改存放数据的位置就行了
```shell
cp harbor.yml.tmpl harbor.yml

vi harbor.yaml

# The IP address or hostname to access admin UI and registry service.
# DO NOT use localhost or 127.0.0.1, because Harbor needs to be accessed by external clients.
hostname: 192.168.11.170
 
# http related config
http:
  # port for http, default is 80. If https enabled, this port will redirect to https port
  port: 8080
 
# https related config
#https:
  # https port for harbor, default is 443
  #port: 443
  # The path of cert and key files for nginx
  #certificate: /your/certificate/path
  #private_key: /your/private/key/path

# The default data volume
data_volume: /usr/local/harbor/data

## 保存退出
```
4. 安装harbor
```shell
./prepare
./install.sh
```