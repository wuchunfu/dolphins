package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.feature.GitLabServ;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.inputs.CreateReleaseInputs;
import com.dolphin.saas.inputs.KubConfigInputs;
import com.dolphin.saas.searchs.ReleaseSearch;
import com.dolphin.saas.service.ClusterService;
import com.dolphin.saas.service.DeployService;
import com.dolphin.saas.service.EngineeringService;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/release")
@Api(tags = "发布业务相关的接口", description = "7个")
public class ReleaseController extends MasterCommon {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private DeployService deployService;

    @Resource
    private EngineeringService engineeringService;

    @Resource
    private ClusterService clusterService;

    /**
     * 取消发布
     *
     * @return
     */
    @ApiOperation("取消发布")
    @RequestMapping(value = "/dropRelease", method = RequestMethod.POST)
    public Map<String, Object> DropRelease(@RequestHeader Map<String, String> headers, Integer releaseId) {
        try {
            deployService.DropRelease(redisUtils.getUUID(headers.get("token")), releaseId);
            return JsonResponseStr(1, "ok", "取消成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }


    /**
     * 根据工程ID获取集群可用发布次数
     *
     * @return
     */
    @ApiOperation("根据工程ID获取集群可用发布次数")
    @RequestMapping(value = "/getEngineerClusterDeploy", method = RequestMethod.POST)
    public Map<String, Object> GetEngineerClusterDeploy(@RequestHeader Map<String, String> headers, Long engineerId) {
        try {
            return JsonResponseMap(1,
                    clusterService.GetEngineerClusterDeploy(redisUtils.getUUID(headers.get("token")), engineerId),
                    "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 创建业务发布接口
     *
     * @return
     */
    @ApiOperation("创建业务发布接口")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> Create(@RequestHeader Map<String, String> headers, CreateReleaseInputs createReleaseInputs) {
        try {
            return JsonResponseMap(1,
                    deployService.createRelease(redisUtils.getUUID(headers.get("token")), createReleaseInputs),
                    "创建发布成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 更新服务配置
     *
     * @param headers         头部
     * @param id              工程ID
     * @param namespace       命名空间
     * @param kubConfigInputs 入参配置
     * @param moduleName      模块名
     * @param v1configmap     configmap的yaml
     * @param v1service       service的yaml
     * @param certCrtInfo     证书内容(crt) -- 配置https才有用
     * @param certKeyInfo     证书内容(key) -- 配置https才有用
     * @param nginxConf       nginx配置 -- 前端项目才有用
     * @return
     */
    @ApiOperation("更新服务配置")
    @RequestMapping(value = "/setEngineerKubConfig", method = RequestMethod.POST)
    public Map<String, Object> setkubconfig(@RequestHeader Map<String, String> headers, Long id, String namespace, KubConfigInputs kubConfigInputs, String moduleName, String v1configmap, String v1service, String certCrtInfo, String certKeyInfo, String nginxConf, Integer ready) {
        try {
            if (id == null || namespace == null || kubConfigInputs == null) {
                throw new Exception("关键字段不能缺失!");
            }

            // 加载configmap配置
            try {
                V1ConfigMap v1ConfigMap = Yaml.loadAs(v1configmap, V1ConfigMap.class);
            } catch (Exception e) {
                throw new Exception("configmap解析失败，请检查: " + e.getMessage());
            }

            // 加载service配置
            try {
                V1Service v1Service = Yaml.loadAs(v1service, V1Service.class);
            } catch (Exception e) {
                throw new Exception("service解析失败，请检查: " + e.getMessage());
            }

            // 如果是https需要配置证书
            if (kubConfigInputs.getHttps() == 1) {
                if (certCrtInfo == null || certKeyInfo == null) {
                    throw new Exception("配置TLS缺失，请配置好证书内容!");
                }
            }

            // 就绪探针值
            if (ready == null) {
                throw new Exception("就绪探针的值没有获取到!");
            }

            // 判断是否是前端项目，如果是，则需要提交nginx_conf
            if (engineeringService.CheckFrontEndAlive(id)) {
                if (nginxConf == null) {
                    throw new Exception("需要配置前端项目的nginx配置!");
                }
            }

            engineeringService.changeEngineerKubConfig(redisUtils.getUUID(headers.get("token")),
                    id, namespace, kubConfigInputs, moduleName, v1configmap, v1service, certCrtInfo, certKeyInfo, nginxConf, ready);
            return JsonResponseStr(1, "ok", "更新成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取单个发布的债务详情接口
     *
     * @return
     */
    @ApiOperation("获取单个发布的债务详情接口")
    @RequestMapping(value = "/DetDetials", method = RequestMethod.POST)
    public Map<String, Object> DetDetials(@RequestHeader Map<String, String> headers, Integer releaseId, String toolsName, Integer page, Integer pageSize) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            if (releaseId == null) {
                throw new Exception("发布的ID需要有!");
            }
            return JsonResponseMap(1, deployService.FindReleaseDetDetials(redisUtils.getUUID(headers.get("token")), releaseId, toolsName, page, pageSize), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), "获取数据异常!" + e.getMessage());
        }
    }

    /**
     * 获取服务配置
     *
     * @param headers      header头
     * @param id           工程ID
     * @param engineerName 工程名称
     * @param namespace    命名空间
     * @param moduleName   模块名
     * @return
     */
    @ApiOperation("获取服务配置")
    @RequestMapping(value = "/getEngineerKubConfig", method = RequestMethod.POST)
    public Map<String, Object> getkubconfig(@RequestHeader Map<String, String> headers, Long id, String engineerName, String namespace, String moduleName) {
        try {
            if (id == null || namespace == null || engineerName == null) {
                throw new Exception("关键字段不能缺失!");
            }
            if (moduleName != null) {
                if (moduleName.strip().equals("")) {
                    moduleName = null;
                }
            }

            // 生成configmap的配置
            V1ConfigMap v1ConfigMap = new V1ConfigMap();
            v1ConfigMap.setApiVersion("v1");
            v1ConfigMap.setKind("ConfigMap");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            if (moduleName != null && !moduleName.equals("")) {
                v1ObjectMeta.setName(moduleName);
            } else {
                v1ObjectMeta.setName(engineerName);
            }
            v1ObjectMeta.setNamespace(namespace);

            v1ConfigMap.setMetadata(v1ObjectMeta);

            String v1configMapYaml = Yaml.dump(v1ConfigMap);

            // 生成service的配置
            V1Service v1Service = new V1Service();
            v1Service.setApiVersion("v1");
            v1Service.setKind("Service");
            v1Service.setMetadata(v1ObjectMeta);
            V1ServiceSpec v1ServiceSpec = new V1ServiceSpec();
            v1ServiceSpec.setType("ClusterIP");
            Map<String, String> apps = new HashMap<>();
            if (moduleName != null && !moduleName.equals("")) {
                apps.put("app", moduleName);
            } else {
                apps.put("app", engineerName);
            }

            v1ServiceSpec.setSelector(apps);
            V1ServicePort v1ServicePort = new V1ServicePort();
            v1ServicePort.setPort(80);
            v1ServicePort.setTargetPort(new IntOrString(80));
            v1ServicePort.setProtocol("TCP");
            List<V1ServicePort> v1ServicePorts = new ArrayList<>();
            v1ServicePorts.add(v1ServicePort);
            v1ServiceSpec.setPorts(v1ServicePorts);
            v1Service.setSpec(v1ServiceSpec);
            String v1ServiceYaml = Yaml.dump(v1Service);

            Map<String, Object> response = engineeringService.getEngineerKubConfig(
                    redisUtils.getUUID(headers.get("token")),
                    id, namespace, moduleName, v1configMapYaml, v1ServiceYaml);

            return JsonResponseMap(1, response, "获取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取所有业务发布的列表
     *
     * @return
     */
    @ApiOperation("获取业务发布列表接口")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize, ReleaseSearch releaseSearch) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1, deployService.FindReleaseJobLists(page, pageSize, redisUtils.getUUID(headers.get("token")), releaseSearch), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), "获取数据异常!" + e.getMessage());
        }
    }

    /**
     * 查看现在是否有发布项目
     *
     * @return
     */
    @ApiOperation("查看现在是否有发布项目")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {
            if (deployService.checkReleaseJobs(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "有发布项目存在!");
            } else {
                return JsonResponseStr(-1, "failed", "没有发布项目!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 读取发布信息
     *
     * @param releaseId 发布的任务id
     * @return
     */
    @ApiOperation("读取发布信息")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(@RequestHeader Map<String, String> headers, Integer releaseId) {
        try {
            if (releaseId != null) {
                return JsonResponseObj(1, deployService.ReadRelease(releaseId, redisUtils.getUUID(headers.get("token"))), "读取成功!");
            } else {
                return JsonResponseStr(0, "failed", "读取资产信息失败!");
            }
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "读取资产数据异常!");
        }
    }

    /**
     * 获取发布的版本列表
     *
     * @param releaseId 工程名称
     * @return
     */
    @ApiOperation("获取发布的版本列表")
    @RequestMapping(value = "/releaseVerOptions", method = RequestMethod.POST)
    public Map<String, Object> ReadReleaseVerOptions(@RequestHeader Map<String, String> headers, Integer releaseId) {
        try {
            if (releaseId != null) {
                return JsonResponse(1, deployService.ReleaseVerOptions(releaseId, redisUtils.getUUID(headers.get("token"))), "读取成功!");
            } else {
                return JsonResponseStr(0, "failed", "读取信息失败!");
            }
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "读取数据异常!");
        }
    }

    /**
     * 执行发布任务
     *
     * @param releaseId 发布的id
     * @param status    发布类型，1：构建，2：发布，3：回滚
     * @return
     */
    @ApiOperation("执行发布、回滚任务")
    @RequestMapping(value = "/releaseExecute", method = RequestMethod.POST)
    public Map<String, Object> ReleaseExecuteJobs(@RequestHeader Map<String, String> headers, Long releaseId, Integer status, String version) {
        try {
            if (releaseId == null || status == null) {
                return JsonResponseStr(-1, "failed", "请勿非法操作，参数不能为空!");
            }
            deployService.ReleaseExecute(releaseId, status, version, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "执行成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "执行操作异常:" + e);
        }
    }

    /**
     * 查看发布流程图
     *
     * @param releaseId 发布的任务id
     * @return
     */
    @ApiOperation("查看发布流程图")
    @RequestMapping(value = "/releaseStages", method = RequestMethod.POST)
    public Map<String, Object> ReleaseStagesJobs(@RequestHeader Map<String, String> headers, Long releaseId) {
        try {
            if (releaseId != null) {
                return JsonResponse(1, deployService.ReleaseStages(releaseId, redisUtils.getUUID(headers.get("token"))), "读取成功!");
            } else {
                return JsonResponseStr(0, "failed", "参数不对，请检查输入!");
            }
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "执行操作异常!");
        }
    }

    /**
     * 查看发布债务列表
     *
     * @param releaseId 发布的任务id
     * @return
     */
    @ApiOperation("查看发布债务列表")
    @RequestMapping(value = "/releaseDetes", method = RequestMethod.POST)
    public Map<String, Object> ReleaseDetes(@RequestHeader Map<String, String> headers, Long releaseId) {
        try {
            if (releaseId != null) {
                return JsonResponse(1, deployService.ReleaseDetes(releaseId, redisUtils.getUUID(headers.get("token"))), "读取成功!");
            } else {
                return JsonResponseStr(0, "failed", "参数不对，请检查输入!");
            }
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "执行操作异常!");
        }
    }

    /**
     * 获得集群id和信息
     *
     * @return
     */
    @ApiOperation("根据云厂商获得集群id和信息")
    @RequestMapping(value = "/cloudClusterId", method = RequestMethod.POST)
    public Map<String, Object> getcloudClusterId(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, deployService.getcloudClusterId(redisUtils.getUUID(headers.get("token"))), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "执行操作异常!");
        }
    }

    /**
     * 根据集群id获取namespace信息
     *
     * @param headers
     * @return
     */
    @ApiOperation("根据集群id获取namespace信息")
    @RequestMapping(value = "/clusterIdNameSpace", method = RequestMethod.POST)
    public Map<String, Object> getclusterIdNameSpace(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, deployService.getclusterIdNameSpace(), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "执行操作异常!");
        }
    }

    /**
     * 获取可以发布的项目
     *
     * @param headers
     * @return
     */
    @ApiOperation("获取可以发布的项目")
    @RequestMapping(value = "/engineerOptions", method = RequestMethod.POST)
    public Map<String, Object> releaseService(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, deployService.getReleaseService(redisUtils.getUUID(headers.get("token"))), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", e.getMessage());
        }
    }

    /**
     * 根据项目名查分支列表和MR信息
     *
     * @return
     */
    @ApiOperation("根据项目名查分支列表和MR信息")
    @RequestMapping(value = "/engineerBranch", method = RequestMethod.POST)
    public Map<String, Object> engineerBranch(@RequestHeader Map<String, String> headers, Long engineerId) {
        try {
            // 获取基础信息
            Map<String, Object> results = deployService.getEngineerBranch(redisUtils.getUUID(headers.get("token")), engineerId);
            GitLabServ gitLabServ = new GitLabServ(
                    "http://" + results.get("url").toString(),
                    results.get("username").toString(),
                    results.get("password").toString());
            return JsonResponse(1, gitLabServ.branchesLists(Long.parseLong(results.get("id").toString())), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), e.getMessage());
        }
    }
}
