package com.dolphin.saas.commons.clouds.tencent.feature;

import com.dolphin.saas.commons.clouds.tencent.entity.Cvm;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CvmServ {

    private final String CVM_URL = "cvm.tencentcloudapi.com";

    private final Credential cred;
    private final HttpProfile httpProfile;

    public CvmServ(Credential cred, HttpProfile httpProfile) {
        this.cred = cred;
        this.httpProfile = httpProfile;
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
     * 计算时间差
     *
     * @param from
     * @param to
     * @return
     */
    public static String diffDate(long from, long to) {
        String diff = "";
        long nd = 1000 * 24 * 60 * 60;

        // 获得两个时间的毫秒时间差异
        long _diff = to - from;

        if (_diff <= 0) {
            diff = "0";
            return diff;
        }

        // 计算差多少天
        long day = _diff / nd;

        if (day > 0) {
            diff += day;
        }
        return diff;
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
            this.httpProfile.setEndpoint(CVM_URL);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
            CvmClient client = new CvmClient(this.cred, cvm.getReGion(), clientProfile);
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
                default:
                    inquiryPriceRunInstancesRequest.setInstanceChargeType("POSTPAID_BY_HOUR");
                    break;
            }

            Placement placement1 = new Placement();
            if (cvm.getZone() == null) {
                throw new Exception("Zone不能不输入!");
            }
            placement1.setZone(cvm.getZone());
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
            throw new Exception("计算失败:" + e.getMessage());
        }
        return results;
    }

    /**
     * 计算可用区的CVM资源
     *
     * @param reGion      归属
     * @param Zone        可用去
     * @param Concurrency 并发
     * @param buyMode     0：按需，1：预付
     * @return
     */
    public Map<String, Object> calculateZoneCvm(String reGion, String Zone, Integer Concurrency, Integer buyMode) {
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
                // 两套方案
                if (results.containsKey("8h/16G")) {
                    tmpResults.add(results.get("8h/16G"));
                    if (results.containsKey("4h/8G")) {
                        tmpResults.add(results.get("4h/8G"));
                        tmpResults.add(results.get("4h/8G"));
                    } else {
                        tmpResults.add(results.get("8h/16G"));
                    }
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
            Map<String, Object> calculateAssetsResults = this.calculateZoneCvm(cvm.getReGion(), cvm.getZone(), concurrency, buyMode);

            // 组装cvm的数据
            ArrayList<Map<String, Object>> cvmList = (ArrayList) calculateAssetsResults.get("cvm");
            for (Map<String, Object> cvmItems : cvmList) {
                // CVM基础设施
                cvm.setBaseDiskSize(100L);
                cvm.setCloudDiskSize(60L);
                cvm.setImageId("img-25szkc8t");
                cvm.setInstanceType(cvmItems.get("instance").toString());

                // CVM规格数据返回
                Map<String, Object> calcuPrice = this.calculateCvm(buyMode, cvm);

                // 总价计算
                totalMoney = totalMoney + (double) calcuPrice.get("price");

                cvmItems.put("price", calcuPrice.get("price"));
                cvmItems.put("baseDisk", 100);
                cvmItems.put("cloudDisk", 60);
                cvmItems.put("imageId", "img-25szkc8t");
                cvmItems.put("cvm", cvmItems.get("spec").toString() + "/160G/100Mbps");
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
     * 获取所有地域
     *
     * @return
     */
    public Map<String, Object> getRegionsLists() {
        Map<String, Object> results = new HashMap<>();
        try {
            this.httpProfile.setEndpoint(CVM_URL);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
            CvmClient client = new CvmClient(this.cred, "", clientProfile);
            DescribeRegionsRequest describeRegionsRequest = new DescribeRegionsRequest();
            DescribeRegionsResponse describeRegionsResponse = client.DescribeRegions(describeRegionsRequest);
            RegionInfo[] regionInfos = describeRegionsResponse.getRegionSet();
            if (regionInfos.length < 1) {
                throw new Exception("获取地域列表失败!");
            }
            ArrayList<Map<String, Object>> regionArr = new ArrayList<>();
            for (RegionInfo regionInfo : regionInfos) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("regionName", regionInfo.getRegionName());
                obj.put("region", regionInfo.getRegion());
                regionArr.add(obj);
            }
            results.put("regionLists", regionArr);
        } catch (Exception e) {
            results.put("error", e);
        }
        return results;
    }

    /**
     * 获取所有可用区列表
     *
     * @param reGion 地域
     * @return 可用区列表
     */
    public Map<String, Object> getRegionsZoneLists(String reGion) {
        Map<String, Object> results = new HashMap<>();

        try {
            this.httpProfile.setEndpoint(CVM_URL);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
            clientProfile.setLanguage(Language.ZH_CN);
            CvmClient client = new CvmClient(this.cred, reGion, clientProfile);
            DescribeZonesRequest describeZonesRequest = new DescribeZonesRequest();
            DescribeZonesResponse describeZonesResponse = client.DescribeZones(describeZonesRequest);
            ZoneInfo[] zoneInfos = describeZonesResponse.getZoneSet();
            if (zoneInfos.length < 1) {
                throw new TencentCloudSDKException("获取可用区失败!");
            }
            ArrayList<Map<String, Object>> zoneArr = new ArrayList<>();
            for (ZoneInfo zoneInfo : zoneInfos) {
                if (zoneInfo.getZoneState().equals("AVAILABLE")) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("zoneName", zoneInfo.getZoneName());
                    obj.put("zone", zoneInfo.getZone());
                    zoneArr.add(obj);
                }
            }
            results.put("zoneLists", zoneArr);
        } catch (TencentCloudSDKException e) {
            results.put("error", e);
        }
        return results;
    }

    /**
     * 获取资产列表
     *
     * @return
     */
    public Map<String, Object> getCvmAssets() {
        Map<String, Object> results = new HashMap<>();
        ArrayList<Map<String, Object>> CvmLists = new ArrayList<>();
        try {
            this.httpProfile.setEndpoint(CVM_URL);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);

            // 获取所有地域
            Map<String, Object> regionsLists = this.getRegionsLists();
            if (regionsLists.containsKey("error")) {
                throw new Exception(regionsLists.get("error").toString());
            }

            ArrayList<Map<String, Object>> regionList = (ArrayList<Map<String, Object>>) regionsLists.get("regionLists");
            // 循环遍历所有地域，都给查一圈
            for (Map<String, Object> stringObjectMap : regionList) {
                CvmClient client = new CvmClient(this.cred, stringObjectMap.get("region").toString(), clientProfile);
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

                DescribeInstancesResponse describeInstancesResponse = client.DescribeInstances(describeInstancesRequest);
                Instance[] instanceSet = describeInstancesResponse.getInstanceSet();
                if (instanceSet.length > 0) {
                    for (Instance instance : instanceSet) {
                        Map<String, Object> instanceObj = new HashMap<>();
                        instanceObj.put("cvm_region_id", stringObjectMap.get("region").toString());
                        switch (instance.getInstanceState()) {
                            case "RUNNING":
                                instanceObj.put("cvm_status", 1);
                                break;
                            case "PENDING":
                                instanceObj.put("cvm_status", 0);
                                break;
                            case "SHUTDOWN":
                            case "TERMINATING":
                            case "STOPPED":
                            case "LAUNCH_FAILED":
                                instanceObj.put("cvm_status", 3);
                                break;
                            case "STOPPING":
                            case "REBOOTING":
                            case "STARTING":
                                instanceObj.put("cvm_status", 2);
                                break;
                        }

                        instanceObj.put("cvm_config", instance.getMemory() + "G/" + instance.getSystemDisk().getDiskSize() + "GB/" + instance.getCPU() + "H");
                        instanceObj.put("cvm_cluster_outside_ip", instance.getPublicIpAddresses()[0]);
                        instanceObj.put("cvm_cluster_inside_ip", instance.getPrivateIpAddresses()[0]);
                        instanceObj.put("cvm_instance_id", instance.getInstanceId());
                        instanceObj.put("cvm_createtime", this.formatData(instance.getCreatedTime()));
                        instanceObj.put("cvm_region_source", 2);
                        instanceObj.put("cvm_tag_name", instance.getInstanceName());
                        if (!instance.getInstanceChargeType().equals("POSTPAID_BY_HOUR")) {
                            if (instance.getExpiredTime() == null) {
                                instanceObj.put("cvm_cost", "0");
                            } else {
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date1 = format.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                    Date date2 = this.reTime(instance.getExpiredTime());
                                    String diff = diffDate(date1.getTime(), date2.getTime());
                                    instanceObj.put("cvm_cost", diff);
                                } catch (Exception e) {
                                    instanceObj.put("cvm_cost", "0");
                                }
                            }
                        } else {
                            instanceObj.put("cvm_cost", "0");
                        }
                        CvmLists.add(instanceObj);
                    }
                }
            }
            results.put("CvmLists", CvmLists);
        } catch (Exception e) {
            results.put("error", e);
        }
        return results;
    }

    /**
     * 获取CVM实例ID组的状态是否全部都正常
     *
     * @param reGion      归属
     * @param instanceIds ID实例组
     * @return
     */
    public Boolean checkInstanceListStatus(String reGion, ArrayList<String> instanceIds) {
        try {
            this.httpProfile.setEndpoint(CVM_URL);
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            CvmClient client = new CvmClient(this.cred, reGion, clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeInstancesStatusRequest describeInstancesStatusRequest = new DescribeInstancesStatusRequest();
            int size = instanceIds.size();
            String[] instanceIds1 = instanceIds.toArray(new String[size]);
            describeInstancesStatusRequest.setInstanceIds(instanceIds1);
            DescribeInstancesStatusResponse describeInstancesStatusResponse = client.DescribeInstancesStatus(describeInstancesStatusRequest);

            InstanceStatus[] instanceStatusSet = describeInstancesStatusResponse.getInstanceStatusSet();
            if (instanceStatusSet.length > 0) {
                for (InstanceStatus instanceStatus : instanceStatusSet) {
                    if (!instanceStatus.getInstanceState().equals("RUNNING")) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("checkInstanceListStatus:" + e);
            return false;
        }
        return true;
    }

    /**
     * 获取正在销售的cvm信息，包括规格、价格
     *
     * @param reGion  地区
     * @param zone    可用区
     * @param payType 0：按需，1：预付
     * @return
     */
    public Map<String, Object> getSellCvmLists(String reGion, String zone, int payType) {
        Map<String, Object> results = new HashMap<>();
        try {
            this.httpProfile.setEndpoint(CVM_URL);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
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
            results.put("error", e);
        }
        return results;
    }

    /**
     * 格式化时间参数
     *
     * @param dateTime
     * @return
     */
    private String formatData(String dateTime) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateT = simpleDateFormat.parse(dateTime);
            return simpleDateFormat2.format(dateT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 格式化处理时间
     *
     * @param dateTime
     * @return
     */
    private Date reTime(String dateTime) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateT = simpleDateFormat.parse(dateTime);
            return simpleDateFormat2.parse(simpleDateFormat2.format(dateT));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
