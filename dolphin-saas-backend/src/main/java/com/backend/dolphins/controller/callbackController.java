package com.backend.dolphins.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.feature.PayServ;
import com.dolphin.saas.service.OrderService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/callback")
public class callbackController extends MasterCommon {

    @Resource
    private OrderService orderService;

    /**
     * 支付回调
     *
     * @return
     */
    @ApiOperation("支付回调")
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public Map<String, Object> CallbackFunc(HttpServletRequest request) {
        try {
            InputStream inStream = request.getInputStream();
            BufferedReader in;
            String result = "";
            in = new BufferedReader(
                    new InputStreamReader(inStream));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();

            PayServ payServ = new PayServ();
            Map<String, Object> callInfo = payServ.callBackDecrypt(result);
            String orderId = callInfo.get("out_trade_no").toString();
            String transactionId = callInfo.get("transaction_id").toString();
            // 更新订单的状态回调
            orderService.UpdateOrderInfo(orderId, transactionId);

            return JsonResponseStr(1, "ok", "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "failed", e.getMessage());
        }
    }
}
