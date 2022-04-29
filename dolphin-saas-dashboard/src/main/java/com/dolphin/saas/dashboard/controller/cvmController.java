package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.TagServ;
import com.dolphin.saas.service.CvmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/cvm")
@Api(tags = "Cvm相关的接口", description = "7个")
public class cvmController extends MasterCommon {

    @Resource
    private CvmService cvmService;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 判断有没有CVM资产在库里
     *
     * @return
     */
    @ApiOperation("判断有没有CVM资产在库里")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {

            if (cvmService.checkCvmAlive(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "存在cvm资产!");
            } else {
                return JsonResponseStr(-1, "failed", "不存在cvm资产!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 更新cvm资产库数据
     *
     * @return
     */
    @ApiOperation("更新cvm资产库数据")
    @RequestMapping(value = "/updateCvmJobs", method = RequestMethod.GET)
    public Map<String, Object> updateCvmJobs(@RequestHeader Map<String, String> headers) {
        try {
            cvmService.UpdateCvmJobs(redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "更新任务创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "创建任务失败:" + e.getMessage());
        }
    }

    /**
     * 确认cvm更新数据状态
     *
     * @return
     */
    @ApiOperation("确认cvm更新数据状态")
    @RequestMapping(value = "/checkCvmJobs", method = RequestMethod.GET)
    public Map<String, Object> checkCvmJobs(@RequestHeader Map<String, String> headers) {
        try {
            if (!cvmService.CheckCvmJobs(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "当前没有任务!");
            } else {
                return JsonResponseStr(-1, "failed", "当前有任务在执行!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 获取CVM数据列表
     *
     * @param page 页码
     * @param size 数据长度
     * @return
     */
    @ApiOperation("获取CVM数据列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer size) {
        try {
            if (page == null && page < 1) {
                page = 1;
            }
            if (size == null || size < 10) {
                size = 10;
            }
            return JsonResponseMap(1, cvmService.FindCvmLists(page, size, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error:" + e.getMessage(), "查询失败!");
        }
    }

    /**
     * 获取所有可以打的标签
     *
     * @return
     */
    @ApiOperation("获取所有可以打的标签")
    @RequestMapping(value = "/getTags", method = RequestMethod.GET)
    public Map<String, Object> getTags() {
        try {
            return JsonResponse(1, cvmService.FindTagLists(), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "获取数据失败!");
        }
    }

    /**
     * 读取CVM资产信息
     *
     * @param cid 资产的id
     * @return
     */
    @ApiOperation("读取CVM资产信息")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(@RequestHeader Map<String, String> headers, Integer cid) {
        try {
            if (cid != null) {
                return JsonResponseMap(1, cvmService.ReadAssetsCvm(cid, redisUtils.getUUID(headers.get("token"))), "读取成功!");
            } else {
                return JsonResponseStr(0, "failed", "读取资产信息失败!");
            }
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", "读取资产数据异常!");
        }
    }

    /**
     * 打服务标签部署
     *
     * @param cid     CVM资产id
     * @param tagServ 服务标签信息
     * @return
     */
    @ApiOperation("打服务标签部署")
    @RequestMapping(value = "/buildTag", method = RequestMethod.POST)
    public Map<String, Object> BuildTag(@RequestHeader Map<String, String> headers, Integer cid, TagServ tagServ) {
        try {
            if (cid != null || tagServ != null) {
                if (cvmService.BuildTagService(cid, tagServ, redisUtils.getUUID(headers.get("token")))) {
                    return JsonResponseStr(1, "ok", "打服务标签成功!");
                } else {
                    return JsonResponseStr(-1, "failed", "打服务标签失败，标签不能重复打!");
                }
            } else {
                return JsonResponseStr(-2, "failed", "打服务标签失败,参数异常!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "打服务标签失败,服务异常!");
        }
    }
}
