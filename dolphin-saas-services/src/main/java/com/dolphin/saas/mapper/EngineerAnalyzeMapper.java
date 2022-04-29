package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.AnalyzeFileLine;
import com.dolphin.saas.entity.EngineerAnalyze;
import com.dolphin.saas.entity.EngineerDockerfile;
import com.dolphin.saas.entity.vo.AnalyzeQuaLists;
import com.dolphin.saas.entity.vo.DockerLists;
import com.dolphin.saas.entity.vo.NameValLists;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface EngineerAnalyzeMapper extends BaseMapper<EngineerAnalyze> {
    /**
     * 获取工程前100个质量问题统计
     *
     * @param queryWrapper
     * @return
     */
    @Select("select engineer_rule, count(engineer_rule) counts FROM ht_engineer_analyze ${ew.customSqlSegment} group by engineer_rule order by counts desc limit 100")
    ArrayList<AnalyzeQuaLists> analyzeQuaSelect(@Param(Constants.WRAPPER) Wrapper<EngineerAnalyze> queryWrapper);

    /**
     * 获取指定工程对应质量问题文件排名
     * @param queryWrapper
     * @return
     */
    @Select("select engineer_searchfile names, count(engineer_searchfile) counts FROM ht_engineer_analyze ${ew.customSqlSegment} group by engineer_searchfile order by counts desc")
    IPage<NameValLists> analyzeQuaSourceTops(IPage<NameValLists> page, @Param(Constants.WRAPPER) Wrapper<EngineerAnalyze> queryWrapper);

    /**
     * 查文件存在的问题类型
     * @param queryWrapper
     * @return
     */
    @Select("select engineer_type FROM ht_engineer_analyze ${ew.customSqlSegment} group by engineer_type")
    ArrayList<EngineerAnalyze> analyzeQuaFileQuestionType(@Param(Constants.WRAPPER) Wrapper<EngineerAnalyze> queryWrapper);
}
