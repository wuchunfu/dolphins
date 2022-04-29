package com.dolphin.saas.service;


import com.dolphin.saas.entity.AnalyzeFileLine;
import com.dolphin.saas.entity.Engineer;
import com.dolphin.saas.entity.EngineerAnalyze;
import com.dolphin.saas.entity.vo.ReleaseExecItems;
import com.dolphin.saas.entity.vo.SelectVal;
import com.dolphin.saas.inputs.KubConfigInputs;
import com.dolphin.saas.searchs.EngineerSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface EngineeringService {
    // 根据工程查质量情况明细 -- 前台API
    Map<String, Object> GitEngineerQuality(Long engineerId, String uuid, Integer Page, Integer Size) throws Exception;
    // 质量分析数据统计，环形图 -- 前台API
    Map<String, Object> GitAnalyzeQuality(Long engineerId, String uuid) throws Exception;

    // 查看工程对应的代码分类组成 -- 前台接口API
    List<AnalyzeFileLine> GitAnalyzeFileLineLists(Long engineerId, String uuid) throws Exception;

    // 修改K8S配置 -- 前台接口API
    void changeEngineerKubConfig(String uuid, Long id, String namespace, KubConfigInputs kubConfigInputs, String moduleName, String v1configmap, String v1service, String certCrtInfo, String certKeyInfo, String nginxConf, Integer ready) throws Exception;

    // 获取项目的配置，没有就创建然后读取 -- 前台接口API
    Map<String, Object> getEngineerKubConfig(String uuid, Long id, String namespace, String moduleName, String v1configmap, String v1service) throws Exception;

    // 创建新的工程 -- 前台接口API
    void createEnginner(Engineer engineer, String uuid) throws Exception;

    // 删除/隐藏工程 -- 前台接口API
    void deleteEnginner(Engineer engineer, String uuid) throws Exception;

    // 获取gitlab的namespace接口
    ArrayList GitlabNamespaceOptions(String uuid) throws Exception;

    // 查看工程的列表
    Map<String, Object> FindEnginnerJobLists(int Page, int Size, String uuid, EngineerSearch engineerSearch);

    // 查看现在是否有工程在仓库里
    Boolean checkEnginnerJobs(String uuid);

    // 读取工程信息
    Map<String, Object> ReadEnginner(Long engineerId, String uuid);

    // 获取dockerfile的id和信息（列表）
    ArrayList DockerfileOptions(Engineer engineer, String uuid) throws Exception;

    // 获取发布规则的（列表）
    ArrayList<Map<String, Object>> ReleaseRulesOptions(String uuid) throws Exception;

    // 获取开发语言的下拉
    ArrayList getLanguageOptions();

    // 获取开发框架的下拉
    ArrayList getFrameWorkOptions(int LanguageId);

    // 查看工程发布列表 -- 前台API
    Map<String, Object> ReadReleaseLists(Integer engineerId, String uuid, Integer Page, Integer Size, int[] status) throws Exception;

    // 获取工程的列表下拉
    ArrayList getReleaseEngineerOptions(String uuid);

    // 更新工程的创建状态（工具端）
    void updateJobsStatus(Long engineerId, Integer engineerCreateJobStatus) throws Exception;

    // 更新工程信息（工具端）
    void UpdateJobsUrl(Engineer engineer) throws Exception;

    // 获取工程列表（工具端）
    ArrayList<Map<String, Object>> FindEnginnerAllJobs() throws Exception;

    // 获取工程信息（工程名称、工程ID、工程仓库地址、工程commitID） -- 工具端API
    ArrayList<Map<String, Object>> FindEngineerInfo() throws Exception;

    // 更新工程commitID还有分析的结果入库 -- 工具端API
    void saveGitAnalyzeRecords(Long engineerId, Map<String, Object> results) throws Exception;

    // 获取有集群的发布云厂商列表
    ArrayList VentorTypeOptions(String uuid);

    // 写入工程分析结果 -- 工具API
    void pushAnalyzeResults(ReleaseExecItems releaseExecItems, Map<String, Object> engineerAnalyze, String ToolsName) throws Exception;

    // 获取所有dockerfile列表 -- 后台API
    Map<String, Object> FindDockerfileLists(Integer page, Integer size) throws Exception;

    // 判断工程是否是前端项目 -- 前台API
    Boolean CheckFrontEndAlive(Long engineerId) throws Exception;
}
