package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.EngineerDockerfile;
import com.dolphin.saas.entity.vo.DockerLists;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface DockerfileMapper extends BaseMapper<EngineerDockerfile> {

    /**
     * Dockerfile列表
     */
    @Select("SELECT dockerfile_id, dockerfile_name, dockerfile_os_path, dockerfile_status, dockerfile_remark, dockerfile_author_id, dockerfile_money, dockerfile_createtime, dockerfile_updatetime, ht_engineer_language.language_name dockerfile_language_name, ht_engineer_framework.framework_name dockerfile_framework_name FROM ht_engineer_dockerfile INNER JOIN ht_engineer_language ON ht_engineer_language.language_id = ht_engineer_dockerfile.dockerfile_language_id INNER JOIN ht_engineer_framework ON ht_engineer_framework.framework_id = ht_engineer_dockerfile.dockerfile_framework_id")
    IPage<DockerLists> selectDockerfilePage(IPage<DockerLists> page, @Param(Constants.WRAPPER) Wrapper<EngineerDockerfile> queryWrapper);
}
