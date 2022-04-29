package com.dolphin.saas.service;

import com.dolphin.saas.entity.ReleaseDebts;
import com.dolphin.saas.entity.ReleaseDeployStages;
import com.dolphin.saas.entity.ReleaseModule;
import com.dolphin.saas.entity.vo.EngneerLabelValueSelect;
import com.dolphin.saas.entity.vo.ReleaseExecItems;
import com.dolphin.saas.entity.vo.ReleaseItems;
import com.dolphin.saas.entity.vo.ReleaseStagesLists;
import com.dolphin.saas.inputs.CreateReleaseInputs;
import com.dolphin.saas.searchs.ReleaseSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DeployService {
    // 取消发布 -- 前台API
    void DropRelease(String uuid, Integer releaseId) throws Exception;

    // 获取发布的债务详情列表 -- 前台API
    Map<String, Object> FindReleaseDetDetials(String uuid, Integer releaseId, String toolsName, int Pages, int Size) throws Exception;

    // 获取可以发布的项目 -- 前台API
    ArrayList<EngneerLabelValueSelect> getReleaseService(String uuid) throws Exception;

    // 创建发布的项目 -- 前台API
    Map<String, Object> createRelease(String uuid, CreateReleaseInputs createReleaseInputs) throws Exception;

    // 查看发布项目的列表 -- 前台API
    Map<String, Object> FindReleaseJobLists(int Page, int Size, String uuid, ReleaseSearch releaseSearch);

    // 查看现在是否有发布项目 -- 前台API
    Boolean checkReleaseJobs(String uuid);

    // 读取发布信息 -- 前台API
    ReleaseItems ReadRelease(int releaseId, String uuid) throws Exception;

    // 获取发布的版本列表 -- 前台API
    ArrayList<Map<String, Object>> ReleaseVerOptions(Integer releaseId, String uuid);

    // 执行发布 -- 前台API
    void ReleaseExecute(Long releaseId, int status, String version, String uuid) throws Exception;

    // 查看发布流程图 -- 前台API
    ArrayList<ReleaseStagesLists> ReleaseStages(Long releaseId, String uuid) throws Exception;

    // 根据项目名查分支列表和MR信息 -- 前台API
    Map<String, Object> getEngineerBranch(String uuid, Long engineerId) throws Exception;

    // 获取所有待发布/回滚的任务 -- 工具API
    ArrayList<ReleaseExecItems> getReleaseLists() throws Exception;

    // 根据云厂商获得集群id和信息
    ArrayList<Map<String, Object>> getcloudClusterId(String uuid);

    // 根据集群id获取namespace信息
    ArrayList<Map<String, Object>> getclusterIdNameSpace();

    // 查看发布债务列表 -- 前台API
    ArrayList ReleaseDetes(Long releaseId, String uuid) throws Exception;

    // 插入工程债务 -- 工具API
    void uploadDebts(ArrayList<ReleaseDebts> releaseDebtsArrayList) throws Exception;

    // 更新发布任务状态 -- 工具API
    void updateReleaseStatus(Long releaseId, int status) throws Exception;

    // 查看任务的执行进度 -- 工具API
    ArrayList<ReleaseStagesLists> getProcessLists(Long releaseId) throws Exception;

    // 更新发布阶段状态 -- 工具API
    void updateReleaseStageStatus(Long releaseId, Long stageStatus, int status) throws Exception;

    // 查对应阶段的ID，进行状态更新 -- 工具API
    void updateReleaseStageToolsStatus(Long releaseId, String stageName, int status) throws Exception;

    // 获取发布的模块(用于Java模块化发布) -- 工具APi
    ReleaseModule findReleaseModule(Long releaseId) throws Exception;

    // 获取判断是否只剩下业务上线发布这个进度 -- 工具APi
    Boolean checkReleaseLast(Long releaseId) throws Exception;

    // 更新发布日志 -- 工具API
    void updateReleaseConsoleLog(Long releaseId, String consoleLog) throws Exception;

    // 获取dockerfile地址 -- 工具API
    String getDockerfileURL(Integer dockerfileId) throws Exception;

    // 获取发布阶段和状态 -- 工具API
    List<ReleaseDeployStages> releaseStageStatus(Long releaseId) throws Exception;
}

