package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.MemberList;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("memberService")
public class MemberServiceimpl extends BaseTools implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private UserLoginLogsMapper userLoginLogsMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private VendorsMapper vendorsMapper;

    @Resource
    private EngineeringMapper engineeringMapper;

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private MerchantOrgMapper merchantOrgMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private MerchantDetailsMapper merchantDetailsMapper;

    @Resource
    private LicenseMapper licenseMapper;

    @Override
    public Map<String, Object> GetUserInfo(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            User user = memberMapper.selectOne(queryWrapper);
            if (user == null){
                throw new Exception("没有获取到用户信息!");
            }

            results.put("uuid", user.getUuid());
            results.put("userName", user.getCommonName());
            results.put("merchantId", user.getMerchantId());
            results.put("data", user.getPerfectInfo());

            // 企业名称
            if (user.getMerchantId() > 0){
                QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("merchant_id", user.getMerchantId());
                Merchant merchant = merchantMapper.selectOne(queryWrapper1);
                results.put("bus", merchant.getMerchantName());
            }else{
                results.put("bus", null);
            }

            // 完善信息的状态
            QueryWrapper<MerchantDetails> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("uuid", user.getUuid());
            if (merchantDetailsMapper.selectCount(queryWrapper2) > 0){
                results.put("authing", 1);
            }else{
                results.put("authing", 0);
            }

            results.put("headerImg", user.getHeaderImg());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> LoginInAccount(User user) throws Exception {
        Map<String, Object> results = new HashMap<>();
        // 获取邮箱、账号、密码
        String password = user.getPassword();
        Long phone = user.getPhone();

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            // 如果是邮箱，则进入邮箱查询逻辑
            if (phone != null) {
                queryWrapper.eq("u_phone", phone);
            }
            if (password != null) {
                queryWrapper.eq("u_login_password", password);
            }

            User userResults = memberMapper.selectOne(queryWrapper);
            if (userResults == null) {
                throw new Exception("登陆失败, 账号/手机号和密码不匹配!");
            }

            // 更新最后登陆时间
            User userItems = new User();
            userItems.setUpdateTime(new Date());
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", userResults.getUuid());

            if (memberMapper.update(userItems, queryWrapper1) < 1) {
                throw new Exception("创建登陆记录失败!");
            }

            UsersLoginLogs usersLoginLogs = new UsersLoginLogs();
            usersLoginLogs.setId(userResults.getId());
            usersLoginLogs.setCreatetime(new Date());

            // 插入登陆成功的记录
            if (userLoginLogsMapper.insert(usersLoginLogs) < 1) {
                throw new Exception("更新登陆记录失败!");
            }

            // 获取获取当前是否在组织里，并且通过审核
            results.put("orgs", this.orgIdentity(userResults.getUuid()));
            results.put("users", userResults);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Object> LoginInAccountOpenAPI(User user) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("u_phone", user.getPhone());
            queryWrapper.eq("u_login_password", user.getPassword());

            User users = memberMapper.selectOne(queryWrapper);
            if (users == null){
                throw new Exception("登录失败，账号密码不匹配!");
            }

            // 更新最后登陆时间
            User userItems = new User();
            userItems.setUpdateTime(new Date());
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", users.getUuid());

            if (memberMapper.update(userItems, queryWrapper1) < 1) {
                throw new Exception("创建登陆记录失败!");
            }

            // 判断是否有License存活，返回对应最高的版本
            QueryWrapper<License> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("uuid", user.getUuid());
            queryWrapper2.eq("license_status", 1);

            int version = 0;
            String accessKey = "";
            String accessSecret = "";

            List<License> licenseList = licenseMapper.selectList(queryWrapper2);
            if (licenseList.size() == 0){
                throw new Exception("PaaS需要先创建License才能使用哦!");
            }
            for (License license: licenseList){
                if (license.getLicenseVersion() > version) {
                    version = license.getLicenseVersion();
                    accessKey = license.getLicenseAccessKey();
                    accessSecret = license.getLicenseAccessSecret();
                }
            }
            results.put("users",users);
            results.put("license", version);
            results.put("accessKey", accessKey);
            results.put("accessSecret", accessSecret);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Object> MerchantInfo(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.select("merchant_id");
            User user = memberMapper.selectOne(queryWrapper);
            if (user == null) {
                throw new Exception("获取商户ID失败!");
            }
            QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("merchant_id", user.getMerchantId());
            queryWrapper1.select("merchant_name", "merchant_type", "merchant_attributes", "merchant_createtime");
            Merchant merchant = merchantMapper.selectOne(queryWrapper1);
            if (merchant == null) {
                throw new Exception("获取商户信息失败!");
            }
            // 获取商户的用户数
            QueryWrapper<MerchantOrganization> merchantOrganizationQueryWrapper = new QueryWrapper<>();
            merchantOrganizationQueryWrapper.eq("merchant_id", user.getMerchantId());
            merchantOrganizationQueryWrapper.eq("user_delete", 0);
            results.put("memberTotal", merchantOrgMapper.selectCount(merchantOrganizationQueryWrapper));
            results.put("merchantId", user.getMerchantId());
            results.put("createTime", merchant.getMerchantCreatetime());
            results.put("merchantAttr", merchant.getMerchantAttributes());
            results.put("merchantType", merchant.getMerchantType());
            results.put("merchantName", merchant.getMerchantName());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Object> UserOptions(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        // 先获取UUID对应的用户类型
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        User user = memberMapper.selectOne(queryWrapper);
        if (user.getUidentity() == 0) {
            results.put("type", "user");
        } else {
            results.put("type", "enterprise");
            // 根据商户ID查是否已经审核通过
            QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("merchant_id", user.getMerchantId());
            Merchant merchant = merchantMapper.selectOne(queryWrapper1);
            // 有效商户才会去查是否有审核通过的用户
            if (merchant.getMerchantStatus() == 3) {
                QueryWrapper<MerchantOrganization> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("uuid", uuid);
                queryWrapper2.eq("merchant_id", user.getMerchantId());
                queryWrapper2.eq("status", 1);
                queryWrapper2.eq("user_delete", 0);
                if (merchantOrgMapper.selectCount(queryWrapper2) > 0) {
                    // 查组织下有哪些用户可以使用
                    QueryWrapper<MerchantOrganization> queryWrapper3 = new QueryWrapper<>();
                    queryWrapper3.eq("merchant_id", user.getMerchantId());
                    queryWrapper3.eq("status", 1);
                    queryWrapper3.eq("user_delete", 0);
                    List<MerchantOrganization> organizationList = merchantOrgMapper.selectList(queryWrapper3);

                    if (organizationList.size() > 0) {
                        ArrayList<Map<String, Object>> orgUsers = new ArrayList<>();
                        for (MerchantOrganization merchantOrganization : organizationList) {
                            Map<String, Object> items = new HashMap<>();
                            items.put("value", merchantOrganization.getUuid());
                            items.put("label", this.getUUidUname(merchantOrganization.getUuid()));
                            orgUsers.add(items);
                        }
                        results.put("orgUsers", orgUsers);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public Map<String, Object> FindUsersLists(Integer Pages, Integer Size) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<MemberList> page = new Page<>(Pages, Size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            memberMapper.selectMembersPage(page, queryWrapper);
            response.put("page", Pages);
            response.put("total", page.getTotal());
            response.put("list", page.getRecords());
            response.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return response;
    }

    @Override
    public Map<String, Object> FindUserVersion(String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            User user = memberMapper.selectOne(queryWrapper);
            String userVer = "";
            String userFree = "";
            switch (user.getUserServiceType()) {
                case 0:
                    userVer = "免费版";
                    userFree = "无限期";
                    break;

                case 1:
                    userVer = "基础版";
                    userFree = user.getUserServiceTimeline().toString();
                    break;

                case 2:
                    userVer = "高级版";
                    userFree = user.getUserServiceTimeline().toString();
                    break;
            }
            response.put("userServiceType", userVer);
            response.put("userDeadLine", userFree);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public void ChangeUserNameInfo(String uuid, String userName) throws Exception {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);

            User user = new User();
            user.setCommonName(userName);
            user.setUserName(userName);
            if (memberMapper.update(user, queryWrapper) < 1) {
                throw new Exception("更新用户名失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String GetMemberPhone(String uuid) throws Exception {
        String phoneNumber = null;
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            User user = memberMapper.selectOne(queryWrapper);
            if (user == null){
                throw new Exception("获取用户手机号失败!");
            }
            phoneNumber = user.getPhone().toString();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return phoneNumber;
    }

    @Override
    public Map<String, Object> GetUserInfoDetial(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            User user = memberMapper.selectOne(queryWrapper);
            String phone = user.getPhone().toString();
            String email = user.getEmail();
            results.put("uname", user.getCommonName());
            results.put("phone", phone.replaceAll("(^\\d{3})\\d.*(\\d{4})", "$1****$2"));
            if (email == null || email.equals("")){
                results.put("email", user.getEmail());
            }else{
                results.put("email", email.replaceAll("(^\\w)[^@]*(@.*$)", "$1****$2"));
            }
            results.put("headerImg", user.getHeaderImg());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public void ChangeUserInfoDetial(String uuid, Integer types, String content) throws Exception {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);

            User user = memberMapper.selectOne(queryWrapper);
            if (user == null){
                throw new Exception("该用户不存在，无法修改!");
            }
            User userChange = new User();

            switch (types) {
                case 1:
                    // 昵称
                    userChange.setCommonName(content);
                    break;

                case 2:
                    // 手机号
                    userChange.setPhone(Long.valueOf(content));
                    break;

                case 3:
                    // 邮箱
                    userChange.setEmail(content);
                    break;
            }
            if (memberMapper.update(userChange, queryWrapper) < 1) {
                throw new Exception("修改用户信息失败，内部服务错误，请联系客服!");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> CheckUserInMerchant(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("u_identity", 1);
            queryWrapper.eq("uuid", uuid);
            if (memberMapper.selectCount(queryWrapper) > 0){
                // 证明是企业
                results.put("identity", "company");
                // 根据企业的ID找到对应的组织
                User user = memberMapper.selectOne(queryWrapper);

                QueryWrapper<MerchantOrganization> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("uuid", uuid);
                queryWrapper1.eq("merchant_id", user.getMerchantId());
                queryWrapper1.eq("status", 1);
                queryWrapper1.eq("user_delete", 0);
                queryWrapper1.eq("user_type", 1);

                if (merchantOrgMapper.selectCount(queryWrapper1) < 1){
                    results.put("admin", 0);
                }else{
                    results.put("admin",1);
                }
            }else{
                results.put("admin", 0);
                results.put("identity", "user");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public void SaveAvatarImg(String uuid, String filePath) throws Exception {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);

            User user = new User();
            user.setHeaderImg(filePath);
            if (memberMapper.update(user, queryWrapper) < 1) {
                throw new Exception("更新头像失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, String> GetMerchantInfo(String uuid) throws Exception {
        return null;
    }

    @Override
    public Map<String, String> PushMerchantAuth(String MerchantName, String creditCode, String officeAddr, String LicenseKey) throws Exception {
        return null;
    }

    /**
     * 根据UUID获取用户名
     *
     * @param uuid
     * @return
     */
    protected String getUUidUname(String uuid) {
        String UserName = "";
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        queryWrapper.select("u_name", "u_login_username");
        User user = memberMapper.selectOne(queryWrapper);
        if (user != null) {
            UserName = user.getCommonName();
            if (UserName == null) {
                UserName = user.getUserName();
            }
        }
        return UserName;
    }

    @Override
    public Map<String, Object> FindUserLists(int Pages, int Size, String uuid) throws Exception {
        // 拼装分页数据
        Map<String, Object> response = new HashMap<>();

        try {
            // 根据uuid找对应的商户id
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("uuid", uuid);
            userQueryWrapper.select("merchant_id");
            User user = memberMapper.selectOne(userQueryWrapper);
            if (user == null) {
                throw new Exception("商户id不存在!");
            }

            // 根据商户id找到对应的用户列表
            // 获取分页的数据
            IPage<MerchantOrganization> page = new Page<>(Pages, Size);
            QueryWrapper<MerchantOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("merchant_id", user.getMerchantId());
            queryWrapper.eq("user_delete", 0);
            merchantOrgMapper.selectPage(page, queryWrapper);
            // 获取原始数据
            List<MerchantOrganization> merchantOrganizationList = page.getRecords();
            ArrayList results = new ArrayList();
            if (merchantOrganizationList.size() > 0) {
                for (MerchantOrganization merchantOrganization : merchantOrganizationList) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("uuid", merchantOrganization.getUuid());
                    QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("uuid", merchantOrganization.getUuid());
                    User user1 = memberMapper.selectOne(queryWrapper1);
                    // 查总数
                    QueryWrapper<UsersLoginLogs> usersLoginLogsQueryWrapper = new QueryWrapper<>();
                    usersLoginLogsQueryWrapper.eq("id", user1.getId());
                    items.put("loginInCount", userLoginLogsMapper.selectCount(usersLoginLogsQueryWrapper));
                    items.put("commonName", user1.getCommonName());
                    items.put("userName", user1.getUserName());
                    items.put("phoneNumber", user1.getPhone());
                    items.put("email", user1.getEmail());
                    items.put("lastLoginTime", user1.getLastLoginTime());
                    items.put("JoinTime", merchantOrganization.getJoinCreatetime());
                    items.put("status", merchantOrganization.getStatus());
                    items.put("userType", merchantOrganization.getUserType());
                    results.add(items);
                }
            }
            response.put("page", Pages);
            response.put("total", page.getTotal());
            response.put("list", results);
            response.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User RegisterUser(String passWord, Long phone, Long code) throws Exception {
        try {
            // 重复注册校验
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("u_phone", phone);
            if (memberMapper.selectCount(userQueryWrapper) > 0) {
                throw new Exception("用户不能重复注册");
            }
            User user = new User();
            user.setCreateTime(new Date());
            user.setCommonName("海豚用户");
            user.setPassword(DigestUtils.md5DigestAsHex(passWord.getBytes()));
            user.setPhone(phone);
            user.setCreateTime(new Date());
            user.setUuid(DigestUtils.md5DigestAsHex(new Date().toString().getBytes()));
            if (memberMapper.insert(user) < 1) {
                throw new Exception("用户注册失败");
            }
            return user;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> newComerTask(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();

        List<String> ComerTaskLists = Arrays.asList("vendors", "cluster", "engineer", "release");
        for (String ComerItems : ComerTaskLists) {
            results.put(ComerItems, 0);
        }

        try {
            // 判断是否有上key
            QueryWrapper<Vendors> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", uuid);
            if (vendorsMapper.selectCount(queryWrapper1) > 0) {
                results.put("vendors", 1);
                // 是否有开集群
                QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uuid", uuid);
                if (clusterMapper.selectCount(queryWrapper) > 0) {
                    results.put("cluster", 1);
                    // 判断是否有工程
                    QueryWrapper<Engineer> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("uuid", uuid);
                    if (engineeringMapper.selectCount(queryWrapper2) > 0) {
                        results.put("engineer", 1);
                        // 判断是否有发布
                        QueryWrapper<Release> queryWrapper3 = new QueryWrapper<>();
                        queryWrapper3.eq("uuid", uuid);
                        if (deployMapper.selectCount(queryWrapper3) > 0) {
                            results.put("release", 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public Map<String, Object> PerfectInfo(String uuid) {
        Map<String, Object> results = new HashMap<>();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        User user = memberMapper.selectOne(queryWrapper);
        if (user == null) {
            results.put("error", "用户信息获取失败!");
        } else {
            results.put("data", user.getPerfectInfo());
        }
        return results;
    }

    @Override
    public Map<String, Object> GuideInfoUsers(String uuid, String commonName) {
        return this.GuideInfoUsers(uuid, commonName, null);
    }

    @Override
    public Map<String, Object> GuideInfoUsers(String uuid, String commonName, Long merchantId) {
        Map<String, Object> results = new HashMap<>();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        User user = memberMapper.selectOne(queryWrapper);
        if (user != null) {
            if (user.getPerfectInfo() == 1) {
                results.put("error", "已经完善过信息了!");
            } else {
                User user1 = new User();
                user1.setCommonName(commonName);
                if (merchantId != null) {
                    user1.setMerchantId(merchantId);
                }
                user1.setPerfectInfo(1);
                user1.setUpdateTime(new Date());
                if (memberMapper.update(user1, queryWrapper) < 1) {
                    results.put("error", "更新信息失败!");
                }
            }
        } else {
            results.put("error", "用户信息无效!");
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void GuideInfoCompany(String uuid, String companyName, Integer companyType, Long merchantId, String commonName) throws Exception {
        try {
            // 判断是否已经有商户了，如果有，不能重复注册
            QueryWrapper<MerchantOrganization> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", uuid);
            if (merchantOrgMapper.selectCount(queryWrapper1) > 0) {
                throw new Exception("已经加入过商户了，不能重复加入!");
            }
            // 先判断是否有商户id，如果有，则证明用的是商户id过来的，不走查询逻辑
            // 直接把用户加入到商户，但是不能重复加入
            if (merchantId != null) {
                // 判断商户是否存在
                QueryWrapper<Merchant> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("merchant_id", merchantId);
                if (merchantMapper.selectCount(queryWrapper2) < 1) {
                    throw new Exception("商户不存在!");
                }
                // 判断用户是否已经加了组织
                QueryWrapper<MerchantOrganization> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uuid", uuid);
                queryWrapper.eq("merchant_id", merchantId);
                if (merchantOrgMapper.selectCount(queryWrapper) > 0) {
                    throw new Exception("加入组织失败，已经加入过组织了!");
                }
                // 加入组织
                MerchantOrganization merchantOrganization = new MerchantOrganization();
                merchantOrganization.setMerchantId(merchantId);
                merchantOrganization.setUuid(uuid);
                merchantOrganization.setJoinCreatetime(new Date());
                if (merchantOrgMapper.insert(merchantOrganization) < 1) {
                    throw new Exception("加入组织失败，请咨询客服!");
                }
                // 更新状态
                Map<String, Object> results2 = this.GuideInfoUsers(uuid, commonName, merchantId);
                if (results2.containsKey("error")) {
                    throw new Exception(results2.get("error").toString());
                }
                // 更新个人信息
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("uuid", uuid);
                User user = new User();
                user.setCommonName(commonName);
                user.setPerfectInfo(1);
                user.setLastLoginTime(new Date());
                if (memberMapper.update(user, userQueryWrapper) < 1) {
                    throw new Exception("更新用户信息失败,请联系客服!");
                }
            } else {
                // 创建商户
                Merchant merchant = new Merchant();
                merchant.setMerchantCreatetime(new Date());
                merchant.setMerchantName(companyName);
                merchant.setMerchantType(companyType);
                if (merchantMapper.insert(merchant) < 1) {
                    throw new Exception("创建商户失败，请联系客服!");
                }
                // 加入组织
                MerchantOrganization merchantOrganization = new MerchantOrganization();
                merchantOrganization.setMerchantId(merchant.getMerchantId());
                merchantOrganization.setUuid(uuid);
                merchantOrganization.setJoinCreatetime(new Date());
                if (merchantOrgMapper.insert(merchantOrganization) < 1) {
                    throw new Exception("加入组织失败，请咨询客服!");
                }
                // 更新个人信息
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("uuid", uuid);
                User user = new User();
                user.setCommonName(commonName);
                user.setMerchantId(merchant.getMerchantId());
                user.setPerfectInfo(1);
                user.setLastLoginTime(new Date());
                if (memberMapper.update(user, userQueryWrapper) < 1) {
                    throw new Exception("更新用户信息失败,请联系客服!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
