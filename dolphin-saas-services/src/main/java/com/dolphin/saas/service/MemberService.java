package com.dolphin.saas.service;

import com.dolphin.saas.entity.User;

import java.util.ArrayList;
import java.util.Map;

public interface MemberService {
    // 获取用户信息（高频接口）
    Map<String, Object> GetUserInfo(String uuid) throws Exception;

    // 邮箱、账号，密码登陆
    Map<String, Object> LoginInAccount(User user) throws Exception;

    // OPENAPI - 登录 - 手机号、密码 -- PaaS端用
    Map<String, Object> LoginInAccountOpenAPI(User user) throws Exception;

    // 注册接口
    User RegisterUser(String passWord, Long phone, Long code) throws Exception;
    // 实名认证
//    Map<String, Object> UserVerified(String uuid);

    // 获取用户列表 -- 前台API
    Map<String, Object> FindUserLists(int Pages, int Size, String uuid) throws Exception;

    // 新手进度检查 -- 前台API
    Map<String, Object> newComerTask(String uuid) throws Exception;

    // 是否完善信息判断
    Map<String, Object> PerfectInfo(String uuid);

    // 个人用户-引导信息提交
    Map<String, Object> GuideInfoUsers(String uuid, String commonName);

    // 个人用户-引导信息提交(带商户id)
    Map<String, Object> GuideInfoUsers(String uuid, String commonName, Long merchantId);

    // 企业用户-引导信息提交
    void GuideInfoCompany(String uuid, String companyName, Integer companyType, Long merchantId, String commonName) throws Exception;

    // 商户-信息详情
    Map<String, Object> MerchantInfo(String uuid) throws Exception;

    // 获取用户数据
    Map<String, Object> UserOptions(String uuid) throws Exception;

    // 获取用户列表数据 -- 后台API
    Map<String, Object> FindUsersLists(Integer page, Integer size) throws Exception;

    // 获取当前用户版本接口 -- 前台API
    Map<String, Object> FindUserVersion(String uuid) throws Exception;

    // 修改用户名接口API -- 前台API
    void ChangeUserNameInfo(String uuid, String userName) throws Exception;

    // 根据UUID获取用户手机号 -- 前台API
    String GetMemberPhone(String uuid) throws Exception;

    // 获取用户信息接口API -- 前台API
    Map<String, Object> GetUserInfoDetial(String uuid) throws Exception;

    // 修改用户信息接口API -- 前台API
    void ChangeUserInfoDetial(String uuid, Integer types, String content) throws Exception;

    // 判断用户是否加入组织,是否管理员 -- 前台API
    Map<String, Object> CheckUserInMerchant(String uuid) throws Exception;

    // 上传头像接口 -- 前台API
    void SaveAvatarImg(String uuid, String filePath) throws Exception;

    // 企业信息获取接口 -- 前台API
    Map<String, String> GetMerchantInfo(String uuid) throws Exception;

    // 提交企业认证信息接口 -- 前台API
    Map<String, String> PushMerchantAuth(String MerchantName, String creditCode, String officeAddr, String LicenseKey) throws Exception;

}
