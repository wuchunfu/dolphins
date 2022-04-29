package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class OrderLists {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("order_type_id")
    private Integer orderTypeId;

    @TableField("order_createtime")
    private Date orderCreatetime;

    @TableField("order_updatetime")
    private Date orderUpdatetime;

    @TableField("order_paytime")
    private Date orderPaytime;

    @TableField("order_money")
    private Float orderMoney;

    @TableField("order_status")
    private Integer orderStatus;

    @TableField("order_source")
    private Integer orderSource;

    @TableField("order_transaction_id")
    private String orderTransactionId;

    @TableField("order_repty_code")
    private String orderReptyCode;

    @TableField("order_goods_id")
    private Long orderGoodsId;

    @TableField("order_user_name")
    private String orderUserName;

    @TableField("order_user_phone")
    private String orderUserPhone;
}
