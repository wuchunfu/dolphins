package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.Orders;
import com.dolphin.saas.entity.vo.OrderLists;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersMapper extends BaseMapper<Orders> {


    /**
     * 获取订单数据
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select("SELECT ht_orders.id id, order_id, order_type_id, order_createtime, order_updatetime, order_paytime, order_money, order_status, order_source, order_transaction_id, order_repty_code, order_goods_id, ht_users.u_name order_user_name, ht_users.u_phone order_user_phone FROM ht_orders LEFT JOIN ht_users ON ht_users.uuid = ht_orders.uuid ${ew.customSqlSegment}")
    IPage<OrderLists> selectOrdersPage(IPage<OrderLists> page, @Param(Constants.WRAPPER) Wrapper<Orders> queryWrapper);
}
