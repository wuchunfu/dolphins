package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.service.AlterMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/alters")
@Api(tags = "通知管理接口", description = "4个")
public class alterController extends MasterCommon {
    @Resource
    private AlterMessageService alterMessageService;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 新增提醒归属
     *
     * @return
     */
    @ApiOperation("新增提醒功能")
    @RequestMapping(value = "/createAlterMessage", method = RequestMethod.POST)
    public Map<String, Object> CreateAlterMessage(@RequestHeader Map<String, String> headers, String code, Integer type) {
        try {
            alterMessageService.createAlterMessage(code, type, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "新增成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 开启关闭提醒功能
     *
     * @return
     */
    @ApiOperation("开启关闭提醒功能")
    @RequestMapping(value = "/changeAlterMessageStatus", method = RequestMethod.POST)
    public Map<String, Object> ChangeAlterMessageStatus(@RequestHeader Map<String, String> headers, Long id, Integer keyStatus) {
        try {
            alterMessageService.changeAlterMessageStatus(id, keyStatus, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "修改成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 删除提醒功能
     *
     * @return
     */
    @ApiOperation("删除提醒功能")
    @RequestMapping(value = "/deleteAlterMessage", method = RequestMethod.POST)
    public Map<String, Object> DeleteAlterMessage(@RequestHeader Map<String, String> headers, Long id) {
        try {
            alterMessageService.deleteAlterMessage(id, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "删除成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 提醒列表功能
     *
     * @return
     */
    @ApiOperation("提醒列表功能")
    @RequestMapping(value = "/alterMessageLists", method = RequestMethod.POST)
    public Map<String, Object> AlterMessageLists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            return JsonResponseMap(1, alterMessageService.AlterMessageLists(page, pageSize, redisUtils.getUUID(headers.get("token"))), "读取列表成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
