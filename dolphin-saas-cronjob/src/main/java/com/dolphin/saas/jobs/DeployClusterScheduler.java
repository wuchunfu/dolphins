package com.dolphin.saas.jobs;

import com.alibaba.fastjson.JSON;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.KuberContorNews;
import com.dolphin.saas.commons.clouds.aliyun.feature.*;
import com.dolphin.saas.commons.clouds.ssh.Remote;
import com.dolphin.saas.commons.clouds.tencent.*;
import com.dolphin.saas.commons.clouds.tencent.entity.KuberContorKv;
import com.dolphin.saas.commons.clouds.tencent.feature.Secure;
import com.dolphin.saas.entity.ServiceDeploy;
import com.dolphin.saas.service.ClusterService;
import com.dolphin.saas.service.VendorsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class DeployClusterScheduler extends BaseTools {

    @Resource
    private RedisCommonUtils redisCommonUtils;

    @Resource
    private ClusterService clusterService;

    @Resource
    private VendorsService vendorsService;

    /**
     * 执行构建集群任务
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void createClusterJobs() {
        // 从数据库获取集群创建任务列表
        try {
            ArrayList<ServiceDeploy> serviceDeployList = clusterService.getClusterCreateJobs();
            for (ServiceDeploy serviceDeploy : serviceDeployList) {
                // 判断是否有幂等锁
                if (!this.redisCommonUtils.hasKeys("ClusterJobs." + serviceDeploy.getClusterId().toString())) {
                    try {
                        // 加幂等锁
                        this.redisCommonUtils.noExpireSset("ClusterJobs." + serviceDeploy.getClusterId().toString(), 0);

                        // 找到对应的AK数据
                        Map<String, String> Akinfo = vendorsService.FindUUidCloudKey(
                                serviceDeploy.getUuid(), serviceDeploy.getClusterCloudId());

                        // 组装公共参数
                        Map<String, Object> paramets = new HashMap<>();
                        // 主秘钥 & 主域名（用于解析租户）
                        paramets.put("masterAccessKeyId", "");
                        paramets.put("masterAccessKeySecret", "");
                        paramets.put("masterDomain", "xxbb.com");
                        // 集群服务对象
                        paramets.put("clusterService", clusterService);
                        // 集群ID
                        paramets.put("cid", serviceDeploy.getClusterId());
                        // 归属地区
                        paramets.put("region", serviceDeploy.getClusterRegionId());
                        // 可用区
                        paramets.put("zone", serviceDeploy.getClusterZoneId());
                        // VPC段
                        paramets.put("subnet", "10.56.0.0/16");
                        // 并发数
                        paramets.put("concurrency", serviceDeploy.getClusterCurrent());
                        // 购买模式
                        paramets.put("buy", serviceDeploy.getClusterType());
                        // 集群的名称
                        paramets.put("clusterName", serviceDeploy.getClusterName());
                        // 购买的企业版本
                        paramets.put("clusterPayMode", serviceDeploy.getClusterPayMode());

                        // 设置集群密码
                        String password = DigestUtils.md5DigestAsHex(
                                String.format("海豚工程devOPS-{}-{}",
                                        serviceDeploy.getClusterId(),
                                        serviceDeploy.getClusterRegionId()).getBytes());
                        paramets.put("password", password.substring(password.length() - 20) + "@dolphins");
                        // redis缓存
                        paramets.put("redisCommonUtils", this.redisCommonUtils);
                        // 集群实例ID
                        paramets.put("clusterInstanceId", serviceDeploy.getClusterInstanceId());
                        // 集群的归属云
                        paramets.put("clusterCloud", serviceDeploy.getClusterCloudId());
                        // 服务基础配置
                        paramets.put("global_password", DigestUtils.md5DigestAsHex(password.getBytes()));

                        // 基础服务相关配置
                        switch (serviceDeploy.getClusterCloudId()) {
                            case 1:
                                /*
                                 * 这里是阿里云
                                 * 所有基础设施的构建部分
                                 */

                                // 1.开启Nas服务
                                if (!this.redisCommonUtils.hasKeys("clusterId." + serviceDeploy.getClusterId() + ".paramets.nas")) {
                                    new NasServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets).run();
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(), 1);
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.nas", 1);
                                }

                                // 2.创建和处理VPC相关
                                if (!this.redisCommonUtils.hasKeys("clusterId." + serviceDeploy.getClusterId() + ".paramets.vpc")) {
                                    Map<String, Object> VpcInfo = new VpcServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"))
                                            .setVal(paramets)
                                            .runner()
                                            .refval();
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(), 2);

                                    if (!VpcInfo.containsKey("vpcId") || !VpcInfo.containsKey("switchId")) {
                                        break;
                                    }
                                    // VPCID & 交换机ID
                                    paramets.put("vpcId", VpcInfo.get("vpcId").toString());
                                    paramets.put("switchId", VpcInfo.get("switchId").toString());
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.vpcId", VpcInfo.get("vpcId").toString());
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.switchId", VpcInfo.get("switchId").toString());
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.vpc", 1);
                                } else {
                                    paramets.put("vpcId", this.redisCommonUtils.get("clusterId." + serviceDeploy.getClusterId() + ".paramets.vpcId").toString());
                                    paramets.put("switchId", this.redisCommonUtils.get("clusterId." + serviceDeploy.getClusterId() + ".paramets.switchId").toString());
                                }

                                // 3.创建Arms相关
                                if (!this.redisCommonUtils.hasKeys("clusterId." + serviceDeploy.getClusterId() + ".paramets.arms")) {
                                    new ArmsServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets)
                                            .run();
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(), 3);
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.arms", 1);
                                }


                                // 4.创建安全组
                                if (!this.redisCommonUtils.hasKeys("clusterId." + serviceDeploy.getClusterId() + ".paramets.secgroup")) {
                                    Map<String, Object> securityGrpupRestuls = new SecurityGroupServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets)
                                            .setVal(paramets)
                                            .runner()
                                            .refval();
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(), 4);

                                    if (!securityGrpupRestuls.containsKey("securityGroupId")) {
                                        break;
                                    }
                                    // 安全组
                                    paramets.put("securityGroupId", securityGrpupRestuls.get("securityGroupId").toString());
                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.secgroup", securityGrpupRestuls.get("securityGroupId").toString());
                                } else {
                                    paramets.put("securityGroupId", this.redisCommonUtils.get("clusterId." + serviceDeploy.getClusterId() + ".paramets.secgroup").toString());
                                }

                                // 实例规格设计
                                // master实例规格
                                if (!this.redisCommonUtils.hasKeys("clusterId." + serviceDeploy.getClusterId() + ".paramets.instanceId")) {
                                    ArrayList<String> masterInstanceLists = null;
                                    ArrayList<String> nodeInstanceLists = null;
                                    switch (serviceDeploy.getClusterCurrent()){
                                        case 1:
                                            masterInstanceLists = new EcsServ(Akinfo.get("secreTld"),
                                                    Akinfo.get("secreKey"),
                                                    paramets).getResourceLists(16, 8);
                                            nodeInstanceLists = new EcsServ(Akinfo.get("secreTld"),
                                                    Akinfo.get("secreKey"),
                                                    paramets).getResourceLists(8, 4);
                                            break;

                                        case 50:
                                        case 250:
                                            masterInstanceLists = new EcsServ(Akinfo.get("secreTld"),
                                                    Akinfo.get("secreKey"),
                                                    paramets).getResourceLists(16, 16);
                                            nodeInstanceLists = masterInstanceLists;
                                            break;
                                    }

                                    if (masterInstanceLists.size() > 5) {
                                        ArrayList<String> masters = new ArrayList<>(masterInstanceLists.subList(0, 4));
                                        paramets.put("masterInstanceLists", masters);
                                    } else {
                                        paramets.put("masterInstanceLists", masterInstanceLists);
                                    }

                                    if (nodeInstanceLists.size() > 5) {
                                        ArrayList<String> noders = new ArrayList<>(nodeInstanceLists.subList(0, 4));
                                        paramets.put("nodeInstanceLists", noders);
                                    } else {
                                        paramets.put("nodeInstanceLists", nodeInstanceLists);
                                    }
                                    // 5.创建集群相关
                                    Map<String, Object> ackResults = new AckServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets)
                                            .runner()
                                            .refval();
                                    // 集群ID & 集群密码
                                    paramets.put("clusterId", ackResults.get("clusterId"));

                                    this.redisCommonUtils.setData("clusterId." + serviceDeploy.getClusterId() + ".paramets.instanceId", ackResults.get("clusterId").toString());
                                } else {
                                    paramets.put("clusterId", this.redisCommonUtils.get("clusterId." + serviceDeploy.getClusterId() + ".paramets.instanceId").toString());
                                }
                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 5);

                                break;

                            case 2:
                                /*
                                 * 这里是腾讯云
                                 * 所有基础设施的构建部分
                                 */

                                // 有缓存就用缓存的，不再走一次逻辑
                                if (this.redisCommonUtils.hasKeys("ClusterJobs.cache.init" + serviceDeploy.getClusterId().toString())){
                                    Object cacheObj = this.redisCommonUtils.get("ClusterJobs.cache.init" + serviceDeploy.getClusterId().toString());
                                    Map<String, String> cacheMap = JSON.parseObject(cacheObj.toString(), Map.class);
                                    // 赋值
                                    paramets.put("securityGroupId", cacheMap.get("securityGroupId"));
                                    paramets.put("vpcId", cacheMap.get("vpcId"));
                                    paramets.put("subNetId", cacheMap.get("subNetId"));
                                    paramets.put("pgroupId", cacheMap.get("pgroupId"));
                                    paramets.put("clusterId", cacheMap.get("clusterId"));
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(), 5);
                                    break;
                                }

                                // 1.服务授权CAM, 可以重复的，有幂等判断
                                new CamServPlugin(Akinfo.get("secreTld"), Akinfo.get("secreKey"))
                                        .setVal(paramets)
                                        .run();
                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 1);
                                // 2.安全组
                                Map<String, Object> secGroupResults = new SecGroupServPlugin(
                                        Akinfo.get("secreTld"), Akinfo.get("secreKey"))
                                        .setVal(paramets)
                                        .runner()
                                        .refval();
                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 2);
                                // 设置安全组的ID
                                paramets.put("securityGroupId", secGroupResults.get("groupId"));

                                // 3.创建vpc服务&子网
                                Map<String, Object> vpcResults = new VpcServPlugin(
                                        Akinfo.get("secreTld"), Akinfo.get("secreKey"))
                                        .setVal(paramets)
                                        .runner()
                                        .refval();
                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 3);

                                if (!vpcResults.containsKey("vpcId") || !vpcResults.containsKey("subNetId")) {
                                    break;
                                }

                                // 设置VPCid & 子网的ID
                                paramets.put("vpcId", vpcResults.get("vpcId"));
                                paramets.put("subNetId", vpcResults.get("subNetId"));

                                // 4.初始化cfs
                                Map<String, Object> cfsResults = new CfsServPlugin(
                                        Akinfo.get("secreTld"), Akinfo.get("secreKey"))
                                        .setVal(paramets)
                                        .runner()
                                        .refval();

                                // 设置Cfs的id
                                if (!cfsResults.containsKey("groupId")) {
                                    break;
                                }
                                paramets.put("pgroupId", cfsResults.get("groupId"));

                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 4);

                                // 5.计算资源&采买TKE
                                Map<String, Object> tkeResults = new TkeServPlugin(
                                        Akinfo.get("secreTld"), Akinfo.get("secreKey"))
                                        .setVal(paramets)
                                        .runner()
                                        .refval();

                                if (!tkeResults.containsKey("clusterId")) {
                                    break;
                                }
                                // 集群ID
                                paramets.put("clusterId", tkeResults.get("clusterId"));

                                this.redisCommonUtils.noExpireSset(
                                        "ClusterJobs." + serviceDeploy.getClusterId().toString(), 5);

                                /*
                                 * 缓存部分的内容，不再重复跟进
                                 */
                                Map<String, Object> cache = new HashMap<>();
                                cache.put("securityGroupId", paramets.get("securityGroupId").toString());
                                cache.put("vpcId", paramets.get("vpcId").toString());
                                cache.put("subNetId", paramets.get("subNetId").toString());
                                cache.put("pgroupId",paramets.get("pgroupId").toString());
                                cache.put("clusterId", paramets.get("clusterId").toString());
                                this.redisCommonUtils.set("ClusterJobs.cache.init" + serviceDeploy.getClusterId().toString(), JSON.toJSONString(cache));

                                break;
                        }

                        /*
                         * 通用配置
                         */
                        Integer clusterStatus = Integer.parseInt(
                                this.redisCommonUtils.get(
                                                "ClusterJobs." + serviceDeploy
                                                        .getClusterId()
                                                        .toString())
                                        .toString());
                        if (clusterStatus >= 5 && clusterStatus < 7) {
                            // 更新集群实例ID
                            clusterService.UpdateClusterId(
                                    serviceDeploy.getClusterId(),
                                    paramets.get("clusterId").toString(),
                                    paramets.get("securityGroupId").toString());
                            this.redisCommonUtils.noExpireSset(
                                    "ClusterJobs." + serviceDeploy.getClusterId().toString(), 7);
                            // 开启CI/CD搭建进度
                            clusterService.UpdateStage(6, serviceDeploy.getClusterId(), 1);
                        }

                        /*
                         * 检查集群是否构建完成
                         */
                        if (Integer.parseInt(
                                this.redisCommonUtils.get(
                                                "ClusterJobs." + serviceDeploy
                                                        .getClusterId()
                                                        .toString())
                                        .toString()) == 7) {
                            switch (serviceDeploy.getClusterCloudId()) {
                                case 1:
                                    // 阿里云
                                    Integer status = new AckServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets).getCreateStatus(paramets.get("clusterName").toString());

                                    if (status == 2) {
                                        // 集群构建成功
                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                                8);
                                    } else if (status == 3) {
                                        throw new Exception("集群阿里云端构建失败,阿里云状态异常!");
                                    }
                                    break;

                                case 2:
                                    // 腾讯云
                                    if (this.redisCommonUtils.hasKeys("ClusterJobs.cache.network" + serviceDeploy.getClusterId().toString())){
                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                                8);
                                        break;
                                    }
                                    Boolean InstallStatus = new ClusterServPlugin(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"))
                                            .checkClusterStatus(
                                                    paramets.get("clusterId").toString(),
                                                    paramets.get("region").toString());
                                    if (InstallStatus) {
                                        // 开启集群外网访问用于部署CI/CD
                                        new ClusterServPlugin(
                                                Akinfo.get("secreTld"),
                                                Akinfo.get("secreKey")).OpenNetWork(paramets);

                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                                8);

                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs.cache.network" + serviceDeploy.getClusterId().toString(), 1
                                        );
                                    }
                                    break;
                            }
                        }

                        if (Integer.parseInt(
                                this.redisCommonUtils.get(
                                                "ClusterJobs." + serviceDeploy
                                                        .getClusterId()
                                                        .toString())
                                        .toString()) == 8) {
                            switch (serviceDeploy.getClusterCloudId()) {
                                case 1:
                                    // 获取集群的配置
                                    String clusterConfig1 = new AckServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets).getKubConfig(paramets.get("clusterId").toString());

                                    if (clusterConfig1 == null) {
                                        break;
                                    }
                                    paramets.put("apiServiceToken", clusterConfig1);

                                    String clusterAddr = new AckServ(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"),
                                            paramets).getKubIpAddr(paramets.get("clusterId").toString());

                                    if (clusterAddr == null){
                                        break;
                                    }

                                    // 获取集群的的API地址
                                    paramets.put("apiServiceUrl", clusterAddr);

                                    // 拿到值才能更新
                                    if (paramets.containsKey("apiServiceUrl") && paramets.containsKey("apiServiceToken")) {
                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                                9);
                                    }
                                    break;

                                case 2:
                                    // 有缓存就用缓存的，不再走一次逻辑
                                    if (this.redisCommonUtils.hasKeys("ClusterJobs.cache.config" + serviceDeploy.getClusterId().toString())){
                                        Object cacheObj = this.redisCommonUtils.get("ClusterJobs.cache.config" + serviceDeploy.getClusterId().toString());
                                        Map<String, String> cacheMap = JSON.parseObject(cacheObj.toString(), Map.class);
                                        // 赋值
                                        paramets.put("apiServiceUrl", cacheMap.get("apiServiceUrl"));
                                        paramets.put("apiServiceToken", cacheMap.get("apiServiceToken"));
                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(), 9);
                                        break;
                                    }

                                    // 获取集群的配置
                                    Map<String, Object> clusterConfig = new ClusterServPlugin(
                                            Akinfo.get("secreTld"),
                                            Akinfo.get("secreKey"))
                                            .clusterSecurityKey(
                                                    paramets.get("clusterId").toString(),
                                                    paramets.get("region").toString());

                                    if (clusterConfig.containsKey("domain")) {
                                        paramets.put("apiServiceUrl", clusterConfig.get("domain").toString());
                                    }
                                    if (clusterConfig.containsKey("token")) {
                                        paramets.put("apiServiceToken", clusterConfig.get("kubconfig").toString());
                                    }
                                    // 拿到值才能更新
                                    if (paramets.containsKey("apiServiceUrl") && paramets.containsKey("apiServiceToken")) {
                                        Map<String, Object> cache = new HashMap<>();
                                        cache.put("apiServiceUrl", paramets.get("apiServiceUrl").toString());
                                        cache.put("apiServiceToken", paramets.get("apiServiceToken").toString());
                                        this.redisCommonUtils.set(
                                                "ClusterJobs.cache.config" + serviceDeploy.getClusterId().toString(), JSON.toJSONString(cache));

                                        this.redisCommonUtils.noExpireSset(
                                                "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                                9);
                                    }
                                    break;
                            }
                        }

                        /*
                         * 打通CI/CD工具联调
                         */
                        if (Integer.parseInt(
                                this.redisCommonUtils.get(
                                                "ClusterJobs." + serviceDeploy
                                                        .getClusterId()
                                                        .toString())
                                        .toString()) == 9) {
                            // 判断外网解析是否已经生效
                            String K8SAPI;
                            if (serviceDeploy.getClusterCloudId() == 1) {
                                K8SAPI = paramets.get("apiServiceUrl").toString();
                            } else {
                                K8SAPI = "https://" + paramets.get("apiServiceUrl").toString();
                            }

                            if (this.testWsdlConnection(K8SAPI)) {
                                // 这个服务需要在JENKINS之前做，不然不行
                                switch (serviceDeploy.getClusterCloudId()) {
                                    case 1:
                                        // 获取所有实例ID
                                        // 创建所有的EIP
                                        // 绑定实例跟EIP信息
                                        ArrayList<Map<String, String>> instanceInfo = new EcsServ(Akinfo.get("secreTld"),
                                                Akinfo.get("secreKey"),
                                                paramets).getInstanceLists(paramets.get("clusterId").toString());


                                        boolean instanceStatus = true;
                                        for (Map<String, String> instanceItems: instanceInfo){
                                            if (!instanceItems.containsKey("publicIp")) {
                                                instanceStatus = false;
                                                break;
                                            }
                                        }
                                        if (!instanceStatus || instanceInfo.size() == 0){
                                            break;
                                        }

                                        paramets.put("instanceInfo", instanceInfo);

                                        // 处理镜像所需要的仓库忽略机制
                                        if (!this.redisCommonUtils.hasKeys("daemon_" + serviceDeploy.getClusterId())) {
                                            // 初始化幂等锁
                                            try {
                                                this.redisCommonUtils.noExpireSset("daemon_" + serviceDeploy.getClusterId(), 1);
                                                String commonds = "sudo python -c \"import json;insec=json.loads(open('/etc/docker/daemon.json').read());insec['insecure-registries'].append('<ADDRESS>');open('/etc/docker/daemon.json', 'w').write(json.dumps(insec))\"";
                                                commonds = commonds.replace("<ADDRESS>", "docker-"+serviceDeploy.getClusterId()+"."+paramets.get("masterDomain").toString());
                                                String reloadService = "sudo systemctl restart dockerd.service";

                                                ArrayList<String> commandLists = new ArrayList<>();
                                                commandLists.add(commonds);
                                                commandLists.add(reloadService);

                                                for (Map<String, String> items : instanceInfo) {
                                                    Remote remote = new Remote();
                                                    remote.setHost(items.get("publicIp"));
                                                    remote.setPassword(paramets.get("password").toString());

                                                    // 执行命令
                                                    new Secure().sshExecCommand(remote, commandLists);
                                                }
                                            }catch (Exception e){
                                                break;
                                            }finally {
                                                // 结束幂等锁,最后整体完成后再删除
                                                this.redisCommonUtils.noExpireSset("daemon_" + serviceDeploy.getClusterId(), 2);
                                            }
                                        }
                                        break;
                                    case 2:
                                        // 获取集群的节点IP
                                        ArrayList<String> clusterIp = new ClusterServPlugin(
                                                Akinfo.get("secreTld"),
                                                Akinfo.get("secreKey"))
                                                .clusterServerIp(
                                                        paramets.get("region").toString(),
                                                        paramets.get("clusterId").toString());

                                        // 处理镜像所需要的仓库忽略机制
                                        if (!this.redisCommonUtils.hasKeys("daemon_" + serviceDeploy.getClusterId())) {
                                            // 初始化幂等锁
                                            this.redisCommonUtils.noExpireSset("daemon_" + serviceDeploy.getClusterId(), 1);

                                            String commonds = "sudo python -c \"import json;insec=json.loads(open('/etc/docker/daemon.json').read());insec['insecure-registries'].append('<ADDRESS>');open('/etc/docker/daemon.json', 'w').write(json.dumps(insec))\"";
                                            commonds = commonds.replace("<ADDRESS>", "docker-"+serviceDeploy.getClusterId()+"."+paramets.get("masterDomain").toString());

                                            String reloadService = "sudo systemctl restart dockerd.service";

                                            ArrayList<String> commandLists = new ArrayList<>();

                                            commandLists.add(commonds);
                                            commandLists.add(reloadService);

                                            boolean ExecStatus = true;
                                            for (String Ip : clusterIp) {
                                                Remote remote = new Remote();
                                                remote.setHost(Ip);
                                                remote.setPassword(paramets.get("password").toString());

                                                // 执行命令
                                                Map<String, Object> results = new Secure().sshExecCommand(remote, commandLists);
                                                if (results == null) {
                                                    ExecStatus = false;
                                                }
                                            }
                                            if (ExecStatus){
                                                // 结束幂等锁,最后整体完成后再删除
                                                this.redisCommonUtils.noExpireSset("daemon_" + serviceDeploy.getClusterId(), 2);
                                            }else{
                                                this.redisCommonUtils.getRedisTemplate().delete("daemon_" + serviceDeploy.getClusterId());
                                                break;
                                            }
                                        }
                                        break;
                                }

                                if (!this.redisCommonUtils.hasKeys("daemon_" + serviceDeploy.getClusterId())){
                                    break;
                                }

                                if (Integer.parseInt(this.redisCommonUtils.get("daemon_" + serviceDeploy.getClusterId()).toString()) != 2){
                                    break;
                                }

                                // 初始化配置
                                KuberContorKv kuberContorKv = new KuberContorKv();
                                kuberContorKv.setParamets(paramets);
                                // 初始化连接集群
                                Map<String, Object> deployAll = new KuberContorNews(kuberContorKv)
                                        .initNameSpace()            // 初始化单个命名空间不然无法部署
                                        .initDomain()               // 初始化域名信息
                                        .deploymentService()        // 部署服务
                                        .configurePipeline()        // 配置流水线
                                        .configJenkins()            // 配置Jenkins
                                        .doubleCheck();             // 最后检查并返回配置数据

                                // 判断任务是否已经全部完成部署，并且全部都能正常访问
                                if (deployAll.containsKey("state") && deployAll.get("state").toString().equals("SUCCESS")) {
                                    // 存放git分组信息
                                    paramets.put("gitGroup", deployAll.get("gitGroup"));
                                    // 存放Token
                                    paramets.put("sonar_token", deployAll.get("sonar_token"));
                                    // 刷新缓存
                                    this.redisCommonUtils.noExpireSset(
                                            "ClusterJobs." + serviceDeploy.getClusterId().toString(),
                                            10);
                                }
                            }
                        }

                        /*
                         * 收尾，确认状态
                         */
                        if (Integer.parseInt(
                                this.redisCommonUtils.get(
                                                "ClusterJobs." + serviceDeploy
                                                        .getClusterId()
                                                        .toString())
                                        .toString()) == 10) {

                            // Gitlab数据
                            try {
                                if (paramets.containsKey("gitGroup")) {
                                    Map<String, Object> gitGroup = (Map<String, Object>) paramets.get("gitGroup");
                                    for (String groupName : gitGroup.keySet()) {
                                        Map<String, Integer> groupInfo = (Map<String, Integer>) gitGroup.get(groupName);
                                        clusterService.UpdateGitLabNameSpace(
                                                serviceDeploy.getClusterId(),
                                                groupName,
                                                groupName,
                                                groupInfo.get(groupName));
                                    }
                                }
                            } catch (Exception e) {
                                log.info("处理gitlab数据失败: {}", e.getMessage());
                            }

                            // 收割数据(基础数据)
                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "git-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                    "Gitlab仓库", "root",
                                    paramets.get("global_password").toString(),
                                    null);

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "nexus-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                    "nexus仓库", "admin", "admin123",
                                    null);

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "docker-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                    "docker仓库", "admin", "admin123",
                                    null);

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "dolphin-mysql-service",
                                    "MYSQL服务(不对外)", "root", paramets.get("global_password").toString(),
                                    null);

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "jenkins-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                    "Jenkins服务", "user", paramets.get("global_password").toString(),
                                    null);

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    "sonar-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                    "sonar服务", "admin", paramets.get("global_password").toString(),
                                    paramets.get("sonar_token").toString());

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    paramets.get("apiServiceUrl").toString(),
                                    "K8S集群服务", null, null,
                                    paramets.get("apiServiceToken").toString());

                            clusterService.SaveClusterMessage(
                                    serviceDeploy.getClusterId(),
                                    null,
                                    "节点密码(通用)", "root", paramets.get("password").toString(),
                                    null);

                            if (serviceDeploy.getClusterPayMode() > 1) {
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "jumpserver-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "堡垒机", "admin", "admin",
                                        null);
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "skywalking-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "链路跟踪", "admin", "admin",
                                        null);
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "sentry-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "异常监控", "admin@sentry.local", "sentry12345",
                                        null);
                            }
                            if (serviceDeploy.getClusterPayMode() > 2){
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "metersphere-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "测试自动化", "admin", "metersphere",
                                        null);
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "grafana-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "大盘监控", "admin", "admin",
                                        null);
                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "hfish-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "蜜罐", "admin", "admin",
                                        null);

                                clusterService.SaveClusterMessage(
                                        serviceDeploy.getClusterId(),
                                        "es-" + serviceDeploy.getClusterId() + "." + paramets.get("masterDomain"),
                                        "ES集群", null, null,
                                        null);
                            }

                            // 更新进度条
                            clusterService.UpdateStage(6, serviceDeploy.getClusterId(), 2);

                            // 处理完，服务部署完，更新整体集群状态
                            clusterService.UpdateClusterIdStatus(serviceDeploy.getClusterId(), 3);
                        }
                    } catch (Exception e) {
                        // 更新集群状态为异常
                        if (e.getMessage().matches("(.*)Connect timed out(.*)")) {
                            log.error("[元豚科技][部署任务]子程序异常: {} 链接超时，稍后重试, 任务信息: {}", e.getMessage(), serviceDeploy);
                            continue;
                        }
                        // 如果是流控，则不要更新状态
                        if (e.getMessage().matches("(.*)Request was denied due to api flow control(.*)")) {
                            log.error("[元豚科技][部署任务]子程序异常: {} 流控API问题，稍后重试, 任务信息: {}", e.getMessage(), serviceDeploy);
                            continue;
                        }
                        if (e.getMessage().matches("(.*) contact us.(.*)")) {
                            log.error("[元豚科技][部署任务]子程序异常: {} 流控API问题，稍后重试, {}", e.getMessage(), serviceDeploy);
                            continue;
                        }
                        // 重试机制
                        if (!this.redisCommonUtils.hasKeys("ClusterJobs.retry." + serviceDeploy.getClusterId().toString())){
                            this.redisCommonUtils.noExpireSset("ClusterJobs.retry." + serviceDeploy.getClusterId().toString(), 1);
                            Thread.sleep(5000);
                            continue;
                        }else{
                            Object retryVal = this.redisCommonUtils.get("ClusterJobs.retry." + serviceDeploy.getClusterId().toString());
                            int retry = Integer.parseInt(retryVal.toString());
                            if (retry < 10){
                                retry = retry + 1;
                                this.redisCommonUtils.noExpireSset("ClusterJobs.retry." + serviceDeploy.getClusterId().toString(), retry);
                                Thread.sleep(5000);
                                continue;
                            }
                        }


                        clusterService.UpdateClusterIdStatus(serviceDeploy.getClusterId(), 4);
                        clusterService.UpdateClusterFailedErrorMessage(serviceDeploy.getClusterId(), e.getMessage());

                        log.error("[元豚科技][部署任务]子程序异常: {}, 任务信息: {}", e.getMessage(), serviceDeploy);
                    } finally {
                        Set<String> Lists = this.redisCommonUtils.getRedisTemplate().keys("*." + serviceDeploy.getClusterId());
                        for (String Lockkey : Lists) {
                            this.redisCommonUtils.getRedisTemplate().delete(Lockkey);
                        }

                        // 删除幂等锁
                        this.redisCommonUtils.getRedisTemplate().delete(
                                "ClusterJobs." + serviceDeploy.getClusterId().toString());
                    }
                }

            }
        } catch (Exception e) {
            log.error("[元豚科技][部署任务]主程序异常: {}", e.getMessage());
        }

    }

    /**
     * 测试地址是否可以正常访问
     *
     * @param address 地址
     * @return
     */
    protected Boolean testWsdlConnection(String address) {
        try {
            URL urlObj = new URL(address);
            HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
            oc.setUseCaches(false);
            oc.setConnectTimeout(3000); // 设置超时时间
            oc.getResponseCode();
        } catch (SSLHandshakeException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
