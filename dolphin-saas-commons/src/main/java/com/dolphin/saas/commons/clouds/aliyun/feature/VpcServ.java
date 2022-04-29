package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.alibaba.fastjson.JSON;
import com.aliyun.vpc20160428.models.*;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class VpcServ extends MasterServ implements Frame {
    private final com.aliyun.vpc20160428.Client client;

    private final Map<String, Object> results = new HashMap<>();

    public VpcServ(String AccessKeyId, String AccessKeySecret) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);

        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_VPC_URL()
        );
        this.client = new com.aliyun.vpc20160428.Client(this.config);
    }

    /**
     * 获取可用地域列表
     *
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, Object>> regionsLists() throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            DescribeRegionsRequest describeRegionsRequest = new DescribeRegionsRequest()
                    .setAcceptLanguage("zh-CN");
            List<DescribeRegionsResponseBody.DescribeRegionsResponseBodyRegionsRegion> regionList = this.client.describeRegions(describeRegionsRequest).getBody().getRegions().getRegion();

            for (DescribeRegionsResponseBody.DescribeRegionsResponseBodyRegionsRegion describeRegionsResponseBodyRegionsRegion : regionList) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", describeRegionsResponseBodyRegionsRegion.getLocalName());
                items.put("value", describeRegionsResponseBodyRegionsRegion.getRegionId());
                items.put("url", describeRegionsResponseBodyRegionsRegion.getRegionEndpoint());

                results.add(items);
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取Regions]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 可用区的列表
     *
     * @param regionsId 地域
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, Object>> zoneLists(String regionsId) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            DescribeZonesRequest describeZonesRequest = new DescribeZonesRequest()
                    .setRegionId(regionsId)
                    .setAcceptLanguage("zh-cn");
            List<DescribeZonesResponseBody.DescribeZonesResponseBodyZonesZone> zoneList = this.client.describeZones(describeZonesRequest).getBody().getZones().getZone();

            for (DescribeZonesResponseBody.DescribeZonesResponseBodyZonesZone describeZonesResponseBodyZonesZone : zoneList) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", describeZonesResponseBodyZonesZone.getLocalName());
                items.put("value", describeZonesResponseBodyZonesZone.getZoneId());

                results.add(items);
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取Zone]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 检测VPC是否存活
     *
     * @param regionsId 归属地域
     * @return
     * @throws Exception
     */
    public Boolean VpcAliveCheck(String regionsId) throws Exception {
        Boolean VpcAlive = false;
        try {
            DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest()
                    .setRegionId(regionsId)
                    .setVpcName("海豚工程VPC服务")
                    .setPageNumber(1)
                    .setPageSize(10);
            List<DescribeVpcsResponseBody.DescribeVpcsResponseBodyVpcsVpc> vpcList = this.client.describeVpcs(describeVpcsRequest).getBody().getVpcs().getVpc();
            if (vpcList.size() > 0) {
                VpcAlive = true;
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][检测VPC存活]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return VpcAlive;
    }

    /**
     * 获取VPCID
     *
     * @param regionsId 归属
     * @return
     * @throws Exception
     */
    public String GetVpcId(String regionsId) throws Exception {
        String VpcId = null;
        try {
            DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest()
                    .setRegionId(regionsId)
                    .setVpcName("海豚工程VPC服务")
                    .setPageNumber(1)
                    .setPageSize(50);
            List<DescribeVpcsResponseBody.DescribeVpcsResponseBodyVpcsVpc> vpcList = this.client.describeVpcs(describeVpcsRequest).getBody().getVpcs().getVpc();
            if (vpcList.size() > 0) {
                if (vpcList.get(0).getStatus().equals("Available")){
                    VpcId = vpcList.get(0).getVpcId();
                }
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取VPCID]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return VpcId;
    }

    /**
     * 创建VPC服务
     *
     * @param regionsId 归属地域
     * @return
     * @throws Exception
     */
    public void VpcCreate(String regionsId, String ipRange) throws Exception {
        try {
            CreateVpcRequest createVpcRequest = new CreateVpcRequest()
                    .setRegionId(regionsId)
                    .setCidrBlock(ipRange)
                    .setVpcName("海豚工程VPC服务")
                    .setDescription("海豚工程专用的VPC的服务");
            this.client.createVpc(createVpcRequest).getBody();
        } catch (Exception e) {
            log.error("[阿里云SDK][创建VPC]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 创建交换机
     *
     * @param regionsId
     * @param zoneId
     * @param ipRange
     * @param vpcId
     * @throws Exception
     */
    public void VpcCreateSwitch(String regionsId, String zoneId, String ipRange, String vpcId) throws Exception {
        try {
            log.info("创建交换机参数: {}, {}, {}, {}", regionsId, zoneId, ipRange, vpcId);
            CreateVSwitchRequest createVSwitchRequest = new CreateVSwitchRequest()
                    .setRegionId(regionsId)
                    .setZoneId(zoneId)
                    .setCidrBlock(ipRange)
                    .setVpcId(vpcId)
                    .setVSwitchName("海豚工程交换机")
                    .setDescription("海豚工程DevOPS专属");
            this.client.createVSwitch(createVSwitchRequest).getBody().getVSwitchId();
        } catch (Exception e) {
            log.error("[阿里云SDK][创建交换机]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取归属区域指定VPC的一个交换机ID
     * @param VpcId
     * @return
     * @throws Exception
     */
    public String GetVpcSwitchId(Map<String, Object> paramets, String VpcId) throws Exception {
        String switchId = null;
        try {
            DescribeVSwitchesRequest describeVSwitchesRequest = new DescribeVSwitchesRequest()
                    .setVpcId(VpcId)
                    .setZoneId(paramets.get("zone").toString())
                    .setRegionId(paramets.get("region").toString());
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeVSwitchesResponseBody.DescribeVSwitchesResponseBodyVSwitchesVSwitch> switches = client.describeVSwitches(describeVSwitchesRequest).getBody().getVSwitches().getVSwitch();

            if (switches.size() > 0) {
                switchId = switches.get(0).getVSwitchId();
            }else{
                for (int i=1; i<=254; i++) {
                    try {
                        this.VpcCreateSwitch(
                                paramets.get("region").toString(),
                                paramets.get("zone").toString(),
                                String.format("10.56.%s.0/24", i),
                                VpcId);
                        break;
                    }catch (Exception e){
                        log.warn("[阿里云SDK][创建交换机]失败，换个地址再试: {}/region: {}, zone: {}, vpcId: {}",
                                String.format("10.56.%s.0/24", i), paramets.get("region").toString(), paramets.get("zone").toString(), VpcId);
                    }
                }
            }

        }catch (Exception e){
            log.error("[阿里云SDK][获取交换机ID]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return switchId;
    }

    /**
     * 创建EIP实例
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, String>> createEipAddr(ArrayList<Map<String, String>> instanceInfo) throws Exception {
        // 先获取数据，然后判断是否已经创建了
        // 如果创建了就不创建了，直接返回，没有创建则创建新的返回
        ArrayList<Map<String, String>> IpAddr = new ArrayList<>();
        for (Map<String, String> instanceItems: instanceInfo){
            // 实例的名称
            String instanceName = "海豚工程-" + instanceItems.get("instanceId");
            Map<String, Object> ItemsResults = this.checkEipAddr(instanceName);

            if (ItemsResults.isEmpty()){
                // 创建EIP
                AllocateEipAddressRequest allocateEipAddressRequest = new AllocateEipAddressRequest()
                        .setRegionId(this.paramets.get("region").toString())
                        .setName(instanceName)
                        .setDescription("海豚工程部署专用");
                this.client.allocateEipAddress(allocateEipAddressRequest).getBody();
            }else{
                // 绑定EIP
                this.buildEipAddr(instanceName, ItemsResults.get("allocationId").toString(), instanceItems.get("instanceId"));
                instanceItems.put("instanceIp", this.checkEipAddr(instanceName).get("ip").toString());
                instanceItems.put("allocationId", ItemsResults.get("allocationId").toString());
                IpAddr.add(instanceItems);
            }
        }

        return IpAddr;
    }

    /**
     * 获取EIP地址
     * @param instanceName
     * @return
     * @throws Exception
     */
    public String getEipAddr(String instanceName) throws Exception {
        String eipAddr = null;
        try {
            DescribeEipAddressesRequest describeEipAddressesRequest = new DescribeEipAddressesRequest()
                    .setEipName(instanceName)
                    .setRegionId(this.paramets.get("region").toString());
            List<DescribeEipAddressesResponseBody.DescribeEipAddressesResponseBodyEipAddressesEipAddress> describeEipAddressesList = this.client.describeEipAddresses(describeEipAddressesRequest).getBody().getEipAddresses().getEipAddress();

            if (describeEipAddressesList.size() > 0){
                eipAddr = describeEipAddressesList.get(0).getIpAddress();
            }
        }catch (Exception e){
            log.error("[阿里云SDK][获取EIP]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return eipAddr;
    }


    /**
     * 解绑+释放EIP资源
     * @param allocationId
     * @throws Exception
     */
    public void releaseBuildEipAddr(String allocationId) throws Exception {
        try {
            /*
             * 先解绑资源
             */
            UnassociateEipAddressRequest unassociateEipAddressRequest = new UnassociateEipAddressRequest()
                    .setAllocationId(allocationId);
            client.unassociateEipAddress(unassociateEipAddressRequest);

            /*
             * 释放资源
             */
            ReleaseEipAddressRequest releaseEipAddressRequest = new ReleaseEipAddressRequest()
                    .setAllocationId(allocationId);
            client.releaseEipAddress(releaseEipAddressRequest);
        }catch (Exception e){
            log.error("[阿里云SDK][解绑、释放EIP]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断EIP是否已经创建了
     * @return
     * @throws Exception
     */
    public Map<String, Object> checkEipAddr(String instanceName) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            DescribeEipAddressesRequest describeEipAddressesRequest = new DescribeEipAddressesRequest()
                    .setEipName(instanceName)
                    .setRegionId(this.paramets.get("region").toString());
            List<DescribeEipAddressesResponseBody.DescribeEipAddressesResponseBodyEipAddressesEipAddress> describeEipAddressesList = this.client.describeEipAddresses(describeEipAddressesRequest).getBody().getEipAddresses().getEipAddress();

            if (describeEipAddressesList.size() > 0){
                results.put("ip", describeEipAddressesList.get(0).getIpAddress());
                results.put("instanceId", describeEipAddressesList.get(0).getInstanceId());
                results.put("allocationId", describeEipAddressesList.get(0).getAllocationId());
            }
        }catch (Exception e){
            log.error("[阿里云SDK][获取EIP]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 绑定资产EIP
     * @param allocationId
     * @param instanceId
     * @throws Exception
     */
    public void buildEipAddr(String instanceName, String allocationId, String instanceId) throws Exception {
        try {
            // 获取所有的绑定列表，确定没有绑定才绑定
            DescribeEipAddressesRequest describeEipAddressesRequest = new DescribeEipAddressesRequest()
                    .setEipName(instanceName)
                    .setAllocationId(allocationId)
                    .setRegionId(this.paramets.get("region").toString());
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeEipAddressesResponseBody.DescribeEipAddressesResponseBodyEipAddressesEipAddress> eipAddress = client.describeEipAddresses(describeEipAddressesRequest).getBody().getEipAddresses().getEipAddress();

            if (eipAddress.size() > 0 && eipAddress.get(0).getInstanceId().equals("")){
                AssociateEipAddressRequest associateEipAddressRequest = new AssociateEipAddressRequest()
                        .setAllocationId(allocationId)
                        .setInstanceId(instanceId);
                client.associateEipAddress(associateEipAddressRequest);
            }
        }catch (Exception e){
            log.error("[阿里云SDK][绑定资产EIP]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public VpcServ setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {

    }

    /**
     * 获取交换机状态
     * @return
     * @throws Exception
     */
    public Boolean SwithAliveCheck(Map<String, Object> paramets, String vpcId) throws Exception {
        try {
            DescribeVSwitchesRequest describeVSwitchesRequest = new DescribeVSwitchesRequest()
                    .setVpcId(vpcId)
                    .setZoneId(paramets.get("zone").toString())
                    .setRegionId(paramets.get("region").toString());
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeVSwitchesResponseBody.DescribeVSwitchesResponseBodyVSwitchesVSwitch> switches = client.describeVSwitches(describeVSwitchesRequest).getBody().getVSwitches().getVSwitch();

            return switches.size() > 0;

//            for(DescribeVSwitchesResponseBody.DescribeVSwitchesResponseBodyVSwitchesVSwitch switchesVSwitch:switches){
//                zoneArrs.remove(switchesVSwitch.getZoneId());
//            }
//
//            if (zoneArrs.size() > 0){
//                for (String zoneItems:zoneArrs){
//                    for (int i=1; i<=254; i++) {
//                        try {
//                            this.VpcCreateSwitch(
//                                    region,
//                                    zoneItems,
//                                    String.format("10.56.%s.0/24", i),
//                                    vpcId);
//                            break;
//                        }catch (Exception e){
//                            log.warn("[阿里云SDK][创建交换机]失败，换个地址再试: {}/region: {}, zone: {}, vpcId: {}",
//                                    String.format("10.56.%s.0/24", i), region, zoneItems, vpcId);
//                        }
//                    }
//                }
//            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void execService() throws Exception {
        String vpcId = null;
        String switchId = null;
        try {
            if (!this.VpcAliveCheck(this.paramets.get("region").toString())) {
                this.VpcCreate(
                        this.paramets.get("region").toString(),
                        this.paramets.get("subnet").toString());
            }
            vpcId = this.GetVpcId(this.paramets.get("region").toString());

            if (vpcId != null){
                // 检查并且创建所有可用区的交换机
                try {
                    switchId = this.GetVpcSwitchId(this.paramets, vpcId);
                }catch (Exception e){
                    log.warn("[阿里云SDK][交换机创建未完成]信息: {}", e.getMessage());
                }

                if (switchId != null) {
                    this.results.put("vpcId", vpcId);
                    this.results.put("switchId", switchId);
                }
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][交换创建处理]异常信息: {}", e.getMessage());
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
        if (!redisCommonUtils.hasKeys("VpcServ." + cid)) {
            redisCommonUtils.noExpireSset("VpcServ." + cid, 1);
            try {
                clusterService.UpdateStage(2, cid, 1);
                this.execService();
                clusterService.UpdateStage(2, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(2, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("VpcServ." + cid);
            }
        }
    }

    /**
     * 删除交换机
     * @param vswId
     * @throws Exception
     */
    public void deleteSwitch(String vswId) throws Exception {
        try {
            DeleteVSwitchRequest deleteVSwitchRequest = new DeleteVSwitchRequest()
                    .setVSwitchId(vswId);
            // 复制代码运行请自行打印 API 的返回值
            client.deleteVSwitch(deleteVSwitchRequest);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public VpcServ runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.results;
    }
}
