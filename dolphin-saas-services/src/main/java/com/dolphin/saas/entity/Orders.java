package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 订单表
 */

@Data
@TableName(value = "ht_orders")
public class Orders {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("order_id")
    private String orderId;

    @TableField("order_type_id")
    private Integer orderTypeId;

    @TableField("order_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;

    @TableField("order_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderUpdateTime;

    @TableField("order_paytime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderPayTime;

    @TableField("order_money")
    private Float orderMoney;

    @TableField("order_status")
    private Integer orderStatus;

    @TableField("order_source")
    private Integer orderSource;

    @TableField("order_repty_code")
    private String orderReptyCode;

    @TableField("order_transaction_id")
    private String orderTransactionId;

    @TableField("order_pay_mode")
    private Integer orderPayMode;

    @TableField("order_goods_id")
    private String orderGoodsId;

    @TableField("order_delete")
    private Integer orderDelete;

    @TableField("uuid")
    private String uuid;

    @TableField("order_detials")
    private String orderDetials;
}
