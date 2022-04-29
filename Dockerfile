FROM registry.cn-beijing.aliyuncs.com/weaveplatform/maven:3.8.2-adoptopenjdk-15-aliyun AS build

ENV MY_HOME=/app
RUN mkdir -p $MY_HOME
ARG MODULE

WORKDIR $MY_HOME

#COPY . $MY_HOME
#ADD pom.xml $MY_HOME
ADD . $MY_HOME
ADD pom.xml $MY_HOME
ADD settings.xml /root/.m2/

#RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
# get all the downloads out of the way
#RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify","clean","--fail-never"]

# add source
#ADD . $MY_HOME

# run maven verify
#RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify"]
# "-s", "/app/settings.xml",
RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","package", "-DskipTests"]

#RUN /bin/bash -x $MY_HOME/mvnw
# Second stage - build image
FROM openjdk:15.0.2-jdk-slim-buster

ARG MODULE

COPY --from=build /app/$MODULE/target/*.jar /app.jar
ADD yaml/ /yaml
ADD wechat/ /wechat
RUN chmod 777 -R /yaml
RUN chmod 777 -R /wechat

ENV JAVA_OPTS=""
ENV SERVER_PORT 8080

EXPOSE ${SERVER_PORT}

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/urandom -jar /app.jar" ]
