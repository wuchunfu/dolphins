package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.RuleGroup;
import com.dolphin.saas.service.RulesGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rulesgroup")
@Api(tags = "发布规则策略组相关的接口", description = "4个")
public class rulesGroupController extends MasterCommon {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RulesGroupService rulesGroupService;

    /**
     * 判断有没有发布规则策略组
     *
     * @return
     */
    @ApiOperation("判断有没有发布规则策略组")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {
            if (rulesGroupService.CheckRulesGroup(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "存在发布策略!");
            } else {
                return JsonResponseStr(-1, "failed", "不存在发布策略!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 创建发布规则策略组
     *
     * @param ruleGroup
     * @return
     */
    @ApiOperation("创建发布规则策略组")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> Create(@RequestHeader Map<String, String> headers, RuleGroup ruleGroup, String ruleInfos) {
        try {
            if (ruleGroup == null) {
                throw new Exception("创建失败,不能不填写发布规则!");
            }
            if (ruleGroup.getRulesName().isEmpty()) {
                throw new Exception("创建失败,发布规则名称不能为空!");
            }
            if (ruleGroup.getRulesType() == null) {
                throw new Exception("创建失败,发布规则策略不能为空!");
            }
            if (ruleInfos == null) {
                throw new Exception("发布的策略不能为空！");
            }

            ArrayList<String> rulesInfos = new ArrayList<>(List.of(ruleInfos.split(",")));

            rulesGroupService.CreateRulesGroup(ruleGroup, rulesInfos, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "创建失败!");
        }
    }

    @ApiOperation("获取全部发布策略列表")
    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    public Map<String, Object> Rules() {
        try {
            return JsonResponse(1, rulesGroupService.GetRulesLists(), "获取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "获取失败:" + e.getMessage());
        }
    }

    /**
     * 获取发布规则策略列表
     *
     * @param page 页码
     * @param pageSize 数据长度
     * @return
     */
    @ApiOperation("获取发布规则策略列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1, rulesGroupService.FindRulesGroupLists(page, pageSize, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), e.getMessage());
        }
    }

    /**
     * 更改发布规则策略的状态
     *
     * @param rid    更新的规则id
     * @param status 更新的状态
     * @return
     */
    @ApiOperation("更改发布规则策略的状态")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public Map<String, Object> Change(@RequestHeader Map<String, String> headers, Integer rid, Integer status) {
        try {
            if (rid == null || status == null) {
                return JsonResponse(-1, new ArrayList(), "规则id和状态必须要提交!");
            }
            Map<String, Object> results = rulesGroupService.UpdateRulesGroup(rid, status, redisUtils.getUUID(headers.get("token")));

            if (results.containsKey("error")) {
                return JsonResponseStr(0, "failed", results.get("error").toString());
            }
            return JsonResponseStr(1, "success", "更新发布策略成功!");
        } catch (Exception e) {
            return JsonResponseStr(-2, "error", "更新发布策略功能异常!");
        }
    }

    /**
     * 删除发布规则策略
     *
     * @param rid 规则id
     * @return
     */
    @ApiOperation("删除发布策略")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Map<String, Object> Delete(@RequestHeader Map<String, String> headers, Integer rid) {
        try {
            rulesGroupService.DeleteRulesGroup(rid, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "删除发布策略成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "删除发布策略失败:" + e.getMessage());
        }
    }
}
