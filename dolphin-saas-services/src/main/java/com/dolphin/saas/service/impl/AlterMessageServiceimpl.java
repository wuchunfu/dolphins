package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.AlterMessage;
import com.dolphin.saas.mapper.AlterMessageMapper;
import com.dolphin.saas.service.AlterMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("alterMessageService")
public class AlterMessageServiceimpl extends BaseTools implements AlterMessageService {

    @Resource
    private AlterMessageMapper alterMessageMapper;

    @Override
    public void createAlterMessage(String code, Integer type, String uuid) throws Exception {
        try {
            QueryWrapper<AlterMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("key_info", code);

            if (alterMessageMapper.selectCount(queryWrapper) > 0) {
                throw new Exception("已有，不用重复创建!");
            }

            AlterMessage alterMessage = new AlterMessage();
            alterMessage.setCreateTime(new Date());
            alterMessage.setKeyStatus(0);
            alterMessage.setUuid(uuid);
            alterMessage.setKeyInfo(code);
            alterMessage.setKeyType(type);

            if (alterMessageMapper.insert(alterMessage) < 1) {
                throw new Exception("创建告警条目失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void changeAlterMessageStatus(Long id, Integer keyStatus, String uuid) throws Exception {
        try {
            QueryWrapper<AlterMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("id", id);
            if (alterMessageMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("当前告警条目不存在!");
            }

            AlterMessage alterMessage = new AlterMessage();
            alterMessage.setKeyStatus(keyStatus);

            if (alterMessageMapper.update(alterMessage, queryWrapper) < 1) {
                throw new Exception("当前告警条目状态更新失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void deleteAlterMessage(Long id, String uuid) throws Exception {
        try {
            QueryWrapper<AlterMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("id", id);
            if (alterMessageMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("当前告警条目不存在!");
            }

            if (alterMessageMapper.delete(queryWrapper) < 1) {
                throw new Exception("删除告警条目失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> AlterMessageLists(int Page, int Size, String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<AlterMessage> page = new Page<>(Page, Size);
            QueryWrapper<AlterMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);

            alterMessageMapper.selectPage(page, queryWrapper);
            response.put("page", Page);
            response.put("total", page.getTotal());
            response.put("list", page.getRecords());
            response.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return response;
    }
}
