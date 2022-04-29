package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.Channel;
import com.dolphin.saas.entity.Merchant;
import com.dolphin.saas.mapper.ChannelMapper;
import com.dolphin.saas.mapper.MerchantMapper;
import com.dolphin.saas.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("channelService")
public class ChannelServiceimpl extends BaseTools implements ChannelService {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Channel createChannel(Channel channel) throws Exception {
        Channel channel1 = new Channel();
        try {
            channel1.setChannelCreateTime(new Date());
            channel1.setChannelName(channel.getChannelName());
            channel1.setChannelType(channel.getChannelType());

            // 设置一个特殊的MD5
            Random random = new Random();
            String code = new Date() + "Dolphins" + random.nextInt(100000);
            channel1.setChannelHashUrl(DigestUtils.md5DigestAsHex(code.getBytes()));

            if (channelMapper.insert(channel1) < 1) {
                throw new Exception("创建渠道失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return channel1;
    }

    @Override
    public Map<String, Object> FindChannelLists(int Page, int Size) throws Exception {
        // 拼装分页数据
        Map<String, Object> results = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<Channel> page = new Page<>(Page, Size);
            QueryWrapper<Channel> queryWrapper = new QueryWrapper<>();

            // 只展示没有被逻辑删除的数据
            channelMapper.selectPage(page, queryWrapper);
            List<Map<String, Object>> records = new ArrayList<>();
            List<Channel> channelList = page.getRecords();

            if (page.getRecords().size() > 0) {
                for (Channel channel : channelList) {
                    Map<String, Object> items = objectMap(channel);
                    QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("merchant_source_type", 2);
                    queryWrapper1.eq("merchant_source_id", channel.getId());
                    queryWrapper1.select("merchant_id");
                    items.put("channelMerchant", merchantMapper.selectCount(queryWrapper1));
                    records.add(items);
                }
            }
            results.put("page", Page);
            results.put("total", page.getTotal());
            results.put("list", records);
            results.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeChannel(Channel channel) throws Exception {
        try {
            QueryWrapper<Channel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", channel.getId());
            if (channelMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("没有这个渠道!");
            }

            if (channel.getChannelStatus() == null || channel.getChannelStatus() < 0 || channel.getChannelStatus() > 1) {
                throw new Exception("无效状态!");
            }

            Channel channel1 = new Channel();
            channel1.setId(channel.getId());
            channel1.setChannelStatus(channel.getChannelStatus());

            if (channelMapper.update(channel1, queryWrapper) < 1){
                throw new Exception("更新状态失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> readChannel(Long channelId) throws Exception {
        Map<String, Object> results;
        try {
            // 获取分页的数据
            QueryWrapper<Channel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", channelId);
            Channel channel = channelMapper.selectOne(queryWrapper);
            if (channel == null) {
                throw new Exception("渠道获取失败!");
            }
            results = this.objectMap(channel);

            // 获取归属的商户
            QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("merchant_source_type", 2);
            queryWrapper1.eq("merchant_source_id", channel.getId());
            queryWrapper1.select("merchant_id", "merchant_name", "merchant_status");
            List<Merchant> merchantList = merchantMapper.selectList(queryWrapper1);
            results.put("channelMerchantLists", merchantList);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
