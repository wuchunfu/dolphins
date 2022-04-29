package com.dolphin.saas.commons.clouds.tencent;

import com.alibaba.fastjson.JSON;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.commons.clouds.tencent.entity.Cvm;
import com.dolphin.saas.commons.clouds.tencent.entity.TCvm;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class TkeServPlugin extends MasterServ implements Frame {
    // 账户余额
    private double accountMoney = 0;
    // 执行结果
    private Map<String, Object> results;

    // 加载集群
    private ClusterServPlugin clusterServPlugin;

    private final String AccessKeyId;
    private final String AccessKeySecret;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public TkeServPlugin(String AccessKeyId, String AccessKeySecret) throws Exception {
        super(AccessKeyId, AccessKeySecret, 2);

        this.AccessKeyId = AccessKeyId;
        this.AccessKeySecret = AccessKeySecret;
        AccountServPlugin accountServPlugin = new AccountServPlugin(AccessKeyId, AccessKeySecret);
        try {
            this.accountMoney = (double) accountServPlugin.runner().refval().get("money");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
        com.dolphin.saas.commons.clouds.tencent.entity.Cvm cvm = new com.dolphin.saas.commons.clouds.tencent.entity.Cvm();
        // 填写基础信息
        cvm.setVpcId(this.paramets.get("vpcId").toString());
        cvm.setZone(this.paramets.get("zone").toString());
        cvm.setReGion(this.paramets.get("region").toString());
        Map<String, Object> buyCalculateResults = this.calculatePriceForCvm(
                Integer.parseInt(this.paramets.get("concurrency").toString()),
                Integer.parseInt(this.paramets.get("buy").toString()), cvm);

        // 判断当前余额是否足以支撑购买消费
        float money = Float.parseFloat(buyCalculateResults.get("totalPrice").toString());

        // 判断当前账户的余额是否小于需要支付的金额
        if (this.accountMoney < money) {
            throw new Exception("当前经费不足以购买K8S集群!");
        }

        /*
         * 生成资源数据，用于购买
         */

        ArrayList cvmLists = (ArrayList) buyCalculateResults.get("cvmLists");
        // 数据结果集
        List<String> cvmResults = new ArrayList<>();
        if (cvmLists.size() > 0) {
            for (Object cvmList : cvmLists) {
                Map<String, Object> cvmItems = (Map<String, Object>) cvmList;
                TCvm tCvm = new TCvm();
                Map<String, Object> Placement = new HashMap<>();
                Placement.put("Zone", this.paramets.get("zone"));
                tCvm.setPlacement(Placement);
                if (Integer.parseInt(this.paramets.get("buy").toString()) == 1) {
                    tCvm.setInstanceChargeType("PREPAID");
                    Map<String, Object> InstanceChargePrepaid = new HashMap<>();
                    InstanceChargePrepaid.put("Period", 1);
                    tCvm.setInstanceChargePrepaid(InstanceChargePrepaid);
                }else{
                    tCvm.setInstanceChargePrepaid(null);
                }
                tCvm.setInstanceType(cvmItems.get("instance").toString());
                Map<String, Object> SystemDisk = new HashMap<>();
                SystemDisk.put("DiskType", "CLOUD_PREMIUM");
                SystemDisk.put("DiskSize", cvmItems.get("baseDisk"));
                tCvm.setSystemDisk(SystemDisk);
                Map<String, Object> DataDisk = new HashMap<>();
                DataDisk.put("DiskType", "CLOUD_PREMIUM");
                DataDisk.put("DiskSize", cvmItems.get("cloudDisk"));
                ArrayList<Map<String, Object>> DataDisks = new ArrayList<>();
                DataDisks.add(DataDisk);
                tCvm.setDataDisks(DataDisks);
                Map<String, Object> virtualPrivateCloud = new HashMap<>();
                virtualPrivateCloud.put("VpcId", this.paramets.get("vpcId").toString());
                virtualPrivateCloud.put("SubnetId", this.paramets.get("subNetId").toString());
                tCvm.setVirtualPrivateCloud(virtualPrivateCloud);
                Map<String, Object> internetAccessible = new HashMap<>();
                internetAccessible.put("InternetChargeType", "TRAFFIC_POSTPAID_BY_HOUR");
                internetAccessible.put("InternetMaxBandwidthOut", 100);
                internetAccessible.put("PublicIpAssigned", true);
                tCvm.setInternetAccessible(internetAccessible);
                Map<String, Object> loginSettings = new HashMap<>();
                loginSettings.put("Password", this.paramets.get("password").toString());
                tCvm.setLoginSettings(loginSettings);
                // 格式化json后放入
                cvmResults.add(JSON.toJSON(tCvm).toString());
            }
        }

        // 写入cvm的数据
        this.paramets.put("cvmResults", cvmResults);
    }

    private static String getKey(Map<String, Object> map, String value) {
        String key = "";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (value.equals(entry.getValue().toString())) {
                key = entry.getKey();
            }
        }
        return key;
    }

    /**
     * 获取正在销售的cvm信息，包括规格、价格
     *
     * @param reGion  地区
     * @param zone    可用区
     * @param payType 0：按需，1：预付
     * @return
     */
    public Map<String, Object> getSellCvmLists(String reGion, String zone, int payType) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CVM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            CvmClient client = new CvmClient(this.cred, reGion, clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeZoneInstanceConfigInfosRequest describeZoneInstanceConfigInfosRequest = new DescribeZoneInstanceConfigInfosRequest();
            Filter[] filters1 = new Filter[2];
            Filter filter1 = new Filter();
            filter1.setName("zone");
            String[] values1 = {zone};
            filter1.setValues(values1);
            filters1[0] = filter1;
            Filter filter2 = new Filter();
            String[] values2;
            filter2.setName("instance-charge-type");
            if (payType == 0) {
                values2 = new String[]{"POSTPAID_BY_HOUR"};
            } else if (payType == 1) {
                values2 = new String[]{"PREPAID"};
            } else {
                throw new TencentCloudSDKException("没有这个付费模式!");
            }
            filter2.setValues(values2);
            filters1[1] = filter2;
            describeZoneInstanceConfigInfosRequest.setFilters(filters1);
            DescribeZoneInstanceConfigInfosResponse describeZoneInstanceConfigInfosResponse = client.DescribeZoneInstanceConfigInfos(describeZoneInstanceConfigInfosRequest);
            InstanceTypeQuotaItem[] instanceTypeQuotaItems = describeZoneInstanceConfigInfosResponse.getInstanceTypeQuotaSet();
            for (InstanceTypeQuotaItem instanceTypeQuotaItem : instanceTypeQuotaItems) {
                // cpu和内存组合
                String cpuMemory = instanceTypeQuotaItem.getCpu().toString() + "h/" + instanceTypeQuotaItem.getMemory().toString() + "G";

                if (instanceTypeQuotaItem.getStatus().equals("SELL")) {
                    // 如果没有就创建
                    if (!results.containsKey(cpuMemory)) {
                        results.put(cpuMemory, new ArrayList<Map<String, Object>>());
                    }
                    // 组装数据
                    Map<String, Object> instancePrice = new HashMap<>();
                    instancePrice.put("instance", instanceTypeQuotaItem.getInstanceType());
                    if (payType == 1) {
                        instancePrice.put("price", instanceTypeQuotaItem.getPrice().getOriginalPrice());
                    } else {
                        instancePrice.put("price", instanceTypeQuotaItem.getPrice().getUnitPrice());
                    }

                    ArrayList<Map<String, Object>> oldArr = (ArrayList<Map<String, Object>>) results.get(cpuMemory);
                    oldArr.add(instancePrice);
                    results.put(cpuMemory, oldArr);
                }
            }

            // 处理数据，排序
            for (String key : results.keySet()) {
                Collections.sort((ArrayList) results.get(key), new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Float name1 = Float.valueOf(o1.get("price").toString());//name1是从你list里面拿出来的一个
                        Float name2 = Float.valueOf(o2.get("price").toString()); //name1是从你list里面拿出来的第二个name
                        return name1.compareTo(name2);
                    }
                });
            }

            // 处理数据，只保留最优的1个
            for (String key : results.keySet()) {
                ArrayList instanceLists = (ArrayList) results.get(key);
                results.put(key, instanceLists.get(0));
            }
        } catch (TencentCloudSDKException e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 计算可用区的CVM资源
     *
     * @param reGion      归属
     * @param Zone        可用区
     * @param Concurrency 并发
     * @param buyMode     0：按需，1：预付
     * @return
     */
    public Map<String, Object> calculateZoneCvm(String reGion, String Zone, Integer Concurrency, Integer buyMode) throws Exception {
        // 最终返回数据
        Map<String, Object> calculateAssetsResults = new HashMap<>();

        // 获取归属的可用区下可以使用的CVM规格
        Map<String, Object> results = this.getSellCvmLists(reGion, Zone, buyMode);

        // 最终实例清单
        ArrayList<Object> CvmResults = new ArrayList<>();

        // 临时数据单
        ArrayList<Object> tmpResults = new ArrayList<>();


        // 按照并发处理规格
        switch (Concurrency) {
            case 1:
                if (results.containsKey("8h/16G")) {
                    tmpResults.add(results.get("8h/16G"));
                    tmpResults.add(results.get("4h/8G"));
                    tmpResults.add(results.get("4h/8G"));
                } else if (results.containsKey("4h/8G")) {
                    tmpResults.add(results.get("4h/8G"));
                    tmpResults.add(results.get("4h/8G"));
                    tmpResults.add(results.get("4h/8G"));
                    tmpResults.add(results.get("4h/8G"));
                }
                calculateAssetsResults.put("service", "10个服务(40个Pod)");
                calculateAssetsResults.put("info", "小型企业业务，纯CI/CD流水线。");
                break;
            case 50:
                if (results.containsKey("16h/32G")) {
                    for (int i = 0; i < 3; i++) {
                        tmpResults.add(results.get("16h/32G"));
                    }
                } else if (results.containsKey("8h/16G")) {
                    for (int i = 0; i < 6; i++) {
                        tmpResults.add(results.get("8h/16G"));
                    }
                } else if (results.containsKey("4h/8G")) {
                    for (int i = 0; i < 12; i++) {
                        tmpResults.add(results.get("4h/8G"));
                    }
                }
                calculateAssetsResults.put("service", "20个服务(80个Pod)");
                calculateAssetsResults.put("info", "中型企业业务，满足基本安全+CI/CD需求。");
                break;
            case 250:
                if (results.containsKey("16h/32G")) {
                    for (int i = 0; i < 4; i++) {
                        tmpResults.add(results.get("16h/32G"));
                    }
                } else if (results.containsKey("8h/16G")) {
                    for (int i = 0; i < 8; i++) {
                        tmpResults.add(results.get("16h/32G"));
                    }
                } else if (results.containsKey("4h/8G")) {
                    for (int i = 0; i < 16; i++) {
                        tmpResults.add(results.get("4h/8G"));
                    }
                }
                calculateAssetsResults.put("service", "30个服务(120个Pod)");
                calculateAssetsResults.put("info", "大型集群业务，满足安全+技术+等保合规需要。");
                break;
        }

        // 反查型号规格
        for (Object tmpResult : tmpResults) {
            String key = getKey(results, tmpResult.toString());
            Map<String, Object> items = (Map<String, Object>) tmpResult;
            items.put("spec", key);
            CvmResults.add(items);
        }
        calculateAssetsResults.put("cvm", CvmResults);
        return calculateAssetsResults;
    }


    /**
     * 根据并发数计算资源价格
     *
     * @param concurrency 并发数
     * @param buyMode     0：按需，1：预付
     * @param cvm         Cvm基础信息
     * @return
     */
    public Map<String, Object> calculatePriceForCvm(Integer concurrency, Integer buyMode, Cvm cvm) throws Exception {
        Map<String, Object> results = new HashMap<>();
        ArrayList<Map<String, Object>> cvmLists = new ArrayList<>();

        // 总价格设置
        double totalMoney = 0.0;

        try {
            // 获取cvm的数据
            Map<String, Object> calculateAssetsResults = this.calculateZoneCvm(
                    this.paramets.get("region").toString(),
                    this.paramets.get("zone").toString(),
                    concurrency,
                    buyMode);

            // 组装cvm的数据
            ArrayList<Map<String, Object>> cvmList = (ArrayList) calculateAssetsResults.get("cvm");
            for (Map<String, Object> cvmItems : cvmList) {
                // CVM基础设施
                cvm.setBaseDiskSize(80L);
                cvm.setCloudDiskSize(100L);
                cvm.setImageId("img-eb30mz89");
                cvm.setInstanceType(cvmItems.get("instance").toString());

                // CVM规格数据返回
                Map<String, Object> calcuPrice = this.calculateCvm(buyMode, cvm);

                // 总价计算
                totalMoney = totalMoney + (double) calcuPrice.get("price");

                cvmItems.put("price", calcuPrice.get("price"));
                cvmItems.put("baseDisk", 80);
                cvmItems.put("cloudDisk", 100);
                cvmItems.put("imageId", "img-eb30mz89");
                cvmItems.put("cvm", cvmItems.get("spec").toString() + "/180G/100Mbps");
                cvmItems.put("network", calcuPrice.get("network"));

                cvmLists.add(cvmItems);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        results.put("cvmLists", cvmLists);
        results.put("totalPrice", (double) Math.round(totalMoney * 100) / 100);

        return results;
    }


    /**
     * 计算CVM的采买价格（用于评估总体价格用）
     *
     * @param buyMode 0:按需，1：预付
     * @param cvm
     * @return
     */
    public Map<String, Object> calculateCvm(Integer buyMode, Cvm cvm) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CVM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            CvmClient client = new CvmClient(this.cred, this.paramets.get("region").toString(), clientProfile);
            InquiryPriceRunInstancesRequest inquiryPriceRunInstancesRequest = new InquiryPriceRunInstancesRequest();
            switch (buyMode) {
                case 1:
                    inquiryPriceRunInstancesRequest.setInstanceChargeType("PREPAID");
                    InstanceChargePrepaid instanceChargePrepaid1 = new InstanceChargePrepaid();
                    instanceChargePrepaid1.setPeriod(1L);
                    instanceChargePrepaid1.setRenewFlag("NOTIFY_AND_AUTO_RENEW");
                    inquiryPriceRunInstancesRequest.setInstanceChargePrepaid(instanceChargePrepaid1);
                    break;
                case 0:
                    inquiryPriceRunInstancesRequest.setInstanceChargeType("POSTPAID_BY_HOUR");
                    break;
            }

            Placement placement1 = new Placement();
            placement1.setZone(this.paramets.get("zone").toString());
            inquiryPriceRunInstancesRequest.setPlacement(placement1);
            if (cvm.getInstanceType() == null) {
                throw new Exception("InstanceType不能不输入!");
            }
            inquiryPriceRunInstancesRequest.setInstanceType(cvm.getInstanceType());
            if (cvm.getImageId() == null) {
                throw new Exception("ImageId不能不输入!");
            }
            inquiryPriceRunInstancesRequest.setImageId(cvm.getImageId());
            SystemDisk systemDisk1 = new SystemDisk();
            systemDisk1.setDiskType("CLOUD_PREMIUM");
            if (cvm.getBaseDiskSize() == null) {
                throw new Exception("DiskSize不能不输入!");
            }
            systemDisk1.setDiskSize(cvm.getBaseDiskSize());
            inquiryPriceRunInstancesRequest.setSystemDisk(systemDisk1);
            DataDisk[] dataDisks1 = new DataDisk[1];
            DataDisk dataDisk1 = new DataDisk();
            dataDisk1.setDiskType("CLOUD_PREMIUM");
            if (cvm.getCloudDiskSize() == null) {
                throw new Exception("CloudDiskSize不能不输入!");
            }
            dataDisk1.setDiskSize(cvm.getCloudDiskSize());
            dataDisks1[0] = dataDisk1;
            inquiryPriceRunInstancesRequest.setDataDisks(dataDisks1);
            //网络带宽
            InternetAccessible internetAccessible1 = new InternetAccessible();
            internetAccessible1.setInternetMaxBandwidthOut(100L);
            internetAccessible1.setPublicIpAssigned(true);
            internetAccessible1.setInternetChargeType("TRAFFIC_POSTPAID_BY_HOUR");
            inquiryPriceRunInstancesRequest.setInternetAccessible(internetAccessible1);

            InquiryPriceRunInstancesResponse inquiryPriceRunInstancesResponse = client.InquiryPriceRunInstances(inquiryPriceRunInstancesRequest);
            // 输出json格式的字符串回包
            if (buyMode == 0) {
                results.put("price", (double) inquiryPriceRunInstancesResponse.getPrice().getInstancePrice().getUnitPrice());
            } else {
                results.put("price", (double) inquiryPriceRunInstancesResponse.getPrice().getInstancePrice().getOriginalPrice());
            }
            results.put("network", inquiryPriceRunInstancesResponse.getPrice().getBandwidthPrice().getUnitPrice() + "/GB");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public void execService() throws Exception {
        /**
         * 判断是否有集群了，有就不要创建了
         */
        try {
            if (this.paramets.get("clusterInstanceId") == null){
                /*
                 * 集群购买执行
                 */
                this.results = new ClusterServPlugin(this.AccessKeyId, this.AccessKeySecret)
                        .setVal(this.paramets)
                        .runner()
                        .refval();
            }else{
                Map<String, Object> results = new HashMap<>();
                results.put("clusterId", this.paramets.get("clusterInstanceId").toString());
                this.results = results;
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {
        ClusterService clusterService = (ClusterService) this.paramets.get("clusterService");
        Long cid = Long.parseLong(this.paramets.get("cid").toString());
        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("TkeServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("TkeServPlugin." + cid, 1);
            try {
                clusterService.UpdateStage(5, cid, 1);
                this.initService();
                this.execService();
                clusterService.UpdateStage(5, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(5, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("TkeServPlugin." + cid, 2);
            }
        }
    }

    @Override
    public Frame runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.results;
    }
}
