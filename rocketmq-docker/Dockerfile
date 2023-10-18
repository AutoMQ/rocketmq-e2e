FROM openjdk:17-jdk-alpine

MAINTAINER automq

USER root

 # Install dependency
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache bash gettext nmap-ncat openssl busybox-extras libc6-compat libgcc libstdc++ 


ARG version

# Rocketmq version
ENV ROCKETMQ_VERSION ${version}


ARG ROCKETMQ_DIR

ENV ROCKETMQ_NAME rocketmq-on-s3
ENV ROCKETMQ_HOME /home/rocketmq/${ROCKETMQ_NAME}-${ROCKETMQ_VERSION}

WORKDIR ${ROCKETMQ_HOME}


# Install
COPY rocketmq/ ${ROCKETMQ_HOME}/dist

RUN mv ${ROCKETMQ_HOME}/dist/* ${ROCKETMQ_HOME}/ && \
    rm -rf ${ROCKETMQ_HOME}/dist



# RUN chown -R ${uid}:${gid} ${ROCKETMQ_HOME}
EXPOSE 8081

RUN chmod a+x ${ROCKETMQ_HOME}/bin/run-server.sh


# Export Java options
RUN export JAVA_OPT=" -Duser.home=/opt"


WORKDIR ${ROCKETMQ_HOME}/bin

CMD ["sh", "-c", "sh ./bin/run-server.sh com.automq.rocketmq.broker.BrokerStartup -c ./conf/broker.yaml"]


