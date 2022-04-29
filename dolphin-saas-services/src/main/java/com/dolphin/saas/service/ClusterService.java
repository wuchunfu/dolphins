package com.dolphin.saas.service;

import com.dolphin.saas.entity.*;
import com.dolphin.saas.searchs.ClusterSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ClusterService {
    // 根据工程ID查集群可用发布次数 -- 前台API
    Map<String, Long> GetEngineerClusterDeploy(String uuid, Long engineerId) throws Exception;

    // 获取当前用户uuid下有多个可用集群 -- 前台API
    Map<String, Integer> GetClusterAlive(String uuid) throws Exception;

    // 根据集群ID和uuid获取集群的AK、安全组ID、归属区域 -- 前台API
    Map<String, String> GetClusterMessage(Long cid, String uuid) throws Exception;

    // 删除集群 -- 前台API
    void DeleteCluster(Long cid, String uuid) throws Exception;

    // 检查是否有集群存活 -- 前台API
    Boolean CheckClusterStatus(String uuid);

    // 集群列表 -- 前台API
    Map<String, Object> FindClusterLists(int Page, int Size, String uuid, ClusterSearch clusterSearch) throws Exception;

    // 读取集群信息 -- 前台API
    Map<String, Object> ReadCluster(Long cid, String uuid) throws Exception;

    // 查部署阶段 -- 前台API
    Map<String, Object> ReadClusterStages(Long cid, String uuid) throws Exception;

    // 集群的创建信息读取 -- 前台API
    ArrayList<ServiceDeployWorkConfig> ReadClusterMessage(Long clusterId, Integer types, String uuid) throws Exception;

    // 集群创建 -- 前台API
    Long ClusterCreate(Integer current, Integer cloudId, String regionId, String zoneId, Integer clusterType, String uuid, Integer payMode) throws Exception;

    // 执行部署 -- 前台API
    Boolean ExecClusterDeploy(Long cid, String uuid);

    // 获取所有待发布的集群 -- 工具API
    ArrayList<ServiceDeploy> getClusterCreateJobs();

    // 更新部署阶段的状态 -- 工具API
    void UpdateStage(Integer stage, Long clusterId, Integer stageStatus) throws Exception;

    // 更新集群的关联git的namespace信息 -- 工具API
    void UpdateGitLabNameSpace(Long clusterId, String groupName, String groupDesc, Integer groupId) throws Exception;

    // 更新部署成功的集群id、安全组ID -- 工具API
    void UpdateClusterId(Long clusterId, String clusterInstanceId, String securityGroupId) throws Exception;

    // 更新整个集群状态 -- 工具API
    void UpdateClusterIdStatus(Long clusterId, Integer status) throws Exception;

    // 更新这个集群构建失败的原因 -- 工具API
    void UpdateClusterFailedErrorMessage(Long clusterId, String content) throws Exception;

    // 获取集群对应的配置信息 -- 工具API
    ServiceDeployWorkConfig getClusterConf(Long clusterId, String clusterConfigTitle) throws Exception;

    // 获取集群configmap和service配置 -- 工具API
    KubConfigYaml GetKubConfigMapService(Long engineerId, String nameSpace,  String ModuleName) throws Exception;

    // 获取集群配置(综合配置) -- 工具API
    KubBaseConfig GetKubBaseConfig(Long engineerId, String nameSpace, String ModuleName) throws Exception;

    // 保存信息 -- 工具API
    void SaveClusterMessage(Long clusterId, String address,  String typeName, String userName, String passWord, String token) throws Exception;

    // 创建集群 -- PaaS专用 -- API
    void ClusterCreateNoCloud(String uuid, String kubconfig, Integer proGram) throws Exception;
}
