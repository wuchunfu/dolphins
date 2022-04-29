package com.backend.dolphins.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.service.DashboardService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class dashboardController extends MasterCommon {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取大盘统计
     *
     * @return
     */
    @ApiOperation("获取大盘统计")
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public Map<String, Object> DashboardIndex(@RequestHeader Map<String, String> headers) {
        try {
            Map<String, Object> results = new HashMap<String, Object>();

            Map<String, Integer> merchantObj = dashboardService.merchantCount();
            Map<String, Integer> ordersObj = dashboardService.orderCount();

            Integer merchantCount = merchantObj.get("merchantCount");
            Integer certifiedCompanyCount = merchantObj.get("certifiedCompanyCount");
            Integer paidOrderCount = ordersObj.get("paidOrderCount");
            Integer paidOrderAmountCount = ordersObj.get("paidOrderAmountCount");
            Integer accumulatedProfitCount = ordersObj.get("accumulatedProfitCount");
            Integer clusterCount = dashboardService.clusterCount();
            Integer releasesCount = dashboardService.releasesCount();
            Integer userCount = dashboardService.memberCount();

            results.put("merchantCount", merchantCount);
            results.put("certifiedCompanyCount", certifiedCompanyCount);
            results.put("paidOrderCount", paidOrderCount);
            results.put("paidOrderAmountCount", paidOrderAmountCount);
            results.put("accumulatedProfitCount", accumulatedProfitCount);
            results.put("clusterCount", clusterCount);
            results.put("releasesCount", releasesCount);
            results.put("userCount", userCount);

            return JsonResponseMap(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "failed", e.getMessage());
        }
    }
}
