package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_merchant_details")
public class MerchantDetails {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("identification_number")
    private String IdentificationNumber;

    @TableField("bank_name")
    private String BankName;

    @TableField("bank_account")
    private String BankAccount;

    @TableField("merchant_address")
    private String MerchantAddress;

    @TableField("createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date CreateTime;

    @TableField("updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date UpdateTime;

    @TableField("uuid")
    private String Uuid;

}
