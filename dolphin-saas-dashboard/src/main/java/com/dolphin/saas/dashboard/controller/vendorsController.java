package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.aliyun.feature.AccountServ;
import com.dolphin.saas.commons.clouds.aliyun.feature.VpcServ;
import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.AccountServPlugin;
import com.dolphin.saas.commons.clouds.tencent.VpcServPlugin;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.Vendors;
import com.dolphin.saas.service.VendorsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ventors")
@Api(tags = "云厂商密钥管理接口", description = "5个")
public class vendorsController extends MasterCommon {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private VendorsService vendorsService;

    /**
     * 判断有没有厂商密钥存在
     *
     * @return
     */
    @ApiOperation("判断有没有厂商密钥存在")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {
            if (vendorsService.CheckCloudConf(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "有厂商数据!");
            } else {
                return JsonResponseStr(-1, "failed", "没有厂商数据!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 创建云厂商配置
     *
     * @param vendors
     * @return
     */
    @ApiOperation("创建云厂商配置")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> Create(@RequestHeader Map<String, String> headers, Vendors vendors, Integer cloudId) {
        try {
            if (vendors == null) {
                throw new Exception("创建失败,不能不填写云厂商信息!");
            }
            vendors.setTypeName(cloudId);
            if (vendors.getTypeName() == null || vendors.getAccessKey() == null || vendors.getAccessSecret() == null) {
                throw new Exception("创建失败,密钥基础信息不能为空!");
            }
            switch (vendors.getTypeName()){
                case 1:
                    // 阿里云
                    new AccountServ(vendors.getAccessKey(), vendors.getAccessSecret())
                            .run();
                    break;
                case 2:
                    // 腾讯云
                    new AccountServPlugin(vendors.getAccessKey(), vendors.getAccessSecret()).runner();
                    break;
            }

            vendorsService.CreateCloudConf(vendors, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "秘钥无效，请确认!");
        }
    }

    /**
     * 获取云厂商密钥列表
     *
     * @param page 页码
     * @param pageSize 数据长度
     * @return
     */
    @ApiOperation("获取云厂商密钥列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1, vendorsService.FindCloudLists(page, pageSize, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), "获取数据异常!");
        }
    }

    /**
     * 更改云厂商密钥信息
     *
     * @param vendors 云厂商信息
     * @return
     */
    @ApiOperation("更改云厂商密钥信息")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public Map<String, Object> Change(@RequestHeader Map<String, String> headers, Vendors vendors, Integer cloudId) {
        try {
            if (vendors == null) {
                throw new Exception("云厂商数据不能为空!");
            }

            vendors.setTypeName(cloudId);
            switch (vendors.getTypeName()){
                case 1:
                    // 阿里云
                    new AccountServ(vendors.getAccessKey(), vendors.getAccessSecret())
                            .run();
                    break;
                case 2:
                    // 腾讯云
                    new AccountServPlugin(vendors.getAccessKey(), vendors.getAccessSecret()).runner();
                    break;
            }

            vendorsService.UpdateCloudConf(vendors, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "success", "更新云厂商数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 读取云厂商密钥信息
     *
     * @param vid 云厂商id号
     * @return
     */
    @ApiOperation("读取云厂商密钥信息")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(@RequestHeader Map<String, String> headers, Integer vid) {
        try {
            if (vid == null) {
                throw new Exception("云厂商id没有输入!");
            }
            return JsonResponseObj(1, vendorsService.ReadCloudConf(vid, redisUtils.getUUID(headers.get("token"))), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", e.getMessage());
        }
    }

    /**
     * 获取云厂商下拉列表
     *
     * @return
     */
    @ApiOperation("获取云厂商下拉列表")
    @RequestMapping(value = "/ventorTypeOptions", method = RequestMethod.GET)
    public Map<String, Object> getVentorTypeOptionsList(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, vendorsService.VentorTypeOptions(redisUtils.getUUID(headers.get("token")), false), "读取成功!");
        } catch (Exception e) {
            return JsonResponse(1, new ArrayList(), e.getMessage());
        }
    }

    /**
     * 获取云厂商下拉列表(检查是否有可用的)
     *
     * @return
     */
    @ApiOperation("获取云厂商下拉列表（检查是否有可用的）")
    @RequestMapping(value = "/ventorTypeOptionsCheck", method = RequestMethod.GET)
    public Map<String, Object> getVentorTypeOptionsCheck(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponse(1, vendorsService.VentorTypeOptions(redisUtils.getUUID(headers.get("token")), true), "读取成功!");
        } catch (Exception e) {
            return JsonResponse(1, new ArrayList(), e.getMessage());
        }
    }

    /**
     * 根据云厂商id获取归属区域列表
     *
     * @return
     */
    @ApiOperation("根据云厂商id获取归属区域列表")
    @RequestMapping(value = "/ventorRegionsOptions", method = RequestMethod.POST)
    public Map<String, Object> getVentorRegionsOptions(Integer cloudId) {
        try {
            if (cloudId == null) {
                throw new Exception("云厂商id不能为空!");
            }

            ArrayList<Map<String, Object>> results = new ArrayList<>();

            switch (cloudId) {
                case 1:
                    results = new VpcServ(this.getALI_SECRETLD(), this.getALI_SECRETKEY()).regionsLists();
                    break;

                case 2:
                    results = new VpcServPlugin(this.getTX_SECRETLD(), this.getTX_SECRETKEY()).regionsLists();
                    break;
            }
            return JsonResponse(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", e.getMessage());
        }
    }

    /**
     * 根据云厂商id&归属区域id获取可用区信息
     *
     * @return
     */
    @ApiOperation("根据云厂商id&归属区域id获取可用区信息")
    @RequestMapping(value = "/ventorRegionsZoneOptions", method = RequestMethod.POST)
    public Map<String, Object> getVentorRegionsZoneOptions(Integer cloudId, String regionId) {
        try {
            if (cloudId == null || regionId == null) {
                throw new Exception("云厂商id或归属id不能为空!");
            }

            ArrayList<Map<String, Object>> results = new ArrayList<>();

            switch (cloudId) {
                case 1:
                    results = new VpcServ(this.getALI_SECRETLD(), this.getALI_SECRETKEY()).zoneLists(regionId);
                    break;
                case 2:
                    results = new VpcServPlugin(this.getTX_SECRETLD(), this.getTX_SECRETKEY()).zoneLists(regionId);
                    break;
            }
            return JsonResponse(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", e.getMessage());
        }
    }

    /**
     * 删除云厂商信息
     *
     * @return
     */
    @ApiOperation("删除云厂商")
    @RequestMapping(value = "/ventorDelete", method = RequestMethod.POST)
    public Map<String, Object> ventorDelete(@RequestHeader Map<String, String> headers, Integer vid) {
        try {
            if (vid == null) {
                throw new Exception("云厂商id不能为空!");
            }
            vendorsService.VentorDelete(vid, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "删除成功!");
        } catch (Exception e) {
            return JsonResponseStr(-1, "error", e.getMessage());
        }
    }
}
