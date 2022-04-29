package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dolphin.saas.entity.GitlabCommits;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public interface GitlabCommitsMapper extends BaseMapper<GitlabCommits> {

    /**
     * 查询人效统计
     *
     * @param days 天数
     * @return
     */
    @Select("SELECT jobs_author name, count(*) value FROM ht_gitlab_commits WHERE DATE_SUB(CURDATE(), INTERVAL #{days} DAY) <= jobs_create_time GROUP BY jobs_author")
    ArrayList<Map<String, Object>> selectPeopleEfficiency(@Param("days") int days);
}
