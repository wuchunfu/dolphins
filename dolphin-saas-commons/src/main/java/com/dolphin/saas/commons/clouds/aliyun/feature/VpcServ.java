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

//    public static void main(String[] args) {
//        List<String> vsw = Arrays.asList(
//                "vsw-2zepg0h1nv0gar0mo31is",
//                "vsw-2ze364nyvtvpdxzdqkbed",
//                "vsw-2zeatxx1l88urlblicuyg",
//                "vsw-2zeib5daonbtqqhtkfs42",
//                "vsw-2zev7xc90904vqiwdyxad",
//                "vsw-2ze5xbjo2sghp3ghizvm1",
//                "vsw-2zerp6h3to82uuip3gvye",
//                "vsw-2ze8j3fdmbofu8wxzu20y",
//                "vsw-2ze80ry4cqs5uro5egx6n",
//                "vsw-2zee8dgtjlltf4yrx2fjj",
//                "vsw-2zeya0xfql3xcl7lz1zbh",
//                "vsw-2ze1zjicaljvniinx8iig",
//                "vsw-2ze4x627s5iunr8753fbx",
//                "vsw-2ze401y10xpcds63mg5vm",
//                "vsw-2zeqqccc3to9vnyyp5vgm",
//                "vsw-2zeuzgihlafcwy9nzl8tt",
//                "vsw-2zeamydc0f1561tvgucu3",
//                "vsw-2zeiq5c6en5qup08qmn46",
//                "vsw-2zesctoop81adddq25rpi",
//                "vsw-2zenha22w1jtoh7gzrxhu",
//                "vsw-2zeq3io9ncy0291xbzefj",
//                "vsw-2zemw42s5ohye2leu6wdg",
//                "vsw-2zest6vc4769kjsojg9ry",
//                "vsw-2ze0d25zp8aa6dloj1t3g",
//                "vsw-2ze9o63ng7ykzbdc2b3hw",
//                "vsw-2ze6ip6t14ntfj71gke42",
//                "vsw-2ze4ccvy99ehf0g7q8kzl",
//                "vsw-2ze6vgjbhwovq7a2cw9qy",
//                "vsw-2zesuoympsfy57iqq0ged",
//                "vsw-2zegxqq5rvniq9xdu4r47",
//                "vsw-2ze7w3txwkph94wrq1dsl",
//                "vsw-2zepusrv904l0ngnwnuyr",
//                "vsw-2zet9sc0dlhz1q0ui214y",
//                "vsw-2zejlqrqef034zt57q6em",
//                "vsw-2zepf18me3375jdjo6nkb",
//                "vsw-2ze35kntmhflyeizdc3dk",
//                "vsw-2zedied7elw8lh0b9ko6h",
//                "vsw-2ze0r6q9v2cy3nl0xptye",
//                "vsw-2ze3x0krhsfyrl3j11960",
//                "vsw-2zeydtn0qd72q8wlm0dyy",
//                "vsw-2zenz421zh74wdd0uqhgm",
//                "vsw-2zeh48um75ous8aulk4nv",
//                "vsw-2ze3uzhmf8vo8g3mon47e",
//                "vsw-2ze067nsp6h4bluuc6dog",
//                "vsw-2zelf7nsheiljq68xfn6j",
//                "vsw-2zej9h8e1vpwfy714vg59",
//                "vsw-2ze53p7c34l5ruz8l69su",
//                "vsw-2ze0f6aq8fzlx4d5o4o1m",
//                "vsw-2zek7x5rm759f7h6hs6co",
//                "vsw-2zexa1c6ccfhhncjyu1vk",
//                "vsw-2zeelboryn9ngrkxrntpd",
//                "vsw-2zekjl7km3o2seurx57mx",
//                "vsw-2zestooi8bjh3w2lpy6og",
//                "vsw-2zej9briz43mdr5l05kkm",
//                "vsw-2zey5gvjib32y2w3otb3i",
//                "vsw-2zefvl4ssnxb9zd3nbwyu",
//                "vsw-2zegdow7o1yxxm3v8gm53",
//                "vsw-2ze0doms6v76pks2yjnkj",
//                "vsw-2ze0jhu600soh69ah3wta",
//                "vsw-2zepcham0jqqmbf78hc23",
//                "vsw-2zeygfmcx9rleup68fjk3",
//                "vsw-2zeg4fbjxuazz6g4yj5ch",
//                "vsw-2zet3z4mknkee49g0eib0",
//                "vsw-2zeovx8in1ny1brutinln",
//                "vsw-2zefzr7dhhnta4i9jr3hf",
//                "vsw-2ze1444zit1jifptc1pu0",
//                "vsw-2ze3q111yab6um3a605id",
//                "vsw-2zewgfl6j2ffrg5ent38g",
//                "vsw-2zev2ncm7hph8rbabjvgy",
//                "vsw-2zehcr2y53pzeyruoxwxm",
//                "vsw-2zen3y0fckhpdcpahow29",
//                "vsw-2zerhdpzsc6w7jm2iim00",
//                "vsw-2zej4pad262sag4przdkv",
//                "vsw-2zepw2pyvrmb2oowungpa",
//                "vsw-2zekzym9rdga0wbufjxgl",
//                "vsw-2zeubb18d9xi7cj30ehm4",
//                "vsw-2ze611u06w05hn6k1k2oh",
//                "vsw-2zev8i7115tg117jtder3",
//                "vsw-2zecuurehof7mxp5lq3ke",
//                "vsw-2zeujt9koarznf1ks8a4z",
//                "vsw-2zeg2fmzt795dvnyiqkka",
//                "vsw-2zediaax7mowvgn98zz8v",
//                "vsw-2ze5ue8ootstv7h528yyc",
//                "vsw-2zepcmrh3fqoqsky3r1sz",
//                "vsw-2zel0x1b4nfj16tk2m847",
//                "vsw-2zekwi0y56w5wpua88fhd",
//                "vsw-2zeqnufa2enn53vimajq7",
//                "vsw-2ze8mjc59wx0s8lklz8v9",
//                "vsw-2zebb9bgcvd7t813u7ztw",
//                "vsw-2zedhtwbz93j5g7u4k0pw",
//                "vsw-2zempqeixgi7tverbf1zt",
//                "vsw-2zekvln3fsrqwcx66om8l",
//                "vsw-2zem51ajjpw5cwlhmc3xu",
//                "vsw-2zenee5ofgsiirlstkhxl",
//                "vsw-2ze6koa0zliav9p48gwqh"
//        );
//        try {
//            for (String s: vsw){
//                VpcServ vpcServ = new VpcServ("LTAI5tCMXX5mU9h7PKCRtxWm", "BE8k5H7ZGVu1cErFn0zS8EJg0JFji0");
//                vpcServ.deleteSwitch(s);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
