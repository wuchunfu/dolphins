package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.aliyun.ecs20140526.models.*;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class EcsServ extends MasterServ implements Frame {
    private final com.aliyun.ecs20140526.Client client;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public EcsServ(String AccessKeyId, String AccessKeySecret, Map<String, Object> paramets) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);
        this.paramets = paramets;

        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_ECS_URL().replace("{REGIONS}", this.paramets.get("region").toString())
        );
        this.client = new com.aliyun.ecs20140526.Client(this.config);
    }

    /**
     * 获取集群所有的实例信息
     *
     * @param clusterId
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, String>> getInstanceLists(String clusterId) throws Exception {
        ArrayList<Map<String, String>> InstancesLists = new ArrayList<>();
        try {
            DescribeInstancesRequest.DescribeInstancesRequestTag tag0 = new DescribeInstancesRequest.DescribeInstancesRequestTag()
                    .setValue(clusterId)
                    .setKey("ack.aliyun.com");
            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
                    .setRegionId(this.paramets.get("region").toString())
                    .setTag(java.util.Arrays.asList(
                            tag0
                    ));
            List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> instances = this.client.describeInstances(describeInstancesRequest).getBody().getInstances().getInstance();

            if (instances.size() > 0) {
                for (DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instanceItems : instances) {
                    Map<String, String> items = new HashMap<>();
                    items.put("instanceId", instanceItems.getInstanceId());

                    try {
                        // 开启外网带宽
                        ModifyInstanceSpecRequest modifyInstanceSpecRequest = new ModifyInstanceSpecRequest()
                                .setInstanceId(instanceItems.getInstanceId())
                                .setInternetMaxBandwidthOut(100);
                        // 复制代码运行请自行打印 API 的返回值
                        client.modifyInstanceSpec(modifyInstanceSpecRequest);
                    }catch (Exception e){
                        log.warn("[*]开启外网带宽失败: {}", e.getMessage());
                    }
                    items.put("instanceIp", instanceItems.getEipAddress().getIpAddress());
                    items.put("instanceSwitchId", instanceItems.getVpcAttributes().getVSwitchId());
                    // 获取公网IP
                    if (instanceItems.getPublicIpAddress().getIpAddress().size() > 0) {
                        items.put("publicIp", instanceItems.getPublicIpAddress().getIpAddress().get(0));
                    } else {
                        String ipAddr = this.getPublicIp(instanceItems.getInstanceId());
                        if (ipAddr != null) {
                            items.put("publicIp", ipAddr);
                        }
                    }
                    InstancesLists.add(items);
                }
            }
        } catch (Exception e) {
            log.info("[阿里云SDK][获取实例ID]失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return InstancesLists;
    }

    /**
     * 获取并绑定公网的IP
     *
     * @param instanceId 实例ID
     * @return
     * @throws Exception
     */
    public String getPublicIp(String instanceId) throws Exception {
        try {
            AllocatePublicIpAddressRequest allocatePublicIpAddressRequest = new AllocatePublicIpAddressRequest()
                    .setInstanceId(instanceId);
            // 复制代码运行请自行打印 API 的返回值
            return this.client.allocatePublicIpAddress(allocatePublicIpAddressRequest).getBody().ipAddress;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算价格成本
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> calculateAssets() throws Exception {
        Map<String, Object> results = new HashMap<>();
        Boolean Period = false;
        double totalPrice = 0F;
        results.put("cloud", "阿里云");
        results.put("createtime", new Date());
        ArrayList<Map<String, Object>> assetList = new ArrayList<>();

        /**
         * 通用资源计算
         */
        if (Integer.parseInt(this.paramets.get("buyType").toString()) == 0) {
            Period = true;
            Map<String, Object> slb = new HashMap<>();
            slb.put("type", "SLB");
            slb.put("price", 9.12 + "元/天");
            slb.put("spec", "SLB入口费用");
            slb.put("total", "N");
            slb.put("info", "标准版");
            slb.put("remark", "价格区间在0.320至0.380元/小时，按照最小单位1天做预算，预计花9.12元，标准费用，无法删除。");
            assetList.add(slb);
            totalPrice = totalPrice + 9.12;

            Map<String, Object> nat = new HashMap<>();
            nat.put("type", "NAT");
            nat.put("price", 14.39 + "元/天");
            nat.put("spec", "NAT出口费用");
            nat.put("total", "N");
            nat.put("info", "标准版");
            nat.put("remark", "价格区间在0.460至0.600元/小时，按照最小单位1天做预算，预计花14.39元，标准费用，无法删除。");
            assetList.add(nat);
            totalPrice = totalPrice + 14.39;

            Map<String, Object> eip = new HashMap<>();
            eip.put("type", "EIP");
            eip.put("price", 10 + "元/天");
            eip.put("spec", "EIP地址费用");
            eip.put("info", "标准版");
            eip.put("total", "1个");
            eip.put("remark", "价格区间在0.600至1元/GB，按照最小单位10GB/天做预算，预计花10元，标准费用，无法删除。");
            assetList.add(eip);
            totalPrice = totalPrice + 10;

            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "ACK");
            ack.put("price", 15.36 + "元/天");
            ack.put("spec", "ACK托管集群的费用");
            ack.put("info", "标准托管版");
            ack.put("total", "1个实例");
            ack.put("remark", "价格区间固定在0.64元/小时，按照最小单位1天做预算，预计花15.36元，标准费用，无法删除。");
            assetList.add(ack);
            totalPrice = totalPrice + 15.36;
        } else {
            Map<String, Object> slb = new HashMap<>();
            slb.put("type", "SLB");
            slb.put("price", 228 + "元/月");
            slb.put("spec", "SLB入口费用");
            slb.put("info", "标准版");
            slb.put("total", "N");
            slb.put("remark", "价格区间在190至228元/月，标准费用，无法删除。");
            assetList.add(slb);
            totalPrice = totalPrice + 228;

            Map<String, Object> nat = new HashMap<>();
            nat.put("type", "NAT");
            nat.put("price", 431.70 + "元/月");
            nat.put("spec", "NAT出口费用");
            nat.put("info", "标准版");
            nat.put("total", "N");
            nat.put("remark", "价格区间在0.460至0.600元/小时，按照最小30天做预算，预计花431.70元，标准费用，无法删除。");
            assetList.add(nat);
            totalPrice = totalPrice + 431.70;

            Map<String, Object> eip = new HashMap<>();
            eip.put("type", "EIP");
            eip.put("price", 300 + "元/月");
            eip.put("spec", "EIP地址费用");
            eip.put("total", "1个");
            eip.put("info", "标准版");
            eip.put("remark", "价格区间在0.600至1元/GB，按照最小单位10GB/天，最少30天做预算，预计花300元，标准费用，无法删除。");
            assetList.add(eip);
            totalPrice = totalPrice + 300;

            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "ACK");
            ack.put("price", 460.8 + "元/月");
            ack.put("spec", "ACK托管集群Pro的费用");
            ack.put("total", "1个实例");
            ack.put("info", "标准托管版");
            ack.put("remark", "价格区间固定在0.64元/小时，按照最小单位30天做预算，预计花460.8元，标准费用，无法删除。");
            assetList.add(ack);
            totalPrice = totalPrice + 460.8;
        }

        Map<String, Object> nas = new HashMap<>();
        nas.put("type", "NAS");
        nas.put("price", 61.8 + "元/月");
        nas.put("spec", "NAS存储费用");
        nas.put("total", "多个实例");
        nas.put("info", "性能型");
        nas.put("remark", "读带宽(峰值)：600MBps/TiB，一般企业够用了；费用组成由存储费用0.15元/GiB/月+续写费用0.06元/GiB+产品标准费用1.85元/GiB/月组成，单月预计在30GiB左右，因为有docker镜像，所以会大一点。");
        assetList.add(nas);
        totalPrice = totalPrice + 61.8;

        try {
            // 组装数据
            Map<String, Object> cvm = new HashMap<>();
            Map<Float, String> priceLists = new HashMap<>();
            ArrayList<String> instanceLists = new ArrayList<>();
            // 台数
            Integer count = 0;
            switch (Integer.parseInt(this.paramets.get("current").toString())) {
                case 1:
                    results.put("current", "约100-200人同时在线");
                    cvm.put("info", "8H/16G");
                    count = 2;
                    // 获取所有可用的实例
                    instanceLists = this.getResourceLists(16, 8);
                    break;
                case 50:
                    results.put("current", "约500-1000人同时在线");
                    cvm.put("info", "16H/16G");
                    count = 3;
                    // 获取所有可用的实例
                    instanceLists = this.getResourceLists(16, 16);
                    break;
                case 250:
                    results.put("current", "约2500~5000人同时在线");
                    cvm.put("info", "16H/16G");
                    count = 4;
                    // 5个节点
                    instanceLists = this.getResourceLists(16, 16);
                    break;
            }

            for (String instanceItems : instanceLists) {
                try {
                    priceLists.put(this.getResourcePrice(instanceItems, Period), instanceItems);
                } catch (Exception e) {
                    log.info("[阿里云SDK][实例价格获取]失效: {}, {}", instanceItems, e.getMessage());
                }
            }
            // 规格排序
            priceLists = sortMapByKey(priceLists);
            ArrayList<Float> tmpArrs = new ArrayList<>(priceLists.keySet());
            /*
             * CVM的数据
             */
            String ecs_period_remark = "价格区间固定在%s元/小时，按照最小单位1天做预算，预计花%s元，标准费用。";
            String ecs_month_remark = "价格区间固定在%s元/月，标准费用。";
            String ecs_remark;
            if (Period) {
                Float prices = new BigDecimal(Float.valueOf(tmpArrs.get(0) * 24))
                        .setScale(2, RoundingMode.HALF_UP).floatValue();
                ecs_remark = String.format(ecs_period_remark, tmpArrs.get(0), prices);
                cvm.put("price", prices + "元/天/台");
                totalPrice = totalPrice + prices * count;
            } else {
                ecs_remark = String.format(ecs_month_remark, tmpArrs.get(0));
                cvm.put("price", tmpArrs.get(0) + "元/月/台");
                totalPrice = totalPrice + tmpArrs.get(0) * count;
            }

            cvm.put("type", "ECS");
            cvm.put("spec", "云服务器费用");
            cvm.put("total", count+"/个");
            cvm.put("remark", ecs_remark);
            assetList.add(cvm);
            results.put("assetList", assetList);
            results.put("totalPrice", totalPrice);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 获取实例规格的价格
     *
     * @param instanceType 规格
     * @param Period       是否按需
     * @return
     * @throws Exception
     */
    public Float getResourcePrice(String instanceType, Boolean Period) throws Exception {
        Float price;
        String unit;
        try {
            if (Period) {
                unit = "Hour";
            } else {
                unit = "Month";
            }
            DescribePriceRequest describePriceRequest = new DescribePriceRequest()
                    .setRegionId(this.paramets.get("region").toString())
                    .setInstanceType(instanceType)
                    .setResourceType("instance")
                    .setPriceUnit(unit);
            // 复制代码运行请自行打印 API 的返回值
            price = this.client.describePrice(describePriceRequest).getBody().getPriceInfo().getPrice().getTradePrice();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return price;
    }

    /**
     * 获取该区域可用的实例规格
     *
     * @param memory 内存
     * @param cpu    CPU
     * @return
     * @throws Exception
     */
    public ArrayList<String> getResourceLists(Integer memory, Integer cpu) throws Exception {
        ArrayList<String> resourceResults = new ArrayList<>();
        try {
            DescribeAvailableResourceRequest describeAvailableResourceRequest = new DescribeAvailableResourceRequest()
                    .setRegionId(this.paramets.get("region").toString())
                    .setDestinationResource("InstanceType")
                    .setZoneId(this.paramets.get("zone").toString())
                    .setMemory(memory.floatValue())
                    .setResourceType("instance")
                    .setCores(cpu);
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeAvailableResourceResponseBody
                    .DescribeAvailableResourceResponseBodyAvailableZonesAvailableZone> zoneLists = client
                    .describeAvailableResource(describeAvailableResourceRequest)
                    .getBody()
                    .getAvailableZones()
                    .getAvailableZone();

            if (zoneLists.size() > 0) {
                List<DescribeAvailableResourceResponseBody
                        .DescribeAvailableResourceResponseBodyAvailableZonesAvailableZoneAvailableResourcesAvailableResource> decribeResouces = zoneLists
                        .get(0).getAvailableResources()
                        .getAvailableResource();
                if (decribeResouces.size() > 0) {
                    List<DescribeAvailableResourceResponseBody
                            .DescribeAvailableResourceResponseBodyAvailableZonesAvailableZoneAvailableResourcesAvailableResourceSupportedResourcesSupportedResource>
                            describeResourceInstance = decribeResouces.get(0)
                            .getSupportedResources().getSupportedResource();

                    for (DescribeAvailableResourceResponseBody
                            .DescribeAvailableResourceResponseBodyAvailableZonesAvailableZoneAvailableResourcesAvailableResourceSupportedResourcesSupportedResource items : describeResourceInstance) {
                        if (items.getStatus().equals("Available")) {
                            resourceResults.add(items.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取实例规格]失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }

        return resourceResults;
    }


    @Override
    public EcsServ setVal(Map<String, Object> paramets) {
        return null;
    }

    @Override
    public void initService() throws Exception {

    }

    @Override
    public void execService() throws Exception {

    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {

    }

    @Override
    public EcsServ runner() throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return null;
    }
}
