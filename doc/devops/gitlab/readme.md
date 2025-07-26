单独一台虚拟机上通过docker安装
``` shell
mkdir -p /data/gitlab

export GITLAB_HOME=/data/gitlab

sudo docker run --detach \
  --publish 443:443 \
  --publish 80:80 \
  --publish 2222:22 \
  --name gitlab \
  --restart always \
  --volume $GITLAB_HOME/config:/etc/gitlab \
  --volume $GITLAB_HOME/logs:/var/log/gitlab \
  --volume $GITLAB_HOME/data:/var/opt/gitlab \
  gitlab/gitlab-ce:18.0.2-ce.0
```