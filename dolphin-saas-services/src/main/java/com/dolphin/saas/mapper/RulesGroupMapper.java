package com.dolphin.saas.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.RuleGroup;
import com.dolphin.saas.entity.vo.RulesInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesGroupMapper extends BaseMapper<RuleGroup> {

    /**
     * 获取发布规则细节顺序内容
     * @param queryWrapper
     * @return
     */
    @Select("SELECT rules_info_sort, rules_info_title FROM ht_release_rules_group_info LEFT JOIN ht_release_rules_info ON ht_release_rules_info.rules_info_id = ht_release_rules_group_info.rules_info_id ${ew.customSqlSegment}")
    List<RulesInfo> rulesObject(@Param(Constants.WRAPPER) Wrapper<RulesInfo> queryWrapper);
}
