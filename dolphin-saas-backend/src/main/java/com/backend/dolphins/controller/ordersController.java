package com.backend.dolphins.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.inputs.CreateReleaseInputs;
import com.dolphin.saas.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Api(tags = "订单相关接口", description = "7个")
public class ordersController extends MasterCommon {

    @Resource
    private OrderService orderService;

    /**
     * 订单列表接口
     *
     * @param page 页码
     * @param size 数据长度
     * @return
     */
    @ApiOperation("订单列表接口")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer size) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 10) {
                size = 10;
            }
            return JsonResponseMap(1, orderService.FindOrdersLists(page, size), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList(), e.getMessage());
        }
    }
}
