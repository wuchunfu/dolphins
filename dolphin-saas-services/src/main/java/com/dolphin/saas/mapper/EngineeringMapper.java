package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.Engineer;
import com.dolphin.saas.entity.Release;
import com.dolphin.saas.entity.vo.DashboardLists;
import com.dolphin.saas.entity.vo.EngneerLabelValueSelect;
import com.dolphin.saas.entity.vo.SelectVal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface EngineeringMapper extends BaseMapper<Engineer> {

    /**
     * 工具标准大盘数据统计
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT engineer_name name, engineer_status status, engineer_createtime time FROM ht_engineer ${ew.customSqlSegment}")
    ArrayList<DashboardLists> engineerCountArr(@Param(Constants.WRAPPER) Wrapper<Engineer> queryWrapper);

    /**
     * 下拉获取所有可用的项目
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT engineer_name label, engineer_id value, language_name language FROM ht_engineer INNER JOIN ht_engineer_language ON ht_engineer_language.language_id = ht_engineer.engineer_language_id ${ew.customSqlSegment}")
    ArrayList<EngneerLabelValueSelect> engineerSelectVals(@Param(Constants.WRAPPER) Wrapper<Engineer> queryWrapper);

    /**
     * 获取工程发布过哪些环境
     */
    @Select("select release_job_namespace FROM ht_release_jobs ${ew.customSqlSegment}  group by release_job_namespace")
    ArrayList<Release> engineerReleaseNameSpace(@Param(Constants.WRAPPER) Wrapper<Release> queryWrapper);
}
