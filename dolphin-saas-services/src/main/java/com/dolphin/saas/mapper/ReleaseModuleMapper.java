package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.Engineer;
import com.dolphin.saas.entity.EngineerLanguage;
import com.dolphin.saas.entity.ReleaseModule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseModuleMapper extends BaseMapper<ReleaseModule> {

    /**
     * 获取模块对应的开发语言
     * @param queryWrapper
     * @return
     */
    @Select("SELECT language_name FROM ht_engineer INNER JOIN ht_engineer_language ON ht_engineer_language.language_id = ht_engineer.engineer_language_id ${ew.customSqlSegment}")
    EngineerLanguage FindEnginnerLanguage(@Param(Constants.WRAPPER) Wrapper<Engineer> queryWrapper);
}
