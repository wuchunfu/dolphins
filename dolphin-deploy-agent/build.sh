docker build -f ./agent/deploy/Dockerfile -t registry.cn-beijing.aliyuncs.com/aidolphins_com/dolphin-deploy-agent:deploy-$1 .
docker build -f ./agent/configur/Dockerfile -t registry.cn-beijing.aliyuncs.com/aidolphins_com/dolphin-deploy-agent:config-$1 .
docker push registry.cn-beijing.aliyuncs.com/aidolphins_com/dolphin-deploy-agent:config-$1
docker push registry.cn-beijing.aliyuncs.com/aidolphins_com/dolphin-deploy-agent:deploy-$1
