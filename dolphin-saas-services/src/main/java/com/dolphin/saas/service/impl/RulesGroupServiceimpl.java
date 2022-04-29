package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.RulesGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("rulesGroupService")
public class RulesGroupServiceimpl extends BaseTools implements RulesGroupService {

    @Resource
    private RulesGroupMapper rulesGroupMapper;

    @Resource
    private EngineeringMapper engineeringMapper;

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private RuleInfoMapper ruleInfoMapper;

    @Resource
    private RuleGroupInfoMapper ruleGroupInfoMapper;

    @Override
    public Map<String, Object> FindRulesGroupLists(int Pages, int Size, String uuid) {
        // 获取分页的数据
        IPage<RuleGroup> page = new Page<>(Pages, Size);
        QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        // 只展示没有被逻辑删除的数据
        queryWrapper.eq("rules_delete", 0);
        rulesGroupMapper.selectPage(page, queryWrapper);

        // 拼装分页数据
        Map<String, Object> results = new HashMap<>();
        results.put("page", Pages);
        results.put("total", page.getTotal());
        List<RuleGroup> ruleGroupList = page.getRecords();
        ArrayList<Map<String, Object>> records = new ArrayList<>();
        if (ruleGroupList.size() > 0) {
            for (RuleGroup ruleGroup : ruleGroupList) {
                Map<String, Object> items = this.objectMap(ruleGroup);
                if (ruleGroup.getRulesType() == 0) {
                    items.put("RulesType", "谨慎策略");
                } else {
                    items.put("RulesType", "放松策略");
                }
                records.add(items);
            }
        }
        results.put("list", records);
        results.put("pageSize", Size);
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void CreateRulesGroup(RuleGroup ruleGroup, ArrayList<String> ruleInfos, String uuid) throws Exception {
        try {
            // 查询这个发布规则组是否已经存在
            QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rules_name", ruleGroup.getRulesName());
            queryWrapper.eq("uuid", uuid);
            if (rulesGroupMapper.selectCount(queryWrapper) > 0) {
                throw new Exception("发布规则已存在,名字不能重复!");
            }

            // 创建规则
            ruleGroup.setRulesStatus(1);
            ruleGroup.setRulesCreatetime(new Date());
            ruleGroup.setUuid(uuid);

            if (rulesGroupMapper.insert(ruleGroup) > 0) {
                // 创建规则的顺序发布策略
                for (String ruleInfo : ruleInfos) {
                    RuleGroupInfo ruleGroupInfo = new RuleGroupInfo();
                    ruleGroupInfo.setRulesInfoId(Long.parseLong(ruleInfo));
                    ruleGroupInfo.setRid(ruleGroup.getRid());
                    if (ruleGroupInfoMapper.insert(ruleGroupInfo) < 1) {
                        throw new Exception("创建发布策略失败!");
                    }
                }
            }
        } catch (Exception e) {
            log.error("CreateRulesGroup:" + e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ArrayList<RuleInfo> GetRulesLists() throws Exception {
        try {
            return new ArrayList<>(ruleInfoMapper.selectList(null));
        } catch (Exception e) {
            log.error("GetRulesLists:" + e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> UpdateRulesGroup(int Rid, int Status, String uuid) {
        Map<String, Object> results = new HashMap<>();
        // 构建需要更新的内容
        RuleGroup newData = new RuleGroup();
        newData.setRulesStatus(Status);
        // 获取所有用了这个规则策略的工程
        QueryWrapper<Engineer> engineerQueryWrapper = new QueryWrapper<>();
        engineerQueryWrapper.eq("engineer_release_rules_id", Rid);
        engineerQueryWrapper.eq("uuid", uuid);
        engineerQueryWrapper.select("engineer_name");
        List<Engineer> engineerList = engineeringMapper.selectList(engineerQueryWrapper);
        if (engineerList.size() > 0) {
            for (Engineer engineer : engineerList) {
                QueryWrapper<Release> releaseQueryWrapper = new QueryWrapper<>();
                releaseQueryWrapper.eq("release_job_name", engineer.getEngineerName());
                releaseQueryWrapper.eq("uuid", uuid);
                releaseQueryWrapper.le("release_job_status", 4);

                // 如果有还没发布的任务，就不能切换
                if (deployMapper.selectCount(releaseQueryWrapper) > 0) {
                    results.put("error", "有在发布的任务，不能切换!");
                    return results;
                }
            }
        }
        // 构建查询参数
        QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rid", Rid);
        queryWrapper.eq("uuid", uuid);
        if (rulesGroupMapper.update(newData, queryWrapper) < 1) {
            results.put("error", "更新发布规则失败!");
        }
        return results;
    }

    @Override
    public Boolean CheckRulesGroup(String uuid) {
        QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        // 找出状态存活的规则
        queryWrapper.eq("rules_status", 1);
        return rulesGroupMapper.selectCount(queryWrapper) >= 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void DeleteRulesGroup(int Rid, String uuid) throws Exception {
        try {
            QueryWrapper<RuleGroup> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rid", Rid);
            queryWrapper.eq("uuid", uuid);
            if (rulesGroupMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("规则不存在!");
            }
            // 逻辑删除规则
            RuleGroup ruleGroup = new RuleGroup();
            ruleGroup.setRulesDelete(1);
            ruleGroup.setRulesUpdatetime(new Date());
            if (rulesGroupMapper.update(ruleGroup, queryWrapper) < 1) {
                throw new Exception("删除规则失败!");
            }
        } catch (Exception e) {
            log.error("DeleteRulesGroup:" + e);
            throw new Exception(e.getMessage());
        }
    }
}
