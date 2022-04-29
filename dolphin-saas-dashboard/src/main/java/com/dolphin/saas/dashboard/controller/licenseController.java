package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.service.LicenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/license")
@Api(tags = "license购买相关的接口", description = "1个")
public class licenseController extends MasterCommon {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private LicenseService licenseService;

    /**
     * 授权情况统计
     *
     * @return
     */
    @ApiOperation("获取License授权列表")
    @RequestMapping(value = "/LicenseLists", method = RequestMethod.POST)
    public Map<String, Object> GetLicense(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            return JsonResponseMap(1, licenseService.FindLicenseLists(page, pageSize, redisUtils.getUUID(headers.get("token"))), "获取数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }


    /**
     * 创建License授权
     *
     * @return
     */
    @ApiOperation("创建License授权")
    @RequestMapping(value = "/createLicense", method = RequestMethod.POST)
    public Map<String, Object> CreateLicense(@RequestHeader Map<String, String> headers, Integer Type) {
        try {
            licenseService.CreateLicense(Type, redisUtils.getUUID(headers.get("token")));
            if (Type == 1) {
                return JsonResponseStr(1, "ok", "创建License授权成功!");
            } else {
                return JsonResponseStr(1, "ok", "请等待，稍后销售联系您!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
