package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.ServiceDeploy;
import com.dolphin.saas.entity.vo.ReleaseLists;
import com.dolphin.saas.entity.vo.RulesInfo;
import com.dolphin.saas.entity.vo.ServiceDeployLists;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterMapper extends BaseMapper<ServiceDeploy> {
    /**
     * 获取发布集群数据
     * @param queryWrapper
     * @return
     */
    @Select("SELECT t_name cluster_cloud_id, cluster_createtime, cluster_current, cluster_id, cluster_name, cluster_instance_id, cluster_region_id, cluster_service_status, CASE ht_service_deploy.cluster_type WHEN 0 THEN '按需付费' ELSE '固定付费' END cluster_type, cluster_updatetime, cluster_zone_id, ht_service_deploy.uuid uuid FROM ht_service_deploy LEFT JOIN ht_vendors_types ON ht_service_deploy.cluster_cloud_id = ht_vendors_types.tid  ${ew.customSqlSegment}")
    IPage<ServiceDeployLists> findAllClusterLists(IPage<ServiceDeployLists> page, @Param(Constants.WRAPPER) Wrapper<ServiceDeployLists> queryWrapper);

    /**
     * 获取发布集群的基础信息
     * @param queryWrapper
     * @return
     */
    @Select("SELECT t_name cluster_cloud_id, cluster_createtime, cluster_current, cluster_id, cluster_name, cluster_instance_id, cluster_region_id, cluster_service_status, CASE ht_service_deploy.cluster_type WHEN 0 THEN '按需付费' ELSE '固定付费' END cluster_type, cluster_updatetime, cluster_zone_id, ht_service_deploy.uuid uuid FROM ht_service_deploy LEFT JOIN ht_vendors_types ON ht_service_deploy.cluster_cloud_id = ht_vendors_types.tid  ${ew.customSqlSegment}")
    ServiceDeployLists findClusterItems(@Param(Constants.WRAPPER) Wrapper<ServiceDeploy> queryWrapper);
}
