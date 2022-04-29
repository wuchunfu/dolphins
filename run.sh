#!/bin/bash
module=$1
tag=$2
##获取当前运行的容器
#c_id=`docker ps -a | grep dolphin-paas-api |awk '{print$1}'`

##构建新镜像
DOCKER_BUILDKIT=1 docker build --build-arg MODULE=${module} -t ${module}:${tag} .

docker tag ${module}:${tag} ${module}:latest
##关闭当前镜像
#docker stop $c_id && docker rm $c_id

##启动新容器
#docker run -d -p8090:8080 dolphin-paas-api:${tag} 
