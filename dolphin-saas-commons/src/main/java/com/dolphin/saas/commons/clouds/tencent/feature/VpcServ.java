package com.dolphin.saas.commons.clouds.tencent.feature;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vpc.v20170312.VpcClient;
import com.tencentcloudapi.vpc.v20170312.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VpcServ {

    private final String VPC_URL = "vpc.tencentcloudapi.com";

    private final Credential cred;
    private final HttpProfile httpProfile;

    public VpcServ(Credential cred, HttpProfile httpProfile) {
        this.cred = cred;
        this.httpProfile = httpProfile;
    }

    /**
     * 创建VPC网段
     *
     * @param IpRange IP范围段
     * @param reGion  归属地址
     * @return
     */
    public Map<String, Object> createVpc(String IpRange, String reGion, String Zone) throws TencentCloudSDKException {
        String VpcName = "海豚工程-专用VPC";
        Map<String, Object> results = new HashMap<>();
        this.httpProfile.setEndpoint(VPC_URL);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(this.httpProfile);
        VpcClient client = new VpcClient(this.cred, reGion, clientProfile);
        // 先查有没有这个vpc网络，有就不新建了
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.setOffset("0");
        describeVpcsRequest.setLimit("100");
        DescribeVpcsResponse describeVpcsResponse = client.DescribeVpcs(describeVpcsRequest);
        Vpc[] vpcLists = describeVpcsResponse.getVpcSet();

        // 判断是否已经有VPC了
        // 如果有，就查有没有海豚相关的VPC
        // 如果有，就优先用已经有了的，不让客户重复创建
        if (vpcLists.length > 0) {
            for (Vpc vpcList : vpcLists) {
                if (vpcList.getVpcName().equals(VpcName)) {
                    results.put("vpcId", vpcList.getVpcId());
                    results.put("ipRange", vpcList.getCidrBlock());
                }
            }
        }

        // 如果没有可用的VPC返回，就直接创建
        if (!results.containsKey("vpcId")) {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateVpcRequest createVpcRequest = new CreateVpcRequest();
            createVpcRequest.setVpcName(VpcName);
            createVpcRequest.setCidrBlock(IpRange);
            CreateVpcResponse createVpcResponse = client.CreateVpc(createVpcRequest);
            results.put("vpcId", createVpcResponse.getVpc().getVpcId());
            results.put("ipRange", createVpcResponse.getVpc().getCidrBlock());
            // 没有VPC就一定没有子网，顺手就创建了
            CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest();
            createSubnetRequest.setVpcId(createVpcResponse.getVpc().getVpcId());
            createSubnetRequest.setSubnetName("海豚工程-私有子网");
            createSubnetRequest.setCidrBlock("10.56.1.0/24");
            createSubnetRequest.setZone(Zone);
            // 返回的resp是一个CreateSubnetResponse的实例，与请求对象对应
            CreateSubnetResponse createSubnetResponse = client.CreateSubnet(createSubnetRequest);
            if (createSubnetResponse.getSubnet().getSubnetId() != null) {
                results.put("subNetId", createSubnetResponse.getSubnet().getSubnetId());
                results.put("ipAdrr", createSubnetResponse.getSubnet().getAvailableIpAddressCount());
            } else {
                throw new TencentCloudSDKException("创建子网失败!");
            }
        } else {
            DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
            DescribeSubnetsResponse describeSubnetsResponse = client.DescribeSubnets(describeSubnetsRequest);
            Subnet[] subnets = describeSubnetsResponse.getSubnetSet();
            for (Subnet subnet : subnets) {
                if (subnet.getVpcId().equals(results.get("vpcId"))) {
                    results.put("subNetId", subnet.getSubnetId());
                }
            }
        }
        return results;
    }
}
