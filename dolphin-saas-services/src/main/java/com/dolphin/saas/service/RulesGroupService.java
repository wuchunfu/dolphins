package com.dolphin.saas.service;

import com.dolphin.saas.entity.RuleGroup;
import com.dolphin.saas.entity.RuleInfo;

import java.util.ArrayList;
import java.util.Map;

public interface RulesGroupService {
    // 分页获取所有的规则列表
    Map<String, Object> FindRulesGroupLists(int Page, int Size, String uuid);

    // 创建规则组
    void CreateRulesGroup(RuleGroup ruleGroup, ArrayList<String> ruleInfos, String uuid) throws Exception;

    // 获取策略列表
    ArrayList<RuleInfo> GetRulesLists() throws Exception;

    // 更新规则状态
    Map<String, Object> UpdateRulesGroup(int Rid, int Status, String uuid);

    // 检查有多少条规则存在
    Boolean CheckRulesGroup(String uuid);

    // 删除发布策略
    void DeleteRulesGroup(int Rid, String uuid) throws Exception;
}
