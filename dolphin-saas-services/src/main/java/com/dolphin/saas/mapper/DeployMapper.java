package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.Release;
import com.dolphin.saas.entity.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public interface DeployMapper extends BaseMapper<Release> {

    /**
     * 获取待发布工程的发布版本&语言&工程名
     */
    @Select("SELECT ht_release_jobs.release_id, ht_release_jobs.release_version, ht_engineer_language.language_name, ht_engineer.engineer_name, ht_release_jobs.release_job_branch FROM ht_release_jobs LEFT JOIN ht_engineer ON ht_engineer.engineer_id = ht_release_jobs.release_engineer_id INNER JOIN ht_engineer_language ON ht_engineer_language.language_id = ht_engineer.engineer_language_id ${ew.customSqlSegment}")
    ImageScanLists selectAllImageStageRelease(@Param(Constants.WRAPPER) Wrapper<Release> queryWrapper);

    /**
     * 获取所有待执行发布的任务
     */
    @Select("SELECT release_id, release_version, release_engineer_id, release_job_status, release_job_branch, release_job_cluster_id, release_job_rollback, release_job_namespace, ht_release_jobs.uuid uuid, engineer_name, engineer_dockerfile_id, engineer_giturl, rules_type, git_group_name FROM ht_release_jobs left JOIN ht_engineer ON ht_engineer.engineer_id = ht_release_jobs.release_engineer_id RIGHT JOIN ht_release_rules_group ON ht_release_rules_group.rid = ht_engineer.engineer_release_rules_id INNER JOIN ht_service_deploy_gitlabs ON ht_service_deploy_gitlabs.cluster_id = ht_release_jobs.release_job_cluster_id AND ht_service_deploy_gitlabs.git_group_id = ht_engineer.engineer_git_group_id WHERE ht_release_jobs.release_job_status IN(1, 3, 5)")
    ArrayList<ReleaseExecItems> selectAllReleaseExecLists();

    /**
     * 获取所有待执行发布的任务
     */
    @Select("SELECT release_id, release_version, release_job_namespace, release_job_branch, release_job_createtime, release_job_status, engineer_name release_job_name, CONCAT(t_name,'/',cluster_instance_id,'/',cluster_region_id) release_job_cloud_id FROM ht_release_jobs LEFT JOIN ht_service_deploy ON ht_release_jobs.release_job_cluster_id = ht_service_deploy.cluster_id INNER JOIN ht_vendors_types ON ht_service_deploy.cluster_cloud_id = ht_vendors_types.tid INNER JOIN ht_engineer ON ht_engineer.engineer_id = ht_release_jobs.release_engineer_id ${ew.customSqlSegment}")
    IPage<ReleaseLists> selectMyPage(IPage<ReleaseLists> page, @Param(Constants.WRAPPER) Wrapper<ReleaseLists> queryWrapper);

    @Select("SELECT release_job_updatetime, release_version FROM ht_release_jobs LEFT JOIN ht_release_module ON ht_release_jobs.release_id = ht_release_module.release_id ${ew.customSqlSegment}")
    ArrayList<ReleaseCallbackLists> releaseBack(@Param(Constants.WRAPPER) Wrapper<ReleaseCallbackLists> queryWrapper);

    /**
     * 获取发布的单条明细
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT release_id, release_job_createtime, release_content, release_job_updatetime, engineer_name, release_version , release_job_status, release_job_branch, release_job_namespace, release_commit_author_createtime, release_commit_author_name , release_commit_id, cluster_name, cluster_instance_id, cluster_region_id, cluster_zone_id, t_name release_cloud_name FROM ht_release_jobs LEFT JOIN ht_service_deploy ON ht_service_deploy.cluster_id = ht_release_jobs.release_job_cluster_id INNER JOIN ht_vendors_types ON ht_vendors_types.tid = ht_service_deploy.cluster_cloud_id INNER JOIN ht_engineer ON ht_engineer.engineer_id = ht_release_jobs.release_engineer_id ${ew.customSqlSegment}")
    ReleaseItems releaseObject(@Param(Constants.WRAPPER) Wrapper<ReleaseItems> queryWrapper);

    /**
     * 查询发布的状态阶段
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT release_status_id stage_id, release_status_name stage_name, release_status_icon, CASE release_status_stages WHEN 0 THEN 'standby' WHEN 1 THEN 'running' WHEN 2 THEN 'success' WHEN 3 THEN 'warning' ELSE 'error' END stage_status FROM ht_release_deploy_stages ${ew.customSqlSegment}")
    ArrayList<ReleaseStagesLists> releaseStagesListsObject(@Param(Constants.WRAPPER) Wrapper<ReleaseStagesLists> queryWrapper);

    /**
     * 获取查询的统计内容
     *
     * @param queryWrapper
     * @return
     */
    @Select("select engineer_name name, release_job_createtime time, release_job_status status from ht_release_jobs LEFT JOIN ht_engineer ON ht_engineer.engineer_id = ht_release_jobs.release_engineer_id ${ew.customSqlSegment}")
    ArrayList<DashboardLists> releaseCountArr(@Param(Constants.WRAPPER) Wrapper<Release> queryWrapper);

    /**
     * 发布统计折线图数据
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT DATE_FORMAT(release_job_createtime, '%Y-%m-%d') c_days, count(*) counts FROM ht_release_jobs ${ew.customSqlSegment}")
    ArrayList<DashboardEcharts> releaseCharts(@Param(Constants.WRAPPER) Wrapper<Release> queryWrapper);
}
