package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.aliyun.ram20150501.models.*;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
public class RamServ extends MasterServ implements Frame {
    private final com.aliyun.ram20150501.Client client;

    private Boolean userAlive = false;

    private Map<String, Object> results = new HashMap<>();

    private final List<String> policyLists = Arrays.asList(
            "AliyunContainerRegistryFullAccess", "AliyunCSFullAccess",
            "AliyunOSSFullAccess", "AliyunECSFullAccess",
            "AliyunSLBFullAccess", "AdministratorAccess",
            "AliyunNASFullAccess", "AliyunASMFullAccess");

    public RamServ(String AccessKeyId, String AccessKeySecret) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);

        // 访问的域名
        this.config.setEndpoint(this.getALIYUN_RAM_URL());
        this.client = new com.aliyun.ram20150501.Client(this.config);
    }

    /**
     * 创建授权用户
     *
     * @param userName
     * @param displayName
     * @param emailInfo
     * @param commentInfo
     * @throws Exception
     */
    public void createUser(String userName, String displayName, String emailInfo, String commentInfo) throws Exception {
        try {

            CreateUserRequest createUserRequest = new CreateUserRequest()
                    .setUserName(userName)
                    .setDisplayName(displayName)
                    .setEmail(emailInfo)
                    .setComments(commentInfo);
            this.client.createUser(createUserRequest);
            log.info("[阿里云SDK][创建用户]创建成功, 用户名: {}, 显示名: {}", userName, displayName);
        } catch (Exception e) {
            log.error("[阿里云SDK][创建用户]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断元豚账户是否存在
     *
     * @return
     * @throws Exception
     */
    public Boolean userCheck(String userName) throws Exception {
        Boolean aiDolphinsUsers = false;
        try {
            ListUsersRequest listUsersRequest = new ListUsersRequest();
            List<ListUsersResponseBody.ListUsersResponseBodyUsersUser> users = this.client.listUsers(listUsersRequest).getBody().getUsers().getUser();
            for (ListUsersResponseBody.ListUsersResponseBodyUsersUser usersResponseBodyUsersUser : users) {
                if (usersResponseBodyUsersUser.getUserName().equals(userName) && usersResponseBodyUsersUser.getDisplayName().equals("海豚工程Devops")) {
                    aiDolphinsUsers = true;
                }
            }
            log.info("[阿里云SDK][获取指定用户]状态: {}", aiDolphinsUsers);
        } catch (Exception e) {
            log.error("[阿里云SDK][用户列表]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return aiDolphinsUsers;
    }

    /**
     * 创建用户对应的token
     *
     * @param UserName
     * @return
     * @throws Exception
     */
    public Map<String, Object> createAccessToken(String UserName) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            ListAccessKeysRequest listAccessKeysRequest = new ListAccessKeysRequest();
            List<ListAccessKeysResponseBody.ListAccessKeysResponseBodyAccessKeysAccessKey> AccessKey = this.client.listAccessKeys(listAccessKeysRequest).getBody().getAccessKeys().getAccessKey();

//            for (ListAccessKeysResponseBody.ListAccessKeysResponseBodyAccessKeysAccessKey items:AccessKey){
//                items.getAccessKeyId()
//            }


            CreateAccessKeyRequest createAccessKeyRequest = new CreateAccessKeyRequest()
                    .setUserName(UserName);
            CreateAccessKeyResponseBody.CreateAccessKeyResponseBodyAccessKey AccessToken = this.client.createAccessKey(createAccessKeyRequest).getBody().getAccessKey();
            results.put("accessKey", AccessToken.getAccessKeyId());
            results.put("accessKeySecret", AccessToken.getAccessKeySecret());
            log.info("[阿里云SDK][创建海豚密钥AK]成功创建, 数据: {}", results);
        } catch (Exception e) {
            log.error("[阿里云SDK][创建海豚密钥AK]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 创建组
     *
     * @param groupName
     * @param groupContent
     * @throws Exception
     */
    public void createGroup(String groupName, String groupContent) throws Exception {
        try {
            // 新建组
            CreateGroupRequest request = new CreateGroupRequest()
                    .setGroupName(groupName)
                    .setComments(groupContent);
            CreateGroupResponseBody.CreateGroupResponseBodyGroup group = this.client.createGroup(request).getBody().group;
            log.info("[阿里云SDK][创建CAM组]创建成功, 组名: {}, ID: {}", group.groupName, group.groupId);
        } catch (Exception e) {
            log.error("[阿里云SDK][创建CAM组]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 加入组
     *
     * @param userName
     * @param groupName
     * @throws Exception
     */
    public void joinGroup(String userName, String groupName) throws Exception {
        try {
            AddUserToGroupRequest addUserToGroupRequest = new AddUserToGroupRequest()
                    .setUserName(userName)
                    .setGroupName(groupName);
            client.addUserToGroup(addUserToGroupRequest);

            log.info("[阿里云SDK][加入CAM组]成功加入! 组名: {}, 用户: {}", groupName, userName);
        } catch (Exception e) {
            log.error("[阿里云SDK][加入CAM组]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取用户组列表
     * @return
     * @throws Exception
     */
    public Map<String, String> getGroupLists() throws Exception {
        Map<String, String> GroupNameList = new HashMap<>();
        try {
            ListGroupsRequest listGroupsRequest = new ListGroupsRequest()
                    .setMaxItems(1000);

            ListGroupsResponse listGroupsResponse = this.client.listGroups(listGroupsRequest);
            List<ListGroupsResponseBody.ListGroupsResponseBodyGroupsGroup> listGroupsResponseBodyGroups  = listGroupsResponse.getBody().getGroups().getGroup();
            for (ListGroupsResponseBody.ListGroupsResponseBodyGroupsGroup listGroupsResponseBodyGroupsGroup :listGroupsResponseBodyGroups){
                GroupNameList.put(listGroupsResponseBodyGroupsGroup.getGroupName(), listGroupsResponseBodyGroupsGroup.getGroupId());
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取用户组列表]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return GroupNameList;
    }

    /**
     * 获取用户组对应的策略
     * @return
     * @throws Exception
     */
    public ArrayList<String> getGroupPolicyLists(String GroupName) throws Exception {
        ArrayList<String> GroupNamePolicyList = new ArrayList<>();
        try {
            ListPoliciesForGroupRequest request = new ListPoliciesForGroupRequest();
            request.setGroupName(GroupName);
            List<ListPoliciesForGroupResponseBody.ListPoliciesForGroupResponseBodyPoliciesPolicy> listPoliciesForGroupResponseBodyPoliciesPolicies = this.client.listPoliciesForGroup(request).getBody().getPolicies().getPolicy();

            for (ListPoliciesForGroupResponseBody.ListPoliciesForGroupResponseBodyPoliciesPolicy listPoliciesForGroupResponseBodyPoliciesPolicy:listPoliciesForGroupResponseBodyPoliciesPolicies) {
                GroupNamePolicyList.add(listPoliciesForGroupResponseBodyPoliciesPolicy.getPolicyName());
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取用户组策略列表]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return GroupNamePolicyList;
    }

    /**
     * 加入用户组策略
     *
     * @param policyName
     * @param groupName
     * @throws Exception
     */
    public void joinPolicys(String policyName, String groupName) throws Exception {
        try {
            AttachPolicyToGroupRequest request = new AttachPolicyToGroupRequest()
                    .setGroupName(groupName)
                    .setPolicyName(policyName)
                    .setPolicyType("System");

            this.client.attachPolicyToGroup(request);
            log.info("[阿里云SDK][策略加入到用户组]成功加入!组名: {}, 策略: {}", groupName, policyName);
        } catch (Exception e) {
            log.error("[阿里云SDK][策略加入到用户组]异常信息: {}", e.getMessage());
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
        this.userAlive = this.userCheck("aidolphins");
    }

    @Override
    public void execService() throws Exception {
        try {
            // 先检查是否有用户，有重复的就新建

            // 如果么有用户


            if (!this.userAlive) {
                // 没有用户就创建
                this.createUser("aidolphins", "海豚工程Devops", "auto@aidolphins.com", "海豚工程的devops");
            }
            if (!this.getGroupLists().containsKey("海豚工程RAM组")){
                // 创建用户组，用于授权做事
                this.createGroup("海豚工程RAM组", "海豚工程专用");
                // 加入用户组
                this.joinGroup("aidolphins", "海豚工程RAM组");
            }

            ArrayList<String> PolicyList = this.getGroupPolicyLists("海豚工程RAM组");
            // 赋予策略授权
            for (String policyItems : policyLists) {
                if (!PolicyList.contains(policyItems)){
                    this.joinPolicys(policyItems, "海豚工程RAM组");
                }
            }

            // 创建授权的token后续用
            results = this.createAccessToken("aidolphins");
        } catch (Exception e) {
            log.error("[阿里云SDK][RAM服务]执行异常: {}", e.getMessage());
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

        if (!redisCommonUtils.hasKeys("RamServ." + cid)){
            redisCommonUtils.noExpireSset("RamServ." + cid, 1);
            try {
                clusterService.UpdateStage(1, cid, 1);
                this.initService();
                this.execService();
                clusterService.UpdateStage(1, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(1, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("RamServ." + cid);
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

    public static void main(String[] args) {
        try {
            RamServ ramServ = new RamServ("LTAI5tDJjyU1Wros3QW3FqXv", "UscaVFytolIIiZvfyK1qkoqo7oWiC0");
            System.out.println(ramServ.userCheck("dolphins-992"));
            ramServ.createUser("dolphins-992", "海豚工程Devops", "auto@aidolphins.com", "海豚工程的devops");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
