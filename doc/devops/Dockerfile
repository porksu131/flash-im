FROM openjdk:17-jdk

LABEL maintainer="flash-im"

ARG CONTAINER_RPORT="8080"
ENV JAVA_OPT="--server.port=8080 --spring.profiles.active=k8s"

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" >/etc/timezone

COPY target/*.jar /app.jar

EXPOSE ${CONTAINER_RPORT}

ENTRYPOINT ["/bin/sh", "-c", "java -Dfile.encoding=utf8 -jar app.jar ${JAVA_OPT}"]

