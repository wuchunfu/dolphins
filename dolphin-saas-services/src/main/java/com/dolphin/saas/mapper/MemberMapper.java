package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.Orders;
import com.dolphin.saas.entity.User;
import com.dolphin.saas.entity.vo.MemberList;
import com.dolphin.saas.entity.vo.OrderLists;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberMapper extends BaseMapper<User> {

    /**
     * 获取所有用户数据
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select("SELECT id, uuid, u_name, u_phone, u_email, u_login_status, u_updatetime, u_createtime, u_lastlogin_time, u_delete, merchant_id, u_login_username FROM ht_users ${ew.customSqlSegment}")
    IPage<MemberList> selectMembersPage(IPage<MemberList> page, @Param(Constants.WRAPPER) Wrapper<User> queryWrapper);
}
