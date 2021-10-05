# Image source: 11.0.2-jdk-slim
FROM openjdk:11.0.2-jdk-slim
LABEL application="state-pension/underpayments-service"
LABEL maintainer="Nilanjan Biswas"

ARG APP_FOLDER=/opt/app

ENV PORT="9007"
ENV CONTEXT_PATH="/underpayments-service/api/v1"
ENV DB_URL="jdbc:postgresql://host.docker.internal:5432/postgres?currentSchema=underpayments"
ENV ORCL_DB_URL="jdbc:oracle:thin:@host.docker.internal:1521:xe"


WORKDIR ${APP_FOLDER}

# COPY bootable Jar into image
COPY target/underpayment-service-*.jar ${APP_FOLDER}/app.jar

# COPY bootstrapping script into image
COPY docker/user-data.sh ${APP_FOLDER}/

RUN mkdir /var/tmp/export

RUN apt-get update
RUN apt-get install -y openssl

ENTRYPOINT ["/bin/sh", "user-data.sh"]
