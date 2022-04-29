package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.MerchantDetails;
import com.dolphin.saas.service.MerchantDetailsService;
import com.dolphin.saas.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@Api(tags = "商户相关接口", description = "1个")
public class merchantController extends MasterCommon {

    @Resource
    private MerchantService merchantService;

    @Resource
    private MerchantDetailsService merchantDetailsService;

    @Resource
    private RedisUtils redisUtils;

    @ApiOperation("获取商户信息接口")
    @RequestMapping(value = "/getMerchantName", method = RequestMethod.POST)
    public Map<String, Object> Create(@RequestHeader Map<String, String> headers, String MerchantName) {
        try {
            String token = redisUtils.getUUID(headers.get("token"));
            if (token != null && MerchantName != null) {
                return JsonResponse(1, merchantService.LikeMerchantName(MerchantName), "获取商户信息成功!");
            } else {
                return JsonResponseStr(-1, "failed", "读取异常!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error" + e.getMessage(), "读取失败!");
        }
    }

    @ApiOperation("获取企业完善信息")
    @RequestMapping(value = "/getMerchantDetial", method = RequestMethod.POST)
    public Map<String, Object> GetMerchantDetial(@RequestHeader Map<String, String> headers) {
        try {
            MerchantDetails merchantDetails = merchantDetailsService.GetMerchantDetial(redisUtils.getUUID(headers.get("token")));
            if (merchantDetails != null){
                return JsonResponseMap(1, objectMap(merchantDetails), "获取信息成功!");
            }else{
                return JsonResponseMap(1, new HashMap<>(), "未完善信息!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    @ApiOperation("修改企业完善信息")
    @RequestMapping(value = "/changeMerchantDetial", method = RequestMethod.POST)
    public Map<String, Object> ChangeMerchantDetial(@RequestHeader Map<String, String> headers, MerchantDetails merchantDetails) {
        try {
            merchantDetailsService.ChangeMerchantDetial(merchantDetails, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "更新信息成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
