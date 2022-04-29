package com.dolphin.saas.commons.clouds.comFinal;

import com.aliyun.teaopenapi.models.Config;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Data
public class MasterServ {
    @Resource
    private ClusterService clusterService;

    // 获取所有的参数
    public Map<String, Object> paramets;

    @Resource
    public RedisCommonUtils redisCommonUtils;

    /**
     * 腾讯云所有关联的链接
     */

    // 用户
    public final String TENCENT_BILL_URL = "billing.tencentcloudapi.com";
    // CFS
    public final String TENCENT_CFS_URL = "cfs.tencentcloudapi.com";
    // 集群
    public final String TENCENT_TKE_URL = "tke.tencentcloudapi.com";
    // cam
    public final String TENCENT_CAM_URL = "cam.tencentcloudapi.com";
    // VPC
    public final String TENCENT_VPC_URL = "vpc.tencentcloudapi.com";
    // HSM
    public final String TENCENT_CLOUD_HSM = "cloudhsm.tencentcloudapi.com";
    // TCR
    private final String TENCENT_TCR_URL = "tcr.tencentcloudapi.com";
    // CVM
    private final String TENCENT_CVM_URL = "cvm.tencentcloudapi.com";
    // SSL
    private final String TENCENT_SSL_URL = "ssl.tencentcloudapi.com";
    // DNS
    private final String TENCENT_DNSPOD_URL = "dnspod.tencentcloudapi.com";


    /**
     * 阿里云所有关联的链接
     */

    // 集群
    public String ALIYUN_CLUSTER_URL = "cs.{REGIONS}.aliyuncs.com";
    // ARMS
    public String ALIYUN_ARMS_URL = "arms.{REGIONS}.aliyuncs.com";
    // RAM
    public final String ALIYUN_RAM_URL = "ram.aliyuncs.com";
    // NAS
    public String ALIYUN_NAS_URL = "nas.{REGIONS}.aliyuncs.com";
    // VPC
    public final String ALIYUN_VPC_URL = "vpc.aliyuncs.com";
    // 金钱
    public final String ALIYUN_BUSINESS_URL = "business.aliyuncs.com";
    // ECS & 安全组
    public final String ALIYUN_ECS_URL = "ecs.{REGIONS}.aliyuncs.com";


    // 阿里云
    public Config config;

    // 腾讯云
    public Credential cred;
    public HttpProfile httpProfile;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     * @param VentorType      厂商类型
     */
    public MasterServ(String AccessKeyId, String AccessKeySecret, Integer VentorType) {
        switch (VentorType) {
            case 1:
                // 阿里云
                this.config = new Config()
                        // 您的AccessKey ID
                        .setAccessKeyId(AccessKeyId)
                        // 您的AccessKey Secret
                        .setAccessKeySecret(AccessKeySecret);
                break;

            case 2:
                // 腾讯云
                this.cred = new Credential(AccessKeyId, AccessKeySecret);
                break;
        }
    }

    /**
     * 生成密码
     *
     * @return
     */
    public String generatePassWord() {
        String retStr;      //生成的密码
        String strTable = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz!@#*";
        //密码使用符号，可更改
        int len = strTable.length();
        boolean bDone = false;  //生成结束标志
        do {
            retStr = "";
            int count = 0;      //生成密码中数字的个数
            int count1 = 0;     //生成密码中字母的个数
            int count2 = 0;     //生成密码中符号的个数

            for (int i = 0; i < 15; i++) {
                int intR = (int) Math.floor(Math.random() * len);
                char c = strTable.charAt(intR);   //找到指定字符

                //判断字符类型并计数：数字，字母，符号
                if (('0' <= c) && (c <= '9')) {
                    count++;
                } else if (('A' <= c) && (c <= 'z')) {
                    count1++;
                } else {
                    count2++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 1 && count1 >= 4) {
                bDone = true;
            }
        } while (!bDone);

        return retStr;
    }

    /**
     * 使用 Map按key进行排序
     * @param map
     * @return
     */
    public static Map<Float, String> sortMapByKey(Map<Float, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<Float, String> sortMap = new TreeMap<Float, String>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }
}
