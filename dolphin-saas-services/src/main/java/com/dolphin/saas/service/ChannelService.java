package com.dolphin.saas.service;

import com.dolphin.saas.entity.Channel;

import java.util.Map;

public interface ChannelService {

    // 创建渠道 -- 后台API
    Channel createChannel(Channel channel) throws Exception;

    // 渠道列表 -- 后台API
    Map<String, Object> FindChannelLists(int Page, int Size) throws Exception;

    // 切换渠道状态 -- 后台API
    void changeChannel(Channel channel) throws Exception;

    // 查看渠道归属信息 -- 后台API
    Map<String, Object> readChannel(Long channelId) throws Exception;

}
