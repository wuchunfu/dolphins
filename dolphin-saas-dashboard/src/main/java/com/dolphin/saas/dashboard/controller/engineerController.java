package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.Engineer;
import com.dolphin.saas.searchs.EngineerSearch;
import com.dolphin.saas.service.EngineeringService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/engineer")
@Api(tags = "工程管理相关的接口", description = "6个")
public class engineerController extends MasterCommon {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private EngineeringService engineeringService;


    /**
     * 根据工程查质量情况明细
     *
     * @param engineerId 工程id
     * @return
     */
    @ApiOperation("根据工程查质量情况明细")
    @RequestMapping(value = "/gitEngineerQuality", method = RequestMethod.POST)
    public Map<String, Object> GitEngineerQuality(@RequestHeader Map<String, String> headers, Long engineerId, Integer page, Integer pageSize) {
        try {
            if (engineerId == null) {
                throw new Exception("不存在这个工程!");
            }
            if (page == null) {
                page = 1;
            }
            // 不能多过100条
            if (pageSize == null || pageSize > 100) {
                pageSize = 10;
            }
            return JsonResponseMap(1, engineeringService.GitEngineerQuality(engineerId, redisUtils.getUUID(headers.get("token")), page, pageSize), "获取数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 查看质量分析统计
     *
     * @param engineerId 工程id
     * @return
     */
    @ApiOperation("查看质量分析统计")
    @RequestMapping(value = "/gitAnalyzeQuality", method = RequestMethod.POST)
    public Map<String, Object> GitAnalyzeQuality(@RequestHeader Map<String, String> headers, Long engineerId) {
        try {
            if (engineerId == null) {
                throw new Exception("不存在这个工程!");
            }
            return JsonResponseMap(1, engineeringService.GitAnalyzeQuality(engineerId, redisUtils.getUUID(headers.get("token"))), "获取数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }


    /**
     * 查看工程对应的代码分类组成
     *
     * @param engineerId 工程id
     * @return
     */
    @ApiOperation("查看工程对应的代码分类组成")
    @RequestMapping(value = "/gitAnalyzeFileLine", method = RequestMethod.POST)
    public Map<String, Object> GitAnalyzeFileLine(@RequestHeader Map<String, String> headers, Long engineerId) {
        try {
            if (engineerId == null) {
                throw new Exception("不存在这个工程!");
            }
            return JsonResponseObj(1, engineeringService.GitAnalyzeFileLineLists(engineerId, redisUtils.getUUID(headers.get("token"))), "获取数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 查看工程发布列表
     *
     * @param engineerId 工程id
     * @return
     */
    @ApiOperation("查看工程发布列表")
    @RequestMapping(value = "/releaseLists", method = RequestMethod.POST)
    public Map<String, Object> ReadReleaseLists(@RequestHeader Map<String, String> headers, Integer engineerId, Integer page, Integer pageSize, int[] status) {
        try {
            if (engineerId == null) {
                throw new Exception("不存在这个工程");
            }

            return JsonResponseObj(1, engineeringService.ReadReleaseLists(engineerId, redisUtils.getUUID(headers.get("token")), page, pageSize, status), "读取工程发布列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 判断是否有工程存在
     *
     * @return
     */
    @ApiOperation("判断是否有工程存在")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {
            if (engineeringService.checkEnginnerJobs(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "有工程存在!");
            } else {
                return JsonResponseStr(-1, "failed", "没有工程!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * gitlab的namespace下拉
     *
     * @return
     */
    @ApiOperation("gitlab的namespace下拉")
    @RequestMapping(value = "/gitlabNamespaceOptions", method = RequestMethod.POST)
    public Map<String, Object> GitlabNamespaceOptions(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, engineeringService.GitlabNamespaceOptions(redisUtils.getUUID(headers.get("token"))), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "读取异常:" + e);
        }
    }

    /**
     * 创建一个工程
     *
     * @param engineer 工程信息
     * @return
     */
    @ApiOperation("创建一个工程")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> Create(@RequestHeader Map<String, String> headers, Engineer engineer) {
        try {
            if (engineer == null) {
                throw new Exception("创建失败,不能不填写工程信息!");
            }
            engineeringService.createEnginner(engineer, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "创建工程成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "创建失败:" + e.getMessage());
        }
    }

    /**
     * 删除一个工程
     *
     * @param engineer 工程信息
     * @return
     */
    @ApiOperation("删除一个工程")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Map<String, Object> Delete(@RequestHeader Map<String, String> headers, Engineer engineer) {
        try {
            if (engineer == null) {
                throw new Exception("删除失败,不能不填写工程信息!");
            }
            engineeringService.deleteEnginner(engineer, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "删除工程成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "删除失败:" + e.getMessage());
        }
    }

    /**
     * 获取有集群的发布云厂商列表
     *
     * @return
     */
    @ApiOperation("获取有集群的发布云厂商列表")
    @RequestMapping(value = "/ventorTypeOptions", method = RequestMethod.GET)
    public Map<String, Object> getVentorTypeOptions(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, engineeringService.VentorTypeOptions(redisUtils.getUUID(headers.get("token"))), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "读取异常:" + e.getMessage());
        }
    }

    /**
     * 获取工程管理列表
     *
     * @param page 页码
     * @return
     */
    @ApiOperation("获取工程管理列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize, EngineerSearch engineerSearch) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1,
                    engineeringService.FindEnginnerJobLists(
                            page, pageSize,
                            redisUtils.getUUID(headers.get("token")),
                            engineerSearch),
                    "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), "获取数据异常!");
        }
    }

    /**
     * 获取单个工程信息
     *
     * @param engineerId 工程id
     * @return
     */
    @ApiOperation("获取单个工程信息")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(@RequestHeader Map<String, String> headers, Long engineerId) {
        try {
            if (engineerId == null) {
                throw new Exception("不存在这个工程!");
            }
            return JsonResponseMap(1,
                    engineeringService.ReadEnginner(engineerId, redisUtils.getUUID(headers.get("token"))), "读取工程信息成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取dockerfile的下拉选择
     *
     * @return
     */
    @ApiOperation("获取dockerfile的下拉选择")
    @RequestMapping(value = "/dockerfileOptions", method = RequestMethod.POST)
    public Map<String, Object> getDockerfileOptions(@RequestHeader Map<String, String> headers, Engineer engineer) {
        try {
            return JsonResponse(1, engineeringService.DockerfileOptions(engineer, redisUtils.getUUID(headers.get("token"))), "获取dockerfile的信息列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取发布规则的下拉选择
     *
     * @return
     */
    @ApiOperation("获取发布规则的下拉选择")
    @RequestMapping(value = "/releaserulesOptions", method = RequestMethod.GET)
    public Map<String, Object> getReleaseRulesOptions(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, engineeringService.ReleaseRulesOptions(redisUtils.getUUID(headers.get("token"))), "获取发布规则的信息列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "读取发布规则信息失败!");
        }
    }

    /**
     * 获取开发语言的下拉选择
     *
     * @return
     */
    @ApiOperation("获取开发语言的下拉选择")
    @RequestMapping(value = "/LanguageOptions", method = RequestMethod.GET)
    public Map<String, Object> getLanguageOptions() {
        try {
            return JsonResponse(1, engineeringService.getLanguageOptions(), "获取开发语言列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "读取发布规则信息失败!");
        }
    }

    /**
     * 获取开发框架的下拉选择
     *
     * @return
     */
    @ApiOperation("获取开发框架的下拉选择")
    @RequestMapping(value = "/FrameWorkOptions", method = RequestMethod.POST)
    public Map<String, Object> getFrameWorkOptions(Integer LanguageId) {
        try {
            if (LanguageId == null) {
                throw new Exception("开发语言不能不输入!");
            }
            return JsonResponse(1, engineeringService.getFrameWorkOptions(LanguageId), "获取开发框架的信息列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取工程的下拉选择（用于发布上线）
     *
     * @return
     */
    @ApiOperation("获取开发框架的下拉选择")
    @RequestMapping(value = "/ReleaseEngineerOptions", method = RequestMethod.POST)
    public Map<String, Object> getReleaseEngineerOptions(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, engineeringService.getReleaseEngineerOptions(redisUtils.getUUID(headers.get("token"))), "获取开发框架的信息列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
