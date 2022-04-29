package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.service.ClueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/clue")
@Api(tags = "线索接口", description = "7个")
public class clueController extends MasterCommon {

    @Resource
    private ClueService clueService;

    /**
     * 新增线索
     *
     * @return
     */
    @ApiOperation("新增线索")
    @RequestMapping(value = "/reservationInfo", method = RequestMethod.POST)
    public Map<String, Object> ReservationInfo(String userName, String phoneNumber, String company, Integer companySize) {
        try {
            clueService.saveReservationInfo(userName, phoneNumber, company, companySize);
            return JsonResponseStr(1, "ok", "提交成功，我们将在24小时内联系您!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
