package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.common.RelativeDateFormat;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.ServiceDeployLists;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.searchs.ClusterSearch;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("clusterService")
public class ClusterServiceimpl extends BaseTools implements ClusterService {

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private ServiceDeploySMapper serviceDeploySMapper;

    @Resource
    private ServiceDeployGitlabsMapper serviceDeployGitlabsMapper;

    @Resource
    private KubBaseMapper kubBaseMapper;

    @Resource
    private ServiceDeployConfigMapper serviceDeployConfigMapper;

    @Resource
    private ServiceDeployErrorMapper serviceDeployErrorMapper;

    @Resource
    private KubConfigYamlMapper kubConfigYamlMapper;

    @Resource
    private VendorsMapper vendorsMapper;

    @Resource
    private EngineeringMapper engineeringMapper;

    @Override
    public Map<String, Long> GetEngineerClusterDeploy(String uuid, Long engineerId) throws Exception {
        Map<String, Long> results = new HashMap<>();
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("engineer_id", engineerId);
            Engineer engineer = engineeringMapper.selectOne(queryWrapper);
            if (engineer == null) {
                throw new Exception("工程不存在!");
            }
            // 查集群的卡发布次数
            QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("cluster_id", engineer.getClusterId());
            ServiceDeploy serviceDeploy = clusterMapper.selectOne(queryWrapper1);
            if (serviceDeploy == null) {
                throw new Exception("集群异常，不存在!");
            }

            results.put("count", serviceDeploy.getClusterDeployCount());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Integer> GetClusterAlive(String uuid) throws Exception {
        Map<String, Integer> results = new HashMap<>();
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("cluster_service_status", 3);

            int aliyunCount = 0, count = 0;
            List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(queryWrapper);
            for (ServiceDeploy serviceDeploy : serviceDeployList) {
                if (serviceDeploy.getClusterCloudId() == 1) {
                    aliyunCount = aliyunCount + 1;
                }
                count = count + 1;
            }
            results.put("total", count);
            results.put("aliyun", aliyunCount);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, String> GetClusterMessage(Long cid, String uuid) throws Exception {
        Map<String, String> results = new HashMap<>();
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("cluster_id", cid);
            queryWrapper.eq("cluster_service_status", 3);

            ServiceDeploy serviceDeploy = clusterMapper.selectOne(queryWrapper);
            if (serviceDeploy == null) {
                throw new Exception("集群不存在,或状态异常!");
            }

            results.put("securityGroupId", serviceDeploy.getClusterSecurityId());
            results.put("regionId", serviceDeploy.getClusterRegionId());
            results.put("cloudId", serviceDeploy.getClusterCloudId().toString());
            results.put("current", serviceDeploy.getClusterCurrent().toString());

            QueryWrapper<Vendors> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("type_name", serviceDeploy.getClusterCloudId());
            queryWrapper1.eq("v_defaults", 1);
            queryWrapper1.eq("v_status", 1);

            List<Vendors> vendorsList = vendorsMapper.selectList(queryWrapper1);
            if (vendorsList.size() > 0) {
                results.put("accessKey", vendorsList.get(0).getAccessKey());
                results.put("accessSecret", vendorsList.get(0).getAccessSecret());
            } else {
                throw new Exception("云厂商秘钥信息异常，导致无法获取，请先处理秘钥问题。");
            }

            // 如果是高级版，读取一下对应的ES记录,复用接口
            if (serviceDeploy.getClusterCurrent() == 250) {
                QueryWrapper<ServiceDeployWorkConfig> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("cluster_config_name", "ES集群");
                queryWrapper2.eq("cluster_id", cid);
                ServiceDeployWorkConfig serviceDeployWorkConfig = serviceDeployConfigMapper.selectOne(queryWrapper2);
                if (serviceDeployWorkConfig == null) {
                    throw new Exception("获取ES集群配置失败!");
                } else {
                    results.put("esAddr", serviceDeployWorkConfig.getClusterConfigAddress());
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void DeleteCluster(Long cid, String uuid) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("cluster_id", cid);
            queryWrapper.in("cluster_service_status", 3, 4);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("待删除集群不存在，请检查!");
            }
            ServiceDeploy serviceDeploy = new ServiceDeploy();
            serviceDeploy.setClusterServiceStatus(5);
            serviceDeploy.setClusterDelete(0);
            if (clusterMapper.update(serviceDeploy, queryWrapper) < 1) {
                throw new Exception("删除集群失败，请联系客服!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Boolean CheckClusterStatus(String uuid) {
        QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        return clusterMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public Map<String, Object> FindClusterLists(int Pages, int Size, String uuid, ClusterSearch clusterSearch) throws Exception {
        // 获取分页的数据
        IPage<ServiceDeployLists> page = new Page<>(Pages, Size);
        QueryWrapper<ServiceDeployLists> queryWrapper = new QueryWrapper<>();
        // 逻辑禁用判断
        queryWrapper.eq("cluster_delete", 1);
        if (uuid != null) {
            queryWrapper.eq("ht_service_deploy.uuid", uuid);
        }

        // 增加搜索参数
        if (clusterSearch != null) {
            // 实例模糊搜索
            if (clusterSearch.getClusterInstanceId() != null) {
                queryWrapper.like("cluster_instance_id", clusterSearch.getClusterInstanceId());
            }

            // 云厂商ID绝对搜索
            if (clusterSearch.getClusterCloudId() != null) {
                queryWrapper.eq("cluster_cloud_id", clusterSearch.getClusterCloudId());
            }

            // 状态搜索
            if (clusterSearch.getClusterServiceStatus() != null) {
                queryWrapper.eq("cluster_service_status", clusterSearch.getClusterServiceStatus());
            }
        }

        // 增加时间排序
        queryWrapper.orderByDesc("cluster_createtime");
        clusterMapper.findAllClusterLists(page, queryWrapper);

        // 拼装分页数据
        Map<String, Object> results = new HashMap<>();
        results.put("page", Pages);
        List<ServiceDeployLists> serviceDeploys = page.getRecords();
        ArrayList<Map<String, Object>> records = new ArrayList<>();
        for (ServiceDeployLists serviceDeploy : serviceDeploys) {
            Map<String, Object> items = this.objectMap(serviceDeploy);

            // 如果状态为异常，查询对应的异常信息
            if (serviceDeploy.getClusterServiceStatus() == 4) {
                QueryWrapper<ServiceDeployError> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("cluster_id", serviceDeploy.getClusterId());
                queryWrapper1.select("cluster_error_log");
                queryWrapper1.orderByDesc("error_createtime");
                List<ServiceDeployError> serviceDeployErrorList = serviceDeployErrorMapper.selectList(queryWrapper1);
                if (serviceDeployErrorList.size() > 0) {
                    items.put("clusterErrorMsg", serviceDeployErrorList.get(0).getClusterErrorLog());
                } else {
                    items.put("clusterErrorMsg", null);
                }
            } else {
                items.put("clusterErrorMsg", null);
            }

            // 增加时间格式化
            if (serviceDeploy.getClusterUpdatetime() != null) {
                items.put("clusterUpdatetime", RelativeDateFormat.format(this.formatData(serviceDeploy.getClusterUpdatetime())));
            }
            records.add(items);
        }
        results.put("total", page.getTotal());
        results.put("list", records);
        results.put("pageSize", Size);
        return results;
    }

    @Override
    public Map<String, Object> ReadCluster(Long cid, String uuid) throws Exception {
        Map<String, Object> resultMap;

        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uuid", this.orgUUidList(uuid));
            queryWrapper.eq("cluster_id", cid);
            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("请勿越权访问!");
            }

            // 获取发布集群的信息
            ServiceDeployLists serviceDeployLists = clusterMapper.findClusterItems(queryWrapper);
            resultMap = this.objectMap(serviceDeployLists);

            if (serviceDeployLists.getClusterServiceStatus() == 3) {
                // 根据id查对应的集群配置信息
                QueryWrapper<ServiceDeployWorkConfig> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("cluster_id", serviceDeployLists.getClusterId());
                queryWrapper1.eq("cluster_config_name", "K8S集群服务");
                ServiceDeployWorkConfig clusterConfigApiAddr = serviceDeployConfigMapper.selectOne(queryWrapper1);
                resultMap.put("clusterApi", clusterConfigApiAddr.getClusterConfigAddress());
                resultMap.put("clusterToken", clusterConfigApiAddr.getClusterConfigToken());
            }

            // 如果状态为异常，查询对应的异常信息
            if (serviceDeployLists.getClusterServiceStatus() == 4) {
                QueryWrapper<ServiceDeployError> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("cluster_id", serviceDeployLists.getClusterId());
                queryWrapper1.select("cluster_error_log");
                ServiceDeployError serviceDeployError = serviceDeployErrorMapper.selectOne(queryWrapper1);
                resultMap.put("clusterErrorMsg", serviceDeployError.getClusterErrorLog());
            } else {
                resultMap.put("clusterErrorMsg", null);
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> ReadClusterStages(Long cid, String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<ServiceDeploy> serviceDeployQueryWrapper = new QueryWrapper<>();
            serviceDeployQueryWrapper.eq("cluster_id", cid);
            serviceDeployQueryWrapper.eq("uuid", uuid);
            // 判断是否归属这个用户
            if (clusterMapper.selectCount(serviceDeployQueryWrapper) < 1) {
                throw new Exception("请勿非法访问!");
            }
            // 查询进度信息
            QueryWrapper<ServiceDeployStages> serviceStagesQueryWrapper = new QueryWrapper<>();
            serviceStagesQueryWrapper.eq("cluster_id", cid);
            serviceStagesQueryWrapper.orderByAsc("stage_pipeline");
            List<ServiceDeployStages> serviceDeployStagesList = serviceDeploySMapper.selectList(serviceStagesQueryWrapper);

            if (serviceDeployStagesList.size() > 0) {
                results.put("clusterStages", serviceDeployStagesList);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public ArrayList<ServiceDeployWorkConfig> ReadClusterMessage(Long clusterId, Integer types, String uuid) throws Exception {
        try {
            // 根据UUID去查集群信息
            QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", uuid);
            queryWrapper1.eq("cluster_id", clusterId);
            if (clusterMapper.selectCount(queryWrapper1) < 1) {
                throw new Exception("集群不存在!");
            }
            // 差对应的创建的信息
            QueryWrapper<ServiceDeployWorkConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_config_types", types);
            queryWrapper.select(
                    "id", "cluster_config_name",
                    "cluster_config_address", "cluster_config_username",
                    "cluster_config_password", "cluster_config_token",
                    "cluster_config_createtime");
            List<ServiceDeployWorkConfig> clusterConfigList = serviceDeployConfigMapper.selectList(queryWrapper);
            return new ArrayList<>(clusterConfigList);
        } catch (Exception e) {
            log.error("[获取集群关键信息异常]提示: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ClusterCreate(Integer current, Integer cloudId, String regionId, String zoneId, Integer clusterType, String uuid, Integer payMode) throws Exception {
        try {
            // 如有一个集群在建就不让建
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_service_status", 2);
            queryWrapper.eq("cluster_cloud_id", cloudId);
            if (clusterMapper.selectCount(queryWrapper) > 0) {
                throw new Exception("有一个集群在建，请等待创建完毕再创建!");
            }

            ServiceDeploy serviceDeploy = new ServiceDeploy();
            serviceDeploy.setClusterName("海豚工程-" + (int) (Math.random() * 10000000));
            serviceDeploy.setClusterCurrent(current);
            serviceDeploy.setClusterCloudId(cloudId);
            serviceDeploy.setClusterRegionId(regionId);
            serviceDeploy.setClusterZoneId(zoneId);
            serviceDeploy.setClusterType(clusterType);
            serviceDeploy.setUuid(uuid);
            serviceDeploy.setClusterPayMode(payMode);
            serviceDeploy.setClusterServiceStatus(1);
            serviceDeploy.setClusterDelete(0);
            serviceDeploy.setClusterCreatetime(new Date());

            if (clusterMapper.insert(serviceDeploy) < 1) {
                throw new Exception("创建集群失败!");
            }

            List<String> TaskLists = new ArrayList<>();
            switch (cloudId) {
                case 1:
                    TaskLists = Arrays.asList(
                            "开启NAS服务", "创建VPC&交换机",
                            "开启ARMS服务", "创建安全组",
                            "创建ACK集群", "部署CI/CD服务"
                    );
                    break;
                case 2:
                    TaskLists = Arrays.asList("服务授权CAM",
                            "安全组配置", "创建VPC&子网",
                            "购买配置CFS", "计算资源&采买集群",
                            "部署CI/CD服务");
                    break;
            }

            int i = 1;
            for (String items : TaskLists) {
                ServiceDeployStages serviceDeployStages = new ServiceDeployStages();
                serviceDeployStages.setStageName(items);
                serviceDeployStages.setClusterId(serviceDeploy.getClusterId());
                serviceDeployStages.setStagePipeline(i);
                serviceDeployStages.setStageStatus(0);
                i = i + 1;
                if (serviceDeploySMapper.insert(serviceDeployStages) < 1) {
                    throw new Exception("无法写入阶段数据!");
                }
            }
            return serviceDeploy.getClusterId();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean ExecClusterDeploy(Long cid, String uuid) {
        // 先判断当前状态是否是待执行状态
        // 如果是，则不能继续重复
        ServiceDeploy serviceDeploy = clusterMapper.selectById(cid);
        if (serviceDeploy.getClusterServiceStatus() != 1) {
            return false;
        }
        QueryWrapper<ServiceDeploy> serviceDeployQueryWrapper = new QueryWrapper<>();
        serviceDeployQueryWrapper.eq("uuid", uuid);
        serviceDeployQueryWrapper.eq("cluster_id", cid);
        ServiceDeploy serviceDeploy1 = new ServiceDeploy();
        serviceDeploy1.setClusterServiceStatus(2);
        return clusterMapper.update(serviceDeploy1, serviceDeployQueryWrapper) > 0;
    }

    @Override
    public ArrayList<ServiceDeploy> getClusterCreateJobs() {
        // 获取部署中的任务，默认是在工具层做幂等，这个地方不做。
        QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_service_status", 2);
        // 抛开禁用的集群
        queryWrapper.eq("cluster_delete", 1);
        List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(queryWrapper);
        return new ArrayList<>(serviceDeployList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateStage(Integer stage, Long clusterId, Integer stageStatus) throws Exception {
        try {
            ServiceDeployStages serviceDeployStages = new ServiceDeployStages();
            serviceDeployStages.setStageStatus(stageStatus);
            QueryWrapper<ServiceDeployStages> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("stage_pipeline", stage);

            ServiceDeployStages thisStages = serviceDeploySMapper.selectOne(queryWrapper);
            // 如果已经完成就不再处理了
            if (thisStages.getStageStatus() != 2) {
                // 如果当前是0，则更新
                if (thisStages.getStageStatus() == 0) {
                    if (stageStatus == 1) {
                        serviceDeployStages.setStageCreatetime(new Date());
                    }
                } else {
                    serviceDeployStages.setStageUpdatetime(new Date());
                }
                if (serviceDeploySMapper.update(serviceDeployStages, queryWrapper) < 1) {
                    throw new Exception("更新阶段失败,集群id:" + clusterId.toString());
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateGitLabNameSpace(Long clusterId, String groupName, String groupDesc, Integer groupId) throws Exception {
        try {
            QueryWrapper<ServiceDeployGitlabs> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("git_group_name", groupName);
            if (serviceDeployGitlabsMapper.selectCount(queryWrapper) < 1) {
                ServiceDeployGitlabs serviceDeployGitlabs = new ServiceDeployGitlabs();
                serviceDeployGitlabs.setGitGroupCreatetime(new Date());
                serviceDeployGitlabs.setClusterId(clusterId);
                serviceDeployGitlabs.setGitGroupDesc(groupDesc);
                serviceDeployGitlabs.setGitGroupName(groupName);
                serviceDeployGitlabs.setGitGroupId(groupId);

                if (serviceDeployGitlabsMapper.insert(serviceDeployGitlabs) < 1) {
                    throw new Exception("创建Gitlab分组失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateClusterId(Long clusterId, String clusterInstanceId, String securityGroupId) throws Exception {
        try {
            ServiceDeploy serviceDeploy = new ServiceDeploy();
            serviceDeploy.setClusterUpdatetime(new Date());
            serviceDeploy.setClusterInstanceId(clusterInstanceId);
            serviceDeploy.setClusterSecurityId(securityGroupId);
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            if (clusterMapper.update(serviceDeploy, queryWrapper) < 1) {
                throw new Exception("集群状态更新失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateClusterIdStatus(Long clusterId, Integer status) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            ServiceDeploy serviceDeploy = new ServiceDeploy();
            serviceDeploy.setClusterUpdatetime(new Date());
            serviceDeploy.setClusterServiceStatus(status);
            if (status == 5) {
                serviceDeploy.setClusterDelete(0);
            }
            if (clusterMapper.update(serviceDeploy, queryWrapper) < 1) {
                throw new Exception("更新集群id状态失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateClusterFailedErrorMessage(Long clusterId, String content) throws Exception {
        try {
            ServiceDeployError serviceDeployError = new ServiceDeployError();
            serviceDeployError.setClusterErrorLog(content);
            serviceDeployError.setErrorCreatetime(new Date());
            serviceDeployError.setClusterId(clusterId);
            if (serviceDeployErrorMapper.insert(serviceDeployError) < 1) {
                throw new Exception(String.format("更新集群的构建异常信息失败, 集群ID: %s, 异常信息: %s", clusterId, content));
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ServiceDeployWorkConfig getClusterConf(Long clusterId, String clusterConfigTitle) throws Exception {
        ServiceDeployWorkConfig clusterConfig;
        try {
            QueryWrapper<ServiceDeployWorkConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_config_name", clusterConfigTitle);
            clusterConfig = serviceDeployConfigMapper.selectOne(queryWrapper);
            if (clusterConfig == null) {
                throw new Exception("获取配置失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return clusterConfig;
    }

    @Override
    public KubConfigYaml GetKubConfigMapService(Long engineerId, String nameSpace, String ModuleName) throws Exception {
        KubConfigYaml kubConfigYaml;
        try {
            QueryWrapper<KubConfigYaml> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);
            queryWrapper.eq("engineer_config_namespace", nameSpace);
            if (ModuleName != null) {
                queryWrapper.eq("engineer_module_name", ModuleName);
            }
            kubConfigYaml = kubConfigYamlMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return kubConfigYaml;
    }

    @Override
    public KubBaseConfig GetKubBaseConfig(Long engineerId, String nameSpace, String ModuleName) throws Exception {
        KubBaseConfig kubBaseConfig;
        try {
            QueryWrapper<KubBaseConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);
            queryWrapper.eq("engineer_config_namespace", nameSpace);
            if (ModuleName != null) {
                queryWrapper.eq("engineer_module_name", ModuleName);
            }
            kubBaseConfig = kubBaseMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return kubBaseConfig;
    }

    @Override
    public void SaveClusterMessage(Long clusterId, String address, String typeName, String userName, String passWord, String token) throws Exception {
        try {
            QueryWrapper<ServiceDeployWorkConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_config_name", typeName);
            if (serviceDeployConfigMapper.selectCount(queryWrapper) < 1) {
                ServiceDeployWorkConfig serviceDeployWorkConfig = new ServiceDeployWorkConfig();
                serviceDeployWorkConfig.setClusterId(clusterId);
                serviceDeployWorkConfig.setClusterConfigName(typeName);
                if (address != null) {
                    serviceDeployWorkConfig.setClusterConfigAddress(address);
                }
                if (userName != null) {
                    serviceDeployWorkConfig.setClusterConfigUsername(userName);
                }
                if (passWord != null) {
                    serviceDeployWorkConfig.setClusterConfigPassword(passWord);
                }
                if (token != null) {
                    serviceDeployWorkConfig.setClusterConfigToken(token);
                }
                if (typeName.equals("Gitlab仓库")) {
                    serviceDeployWorkConfig.setClusterConfigTypes(1);
                }
                serviceDeployWorkConfig.setClusterConfigCreatetime(new Date());
                if (serviceDeployConfigMapper.insert(serviceDeployWorkConfig) < 1) {
                    throw new Exception("保存配置信息失败:" + serviceDeployWorkConfig);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void ClusterCreateNoCloud(String uuid, String kubconfig, Integer proGram) throws Exception {
        try {
            ServiceDeploy serviceDeploy = new ServiceDeploy();
            serviceDeploy.setClusterCloudId(0);
            serviceDeploy.setClusterRegionId("null");
            serviceDeploy.setClusterZoneId("null");
            serviceDeploy.setUuid(uuid);
            switch (proGram) {
                case 1:
                    serviceDeploy.setClusterCurrent(1);
                    serviceDeploy.setClusterPayMode(1);
                    break;
                case 2:
                    serviceDeploy.setClusterCurrent(50);
                    serviceDeploy.setClusterPayMode(1);
                    break;

                case 3:
                    serviceDeploy.setClusterCurrent(250);
                    serviceDeploy.setClusterPayMode(1);
                    break;
            }
            serviceDeploy.setClusterDelete(1);
            serviceDeploy.setClusterType(1);
            serviceDeploy.setClusterServiceStatus(2);
            serviceDeploy.setClusterCreatetime(new Date());

            if (clusterMapper.insert(serviceDeploy) < 1) {
                throw new Exception("创建集群失败!");
            }

            List<String> TaskLists = new ArrayList<>();
            switch (proGram) {
                case 1:
                    TaskLists = Arrays.asList(
                            "部署GitLab", "部署Sonar",
                            "部署Yapi", "部署Nexus3",
                            "部署Jenkins", "部署Agent服务"
                    );
                    break;
                case 2:
                    TaskLists = Arrays.asList(
                            "部署GitLab", "部署Sonar",
                            "部署Yapi", "部署Nexus3",
                            "部署Jenkins", "部署Agent服务",
                            "部署Jumpserver", "部署Sentry",
                            "部署Skywalking", "部署Cobra"
                    );
                    break;
                case 3:
                    TaskLists = Arrays.asList(
                            "部署GitLab", "部署Sonar",
                            "部署Yapi", "部署Nexus3",
                            "部署Jenkins", "部署Agent服务",
                            "部署Jumpserver", "部署Sentry",
                            "部署Skywalking", "部署Cobra", "部署Promethus",
                            "部署Grafana"
                    );
                    break;
            }

            int i = 1;
            for (String items : TaskLists) {
                ServiceDeployStages serviceDeployStages = new ServiceDeployStages();
                serviceDeployStages.setStageName(items);
                serviceDeployStages.setClusterId(serviceDeploy.getClusterId());
                serviceDeployStages.setStagePipeline(i);
                serviceDeployStages.setStageStatus(0);
                i = i + 1;
                if (serviceDeploySMapper.insert(serviceDeployStages) < 1) {
                    throw new Exception("无法写入阶段数据!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
