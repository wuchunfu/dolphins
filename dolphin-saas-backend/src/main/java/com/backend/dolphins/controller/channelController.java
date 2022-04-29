package com.backend.dolphins.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.entity.Channel;
import com.dolphin.saas.service.ChannelService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/channel")
public class channelController extends MasterCommon {

    private final String URL = "http://";

    @Resource
    private ChannelService channelService;

    /**
     * 获取渠道列表
     *
     * @param page 页码
     * @param size 数据长度
     * @return
     */
    @ApiOperation("获取渠道列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(Integer page, Integer size) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 10) {
                size = 10;
            }
            return JsonResponseMap(1, channelService.FindChannelLists(page, size), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList(), e.getMessage());
        }
    }

    /**
     * 查看单个渠道
     *
     * @param channelId 渠道id
     * @return
     */
    @ApiOperation("查看单个渠道")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(Long channelId) {
        try {
            Channel channel = new Channel();
            channel.setId(channelId);
            Map<String, Object> resultMap = channelService.readChannel(channelId);
            return JsonResponseMap(1, resultMap, "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 创建一个新的渠道
     *
     * @return
     */
    @ApiOperation("创建一个新的渠道")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public Map<String, Object> Create(Channel channel) {
        try {
            Channel channel1 = channelService.createChannel(channel);
            String url = URL + channel1.getChannelHashUrl();
            return JsonResponseStr(1, url, "创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 切换渠道状态
     *
     * @param channel
     * @return
     */
    @ApiOperation("切换渠道状态")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public Map<String, Object> Change(Channel channel) {
        try {
            if (channel == null) {
                throw new Exception("要设置更新的渠道信息!");
            }

            if (channel.getChannelStatus() == null || channel.getId() == null) {
                throw new Exception("渠道ID或渠道状态不能为空!");
            }

            channelService.changeChannel(channel);
            return JsonResponseStr(1, "ok", "切换成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

}
