package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.*;
import com.dolphin.saas.inputs.CreateReleaseInputs;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.searchs.ReleaseSearch;
import com.dolphin.saas.service.DeployService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service("deployService")
public class DeployServiceimpl extends BaseTools implements DeployService {

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private ReleaseStragesMapper releaseStragesMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private ReleaseDetsMapper releaseDetsMapper;

    @Resource
    private EngineeringMapper engineeringMapper;

    @Resource
    private RulesGroupMapper rulesGroupMapper;

    @Resource
    private ReleaseModuleMapper releaseModuleMapper;

    @Resource
    private ServiceDeployConfigMapper serviceDeployConfigMapper;

    @Resource
    private EngineerAnalyzeMapper engineerAnalyzeMapper;

    @Resource
    private VendorsTypeMapper vendorsTypeMapper;

    @Resource
    private DockerfileMapper dockerfileMapper;

    @Override
    public ArrayList ReleaseDetes(Long releaseId, String uuid) throws Exception {
        ArrayList results = new ArrayList();
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            // 获取所有关联的UUID
            ArrayList<String> uuidOrgLists = this.orgUUidList(uuid);

            if (uuidOrgLists.size() != 0) {
                queryWrapper.in("uuid", uuidOrgLists);
                queryWrapper.eq("release_id", releaseId);
                if (deployMapper.selectCount(queryWrapper) < 1) {
                    throw new Exception("请勿非法操作，该项目债务不存在!");
                }
                // 获取债务信息
                QueryWrapper<ReleaseDebts> releaseDebtsQueryWrapper = new QueryWrapper<>();
                releaseDebtsQueryWrapper.eq("release_id", releaseId);
                results = new ArrayList<>(releaseDetsMapper.selectList(releaseDebtsQueryWrapper));
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadDebts(ArrayList<ReleaseDebts> releaseDebtsArrayList) throws Exception {
        try {
            for (ReleaseDebts releaseDebtItems : releaseDebtsArrayList) {
                if (releaseDetsMapper.insert(releaseDebtItems) < 1) {
                    throw new Exception(String.format("写入债务失败: %s", releaseDebtItems.toString()));
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReleaseStatus(Long releaseId, int status) throws Exception {
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            if (deployMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("没有这个发布的id");
            }

            Release release = new Release();
            release.setReleaseJobStatus(status);
            release.setReleaseJobUpdatetime(new Date());
            if (deployMapper.update(release, queryWrapper) < 1) {
                throw new Exception("更新发布状态失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList<ReleaseStagesLists> getProcessLists(Long releaseId) throws Exception {
        // 查看进度详情
        QueryWrapper<ReleaseStagesLists> releaseDStagQueryWrapper = new QueryWrapper<>();
        releaseDStagQueryWrapper.eq("release_id", releaseId);
        return deployMapper.releaseStagesListsObject(releaseDStagQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReleaseStageStatus(Long releaseId, Long stageStatus, int status) throws Exception {
        try {
            QueryWrapper<ReleaseDeployStages> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            queryWrapper.eq("release_status_id", stageStatus);

            ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
            releaseDeployStages.setReleaseStatusStages(status);
            if (releaseStragesMapper.update(releaseDeployStages, queryWrapper) < 1) {
                throw new Exception("执行更新发布节点失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void updateReleaseStageToolsStatus(Long releaseId, String stageName, int status) throws Exception {
        try {
            QueryWrapper<ReleaseDeployStages> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_status_name", stageName);
            queryWrapper.eq("release_id", releaseId);

            ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
            releaseDeployStages.setReleaseStagesUpdatetime(new Date());
            releaseDeployStages.setReleaseStatusStages(status);

            if (releaseStragesMapper.update(releaseDeployStages, queryWrapper) < 1) {
                throw new Exception("执行更新发布节点失败!");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ReleaseModule findReleaseModule(Long releaseId) throws Exception {
        ReleaseModule releaseModule = null;
        try {
            QueryWrapper<ReleaseModule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            releaseModule = releaseModuleMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return releaseModule;
    }

    @Override
    public Boolean checkReleaseLast(Long releaseId) throws Exception {
        try{
            QueryWrapper<ReleaseDeployStages> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);

            List<ReleaseDeployStages> releaseDeployStagesList = releaseStragesMapper.selectList(queryWrapper);
            ArrayList<ReleaseDeployStages> checkLists = new ArrayList<>();

            int running = 0;
            int error = 0;
            int standby = 0;
            for (ReleaseDeployStages releaseDeployStages: releaseDeployStagesList){
                if (releaseDeployStages.getReleaseStatusStages() == 1){
                    running = running + 1;
                }else if (releaseDeployStages.getReleaseStatusStages() > 2){
                    error = error + 1;
                }else if (releaseDeployStages.getReleaseStatusStages() == 0){
                    standby = standby + 1;
                    checkLists.add(releaseDeployStages);
                }
            }

            // 有正在运行的，肯定没结束
            if (running > 0){
                return false;
            }

            // 如果有失败的，直接就结束了
            if (error > 0){
                throw new Exception("执行服务发布工具部分出错了,程序异常!");
            }

            if (standby == 1){
                // 判断是否等于
                if (checkLists.get(0).getReleaseStatusName().equals("业务上线发布")){
                    return true;
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return false;
    }

    @Override
    public void updateReleaseConsoleLog(Long releaseId, String consoleLog) throws Exception {
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);

            Release release = new Release();
            release.setReleaseContent(consoleLog);

            if (deployMapper.update(release, queryWrapper) < 1) {
                throw new Exception(String.format("更新发布日志数据失败, ID: %s", releaseId));
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getDockerfileURL(Integer dockerfileId) throws Exception {
        String dockerfileUrl;
        try {
            QueryWrapper<EngineerDockerfile> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dockerfile_id", dockerfileId);
            if (dockerfileMapper.selectCount(queryWrapper) < 1){
                throw new Exception("dockerfile不存在!");
            }
            dockerfileUrl = dockerfileMapper.selectOne(queryWrapper).getDockerfileOsPath();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return dockerfileUrl;
    }

    @Override
    public List<ReleaseDeployStages> releaseStageStatus(Long releaseId) throws Exception {
        List<ReleaseDeployStages> releaseDeployStagesList;
        try{
            QueryWrapper<ReleaseDeployStages> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            releaseDeployStagesList = releaseStragesMapper.selectList(queryWrapper);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return releaseDeployStagesList;
    }

    @Override
    public ArrayList<Map<String, Object>> getcloudClusterId(String uuid) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        QueryWrapper<ServiceDeploy> serviceDeployQueryWrapper = new QueryWrapper<>();
        serviceDeployQueryWrapper.eq("cluster_service_status", 3);
        serviceDeployQueryWrapper.in("uuid", this.orgUUidList(uuid));
        List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(serviceDeployQueryWrapper);

        if (serviceDeployList.size() > 0) {
            for (ServiceDeploy deploy : serviceDeployList) {
                Map<String, Object> items = new HashMap<>();
                QueryWrapper<VendorsType> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("tid", deploy.getClusterCloudId());
                queryWrapper.select("t_name");
                VendorsType vendorsType = vendorsTypeMapper.selectOne(queryWrapper);
                items.put("label", vendorsType.getTName() + "/" + deploy.getClusterInstanceId());
                items.put("value", deploy.getClusterId());
                if (deploy.getClusterInstanceId() != null) {
                    results.add(items);
                }
            }
        }

        return results;
    }

    @Override
    public ArrayList<Map<String, Object>> getclusterIdNameSpace() {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        for (String nameSpace : new ArrayList<>(Arrays.asList("test", "dev", "demos", "online", "devops"))) {
            Map<String, Object> items = new HashMap<>();
            items.put("label", nameSpace);
            items.put("value", nameSpace);
            results.add(items);
        }
        return results;
    }

    @Override
    public void DropRelease(String uuid, Integer releaseId) throws Exception {
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("release_id", releaseId);

            Release release = deployMapper.selectOne(queryWrapper);
            if (release == null) {
                throw new Exception("获取发布失败!");
            }
            if (release.getReleaseJobStatus() != 0 && release.getReleaseJobStatus() != 2){
                throw new Exception("状态不对，无法取消!");
            }

            Release release1 = new Release();
            release1.setReleaseJobUpdatetime(new Date());
            release1.setReleaseJobStatus(8);

            if (deployMapper.update(release1, queryWrapper) < 1){
                throw new Exception("取消发布失败!");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> FindReleaseDetDetials(String uuid, Integer releaseId, String toolsName, int Pages, int Size) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            IPage<EngineerAnalyze> page = new Page<>(Pages, Size);
            // 先查发布归属的的工程
            QueryWrapper<Release> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.in("uuid", this.orgUUidList(uuid));
            queryWrapper2.eq("release_id", releaseId);
            if (deployMapper.selectCount(queryWrapper2) < 1) {
                throw new Exception("没有查看这个债务的权利!");
            }

            Release release = deployMapper.selectOne(queryWrapper2);

            // 工具工程ID、分支、发布的版本发布Id去查债务
            QueryWrapper<EngineerAnalyze> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", release.getReleaseEngineerId());
            queryWrapper.eq("enginner_version", release.getReleaseVersion());
            queryWrapper.eq("engineer_branch", release.getReleaseJobBranch());
            if (toolsName != null) {
                queryWrapper.eq("engineer_tools", toolsName);
            }
            engineerAnalyzeMapper.selectPage(page, queryWrapper);
            results.put("page", Pages);
            results.put("total", page.getTotal());

            results.put("list", page.getRecords());
            results.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public ArrayList<EngneerLabelValueSelect> getReleaseService(String uuid) throws Exception {
        ArrayList<EngneerLabelValueSelect> results = new ArrayList<>();
        try {
            ArrayList<String> uuidOrgLists = this.orgUUidList(uuid);

            if (uuidOrgLists.size() > 0) {
                QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("ht_engineer.uuid", uuidOrgLists);
                queryWrapper.eq("engineer_status", 2);
                results = engineeringMapper.engineerSelectVals(queryWrapper);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createRelease(String uuid, CreateReleaseInputs createReleaseInputs) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000+08:00");

            // 找到工程对应的集群，把集群ID查出来,不用传了
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", createReleaseInputs.getEngineerId());
            queryWrapper.eq("uuid", uuid);
            Engineer engineer = engineeringMapper.selectOne(queryWrapper);
            if (engineer == null){
                throw new Exception("工程不存在，或者没有权限!");
            }

            Release release = new Release();
            // 构建版本号
            release.setReleaseVersion(this.refDate());
            // 基础参数
            release.setReleaseJobCreatetime(new Date());
            release.setReleaseJobStatus(0);
            release.setUuid(uuid);
            release.setReleaseJobBranch(createReleaseInputs.getBranchName());
            release.setReleaseJobClusterId(engineer.getClusterId());
            release.setReleaseEngineerId(createReleaseInputs.getEngineerId());
            release.setReleaseJobNamespace(createReleaseInputs.getNameSpace());
            release.setReleaseCommitTime(sdf.parse(createReleaseInputs.getCommitTime()));
            release.setReleaseCommitAuthorName(createReleaseInputs.getAuthorName());
            release.setReleaseCommitId(createReleaseInputs.getCommitId());
            release.setReleaseContent("发布准备中...");

            // 创建发布
            if (deployMapper.insert(release) < 1) {
                throw new Exception("创建发布失败!");
            }

            // 插入发布模块
            if (createReleaseInputs.getJavaMoule() != null && !createReleaseInputs.getJavaMoule().trim().equals("")) {
                QueryWrapper<Engineer> engineerQueryWrapper1 = new QueryWrapper<>();
                engineerQueryWrapper1.eq("ht_engineer.engineer_id", createReleaseInputs.getEngineerId());
                EngineerLanguage engineerLanguage = releaseModuleMapper.FindEnginnerLanguage(engineerQueryWrapper1);
                if (engineerLanguage != null) {
                    if (engineerLanguage.getLanguageName().equals("Java")) {
                        ReleaseModule releaseModule = new ReleaseModule();
                        releaseModule.setReleaseId(release.getReleaseId());
                        releaseModule.setReleaseModule(createReleaseInputs.getJavaMoule());
                        if (releaseModuleMapper.insert(releaseModule) < 1) {
                            throw new Exception("插入发布模块失败!");
                        }
                    }
                }
            }

            // 找到规则对应的数据
            QueryWrapper<RulesInfo> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("rid", engineer.getEngineerReleaseRulesId());
            queryWrapper1.orderByAsc("rules_info_sort");
            List<RulesInfo> rulesInfoList = rulesGroupMapper.rulesObject(queryWrapper1);

            if (rulesInfoList.size() < 1) {
                throw new Exception("获取发布策略详情失败!");
            }

            // 插入发布策略
            for (RulesInfo rulesInfo : rulesInfoList) {
                ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
                releaseDeployStages.setReleaseId(release.getReleaseId());
                releaseDeployStages.setReleaseStatusName(rulesInfo.getRulesInfoTitle());
                releaseDeployStages.setReleaseStatusId(rulesInfo.getRulesInfoSort());
                if (releaseStragesMapper.insert(releaseDeployStages) < 1) {
                    throw new Exception("插入发布阶段失败!");
                }
            }
            response.put("releaseId", release.getReleaseId());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public Map<String, Object> FindReleaseJobLists(int Pages, int Size, String uuid, ReleaseSearch releaseSearch) {
        // 获取分页的数据
        Map<String, Object> results = new HashMap<>();
        IPage<ReleaseLists> page = new Page<>(Pages, Size);
        List<ReleaseLists> releaseList = new ArrayList<>();
        QueryWrapper<ReleaseLists> queryWrapper = new QueryWrapper<>();

        // 获取所有关联的UUID
        ArrayList<String> uuidOrgLists = this.orgUUidList(uuid);

        if (uuidOrgLists.size() != 0) {
            if (releaseSearch != null) {
                // 工程名称查询
                if (releaseSearch.getReleaseJobName() != null) {
                    queryWrapper.like("ht_release_jobs.release_job_name", releaseSearch.getReleaseJobName());
                }

                // 时间查询
                if (releaseSearch.getReleaseJobCreatetime() != null && releaseSearch.getReleaseJobUpdatetime() != null) {
                    queryWrapper.between("ht_release_jobs.release_job_name", releaseSearch.getReleaseJobCreatetime(), releaseSearch.getReleaseJobUpdatetime());
                }

                // 版本号查询
                if (releaseSearch.getReleaseVersion() != null) {
                    queryWrapper.like("release_version", releaseSearch.getReleaseVersion());
                }

                // 状态筛选
                if (releaseSearch.getReleaseJobStatus() != null && releaseSearch.getReleaseJobStatus().length > 0){
                    ArrayList<Integer> ints = new ArrayList<>();
                    for (int i:releaseSearch.getReleaseJobStatus()){
                        ints.add(i);
                    }
                    queryWrapper.in("release_job_status", ints);
                }
            }

            queryWrapper.in("ht_release_jobs.uuid", uuidOrgLists);
            queryWrapper.orderByDesc("release_job_createtime");
            deployMapper.selectMyPage(page, queryWrapper);
            results.put("page", Pages);
            results.put("total", page.getTotal());
            releaseList = page.getRecords();
        }

        results.put("list", releaseList);
        results.put("pageSize", Size);
        return results;
    }

    @Override
    public Boolean checkReleaseJobs(String uuid) {
        QueryWrapper<Release> queryWrapper = new QueryWrapper<>();

        // 获取所有关联的UUID
        ArrayList<String> uuidOrgLists = this.orgUUidList(uuid);

        if (uuidOrgLists.size() != 0) {
            queryWrapper.in("uuid", uuidOrgLists);
        }
        return deployMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public ReleaseItems ReadRelease(int releaseId, String uuid) throws Exception {
        ReleaseItems releaseItems;
        try {
            QueryWrapper<ReleaseItems> releaseQueryWrapper = new QueryWrapper<>();
            releaseQueryWrapper.eq("release_id", releaseId);
            releaseQueryWrapper.eq("ht_release_jobs.uuid", uuid);

            releaseItems = deployMapper.releaseObject(releaseQueryWrapper);

            // 查询模块字段
            QueryWrapper<ReleaseModule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            ReleaseModule releaseModule = releaseModuleMapper.selectOne(queryWrapper);
            if (releaseModule != null) {
                releaseItems.setReleaseModule(releaseModule.getReleaseModule());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return releaseItems;
    }

    @Override
    public ArrayList<Map<String, Object>> ReleaseVerOptions(Integer releaseId, String uuid) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        // 根据发布的ID查发布信息
        QueryWrapper<Release> releaseQueryWrapper = new QueryWrapper<>();
        releaseQueryWrapper.eq("release_id", releaseId);
        releaseQueryWrapper.eq("uuid", uuid);
        releaseQueryWrapper.select("release_engineer_id", "release_job_namespace", "release_job_cluster_id");
        Release release = deployMapper.selectOne(releaseQueryWrapper);

        if (release != null) {
            // 判断是否是模块发布
            QueryWrapper<ReleaseModule> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("release_id", releaseId);
            ReleaseModule releaseModule = releaseModuleMapper.selectOne(queryWrapper);
            QueryWrapper<ReleaseCallbackLists> queryWrapper1 = new QueryWrapper<>();
            if (releaseModule != null) {
                // 如果是模块，则以模块进入查询
                queryWrapper1.eq("release_module", releaseModule.getReleaseModule());
            }
            // 不是模块则以ID为主查询
            queryWrapper1.eq("release_job_namespace", release.getReleaseJobNamespace());
            queryWrapper1.eq("release_job_status", 4);
            queryWrapper1.eq("release_job_cluster_id", release.getReleaseJobClusterId());
            queryWrapper1.eq("release_engineer_id", release.getReleaseEngineerId());
            queryWrapper1.orderByDesc("release_job_updatetime");
            queryWrapper1.last("limit 5");

            List<ReleaseCallbackLists> releaseCallbackLists = deployMapper.releaseBack(queryWrapper1);
            if (releaseCallbackLists.size() > 0) {
                // 替换namespace的名字
                for (ReleaseCallbackLists release1 : releaseCallbackLists) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("updatetime", release1.getReleaseJobUpdatetime());
                    items.put("version", release1.getReleaseVersion());
                    results.add(items);
                }
            }
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ReleaseExecute(Long releaseId, int status, String version, String uuid) throws Exception {
        try {
            // 先查任务状态是否是正常待发布状态
            // 只有待发布才可以发布
            // 只有已发布才可以回滚
            switch (status) {
                case 1: // 发布逻辑
                    QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("release_id", releaseId);
                    queryWrapper.eq("release_job_status", 0);
                    queryWrapper.eq("uuid", uuid);
                    queryWrapper.select("release_id");
                    if (deployMapper.selectCount(queryWrapper) > 0) {
                        // 执行构建逻辑
                        Release release = new Release();
                        release.setReleaseJobStatus(1);
                        QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.eq("release_id", releaseId);
                        if (deployMapper.update(release, queryWrapper1) < 1) {
                            throw new Exception("执行构建失败");
                        }
                    } else {
                        throw new Exception("执行构建失败,该任务不存在!");
                    }
                    break;
                case 2: // 发布逻辑
                    QueryWrapper<Release> queryWrapper2 = new QueryWrapper<>();
                    // 只有已完成的任务才能回滚
                    queryWrapper2.eq("release_id", releaseId);
                    queryWrapper2.eq("release_job_status", 2);
                    queryWrapper2.eq("uuid", uuid);
                    queryWrapper2.select("release_id");

                    if (deployMapper.selectCount(queryWrapper2) > 0) {
                        // 执行回滚逻辑
                        Release release = new Release();
                        release.setReleaseJobStatus(3);
                        QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.eq("release_id", releaseId);
                        if (deployMapper.update(release, queryWrapper1) < 1) {
                            throw new Exception("执行发布失败");
                        }
                    } else {
                        throw new Exception("执行发布失败,该任务不存在!");
                    }
                    break;
                case 3: // 回滚逻辑
                    QueryWrapper<Release> queryWrapper3 = new QueryWrapper<>();
                    // 只有已完成的任务才能回滚
                    queryWrapper3.eq("release_id", releaseId);
                    queryWrapper3.eq("release_job_status", 4);
                    queryWrapper3.eq("uuid", uuid);
                    queryWrapper3.select("release_id");

                    if (deployMapper.selectCount(queryWrapper3) > 0) {
                        // 执行回滚逻辑
                        Release release = new Release();
                        release.setReleaseJobStatus(5);
                        // 回滚的版本
                        release.setReleaseJobRollback(version);
                        QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.eq("release_id", releaseId);
                        if (deployMapper.update(release, queryWrapper1) < 1) {
                            throw new Exception("执行回滚失败");
                        }
                    } else {
                        throw new Exception("执行回滚失败,该任务不存在!");
                    }
                    break;

            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList<ReleaseStagesLists> ReleaseStages(Long releaseId, String uuid) throws Exception {
        ArrayList<ReleaseStagesLists> results = new ArrayList<>();

        try {
            // 获取归属的uuid
            ArrayList<String> uuidOrgLists = this.orgUUidList(uuid);

            if (uuidOrgLists.size() > 0) {
                // 查看是否存在这个项目,如果在组织里则查组织里的
                QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("release_id", releaseId);
                queryWrapper.in("uuid", uuidOrgLists);
                Release release = deployMapper.selectOne(queryWrapper);
                if (release == null) {
                    throw new Exception("该项目不存在，请勿非法访问!");
                }
                // 查看进度详情
                QueryWrapper<ReleaseStagesLists> releaseDStagQueryWrapper = new QueryWrapper<>();
                releaseDStagQueryWrapper.eq("release_id", releaseId);
                results = deployMapper.releaseStagesListsObject(releaseDStagQueryWrapper);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public Map<String, Object> getEngineerBranch(String uuid, Long engineerId) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            // 获取项目对应的集群
            QueryWrapper<Engineer> engineerQueryWrapper = new QueryWrapper<>();
            engineerQueryWrapper.eq("engineer_id", engineerId);
            engineerQueryWrapper.eq("uuid", uuid);
            engineerQueryWrapper.select("engineer_git_id", "engineer_giturl");
            Engineer engineer = engineeringMapper.selectOne(engineerQueryWrapper);

            if (engineer == null) {
                throw new Exception("项目工程不存在!");
            }

            // 获取git的工程id
            results.put("id", engineer.getEngineerGitId());

            // 查当前可用集群
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uuid", this.orgUUidList(uuid));
            queryWrapper.eq("cluster_service_status", 3);
            queryWrapper.select("cluster_id");

            ArrayList<Long> clusterIds = new ArrayList<>();
            List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(queryWrapper);
            for (ServiceDeploy serviceDeploy : serviceDeployList) {
                clusterIds.add(serviceDeploy.getClusterId());
            }

            // 查所有集群可用的集群ID下的gitlab
            QueryWrapper<ServiceDeployWorkConfig> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("cluster_id", clusterIds);
            queryWrapper1.eq("cluster_config_name", "Gitlab仓库");
            List<ServiceDeployWorkConfig> serviceDeployWorkConfigList = serviceDeployConfigMapper.selectList(queryWrapper1);
            if (serviceDeployWorkConfigList.size() < 1) {
                throw new Exception("没有找到集群配置的Git信息!");
            }

            // 用项目的地址查
            for (ServiceDeployWorkConfig serviceDeployWorkConfig : serviceDeployWorkConfigList) {
                QueryWrapper<Engineer> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("engineer_id", engineerId);
                queryWrapper2.like("engineer_giturl", serviceDeployWorkConfig.getClusterConfigAddress());
                if (engineeringMapper.selectOne(queryWrapper2) != null) {
                    results.put("url", serviceDeployWorkConfig.getClusterConfigAddress());
                    results.put("username", serviceDeployWorkConfig.getClusterConfigUsername());
                    results.put("password", serviceDeployWorkConfig.getClusterConfigPassword());
                }
            }

            if (!results.containsKey("url")) {
                throw new Exception("无法获取分支，没有匹配的云上仓库。");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public ArrayList<ReleaseExecItems> getReleaseLists() throws Exception {
        ArrayList<ReleaseExecItems> releaseExecLists;
        try {
            // 获取构建中、发布中、回滚中的数据
            releaseExecLists = deployMapper.selectAllReleaseExecLists();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return releaseExecLists;
    }

}
