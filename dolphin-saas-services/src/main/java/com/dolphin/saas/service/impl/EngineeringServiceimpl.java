package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.AnalyzeQuaLists;
import com.dolphin.saas.entity.vo.DockerLists;
import com.dolphin.saas.entity.vo.NameValLists;
import com.dolphin.saas.entity.vo.ReleaseExecItems;
import com.dolphin.saas.inputs.KubConfigInputs;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.searchs.EngineerSearch;
import com.dolphin.saas.service.EngineeringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service("emginnerService")
public class EngineeringServiceimpl extends BaseTools implements EngineeringService {

    @Resource
    private EngineeringMapper engineeringMapper;

    @Resource
    private DockerfileMapper dockerfileMapper;

    @Resource
    private RulesGroupMapper rulesGroupMapper;

    @Resource
    private FrameworkMapper frameworkMapper;

    @Resource
    private LanguageMapper languageMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private VendorsTypeMapper vendorsTypeMapper;

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private ServiceDeployGitlabsMapper serviceDeployGitlabsMapper;

    @Resource
    private EngineerAnalyzeMapper engineerAnalyzeMapper;

    @Resource
    private ServiceDeployConfigMapper serviceDeployConfigMapper;

    @Resource
    private KubBaseMapper kubBaseMapper;

    @Resource
    private KubConfigYamlMapper kubConfigYamlMapper;

    @Resource
    private AnalyzeFileLineMapper analyzeFileLineMapper;

    @Override
    public Map<String, Object> ReadReleaseLists(Integer engineerId, String uuid, Integer Pages, Integer Size, int[] status) throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            // ????????????????????????????????????
            Map<Integer, String> vendorKV = new HashMap<>();
            List<VendorsType> vendorsTypeList = vendorsTypeMapper.selectList(null);
            for (VendorsType vendorsType: vendorsTypeList){
                vendorKV.put(vendorsType.getTid(), vendorsType.getTName());
            }

            IPage<Release> page = new Page<>(Pages, Size);
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);
            queryWrapper.eq("uuid", uuid);
            queryWrapper.select("engineer_id");
            if (engineeringMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("?????????????????????,??????????????????!");
            }

            QueryWrapper<Release> releaseQueryWrapper = new QueryWrapper<>();
            releaseQueryWrapper.eq("release_engineer_id", engineerId);
            // ???????????????????????????????????????
            if (status.length > 0){
                List<Integer> ints = new ArrayList<>();
                for (int i: status){
                    ints.add(i);
                }
                releaseQueryWrapper.in("release_job_status", ints);
            }
            // ????????????????????????
            releaseQueryWrapper.orderByDesc("release_job_createtime");
            deployMapper.selectPage(page, releaseQueryWrapper);
            List<Release> releaseList = page.getRecords();

            ArrayList<Map<String, Object>> result = new ArrayList<>();

            if (releaseList.size() > 0) {
                for (Release release : releaseList) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("id", release.getReleaseId());
                    items.put("createtime", release.getReleaseJobCreatetime());
                    // ????????????????????????????????????ID??????
                    QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("cluster_id", release.getReleaseJobClusterId());
                    ServiceDeploy serviceDeploy = clusterMapper.selectOne(queryWrapper1);

                    items.put("cloudId", vendorKV.get(serviceDeploy.getClusterCloudId()));
                    items.put("status", release.getReleaseJobStatus());
                    // ???????????????
                    Long starttime = this.getTimes(release.getReleaseJobCreatetime().toString());
                    Long countTime;
                    if (release.getReleaseJobUpdatetime() != null) {
                        Long endtime = this.getTimes(release.getReleaseJobUpdatetime().toString());
                        countTime = endtime - starttime;
                    } else {
                        countTime = 0L;
                    }

                    items.put("ms", countTime + "s");
                    result.add(items);
                }
            }
            results.put("list", result);
            results.put("total", page.getTotal());
            results.put("pageSize", Size);
            results.put("page", Pages);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public ArrayList getLanguageOptions() {
        List<EngineerLanguage> engineerLanguageList = languageMapper.selectList(null);
        ArrayList results = new ArrayList();
        if (engineerLanguageList.size() > 0) {
            for (EngineerLanguage language : engineerLanguageList) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", language.getLanguageName());
                items.put("value", language.getLanguageId());
                results.add(items);
            }
        }
        return results;
    }

    @Override
    public ArrayList getFrameWorkOptions(int LanguageId) {
        QueryWrapper<EngineerFramework> engineerFrameworkQueryWrapper = new QueryWrapper<>();
        engineerFrameworkQueryWrapper.eq("framework_language_id", LanguageId);
        List<EngineerFramework> engineerFrameworkList = frameworkMapper.selectList(engineerFrameworkQueryWrapper);
        ArrayList results = new ArrayList();
        if (engineerFrameworkList.size() > 0) {
            for (EngineerFramework framework : engineerFrameworkList) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", framework.getFrameworkName());
                items.put("value", framework.getFrameworkId());
                results.add(items);
            }
        }
        return results;
    }

    @Override
    public Map<String, Object> GitEngineerQuality(Long engineerId, String uuid, Integer Pages, Integer Size) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // ?????????????????????
            IPage<NameValLists> page = new Page<>(Pages, Size);
            QueryWrapper<EngineerAnalyze> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);
            engineerAnalyzeMapper.analyzeQuaSourceTops(page, queryWrapper);

            ArrayList<Map<String, Object>> results = new ArrayList<>();
            for (NameValLists nameValLists :page.getRecords()){
                // ????????????
                Map<String, Object> items = this.objectMap(nameValLists);

                // ???????????????????????????
                QueryWrapper<EngineerAnalyze> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("engineer_searchfile", nameValLists.getNames());
                ArrayList<EngineerAnalyze> engineerAnalyzes = engineerAnalyzeMapper.analyzeQuaFileQuestionType(queryWrapper1);

                // ??????????????????????????????
                List<Integer> typeArr = new ArrayList<>();
                for (EngineerAnalyze engineerAnalyze: engineerAnalyzes){
                    typeArr.add(engineerAnalyze.getEngineerType());
                }

                items.put("typeArr", typeArr);
                results.add(items);
            }
            response.put("page", Pages);
            response.put("total", page.getTotal());
            response.put("list", results);
            response.put("pageSize", Size);

        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public Map<String, Object> GitAnalyzeQuality(Long engineerId, String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // ?????????????????????
            QueryWrapper<EngineerAnalyze> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);
            List<AnalyzeQuaLists> engineerAnalyzeList = engineerAnalyzeMapper.analyzeQuaSelect(queryWrapper);
            // ????????????
            QueryWrapper<EngineerAnalyze> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("engineer_id", engineerId);
            queryWrapper1.select("engineer_id");
            Integer counts = engineerAnalyzeMapper.selectCount(queryWrapper1);

            response.put("count", counts);
            response.put("chart", engineerAnalyzeList);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public List<AnalyzeFileLine> GitAnalyzeFileLineLists(Long engineerId, String uuid) throws Exception {
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("engineer_id", engineerId);

            if (engineeringMapper.selectCount(queryWrapper) < 1){
                throw new Exception("?????????????????????!");
            }

            QueryWrapper<AnalyzeFileLine> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("engineer_id", engineerId);
            // ?????????????????????
            return analyzeFileLineMapper.selectList(queryWrapper1);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void changeEngineerKubConfig(String uuid, Long id, String namespace, KubConfigInputs kubConfigInputs, String moduleName, String v1configmap, String v1service, String certCrtInfo, String certKeyInfo, String nginxConf, Integer ready) throws Exception {
        try {
            // ????????????????????????????????????????????????
            ArrayList<String> uuids = this.orgUUidList(uuid);
            QueryWrapper<Engineer> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("engineer_id", id);
            queryWrapper2.in("uuid", uuids);
            Engineer engineer = engineeringMapper.selectOne(queryWrapper2);
            if ( engineer== null) {
                throw new Exception("??????????????????????????????!");
            }

            // ??????????????????
            QueryWrapper<KubBaseConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", id);
            queryWrapper.eq("engineer_config_namespace", namespace);
            if (moduleName != null) {
                queryWrapper.eq("engineer_module_name", moduleName);
            }else{
                queryWrapper.eq("engineer_module_name", engineer.getEngineerName());
            }
            KubBaseConfig kubBaseConfig1 = new KubBaseConfig();
            kubBaseConfig1.setEngineerId(id);
            kubBaseConfig1.setEngineerConfigNameSpace(namespace);
            kubBaseConfig1.setEngineerDeploymentLimitCpu(kubConfigInputs.getCpuMax());
            kubBaseConfig1.setEngineerDeploymentReqCpu(kubConfigInputs.getCpuMin());
            kubBaseConfig1.setEngineerDeploymentLimitMemory(kubConfigInputs.getMemoyMax());
            kubBaseConfig1.setEngineerDeploymentReqMemory(kubConfigInputs.getMemoyMin());
            kubBaseConfig1.setEngineerReady(ready);
            if (moduleName != null) {
                kubBaseConfig1.setEngineerModuleName(moduleName);
            }else{
                kubBaseConfig1.setEngineerModuleName(engineer.getEngineerName());
            }
            kubBaseConfig1.setEngineerIngressHttps(kubConfigInputs.getHttps());
            kubBaseConfig1.setEngineerDeploymentMinPod(kubConfigInputs.getPodMin());
            kubBaseConfig1.setEngineerDeploymentMaxPod(kubConfigInputs.getPodMax());
            kubBaseConfig1.setEngineerIngressHostName(kubConfigInputs.getHost());

            if (kubBaseMapper.update(kubBaseConfig1, queryWrapper) < 1) {
                throw new Exception("????????????????????????!");
            }

            // ??????yaml???????????????
            QueryWrapper<KubConfigYaml> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("engineer_id", id);
            if (moduleName != null) {
                queryWrapper1.eq("engineer_module_name", moduleName);
            }else{
                queryWrapper1.eq("engineer_module_name", engineer.getEngineerName());
            }
            queryWrapper1.eq("engineer_config_namespace", namespace);
            KubConfigYaml kubConfigYaml = new KubConfigYaml();
            kubConfigYaml.setEngineerServices(v1service);
            kubConfigYaml.setEngineerConfigmap(v1configmap);
            if (certCrtInfo != null) {
                kubConfigYaml.setEngineerCertcrtInfo(certCrtInfo);
            }
            if (certKeyInfo != null) {
                kubConfigYaml.setEngineerCertkeyInfo(certKeyInfo);
            }
            if (nginxConf != null) {
                kubConfigYaml.setEngineerNginxInfo(nginxConf);
            }

            if (kubConfigYamlMapper.update(kubConfigYaml, queryWrapper1) < 1) {
                throw new Exception("??????configmap???service??????!");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getEngineerKubConfig(String uuid, Long id, String namespace, String moduleName, String v1configmap, String v1service) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // ????????????????????????????????????????????????
            ArrayList<String> uuids = this.orgUUidList(uuid);
            QueryWrapper<Engineer> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("engineer_id", id);
            queryWrapper2.in("uuid", uuids);
            if (engineeringMapper.selectCount(queryWrapper2) < 1) {
                throw new Exception("??????????????????????????????!");
            }
            // ???????????????????????????
            Engineer engineer = engineeringMapper.selectOne(queryWrapper2);

            // ????????????????????????
            QueryWrapper<KubBaseConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", id);
            queryWrapper.eq("engineer_config_namespace", namespace);
            if (moduleName != null) {
                queryWrapper.eq("engineer_module_name", moduleName);
            }else{
                queryWrapper.eq("engineer_module_name", engineer.getEngineerName());
            }
            KubBaseConfig kubBaseConfig = null;
            if (kubBaseMapper.selectCount(queryWrapper) < 1) {
                KubBaseConfig kubBaseConfig1 = new KubBaseConfig();
                kubBaseConfig1.setEngineerId(id);
                kubBaseConfig1.setEngineerIngressHttps(0);
                kubBaseConfig1.setEngineerDeploymentMinPod(1);
                kubBaseConfig1.setEngineerDeploymentMaxPod(1);
                kubBaseConfig1.setEngineerDeploymentReqMemory(256);
                kubBaseConfig1.setEngineerDeploymentLimitMemory(512);
                kubBaseConfig1.setEngineerDeploymentReqCpu(0.25);
                kubBaseConfig1.setEngineerDeploymentLimitCpu(0.5);
                kubBaseConfig1.setEngineerConfigNameSpace(namespace);
                kubBaseConfig1.setEngineerReady(0);
                if (moduleName != null){
                    kubBaseConfig1.setEngineerModuleName(moduleName);
                }else{
                    kubBaseConfig1.setEngineerModuleName(engineer.getEngineerName());
                }

                kubBaseConfig1.setEngineerIngressHostName("demo.test.com");
                if (kubBaseMapper.insert(kubBaseConfig1) < 1) {
                    throw new Exception("??????K8S????????????!");
                }
            }

            Map<String, Object> defaultConfig = new HashMap<>();
            // ?????????
            kubBaseConfig = kubBaseMapper.selectOne(queryWrapper);
            // ??????
            defaultConfig.put("host", kubBaseConfig.getEngineerIngressHostName());
            // ????????????HTTPS
            defaultConfig.put("https", kubBaseConfig.getEngineerIngressHttps());
            // ??????POD???
            defaultConfig.put("podMin", kubBaseConfig.getEngineerDeploymentMinPod());
            // ??????POD???
            defaultConfig.put("podMax", kubBaseConfig.getEngineerDeploymentMaxPod());
            // ??????CPU???
            defaultConfig.put("cpuMax", kubBaseConfig.getEngineerDeploymentLimitCpu());
            // ??????CPU???
            defaultConfig.put("cpuMin", kubBaseConfig.getEngineerDeploymentReqCpu());
            // ????????????
            defaultConfig.put("memoyMin", kubBaseConfig.getEngineerDeploymentReqMemory());
            // ????????????
            defaultConfig.put("memoyMax", kubBaseConfig.getEngineerDeploymentLimitMemory());
            // ????????????
            defaultConfig.put("ready", kubBaseConfig.getEngineerReady());

            response.put("podConfig", defaultConfig);

            // ???configmap???service?????????
            QueryWrapper<KubConfigYaml> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("engineer_id", id);
            if (moduleName != null) {
                queryWrapper1.eq("engineer_module_name", moduleName);
            }else{
                queryWrapper1.eq("engineer_module_name", engineer.getEngineerName());
            }
            queryWrapper1.eq("engineer_config_namespace", namespace);
            if (kubConfigYamlMapper.selectCount(queryWrapper1) < 1) {
                KubConfigYaml kubConfigYaml = new KubConfigYaml();
                kubConfigYaml.setEngineerId(id);
                kubConfigYaml.setEngineerConfigNamespace(namespace);
                kubConfigYaml.setEngineerConfigmap(v1configmap);
                kubConfigYaml.setEngineerServices(v1service);
                if (moduleName != null) {
                    kubConfigYaml.setEngineerModuleName(moduleName);
                }else{
                    kubConfigYaml.setEngineerModuleName(engineer.getEngineerName());
                }
                // ??????????????????????????????NGINX??????
                String nginxConf = "";
                if (engineer.getEngineerLanguageId() == 4) {
                    String FilePath;
                    File folder = new File("/yaml");
                    if (!folder.exists()) {
                        FilePath = "yaml";
                    } else {
                        FilePath = "/yaml";
                    }

                    File file = new File(FilePath + "/nginx.conf");
                    long fileLength = file.length();
                    byte[] filecontent = new byte[(int) fileLength];
                    new FileInputStream(file).read(filecontent);

                    nginxConf = new String(filecontent, StandardCharsets.UTF_8);
                    kubConfigYaml.setEngineerNginxInfo(nginxConf);
                }

                kubConfigYamlMapper.insert(kubConfigYaml);
                response.put("configmap", v1configmap);
                response.put("service", v1service);
                response.put("nginx", nginxConf);
            } else {
                KubConfigYaml kubConfigYaml = kubConfigYamlMapper.selectOne(queryWrapper1);
                response.put("configmap", kubConfigYaml.getEngineerConfigmap());
                response.put("service", kubConfigYaml.getEngineerServices());
                response.put("nginx", kubConfigYaml.getEngineerNginxInfo());
                // ????????????https
                if (kubBaseConfig.getEngineerIngressHttps() == 1) {
                    response.put("certCrtInfo", kubConfigYaml.getEngineerCertcrtInfo());
                    response.put("certKeyInfo", kubConfigYaml.getEngineerCertkeyInfo());
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createEnginner(Engineer engineer, String uuid) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("cluster_service_status", 3);

            // ??????????????????
            List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(queryWrapper);

            // ??????????????????????????????????????????
            if (serviceDeployList.size() < 1) {
                throw new Exception("??????????????????????????????????????????!");
            } else {
                // ??????????????????????????????????????????????????????ID???????????????????????????
                if (serviceDeployList.size() == 1){
                    engineer.setClusterId(serviceDeployList.get(0).getClusterId());
                }else{
                    // ????????????ID???????????????
                    if (engineer.getClusterId() == null){
                        throw new Exception("?????????????????????????????????!");
                    }
                    // ???????????????????????????????????????ID?????????????????????????????????
                    List<Long> clusterIds = new ArrayList<>();
                    for (ServiceDeploy serviceDeploy: serviceDeployList){
                        clusterIds.add(serviceDeploy.getClusterId());
                    }
                    if (!clusterIds.contains(engineer.getClusterId())){
                        throw new Exception("?????????????????????ID?????????????????????!");
                    }
                }

                // ????????????????????????
                QueryWrapper<Engineer> engineerQueryWrapper = new QueryWrapper<>();
                engineerQueryWrapper.eq("engineer_name", engineer.getEngineerName());
                engineerQueryWrapper.eq("uuid", uuid);
                engineerQueryWrapper.eq("cluster_id", engineer.getClusterId());
                engineerQueryWrapper.lt("engineer_status", 3);

                if (engineeringMapper.selectCount(engineerQueryWrapper) > 0) {
                    throw new Exception("????????????????????????????????????????????????!");
                }

                // ????????????
                engineer.setUuid(uuid);
                engineer.setEngineerCreatetime(new Date());
                engineer.setEngineerStatus(0);
                engineer.setEngineerUpdatetime(null);
                if (engineeringMapper.insert(engineer) < 1) {
                    throw new Exception("??????????????????????????????????????????!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEnginner(Engineer engineer, String uuid) throws Exception {
        try {
            // ????????????????????????
            QueryWrapper<Engineer> engineerQueryWrapper = new QueryWrapper<>();
            engineerQueryWrapper.eq("engineer_id", engineer.getEngineerId());
            engineerQueryWrapper.eq("uuid", uuid);
            if (engineeringMapper.selectCount(engineerQueryWrapper) < 1) {
                throw new Exception("???????????????!");
            }
            engineer.setEngineerStatus(3);
            engineeringMapper.update(engineer, engineerQueryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList GitlabNamespaceOptions(String uuid) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            // ??????UUID????????????????????????
            // ?????????????????????????????????namespaceID??????
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uuid", this.orgUUidList(uuid));
            queryWrapper.eq("cluster_service_status", 3);
            queryWrapper.orderByAsc("cluster_updatetime");
            queryWrapper.select("cluster_id");
            List<ServiceDeploy> serviceDeployList = clusterMapper.selectList(queryWrapper);

            if (serviceDeployList.size() < 1){
                throw new Exception("?????????????????????,????????????gitlab????????????!");
            }

            QueryWrapper<ServiceDeployGitlabs> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("cluster_id", serviceDeployList.get(0).getClusterId());
            queryWrapper1.select("git_group_name", "git_group_id", "git_group_desc");
            List<ServiceDeployGitlabs> serviceDeployGitlabs = serviceDeployGitlabsMapper.selectList(queryWrapper1);

            if (serviceDeployGitlabs.size() > 0) {
                for (ServiceDeployGitlabs serviceDeployGitlabs1 : serviceDeployGitlabs) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("value", serviceDeployGitlabs1.getGitGroupId());
                    items.put("label", serviceDeployGitlabs1.getGitGroupName());
                    items.put("info", serviceDeployGitlabs1.getGitGroupDesc());
                    results.add(items);
                }
            }else{
                throw new Exception("??????GitLab????????????????????????,???????????????!");
            }
        } catch (Exception e) {
            throw new Exception("??????Gitlab????????????!");
        }
        return results;
    }

    @Override
    public Map<String, Object> FindEnginnerJobLists(int Pages, int Size, String uuid, EngineerSearch engineerSearch) {
        // ?????????????????????
        IPage<Engineer> page = new Page<>(Pages, Size);
        QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("engineer_createtime");
        queryWrapper.in("uuid", this.orgUUidList(uuid));

        // ??????????????????
        if (engineerSearch != null) {
            // ????????????????????????
            if (engineerSearch.getEngineerName() != null) {
                queryWrapper.like("engineer_name", engineerSearch.getEngineerName());
            }

            // ????????????ID
            if (engineerSearch.getEngineerLanguageId() != null) {
                queryWrapper.eq("engineer_language_id", engineerSearch.getEngineerLanguageId());
            }

            // ????????????ID
            if (engineerSearch.getEngineerFrameworkId() != null) {
                queryWrapper.eq("engineer_framework_id", engineerSearch.getEngineerFrameworkId());
            }
        }
        engineeringMapper.selectPage(page, queryWrapper);

        // ??????????????????
        Map<String, Object> results = new HashMap<>();
        results.put("page", Pages);
        results.put("total", page.getTotal());
        List<Engineer> engineerList = page.getRecords();
        ArrayList records = new ArrayList();
        for (Engineer engineer : engineerList) {
            Map<String, Object> items = this.objectMap(engineer);
            EngineerLanguage engineerLanguage = languageMapper.selectById(engineer.getEngineerLanguageId());
            EngineerFramework engineerFramework = frameworkMapper.selectById(engineer.getEngineerFrameworkId());
            RuleGroup ruleGroup = rulesGroupMapper.selectById(engineer.getEngineerReleaseRulesId());
            items.put("engineerLanguageId", engineerLanguage.getLanguageName());
            items.put("engineerFrameworkId", engineerFramework.getFrameworkName());
            items.put("engineerReleaseRulesId", ruleGroup.getRulesName());
            items.remove("engineerDockerfileId");
            items.remove("engineerCodeing");
            items.remove("engineerVocational");
            items.remove("engineerSecurity");
            items.remove("engineerTesting");
            items.remove("engineerDevops");
            records.add(items);
        }
        results.put("list", records);
        results.put("pageSize", Size);
        return results;
    }

    @Override
    public Boolean checkEnginnerJobs(String uuid) {
        QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);

        return engineeringMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public Map<String, Object> ReadEnginner(Long engineerId, String uuid) {
        // ????????????????????????
        QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("engineer_id", engineerId);
        queryWrapper.eq("uuid", uuid);

        Engineer engineer = engineeringMapper.selectOne(queryWrapper);
        Map<String, Object> results = this.objectMap(engineer);

        // ??????dockerfile?????????
        EngineerDockerfile dockerfile = dockerfileMapper.selectById(engineer.getEngineerDockerfileId());
        // ??????????????????????????????
        EngineerFramework framework = frameworkMapper.selectById(engineer.getEngineerFrameworkId());
        EngineerLanguage language = languageMapper.selectById(engineer.getEngineerLanguageId());
        // ???????????????
        RuleGroup ruleGroup = rulesGroupMapper.selectById(engineer.getEngineerReleaseRulesId());
        // ???????????????????????????
        QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("release_engineer_id", engineerId);
        queryWrapper1.in("release_job_status", 4);

        List<String> NameSpaces = new ArrayList<>();
        List<Release> nameSpaceList = engineeringMapper.engineerReleaseNameSpace(queryWrapper1);
        for (Release release: nameSpaceList){
            NameSpaces.add(release.getReleaseJobNamespace());
        }
        // ?????????????????????
        results.put("engineerDockerfile", dockerfile.getDockerfileName());
        results.put("engineerFramework", framework.getFrameworkName());
        results.put("engineerLanguage", language.getLanguageName());
        results.put("engineerReleaseRules", ruleGroup.getRulesName());
        results.put("engineerNameSpace", NameSpaces);

        // ????????????
        results.remove("engineerDockerfileId");
        results.remove("engineerFrameworkId");
        results.remove("engineerLanguageId");
        results.remove("engineerReleaseRulesId");

        return results;
    }

    @Override
    public ArrayList DockerfileOptions(Engineer engineer, String uuid) throws Exception {
        try {
            QueryWrapper<EngineerDockerfile> queryWrapper = new QueryWrapper<>();
            if (engineer.getEngineerLanguageId() == null) {
                throw new Exception("???????????????????????????!");
            }
            if (engineer.getEngineerFrameworkId() != null) {
                queryWrapper.eq("dockerfile_language_id", engineer.getEngineerLanguageId());
            }
            queryWrapper.eq("dockerfile_framework_id", engineer.getEngineerFrameworkId());
            queryWrapper.eq("dockerfile_status", 1);
            queryWrapper.select("dockerfile_id", "dockerfile_name", "dockerfile_os_path", "dockerfile_remark");
            List<EngineerDockerfile> dockerfileLists = dockerfileMapper.selectList(queryWrapper);
            ArrayList results = new ArrayList();
            for (EngineerDockerfile value : dockerfileLists) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", value.getDockerfileName());
                items.put("value", value.getDockerfileId());
                results.add(items);
            }
            return results;
        } catch (Exception e) {
            log.error("DockerfileOptions:" + e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList<Map<String, Object>> ReleaseRulesOptions(String uuid) throws Exception {
        try {
            QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rules_status", 1);
            queryWrapper.eq("uuid", uuid);
            queryWrapper.select("rules_name", "rid");
            List<RuleGroup> rulesGroupLists = rulesGroupMapper.selectList(queryWrapper);
            ArrayList<Map<String, Object>> results = new ArrayList<>();
            for (RuleGroup value : rulesGroupLists) {
                Map<String, Object> items = new HashMap<>();
                items.put("value", value.getRid().toString());
                items.put("label", value.getRulesName());
                results.add(items);
            }
            return results;
        } catch (Exception e) {
            log.error("ReleaseRulesOptions:" + e);
            throw new Exception("????????????????????????!");
        }
    }

    @Override
    public ArrayList getReleaseEngineerOptions(String uuid) {
        QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        queryWrapper.eq("engineer_status", 1);
        queryWrapper.select("engineer_name", "engineer_id");
        List<Engineer> engineerList = engineeringMapper.selectList(queryWrapper);
        ArrayList results = new ArrayList();
        for (Engineer value : engineerList) {
            Map<String, String> items = new HashMap<>();
            items.put("value", value.getEngineerId().toString());
            items.put("label", value.getEngineerName());
            results.add(items);
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobsStatus(Long engineerId, Integer engineerCreateJobStatus) throws Exception {
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);

            Engineer engineer = new Engineer();
            engineer.setEngineerId(engineerId);
            engineer.setEngineerUpdatetime(new Date());
            engineer.setEngineerStatus(engineerCreateJobStatus);
            if (engineeringMapper.update(engineer, queryWrapper) < 1) {
                throw new Exception("????????????????????????!");
            }
        } catch (Exception e) {
            log.error("updateJobsStatus:" + e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void UpdateJobsUrl(Engineer engineer) throws Exception {
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineer.getEngineerId());
            if (engineeringMapper.update(engineer, queryWrapper) < 1) {
                throw new Exception("??????????????????!");
            }
        } catch (Exception e) {
            log.error("updateJobsUrl:" + e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList<Map<String, Object>> FindEnginnerAllJobs() throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            // ?????????????????????????????????????????????UUID????????????????????????????????????gitlab
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_status", 0);
            List<Engineer> engineerList = engineeringMapper.selectList(queryWrapper);
            for (Engineer engineer : engineerList) {
                try {
                    Map<String, Object> items = this.objectMap(engineer);
                    // ??????????????????ID?????????????????????Gitlab????????????
                    QueryWrapper<ServiceDeployWorkConfig> queryWrapper3 = new QueryWrapper<>();
                    queryWrapper3.eq("cluster_id", engineer.getClusterId());
                    queryWrapper3.eq("cluster_config_name", "Gitlab??????");
                    ServiceDeployWorkConfig clusterSysConfig = serviceDeployConfigMapper.selectOne(queryWrapper3);

                    if (clusterSysConfig == null){
                        // ???????????????????????????????????????
                        updateJobsStatus(engineer.getEngineerId(), 3);
                    }else{
                        items.put("address", clusterSysConfig.getClusterConfigAddress());
                        items.put("username", clusterSysConfig.getClusterConfigUsername());
                        items.put("password", clusterSysConfig.getClusterConfigPassword());
                        results.add(items);
                    }
                } catch (Exception e) {
                    log.error("???????????????????????????: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public ArrayList<Map<String, Object>> FindEngineerInfo() throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            // ???????????????????????????
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_status", 2);
            queryWrapper.select("engineer_id", "engineer_name", "engineer_giturl", "cluster_id", "commit_id");
            List<Engineer> engineerList = engineeringMapper.selectList(queryWrapper);

            for (Engineer engineer: engineerList){
                Map<String, Object> engineerInfo = this.objectMap(engineer);
                // git??????
                QueryWrapper<ServiceDeployWorkConfig> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("cluster_id", engineer.getClusterId());
                queryWrapper1.eq("cluster_config_name", "Gitlab??????");
                ServiceDeployWorkConfig clusterConfig = serviceDeployConfigMapper.selectOne(queryWrapper1);
                if (clusterConfig == null) {
                    throw new Exception("??????????????????!");
                }
                engineerInfo.put("userName", clusterConfig.getClusterConfigUsername());
                engineerInfo.put("passWord", clusterConfig.getClusterConfigPassword());
                results.add(engineerInfo);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public void saveGitAnalyzeRecords(Long engineerId, Map<String, Object> results) throws Exception {
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_id", engineerId);

            Engineer engineer = new Engineer();
            engineer.setCommitId(results.get("commidId").toString());
            engineer.setEngineerUpdatetime(new Date());

            if (engineeringMapper.update(engineer, queryWrapper) < 1){
                throw new Exception("????????????????????????!");
            }

            ArrayList<Map<String, Object>> objs = (ArrayList<Map<String, Object>>) results.get("analysis");

            log.info("objs: {}", objs.toString());
            // ?????????????????????
            QueryWrapper<AnalyzeFileLine> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("engineer_id", engineerId);
            List<AnalyzeFileLine> analyzeFileLines = analyzeFileLineMapper.selectList(queryWrapper1);

            Set<String> oldlangs = new HashSet<>();
            Set<String> newlangs = new HashSet<>();
            Map<String, Object> diff = new HashMap<>();

            // ???????????????????????????
            if (analyzeFileLines.size() > 0) {
                // ????????????
                for (AnalyzeFileLine analyzeFileLine: analyzeFileLines){
                    oldlangs.add(analyzeFileLine.getEngineerLanguage());
                }
                // ????????????
                for (Map<String, Object> objItems: objs){
                    newlangs.add((String) objItems.get("name"));
                }

                // ????????????
                diff = this.diffSet(oldlangs, newlangs);
            }

            List<String> add = (List<String>) diff.get("add");
            List<String> delete = (List<String>) diff.get("delete");
            List<String> update = (List<String>) diff.get("update");

            log.info("add: {}", add);
            log.info("delete: {}", delete);
            log.info("update: {}", update);

            // ?????????
            if (delete != null){
                for (String deleteItems: delete){
                    QueryWrapper<AnalyzeFileLine> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("engineer_id", engineerId);
                    queryWrapper2.eq("engineer_language", deleteItems);
                    if (analyzeFileLineMapper.delete(queryWrapper2) < 1){
                        throw new Exception("????????????????????????!");
                    }
                }
            }

            // ?????????????????????
            for (Map<String, Object> objItems: objs){
                AnalyzeFileLine analyzeFileLine = new AnalyzeFileLine();
                analyzeFileLine.setEngineerFile(Long.parseLong(objItems.get("count").toString()));
                analyzeFileLine.setEngineerId(engineerId);
                analyzeFileLine.setCreateTime(new Date());
                analyzeFileLine.setEngineerLanguage(objItems.get("name").toString());
                analyzeFileLine.setEngineerLine(Long.parseLong(objItems.get("line").toString()));

                log.info("analyzeFileLine: {}", analyzeFileLine);

                // ???????????????????????????
                if (diff.keySet().size() < 1){
                    // ???????????????????????????
                    if (analyzeFileLineMapper.insert(analyzeFileLine) < 1){
                        throw new Exception("????????????????????????!");
                    }
                }else{
                    // ????????????????????????????????????????????????
                    if (add.contains(analyzeFileLine.getEngineerLanguage())){
                        if (analyzeFileLineMapper.insert(analyzeFileLine) < 1){
                            throw new Exception("????????????????????????!");
                        }
                    }
                    if (update.contains(analyzeFileLine.getEngineerLanguage())){
                        QueryWrapper<AnalyzeFileLine> queryWrapper3 = new QueryWrapper<>();
                        queryWrapper3.eq("engineer_id", engineerId);
                        queryWrapper3.eq("engineer_language", analyzeFileLine.getEngineerLanguage());
                        analyzeFileLine.setUpdateTime(new Date());
                        analyzeFileLine.setCreateTime(null);
                        if (analyzeFileLineMapper.update(analyzeFileLine, queryWrapper3) < 1){
                            throw new Exception("????????????????????????!");
                        }
                    }
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList VentorTypeOptions(String uuid) {
        QueryWrapper<VendorsType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t_status", 1);
        queryWrapper.select("t_name", "tid");
        List<VendorsType> vendorsTypeList = vendorsTypeMapper.selectList(queryWrapper);

        ArrayList<Integer> cloudIds = new ArrayList<>();
        QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("uuid", uuid);
        queryWrapper1.eq("cluster_service_status", 3);
        queryWrapper1.select("cluster_cloud_id");
        List<ServiceDeploy> serviceDeploys = clusterMapper.selectList(queryWrapper1);
        for (ServiceDeploy serviceDeploy : serviceDeploys) {
            if (!cloudIds.contains(serviceDeploy.getClusterCloudId())) {
                cloudIds.add(serviceDeploy.getClusterCloudId());
            }
        }

        ArrayList<Map<String, Object>> results = new ArrayList<>();
        for (VendorsType vendorsType : vendorsTypeList) {
            if (cloudIds.contains(vendorsType.getTid())) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", vendorsType.getTName());
                items.put("value", vendorsType.getTid());
                results.add(items);
            }
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pushAnalyzeResults(ReleaseExecItems releaseExecItems, Map<String, Object> engineerAnalyze, String ToolsName) throws Exception {
        try {
            if (engineerAnalyze.containsKey("issuesAllDetials")) {
                ArrayList<Map<String, Object>> issuesAllDetials = (ArrayList<Map<String, Object>>) engineerAnalyze.get("issuesAllDetials");
                for (Map<String, Object> issueItems : issuesAllDetials) {
                    try {
                        // ????????????
                        Integer issueType = 0;
                        // ??????????????????
                        Integer questionType = 0;
                        // ????????????
                        EngineerAnalyze engineerAnalyze1 = new EngineerAnalyze();
                        engineerAnalyze1.setEngineerId(releaseExecItems.getReleaseEngineerId());
                        engineerAnalyze1.setEngineerBranch(releaseExecItems.getReleaseJobBranch());
                        engineerAnalyze1.setEnginnerVersion(releaseExecItems.getReleaseVersion());
                        engineerAnalyze1.setEngineerCreatetime(new Date());
                        engineerAnalyze1.setEngineerSearchfile(issueItems.get("fileName").toString());
                        engineerAnalyze1.setEngineerRule(issueItems.get("ruleName").toString());
                        engineerAnalyze1.setEngineerCode(issueItems.get("code").toString());
                        switch (issueItems.get("issuesType").toString()) {
                            case "CODE_SMELL":
                                issueType = 2;
                                break;

                            case "BUG":
                                issueType = 1;
                                break;

                            case "VULNERABILITY":
                                issueType = 3;
                                break;
                        }
                        engineerAnalyze1.setEngineerType(issueType);
                        engineerAnalyze1.setEngineerMessage(issueItems.get("issuesMessage").toString());
                        long lineNumber;
                        if (issueItems.get("lineNumber") == null) {
                            lineNumber = 0L;
                        } else {
                            lineNumber = Long.parseLong(String.valueOf(issueItems.get("lineNumber")));
                        }
                        engineerAnalyze1.setEngineerCodeLine(lineNumber);
                        switch (issueItems.get("severity").toString()) {
                            case "MINOR":
                                questionType = 1;
                                break;

                            case "MAJOR":
                                questionType = 2;
                                break;

                            case "CRITICAL":
                                questionType = 3;
                                break;

                            case "BLOCKER":
                                questionType = 4;
                                break;
                        }
                        engineerAnalyze1.setEngineerSeverity(questionType);
                        engineerAnalyze1.setEngineerTools(ToolsName);

                        if (engineerAnalyzeMapper.insert(engineerAnalyze1) < 1) {
                            throw new Exception("????????????????????????!");
                        }
                    } catch (Exception e) {
                        throw new Exception(String.format("issueItems: %s, ??????????????????: %s", issueItems, e.getMessage()));
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> FindDockerfileLists(Integer Pages, Integer Size) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // ?????????????????????
            IPage<DockerLists> page = new Page<>(Pages, Size);
            QueryWrapper<EngineerDockerfile> queryWrapper = new QueryWrapper<>();

            dockerfileMapper.selectDockerfilePage(page, queryWrapper);
            response.put("page", Pages);
            response.put("total", page.getTotal());
            response.put("list", page.getRecords());
            response.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return response;
    }

    @Override
    public Boolean CheckFrontEndAlive(Long engineerId) throws Exception {
        try {
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("engineer_language_id", 4);
            queryWrapper.eq("engineer_id", engineerId);
            return engineeringMapper.selectCount(queryWrapper) > 0;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
