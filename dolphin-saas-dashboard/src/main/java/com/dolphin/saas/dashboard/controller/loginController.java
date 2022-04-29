package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.JwtUtil;
import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.feature.SendSmsServ;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.User;
import com.dolphin.saas.service.MemberService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.HttpProfile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/login")
@Api(tags = "用户登陆相关的接口", description = "1个")
public class loginController extends MasterCommon {

    @Resource
    private MemberService memberService;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 发送短信验证码接口
     *
     * @return
     */
    @ApiOperation("发送短信验证码接口")
    @RequestMapping(value = "/codes", method = RequestMethod.POST)
    public Map<String, Object> SendCodes(String phoneNumber) {
        try {
            Random random = new Random();
            int SmsCode = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;

            Credential cred = new Credential(this.getTX_SECRETLD(), this.getTX_SECRETKEY());
            HttpProfile httpProfile = new HttpProfile();

            SendSmsServ sendSmsServ = new SendSmsServ(cred, httpProfile);
            sendSmsServ.SendCode(phoneNumber, String.valueOf(SmsCode));

            if (redisUtils.hasKeys(phoneNumber)) {
                throw new Exception("请勿高频发送，60秒之后再试!");
            }

            redisUtils.setMinute(phoneNumber, String.valueOf(SmsCode));
            return JsonResponseStr(1, "ok", "短信发送成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "failed", e.getMessage());
        }
    }

    /**
     * 判断有没有发布规则策略组
     *
     * @return
     */
    @ApiOperation("登陆接口")
    @RequestMapping(value = "/in", method = RequestMethod.POST)
    public Map<String, Object> Judgment(String passWord, String phone, String code) {
        User user = new User();

        if (passWord != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(passWord.getBytes()));
        }

        if (phone != null) {
            user.setPhone(Long.valueOf(phone));
        }

        try {
            if (user.getPhone() == null) {
                throw new Exception("手机号不能为空!");
            }

            if (user.getPassword() == null && code == null) {
                throw new Exception("验证码和密码必须得有一个!");
            }

            // 验证码登录
            if (code != null) {
                if (!redisUtils.hasKeys(phone)) {
                    throw new Exception("没有发送验证码!");
                }
                if (!redisUtils.get(phone).equals(code)){
                    throw new Exception("验证码错误!");
                }
            }

            Map<String, Object> results = memberService.LoginInAccount(user);
            Map<String, String> map = new HashMap<>();
            User userResult = (User) results.get("users");
            Map<String, Object> map2 = (Map<String, Object>) results.get("orgs");
            map.put("uuid", userResult.getUuid());
            map.put("userName", userResult.getCommonName());
            map.put("headerImg", userResult.getHeaderImg());
            map.put("merchantId", String.valueOf(userResult.getMerchantId()));
            map.put("userType", map2.get("uidentity").toString());
            if (map2.get("uidentity").toString().equals("enterprise")) {
                map.put("uidentityConfirm", map2.get("confirm").toString());
            }
            String token = JwtUtil.generateToken(map);

            if (!redisUtils.set(token, map)) {
                throw new Exception("登陆失败!");
            }

            return JsonResponseStr(1, token, "登陆成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    @ApiOperation("注册接口")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Map<String, Object> Register(String passWord, Long phone, Long code) {
        if (passWord == null || phone == null || code == null) {
            return JsonResponseStr(-1, "failed", "基础信息有缺失!");
        }

        try {
            // 手机验证码没有发送
            if (!redisUtils.hasKeys(phone.toString())) {
                return JsonResponseStr(-2, "failed", "手机号验证码失效，请重新发送验证码!");
            }

            // 验证码验证
            if (!redisUtils.get(phone.toString()).equals(code.toString())) {
                return JsonResponseStr(-3, "failed", "手机号验证码不对，请重新输入!");
            }

            User registerUser = memberService.RegisterUser(passWord, phone, code);

            Map<String, String> map = new HashMap<>();
            map.put("uuid", registerUser.getUuid());
            map.put("userName", registerUser.getCommonName());
            map.put("headerImg", registerUser.getHeaderImg());
            map.put("merchantId", String.valueOf(registerUser.getMerchantId()));

            String token = JwtUtil.generateToken(map);

            if (redisUtils.set(token, map)) {
                return JsonResponseStr(1, token, "注册并登陆成功!");
            } else {
                return JsonResponseStr(-3, "failed", "登陆失败!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "注册失败:" + e.getMessage());
        }
    }
}
