/**
  用户部分
 */
CREATE TABLE `ht_users`
(
    `id`                      bigint auto_increment NOT NULL COMMENT '用户主id',
    `uuid`                    char(32)    NOT NULL COMMENT '用户身份id',
    `u_name`                  varchar(68) NOT NULL COMMENT '中文名',
    `u_phone`                 bigint        DEFAULT NULL COMMENT '手机号',
    `u_login_password`        char(32)    NOT NULL COMMENT '密码',
    `u_login_username`        varchar(68) NOT NULL COMMENT '用户名',
    `u_login_status`          tinyint       DEFAULT 1 COMMENT '状态,0:未启用,1:启用',
    `u_email`                 varchar(200)  DEFAULT '' COMMENT '邮箱',
    `u_createtime`            datetime    NOT NULL COMMENT '创建时间',
    `u_updatetime`            datetime      DEFAULT NULL COMMENT '更新时间',
    `u_lastlogin_time`        datetime      DEFAULT NULL COMMENT '最后登陆时间',
    `u_delete`                tinyint       DEFAULT 0 COMMENT '逻辑删除,0:正常,1:删除',
    `u_header_img`            varchar(255)  DEFAULT null COMMENT '头像',
    `merchant_id`             bigint        DEFAULT 0 COMMENT '商户id',
    `u_identity`              tinyint(1) DEFAULT 0 COMMENT '身份,0:个人用户,1:企业用户',
    `u_money`                 DECIMAL(5, 2) DEFAULT 0 COMMENT '余额',
    `u_perfect_info`          tinyint(1) DEFAULT 0 COMMENT '是否完善信息,0:未完善,1:已完善',
    `u_user_service_type`     tinyint(1) DEFAULT 0 COMMENT '用户版本,0:免费版,1:基础版,2:高级版',
    `u_user_service_timeline` datetime      DEFAULT NULL COMMENT '过期时间,如果免费版本，这个无效字段',
    PRIMARY KEY (`id`, `uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户数据表';

CREATE TABLE `ht_users_active_record`
(
    `id`           bigint      NOT NULL COMMENT '用户主id',
    `a_info`       varchar(68) NOT NULL COMMENT '内容',
    `a_createtime` datetime    NOT NULL COMMENT '创建时间',
    `a_delete`     tinyint DEFAULT 0 COMMENT '逻辑删除,0:正常,1:删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户活动记录表';

CREATE TABLE `ht_users_login_logs`
(
    `id`               bigint   NOT NULL COMMENT '用户主id',
    `login_createtime` datetime NOT NULL COMMENT '登陆时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户登陆日志表';

/**
  商户部分
 */

CREATE TABLE `ht_merchant`
(
    `merchant_id`              bigint       NOT NULL auto_increment PRIMARY KEY COMMENT 'id',
    `merchant_name`            varchar(255) NOT NULL COMMENT '商户名称',
    `merchant_type`            tinyint(1) DEFAULT 0 COMMENT '商户类型,0:微型商户,1:小型商户,2:中型商户,3:大型商户',
    `merchant_attributes`      tinyint(1) DEFAULT 0 COMMENT '商户属性,0:体验商户,1:天使商户,2:付费商户,3:免费商户,4:合作商户',
    `merchant_status`          mediumint(2) DEFAULT 0 COMMENT '状态,0:待审核,1:审核中,2:审核失败,3:启用,4:不启用,5:无效,6:欠费停用',
    `merchant_delete_status`   tinyint(1) DEFAULT 0 COMMENT '逻辑删除状态,0:不删除,1:删除',
    `merchant_license_picture` varchar(255) DEFAULT null COMMENT '营业执照图片',
    `merchant_createtime`      datetime     not null COMMENT '创建时间',
    `merchant_updtetime`       datetime     default null COMMENT '更新时间',
    `merchant_remark`          varchar(255) default '无' COMMENT '备注',
    `merchant_source_type`     tinyint(1) default 1 COMMENT '商户来源,1:自然来源,2:合作方/下游渠道',
    `merchant_source_id`       int          default 0 COMMENT '商户来源的ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商户表';

/**
  商户审核拒绝表
 */
CREATE TABLE `ht_merchant_audit_failed_logs`
(
    `merchant_id`                bigint   NOT NULL COMMENT '商户id',
    `merchant_audit_info`        varchar(255) default '无' COMMENT '审核失败的理由',
    `merchant_audit_createtime`  datetime NOT NULL COMMENT '创建时间',
    `merchant_audit_customer_id` smallint NOT NULL COMMENT '审核人id，内部用户',
    primary key (`merchant_id`, `merchant_audit_createtime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商户审核拒绝表';

/**
  商户来源表
 */
CREATE TABLE `ht_merchant_sources`
(
    `merchant_source_id`            smallint auto_increment NOT NULL PRIMARY KEY COMMENT '商户来源id',
    `merchant_source_name`          varchar(255) NOT NULL COMMENT '商户来源渠道名称',
    `merchant_source_phone`         bigint       NOT NULL COMMENT '商户来源渠道对接人电话',
    `merchant_source_createtime`    datetime     NOT NULL COMMENT '创建时间',
    `merchant_source_updatetime`    datetime     default NULL COMMENT '更新时间',
    `merchant_source_delete_status` tinyint(1) DEFAULT 0 COMMENT '逻辑删除状态,0:不删除,1:删除',
    `merchant_source_remark`        varchar(255) default '无' COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商户来源表';

/**
  商户用户组织表
 */
CREATE TABLE `ht_merchant_organization`
(
    `merchant_id`     bigint   NOT NULL COMMENT '商户id',
    `uuid`            char(32) NOT NULL COMMENT '用户id',
    `join_createtime` datetime NOT NULL COMMENT '创建时间',
    `join_updatetime` datetime default NULL COMMENT '更新时间',
    `user_type`       tinyint(1) default 0 COMMENT '角色,0:普通员工,1:超级管理员',
    `user_main`       tinyint(1) default 0 COMMENT '是否主账号,0:子账号,1:主账号',
    `status`          tinyint(1) default 0 COMMENT '审核状态,0:待审核,1:通过,2:拒绝',
    `user_delete`     tinyint(1) default 0 COMMENT '组织删除状态,0:正常,1:删除',
    primary key (`merchant_id`, `uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户组织表';

/**
  订单表
 */
CREATE TABLE `ht_orders`
(
    `id`                   bigint         NOT NULL auto_increment primary key COMMENT '唯一id',
    `order_id`             char(32)       NOT NULL COMMENT '订单生成的编号',
    `order_type_id`        smallint     DEFAULT 1 COMMENT '订单类型,1:集群订单,2:Dockerfile订单,3:开放平台服务订单,4:版本升级订单,5:套餐组合订单',
    `order_createtime`     datetime       NOT NULL COMMENT '订单创建时间',
    `order_updatetime`     datetime     default NULL COMMENT '订单更新时间',
    `order_paytime`        datetime     default NULL COMMENT '订单支付时间',
    `order_money`          DECIMAL(65, 2) NOT NULL COMMENT '商品价格',
    `order_status`         tinyint(1) default 0 COMMENT '订单状态,0:待支付,1:已支付,2:支付失败,3:取消支付',
    `order_source`         tinyint(1) default 0 COMMENT '订单来源,0:微信,1:支付宝',
    `order_transaction_id` varchar(255) default NULL COMMENT '支付后的订单号,支付回调返回',
    `order_repty_code`     varchar(255) default null COMMENT '订单回执,可以存ID或者回执的数据',
    `order_pay_mode`       tinyint(1) default 1 COMMENT '订单支付方式,1:直接支付,2:代金劵支付,3:优惠卷支付',
    `order_delete`         tinyint(1) default 0 COMMENT '逻辑删除,0:正常,1:删除',
    `order_goods_id`       varchar(32)  default '' COMMENT '订单商品的ID',
    `uuid`                 char(32)       NOT NULL COMMENT '用户身份id',
    `order_detials`        varchar(255) default NULL COMMENT '订单备注',
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单表';

INSERT INTO `ht_merchant_sources` (merchant_source_name, merchant_source_phone,
                                   merchant_source_createtime) value ('阿里云代理商A', 13800138000, now());
INSERT INTO `ht_merchant_sources` (merchant_source_name, merchant_source_phone,
                                   merchant_source_createtime) value ('阿里云代理商B', 13800138000, now());

/**
  发布规则管理表
 */
CREATE TABLE `ht_release_rules_group`
(
    `rid`              bigint       NOT NULL auto_increment COMMENT 'id',
    `uuid`             char(32)     NOT NULL COMMENT '用户身份id',
    `rules_name`       varchar(255) NOT NULL COMMENT '规则名',
    `rules_type`       tinyint(1) DEFAULT 0 COMMENT '发布策略,0: 谨慎策略，1:放松策略',
    `rules_status`     tinyint(1) DEFAULT 0 COMMENT '状态,0:不启用，1:启用',
    `rules_createtime` datetime     NOT NULL COMMENT '创建时间',
    `rules_updatetime` datetime default NULL COMMENT '更新时间',
    `rules_change`     tinyint(1) DEFAULT 0 COMMENT '是否能修改，0：能，1：不能',
    `rules_delete`     tinyint(1) DEFAULT 0 COMMENT '是否删除，0：存在，1：删除',
    PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发布规则';

/**
  发布规则的策略关联
 */
CREATE TABLE `ht_release_rules_group_info`
(
    `rid`               bigint NOT NULL auto_increment COMMENT 'id',
    `rules_info_id`     bigint NOT NULL COMMENT '策略ID',
    `rules_info_change` tinyint(1) DEFAULT 0 COMMENT '发布策略,0: 能修改，1:不能修改',
    PRIMARY KEY (`rid`, `rules_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发布规则的策略关联';

/**
  发布策略和详情
 */
CREATE TABLE `ht_release_rules_info`
(
    `rules_info_id`     bigint       NOT NULL auto_increment COMMENT '策略ID',
    `rules_info_title`  varchar(255) NOT NULL COMMENT '策略名称',
    `rules_info_master` tinyint(1) DEFAULT 0 COMMENT '是否强制，0：不强制，1：强制',
    `rules_info_sort`   tinyint(2) DEFAULT 1 COMMENT '排序，顺序',
    PRIMARY KEY (`rules_info_id`, `rules_info_title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发布策略和详情';

INSERT INTO `ht_release_rules_info` (rules_info_title, rules_info_master, rules_info_sort)
values ('代码拉取', 1, 1),
       ('质量检测', 0, 2),
       ('构建镜像', 1, 3),
       ('业务上线发布', 1, 4);

/**
  密钥管理表
 */
CREATE TABLE `ht_vendors`
(
    `vid`             int         NOT NULL auto_increment COMMENT 'id',
    `type_name`       tinyint(2) NOT NULL COMMENT '云厂商id',
    `v_access_key`    varchar(60) NOT NULL COMMENT 'accessKey',
    `v_access_secret` varchar(60) NOT NULL COMMENT 'accessSecret',
    `v_createtime`    datetime    NOT NULL COMMENT '创建时间',
    `v_updatetime`    datetime DEFAULT NULL COMMENT '更新时间',
    `v_status`        tinyint(1) DEFAULT 1 COMMENT '状态,0:待检查,1:启用,2:无效',
    `v_delete`        tinyint(1) DEFAULT 0 COMMENT '逻辑删除状态,0:正常，1:删除',
    `v_defaults`      tinyint(1) NOT NULL COMMENT '是否设置为默认,0:不设置，1:设置',
    `uuid`            char(32)    NOT NULL COMMENT '用户身份id',
    PRIMARY KEY (`vid`, `type_name`, `v_access_key`, `v_access_secret`, `uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='云厂商密钥管理';

/**
  密钥厂商表
 */
CREATE TABLE `ht_vendors_types`
(
    `tid`      tinyint(2) NOT NULL auto_increment PRIMARY KEY COMMENT '厂商id',
    `t_name`   varchar(25) NOT NULL COMMENT '云厂商名称',
    `t_status` tinyint(1) DEFAULT 0 COMMENT '状态,0:不启用，1:启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='云厂商管理';

INSERT INTO ht_vendors_types(`t_name`, `t_status`)
values ('阿里云', 1);
INSERT INTO ht_vendors_types(`t_name`, `t_status`)
values ('腾讯云', 1);
INSERT INTO ht_vendors_types(`t_name`, `t_status`)
values ('ucloud', 1);
INSERT INTO ht_vendors_types(`t_name`, `t_status`)
values ('华为云', 0);
INSERT INTO ht_vendors_types(`t_name`, `t_status`)
values ('金山云', 0);

/**
  cvm资产表
 */
CREATE TABLE `ht_cvm_source`
(
    `cid`                    bigint      NOT NULL auto_increment COMMENT '资产id',
    `uuid`                   char(32)    NOT NULL COMMENT '用户身份id',
    `cvm_instance_id`        varchar(68) NOT NULL COMMENT '实例id',
    `cvm_tag_name`           varchar(68) NOT NULL COMMENT 'CVM的别名',
    `cvm_cluster_inside_ip`  varchar(26) NOT NULL COMMENT '内网IP',
    `cvm_cluster_outside_ip` varchar(26)  DEFAULT NULL COMMENT '外网IP',
    `cvm_region_id`          varchar(25) NOT NULL COMMENT '地域id',
    `cvm_config`             char(68)    NOT NULL COMMENT '资产配置:内存/硬盘/CPU',
    `cvm_cost`               char(68)    NOT NULL COMMENT '成本计算:活多少日/多少分钟',
    `cvm_createtime`         datetime    NOT NULL COMMENT '创建时间',
    `cvm_updatetime`         datetime     DEFAULT NULL COMMENT '最后更新时间',
    `cvm_region_source`      tinyint     NOT NULL COMMENT '归属云服务商id',
    `cvm_status`             tinyint(1) DEFAULT 0 COMMENT '状态,0:待初始化,1:运行中,2:更新中,3:停用',
    `cvm_delete`             tinyint(1) DEFAULT 0 COMMENT '状态,0:存活,1:删除',
    `cvm_remark`             varchar(255) DEFAULT '无' COMMENT '资源备注',
    PRIMARY KEY (`cid`, `cvm_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='cvm资源列表';

/**
  SSH服务表
 */
CREATE TABLE `ht_cvm_ssh_services`
(
    `cid`                bigint   NOT NULL COMMENT '资产id',
    `service_port`       MEDIUMINT    DEFAULT 0 COMMENT '端口号',
    `service_createtime` datetime NOT NULL COMMENT '创建时间',
    `service_updatetime` datetime     default NULL COMMENT '更新时间',
    `service_username`   varchar(50)  DEFAULT NULL COMMENT '账号',
    `service_password`   varchar(128) DEFAULT NULL COMMENT '密码',
    `service_status`     tinyint      DEFAULT 0 COMMENT '服务状态,0:待初始化,1:可用,2:不可用',
    PRIMARY KEY (`cid`, `service_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='cvm资源服务SSH账号密码';

/**
  CVM服务标签，用于部署
 */
CREATE TABLE `ht_cvm_tag_services`
(
    `cid`                bigint   NOT NULL COMMENT '资产id',
    `service_id`         char(32) NOT NULL auto_increment COMMENT '服务自己的id',
    `tag_id`             int      NOT NULL COMMENT 'CVM的服务标签的id',
    `service_port`       MEDIUMINT    DEFAULT 0 COMMENT '端口号',
    `service_createtime` datetime NOT NULL COMMENT '创建时间',
    `service_updatetime` datetime     default NULL COMMENT '更新时间',
    `service_info`       varchar(255) default '无' COMMENT '部署的结果信息',
    `service_status`     tinyint      DEFAULT 0 COMMENT '服务状态,0:待初始化,1:运行中,2:停止服务,3:部署中,4:检测中',
    PRIMARY KEY (`cid`, `service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='CVM服务标签';

/**
  CVM可用服务标签
 */
CREATE TABLE `ht_cvm_tags`
(
    `tagid`      smallint    NOT NULL auto_increment PRIMARY KEY COMMENT '标签id',
    `tag_name`   varchar(25) NOT NULL COMMENT 'CVM的服务标签的id',
    `tag_delete` tinyint(1) DEFAULT 0 COMMENT '标签状态,0:存活,1:删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='CVM可用服务标签';

INSERT INTO ht_cvm_tags(`tag_name`)
values ('Jumpserver');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Yapi');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Jenkins');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Sonar');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Prometheus');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Grafana');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Sentry');
INSERT INTO ht_cvm_tags(`tag_name`)
values ('Skywalking');

/**
 * 基础集群部署
 */
CREATE TABLE `ht_service_deploy`
(
    `cluster_id`             bigint       NOT NULL auto_increment PRIMARY KEY COMMENT '集群id',
    `cluster_name`           varchar(100) not null comment '集群名字',
    `uuid`                   char(32)     NOT NULL COMMENT '用户身份id',
    `cluster_instance_id`    varchar(64) DEFAULT NULL COMMENT '实例id',
    `cluster_cloud_id`       tinyint(2) NOT NULL COMMENT '云服务商的id',
    `cluster_region_id`      varchar(25)  NOT NULL COMMENT '归属的可用区',
    `cluster_zone_id`        varchar(25)  NOT NULL COMMENT '归属的地域',
    `cluster_current`        int         DEFAULT 1 COMMENT '集群的业务并发数',
    `cluster_delete`         tinyint(1) DEFAULT 0 COMMENT '逻辑禁用,0:禁用,1:启用',
    `cluster_pay_mode`       tinyint(1) DEFAULT 1 COMMENT '集群类型,1:基础版,2:高级版',
    `cluster_type`           tinyint(1) DEFAULT 0 COMMENT '集群的类型,0:按需付费,1:固定付费',
    `cluster_security_id`    varchar(68) DEFAULT NULL COMMENT '集群安全组ID',
    `cluster_deploy_count`   int         DEFAULT 0 COMMENT '集群可用发布次数',
    `cluster_service_status` tinyint     DEFAULT 1 COMMENT '集群任务的状态,1:待决策,2:配置部署中,3:运行中,4:异常,5:停用,6:回收中',
    `cluster_createtime`     datetime     NOT NULL COMMENT '集群任务创建时间',
    `cluster_updatetime`     datetime    DEFAULT NULL COMMENT '集群任务更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='基础集群部署';

/**
 * 基础集群资产信息
 */
CREATE TABLE `ht_service_assets`
(
    `cluster_id`          bigint       NOT NULL COMMENT '集群id',
    `cluster_assets_info` varchar(255) NOT NULL COMMENT '云资产信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='基础集群资产信息';

/**
  集群代码仓库对应的namespace
 */
CREATE TABLE `ht_service_deploy_gitlabs`
(
    `cluster_id`           bigint      NOT NULL COMMENT '集群id',
    `git_group_name`       varchar(32) NOT NULL COMMENT '分组名',
    `git_group_desc`       varchar(68) NOT NULL COMMENT '分组备注',
    `git_group_id`         SMALLINT    NOT NULL COMMENT '分组id',
    `git_group_createtime` datetime    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`cluster_id`, `git_group_name`, `git_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集群代码仓库对应的namespace';

/**
  集群的阶段数据
 */
CREATE TABLE `ht_service_deploy_stages`
(
    `cluster_id`       bigint      NOT NULL COMMENT '集群id',
    `stage_pipeline`   tinyint     NOT NULL COMMENT '阶段顺序',
    `stage_name`       varchar(68) NOT NULL COMMENT '阶段名称',
    `stage_createtime` datetime    NOT NULL COMMENT '创建时间',
    `stage_updatetime` datetime     DEFAULT null COMMENT '更新时间',
    `stage_info`       varchar(255) DEFAULT '无' COMMENT '部署结果内容上报',
    `stage_status`     tinyint     NOT NULL COMMENT '状态,0:待执行,1:执行中,2:成功,3:失败',
    PRIMARY KEY (`cluster_id`, `stage_name`, `stage_pipeline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集群的构建阶段记录表';


/**
 * 集群部署失败的记录表
 */
CREATE TABLE `ht_service_deploy_errorlog`
(
    `id`                bigint     NOT NULL auto_increment PRIMARY KEY,
    `cluster_id`        bigint     NOT NULL COMMENT '集群id',
    `error_createtime`  datetime   NOT NULL COMMENT '创建时间',
    `cluster_error_log` mediumtext NOT NULL comment '错误内容'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='集群部署失败的记录表';

/**
 * 基础配置
 */
CREATE TABLE `ht_service_deploy_workconfig`
(
    `id`                        bigint       NOT NULL auto_increment PRIMARY KEY,
    `cluster_id`                bigint       NOT NULL COMMENT '集群id',
    `cluster_config_name`       varchar(255) not null comment '配置的名称',
    `cluster_config_address`    varchar(255) DEFAULT NULL COMMENT '配置的地址',
    `cluster_config_username`   varchar(255) DEFAULT NULL COMMENT '配置的账号',
    `cluster_config_password`   varchar(255) DEFAULT NULL COMMENT '配置的密码',
    `cluster_config_token`      varchar(255) DEFAULT NULL COMMENT '配置的秘钥',
    `cluster_config_createtime` datetime     NOT NULL COMMENT '创建配置时间',
    `cluster_config_updatetime` datetime     DEFAULT NULL COMMENT '更新配置时间',
    `cluster_config_default`    tinyint(1) DEFAULT 1 COMMENT '是否默认,0:不是，1:是',
    `cluster_config_types`      tinyint(1) DEFAULT 0 COMMENT '配置的类型,0:对专业用户,1:对普通用户'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='基础集群部署系统配置';

/**
  工程信息表
 */
CREATE TABLE `ht_engineer`
(
    `engineer_id`               int auto_increment NOT NULL COMMENT '工程id',
    `uuid`                      char(32)    NOT NULL COMMENT '用户身份id',
    `engineer_name`             varchar(68) NOT NULL COMMENT '工程名称',
    `engineer_cloud_id`         tinyint(2) NOT NULL COMMENT '归属云id',
    `engineer_language_id`      smallint     DEFAULT 0 COMMENT '开发语言',
    `engineer_framework_id`     smallint     DEFAULT 0 COMMENT '开发框架',
    `engineer_dockerfile_id`    smallint     DEFAULT 0 COMMENT 'dockerfile的模版',
    `engineer_release_rules_id` smallint     DEFAULT 0 COMMENT '发布策略组id',
    `engineer_remark`           varchar(255) DEFAULT '无' COMMENT '工程备注',
    `engineer_giturl`           varchar(255) DEFAULT NULL COMMENT 'git仓库地址',
    `engineer_security`         bigint       default null COMMENT '安全负责人',
    `engineer_devops`           bigint       default null COMMENT '运维负责人',
    `engineer_vocational`       bigint       default null COMMENT '业务负责人',
    `engineer_testing`          bigint       default null COMMENT '测试负责人',
    `engineer_codeing`          bigint       default null COMMENT '开发负责人',
    `engineer_status`           tinyint      default 1 COMMENT '工程状态,0:待初始化,1:创建中,2:启用,3:停用',
    `engineer_createtime`       datetime    NOT NULL COMMENT '工程创建时间',
    `engineer_updatetime`       datetime     DEFAULT NULL COMMENT '工程更新时间',
    `engineer_git_id`           smallint     DEFAULT 0 COMMENT '默认的git仓库id',
    `engineer_git_group_id`     smallint    NOT NULL COMMENT 'git仓库分组id',
    PRIMARY KEY (`engineer_id`, `uuid`, `engineer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='标准工程';

/**
  配置中心(各种配置)
 */
CREATE TABLE `ht_engineer_kub_config`
(
    `id`                           bigint auto_increment NOT NULL COMMENT 'id',
    `engineer_id`                  int NOT NULL COMMENT '工程id',
    `engineer_config_type`         tinyint(1) DEFAULT 0 COMMENT '配置类型,0:configmap,1:service,2:deployment',
    `engineer_config_key`          varchar(255) DEFAULT NULL COMMENT 'configmap配置名称',
    `engineer_config_val`          varchar(255) DEFAULT null COMMENT 'configmap配置内容',
    `engineer_service_name`        varchar(255) DEFAULT null COMMENT 'service配置的name',
    `engineer_service_port`        smallint     DEFAULT 0 COMMENT 'service配置的端口',
    `engineer_service_target_port` smallint     DEFAULT 0 COMMENT 'service配置的目的端口',
    `engineer_service_protocol`    varchar(255) DEFAULT null COMMENT 'service配置的协议',
    `engineer_deployment_port`     smallint     DEFAULT null COMMENT 'deployment配置的端口',
    `engineer_config_namespace`    varchar(25)  default null COMMENT '命名空间',
    `engineer_module_name`         varchar(48)  default null COMMENT '模块名',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置中心(各种配置)';

CREATE TABLE `ht_enginner_kub_config_yaml`
(
    `id`                        bigint auto_increment NOT NULL COMMENT 'id',
    `engineer_id`               int NOT NULL COMMENT '工程id',
    `engineer_module_name`      varchar(48) default NULL COMMENT '模块名',
    `engineer_config_namespace` varchar(25) default null COMMENT '命名空间',
    `engineer_configmap`        TEXT        DEFAULT NULL COMMENT 'configmap配置',
    `engineer_services`         TEXT        DEFAULT NULL COMMENT 'service配置',
    `engineer_certcrt_info`     TEXT        DEFAULT NULL COMMENT '秘钥crt配置',
    `engineer_certkey_info`     TEXT        DEFAULT NULL COMMENT '秘钥key配置',
    `engineer_nginx_info`       TEXT        DEFAULT NULL COMMENT 'nginx配置',
    PRIMARY KEY (`id`)
)

/**
  配置中心（综合配置）
 */

CREATE TABLE `ht_engineer_kub_base_config`
(
    `id`                              bigint auto_increment NOT NULL COMMENT 'id',
    `engineer_id`                     int NOT NULL COMMENT '工程id',
    `engineer_ingress_secret_id`      varchar(255)  DEFAULT NULL COMMENT '证书ID，只有在开启HTTPS有效',
    `engineer_ingress_secret_name`    varchar(255)  DEFAULT NULL COMMENT '证书ID，只有在开启HTTPS有效',
    `engineer_ingress_hostName`       varchar(255)  default null COMMENT '配置域名',
    `engineer_ingress_https`          tinyint(1) default 0 COMMENT '是否开启https,0:不开启,1:开启',
    `engineer_hpa_cpuquota`           smallint      default 80 COMMENT 'CPU扩容指标',
    `engineer_hpa_maxpod`             smallint      default 2 COMMENT 'HPA最大POD数',
    `engineer_hpa_minpod`             smallint      default 1 COMMENT 'HPA最小POD数',
    `engineer_deployment_minpod`      smallint      default 1 COMMENT 'Deployment最小pod数',
    `engineer_deployment_maxpod`      smallint      default 2 COMMENT 'Deployment最大pod数',
    `engineer_deployment_reqcpu`      DECIMAL(5, 2) default 0.25 COMMENT 'Deployment最小CPU数',
    `engineer_deployment_limitcpu`    DECIMAL(5, 2) default 0.25 COMMENT 'Deployment最大CPU数',
    `engineer_deployment_reqmemory`   smallint      default 256 COMMENT 'Deployment最小内存设置',
    `engineer_deployment_limitmemory` smallint      default 256 COMMENT 'Deployment最大内存设置',
    `engineer_config_namespace`       varchar(25)   default null COMMENT '命名空间',
    `engineer_module_name`            varchar(48)   default null COMMENT '模块名',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置中心(综合配置)';

CREATE TABLE `ht_engineer_dockerfile`
(
    `dockerfile_id`           int auto_increment NOT NULL COMMENT 'id',
    `dockerfile_name`         varchar(30)  NOT NULL COMMENT 'dockerfile名称',
    `dockerfile_os_path`      varchar(255) NOT NULL COMMENT 'dockerfile存储的cos或者oss路径',
    `dockerfile_status`       tinyint       DEFAULT 1 COMMENT '状态,1:启用,2:不启用',
    `dockerfile_language_id`  smallint      DEFAULT 0 COMMENT '开发语言ID',
    `dockerfile_framework_id` smallint      DEFAULT 0 COMMENT '开发框架ID',
    `dockerfile_remark`       varchar(100)  DEFAULT '无' COMMENT '备注',
    `dockerfile_author_id`    int           DEFAULT 0 COMMENT 'dockerfile作者uid',
    `dockerfile_money`        DECIMAL(5, 2) DEFAULT 0 COMMENT 'dockerfile价格',
    `dockerfile_createtime`   datetime     NOT NULL COMMENT '创建时间',
    `dockerfile_updatetime`   datetime      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dockerfile_id`, `dockerfile_name`, `dockerfile_os_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dockerfile数据表';

INSERT INTO ht_engineer_dockerfile (dockerfile_name, dockerfile_os_path, dockerfile_createtime, dockerfile_language_id,
                                    dockerfile_framework_id)
values ('Java Spring boot', 'http://os.com/Dockerfile', now(), 1, 1);

CREATE TABLE `ht_engineer_language`
(
    `language_id`     smallint auto_increment NOT NULL COMMENT '语言id',
    `language_name`   varchar(68) NOT NULL COMMENT '语言名称',
    `language_status` tinyint DEFAULT 1 COMMENT '状态,1:启用,2:禁用',
    PRIMARY KEY (`language_id`, `language_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='开发语言表';

INSERT INTO ht_engineer_language (`language_name`)
values ('Java');
INSERT INTO ht_engineer_language (`language_name`)
values ('PHP');
INSERT INTO ht_engineer_language (`language_name`)
values ('GoLang');
INSERT INTO ht_engineer_language (`language_name`)
values ('Node');
INSERT INTO ht_engineer_language (`language_name`)
values ('Python');

CREATE TABLE `ht_engineer_framework`
(
    `framework_id`          smallint auto_increment NOT NULL COMMENT '框架id',
    `framework_name`        varchar(68) NOT NULL COMMENT '框架名称',
    `framework_status`      tinyint DEFAULT 1 COMMENT '状态,1:启用,2:禁用',
    `framework_language_id` smallint    NOT NULL COMMENT '框架对应的开发语言',
    PRIMARY KEY (`framework_id`, `framework_name`, `framework_language_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='开发框架表';

INSERT INTO ht_engineer_framework (`framework_name`, `framework_language_id`)
values ('SpringBoot', 1);
INSERT INTO ht_engineer_framework (`framework_name`, `framework_language_id`)
values ('Laravel8', 2);
INSERT INTO ht_engineer_framework (`framework_name`, `framework_language_id`)
values ('Gin', 3);
INSERT INTO ht_engineer_framework (`framework_name`, `framework_language_id`)
values ('Vue', 4);
INSERT INTO ht_engineer_framework (`framework_name`, `framework_language_id`)
values ('ThinkPHP5', 2);

CREATE TABLE `ht_engineer_analyze`
(
    `engineer_id`         int      NOT NULL COMMENT '工程id',
    `engineer_branch`     varchar(50)  DEFAULT '' COMMENT '工程分支',
    `enginner_version`    varchar(100) DEFAULT '' COMMENT '工程的发布的版本',
    `engineer_type`       tinyint(2) NOT NULL COMMENT '分析类型,1:bug,2:code_smells,3:vulnerabilities',
    `engineer_rule`       varchar(100) DEFAULT '' COMMENT '规则名',
    `engineer_severity`   tinyint(1) DEFAULT 1 COMMENT '安全/风险级别,1:低风险,2:中风险,3:高风险,4:致命风险',
    `engineer_searchfile` varchar(255) DEFAULT '' COMMENT '扫描文件路径',
    `engineer_code_line`  int          DEFAULT 0 COMMENT '开始行数',
    `engineer_message`    varchar(255) DEFAULT '' COMMENT '问题信息',
    `engineer_createtime` datetime NOT NULL COMMENT '创建时间',
    `engineer_tools`      varchar(50)  DEFAULT '' COMMENT '分析来源,工具名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='标准工程分析详情';

CREATE TABLE `ht_release_container_analyze`
(
    `release_id`                int      NOT NULL COMMENT '工程id',
    `release_vul_id`            varchar(50)  DEFAULT '' COMMENT '漏洞ID',
    `release_pkg_name`          varchar(68)  DEFAULT '' COMMENT '弱点工具名称',
    `release_installed_version` varchar(68)  DEFAULT '' COMMENT '当前安装的版本',
    `release_fixed_version`     varchar(68)  DEFAULT '' COMMENT '修复的版本',
    `release_severity`          varchar(50)  DEFAULT '' COMMENT '安全级别',
    `release_title`             varchar(255) DEFAULT '' COMMENT '风险标题',
    `release_description`       varchar(255) DEFAULT '' COMMENT '风险介绍',
    `release_references`        TEXT     NOT NULL COMMENT '引用',
    `release_createtime`        datetime NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工程容器扫描分析';

#
PASS: dophinJobs1234
# INSERT INTO ht_users ( u_name, u_login_password, u_login_username, u_createtime) values ('测试用户', '752f010eb8c96047fef55256c953cf4b', 'test', now());

CREATE TABLE `ht_release_jobs`
(
    `release_id`                       bigint auto_increment NOT NULL COMMENT '唯一id',
    `release_version`                  varchar(36)  NOT NULL COMMENT '版本号:2021_12_24:214315',
    `release_engineer_id`              int          NOT NULL COMMENT '工程id',
    `release_job_createtime`           datetime     NOT NULL COMMENT '创建时间',
    `release_job_updatetime`           datetime    DEFAULT NULL COMMENT '更新时间',
    `release_job_status`               tinyint     DEFAULT 0 COMMENT '状态,0:等待中,1:构建中,2:待发布,3:发布中,4:已发布,5:回滚中,6:已回滚,7:发布异常,8:已取消',
    `release_job_branch`               varchar(100) NOT NULL COMMENT '分支',
    `release_job_cluster_id`           bigint       NOT NULL COMMENT '发布到哪个集群',
    `release_job_rollback`             varchar(36) DEFAULT NULL COMMENT '回滚到哪个版本',
    `release_job_namespace`            varchar(68)  NOT NULL COMMENT '发布到哪个namespace',
    `release_commit_author_createtime` datetime     NOT NULL COMMENT '作者提交代码时间',
    `release_commit_author_name`       varchar(28)  NOT NULL COMMENT '作者名称',
    `release_commit_id`                varchar(255) NOT NULL COMMENT 'CommitID',
    `release_content`                  MEDIUMTEXT  DEFAULT null COMMENT '发布日志',
    `uuid`                             char(32)     NOT NULL COMMENT '用户身份id',
    PRIMARY KEY (`release_id`, `release_engineer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务发布表';

CREATE TABLE `ht_release_module`
(
    `release_id`     bigint      not null COMMENT '发布ID',
    `release_module` varchar(48) not null COMMENT '模块',
    primary key (`release_id`, `release_module`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发布模块';

CREATE TABLE `ht_release_debts`
(
    `release_id`              int          NOT NULL COMMENT '唯一id',
    `release_debt_id`         varchar(20)  NOT NULL COMMENT '债务id号',
    `release_debt_name`       varchar(100) NOT NULL COMMENT '债务名称',
    `release_debt_createtime` datetime     NOT NULL COMMENT '创建时间',
    `release_debt_updatetime` datetime     DEFAULT NULL COMMENT '更新时间',
    `release_debt_status`     tinyint      DEFAULT 1 COMMENT '状态,0:不展示,1:展示',
    `release_debt_star`       smallint     NOT NULL COMMENT '债务等级，星号',
    `release_debt_info`       varchar(255) DEFAULT NULL COMMENT '债务详细信息',
    PRIMARY KEY (`release_id`, `release_debt_name`, `release_debt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务发布债务表';

CREATE TABLE `ht_release_deploy_stages`
(
    `release_id`                int          NOT NULL COMMENT '唯一id',
    `release_status_id`         int          NOT NULL COMMENT '发布的阶段',
    `release_status_name`       varchar(150) NOT NULL COMMENT '发布的阶段内容',
    `release_status_icon`       varchar(50) DEFAULT NULL COMMENT '创建时间',
    `release_status_stages`     tinyint     DEFAULT 0 COMMENT '进度条,0:standby,1:running,2:success,3:warning,4:error',
    `release_stages_createtime` datetime     NOT NULL COMMENT '执行时间',
    `release_stages_updatetime` datetime    DEFAULT NULL COMMENT '执行完毕时间',
    PRIMARY KEY (`release_id`, `release_status_id`, `release_status_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务发布进度表';
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 1, '创建发布任务', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 2, '拉取GitLab代码', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 3, '拉取基础镜像', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 4, '构建业务镜像', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 5, '推送到镜像仓库', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 6, '代码质量检测', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 7, '漏洞安全检测', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 8, '自动化冒烟测试', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 9, '部署到K8S集群', '', 0, now());
INSERT INTO ht_release_deploy_stages (release_id, release_status_id, release_status_name, release_status_icon,
                                      release_status_stages, release_stages_createtime)
VALUES (1, 10, '钉钉发布通知', '', 0, now());

CREATE TABLE `ht_gitlab_commits`
(
    `id`                    int          NOT NULL auto_increment COMMENT '唯一id',
    `uuid`                  char(32)     NOT NULL COMMENT '用户身份id',
    `jobs_id`               int          NOT NULL COMMENT '工程的id',
    `jobs_name`             varchar(255) NOT NULL COMMENT '工程的名称',
    `jobs_commitid`         varchar(255) NOT NULL COMMENT 'commitId',
    `jobs_shortid`          varchar(32)  NOT NULL COMMENT '短id',
    `jobs_author`           varchar(68)  NOT NULL COMMENT '提交人',
    `jobs_create_time`      datetime     NOT NULL COMMENT '创建时间',
    `jobs_change_code_line` int          NOT NULL COMMENT '变更行数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='获取提交统计';

CREATE TABLE `ht_withdraw`
(
    `withdraw_id`         bigint   NOT NULL auto_increment primary key COMMENT 'id',
    `uuid`                char(32) NOT NULL COMMENT '用户身份id',
    `withdraw_createtime` datetime NOT NULL COMMENT '创建时间',
    `withdraw_updatetime` datetime      default NULL COMMENT '更新时间',
    `withdraw_status`     tinyint(1) default 0 COMMENT '提现状态,0:待处理,1:处理中,2:提现成功,3:提现失败',
    `withdraw_prices`     DECIMAL(5, 2) default 0 COMMENT '提现金额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='提现表';

CREATE TABLE `ht_clue`
(
    `clue_id`           bigint      NOT NULL auto_increment primary key COMMENT 'id',
    `clue_username`     varchar(32) NOT NULL COMMENT '客户名称',
    `clue_createtime`   datetime    NOT NULL COMMENT '创建时间',
    `clue_updatetime`   datetime    default NULL COMMENT '更新时间',
    `clue_phonenumber`  varchar(32) NOT NULL COMMENT '联系方式',
    `clue_company`      varchar(68) default 0 COMMENT '企业名称',
    `clue_company_size` tinyint(1) default 0 COMMENT '企业规模,0:10人以下,1:10-300,2:300-1000,3:1000以上',
    `clue_info`         longtext   default '' COMMENT '线索备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='线索表';


CREATE TABLE `ht_coupon`
(
    `coupon_id`         bigint        NOT NULL auto_increment primary key COMMENT 'id',
    `coupon_type`       tinyint(1) NOT NULL COMMENT '优惠券对象(0:人,1:渠道)',
    `coupon_money`      DECIMAL(5, 2) NOT NULL COMMENT '优惠券金额',
    `coupon_createtime` datetime      NOT NULL COMMENT '创建时间',
    `coupon_updatetime` datetime default NULL COMMENT '更新时间',
    `coupon_hashid`     char(32)      NOT NULL COMMENT '优惠券实际的ID',
    `coupon_status`     tinyint(1) DEFAULT 0 COMMENT '状态,0:未启用，1:已启用，2:禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='优惠券主表';

CREATE TABLE `ht_coupon_fixed`
(
    `id`                bigint        NOT NULL auto_increment primary key COMMENT 'id',
    `coupon_id`         bigint        NOT NULL COMMENT '优惠券id',
    `uuid`              char(32) DEFAULT NULL COMMENT '用户ID',
    `coupon_channelid`  bigint   DEFAULT 0 COMMENT '渠道ID',
    `coupon_money`      DECIMAL(5, 2) NOT NULL COMMENT '优惠券金额',
    `coupon_createtime` datetime      NOT NULL COMMENT '创建时间',
    `coupon_updatetime` datetime default NULL COMMENT '更新时间',
    `coupon_status`     tinyint(1) default 0 COMMENT '状态,0:待激活, 1:未消费，2:已消费，3:停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='优惠券子表';

CREATE TABLE `ht_channel`
(
    `id`                 int         NOT NULL auto_increment primary key COMMENT 'id',
    `channel_name`       varchar(32) NOT NULL COMMENT '渠道名称',
    `channel_type`       tinyint(1) DEFAULT 1 COMMENT '渠道类型,1:被动渠道、2:主动渠道',
    `channel_status`     tinyint(1) DEFAULT 1 COMMENT '渠道状态,0:停用，1:在用',
    `channel_createtime` datetime    NOT NULL COMMENT '创建时间',
    `channel_updatetime` datetime default NULL COMMENT '更新时间',
    `channel_hashurl`    char(32)    NOT NULL COMMENT '生成的邀请码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='渠道表';

CREATE TABLE `ht_domain`
(
    `id`                int         NOT NULL auto_increment primary key COMMENT 'id',
    `domain_name`       varchar(32) NOT NULL COMMENT '域名名称',
    `domain_source`     tinyint(2) DEFAULT 1 COMMENT '域名来源,参考厂商表tid',
    `domain_status`     tinyint(1) DEFAULT 1 COMMENT '域名状态,0:停用/失效，1:在用',
    `domain_createtime` datetime    NOT NULL COMMENT '创建时间',
    `domain_updatetime` datetime default NULL COMMENT '更新时间',
    `uuid`              char(32)    NOT NULL COMMENT '用户身份id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='域名表';

CREATE TABLE `ht_merchant_details`
(
    `id`                    int         NOT NULL auto_increment primary key COMMENT 'id',
    `identification_number` varchar(80) NOT NULL COMMENT '纳税人识别号',
    `bank_name`             varchar(68) DEFAULT NULL COMMENT '开户行',
    `bank_account`          varchar(68) DEFAULT NULL COMMENT '银行账号',
    `merchant_address`      varchar(68) DEFAULT NULL COMMENT '公司地址',
    `createtime`            datetime    NOT NULL COMMENT '创建时间',
    `updatetime`            datetime    default NULL COMMENT '更新时间',
    `uuid`                  char(32)    NOT NULL COMMENT '用户身份id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公司信息表';

CREATE TABLE `ht_alter_message_keys`
(
    `id`         int      NOT NULL auto_increment primary key COMMENT 'id',
    `key_type`   tinyint(1) NOT NULL COMMENT 'key类型,0:钉钉，1:企业微信',
    `key_info`   varchar(68) DEFAULT NULL COMMENT 'key内容',
    `key_status` tinyint(1) DEFAULT 1 COMMENT 'key状态,0:不启用,1:启用',
    `createtime` datetime NOT NULL COMMENT '创建时间',
    `updatetime` datetime    default NULL COMMENT '更新时间',
    `uuid`       char(32) NOT NULL COMMENT '用户身份id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='企业微信/钉钉提醒集成';

CREATE TABLE `ht_engineer_analyze_fileline`
(
    `engineer_id`       int         NOT NULL COMMENT '工程id',
    `engineer_language` varchar(32) NOT NULL COMMENT '语言',
    `engineer_file`     int      DEFAULT 0 COMMENT '文件数',
    `engineer_line`     int      DEFAULT 0 COMMENT '代码行数',
    `createtime`        datetime    NOT NULL COMMENT '创建时间',
    `updatetime`        datetime default NULL COMMENT '更新时间',
    `commitid`          datetime default NULL COMMENT 'commit的ID关联',
    PRIMARY KEY (`engineer_id`, `engineer_language`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工程分析文件代码行数';

CREATE TABLE `ht_license_manage`
(
    `license_id`            bigint   NOT NULL auto_increment COMMENT 'license的id',
    `license_access_key`    char(32) NOT NULL COMMENT 'key',
    `license_access_secret` char(32) NOT NULL COMMENT 'secret',
    `license_createtime`    datetime NOT NULL COMMENT '创建时间',
    `license_status`        tinyint(1) DEFAULT 1 COMMENT '状态,0:待使用,1:已生效,2:已失效',
    `license_version`        tinyint(1) DEFAULT 0 COMMENT '版本,0:社区版,1:商业版',
    `license_deploy_count`   tinyint(1) DEFAULT 5 COMMENT '小型企业可部署次数,社区版有效',
    `uuid`       char(32) NOT NULL COMMENT '用户身份id',
    PRIMARY KEY (`license_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='License管理';