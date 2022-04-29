package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.entity.WxPayKv;
import com.dolphin.saas.commons.clouds.tencent.feature.PayServ;
import com.dolphin.saas.commons.clouds.tencent.feature.SendSmsServ;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.Orders;
import com.dolphin.saas.inputs.ChangeUserInfoInputs;
import com.dolphin.saas.service.MemberService;
import com.dolphin.saas.service.MerchantService;
import com.dolphin.saas.service.OrderService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.HttpProfile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/users")
@Api(tags = "用户相关接口", description = "7个")
public class MemberController extends MasterCommon {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private MemberService memberService;

    @Resource
    private OrderService orderService;

    @Resource
    private MerchantService merchantService;

    @ApiOperation("修改用户名接口")
    @RequestMapping(value = "/changeUserNameInfo", method = RequestMethod.POST)
    public Map<String, Object> ChangeUserNameInfo(@RequestHeader Map<String, String> headers, String userName) {
        try {
            memberService.ChangeUserNameInfo(redisUtils.getUUID(headers.get("token")), userName);
            return JsonResponseStr(1, "ok", "修改用户名成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "读取失败!");
        }
    }

    @ApiOperation("获取用户信息接口")
    @RequestMapping(value = "/getUserInfo", method = RequestMethod.POST)
    public Map<String, Object> GetUserInfo(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1, memberService.GetUserInfo(redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "读取失败!");
        }
    }

    @ApiOperation("获取用户版本接口")
    @RequestMapping(value = "/getUserVersion", method = RequestMethod.POST)
    public Map<String, Object> GetUserVersion(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1,
                    memberService.FindUserVersion(
                            redisUtils.getUUID(headers.get("token"))), "获取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    @ApiOperation("获取用户订单接口")
    @RequestMapping(value = "/getUserOrders", method = RequestMethod.POST)
    public Map<String, Object> GetUserOrders(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1,
                    orderService.FindUserOrdersLists(page, pageSize, redisUtils.getUUID(headers.get("token"))),
                    "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), e.getMessage());
        }
    }

    @ApiOperation("获取用户订单单个信息接口")
    @RequestMapping(value = "/getUserOrderInfo", method = RequestMethod.POST)
    public Map<String, Object> GetUserOrderInfo(@RequestHeader Map<String, String> headers, Integer id) {
        try {
            return JsonResponseMap(1, orderService.FindUserOrdersInfo(id, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    @ApiOperation("升级用户等级")
    @RequestMapping(value = "/upgradeMemberShip", method = RequestMethod.POST)
    public Map<String, Object> UpgradeMemberShip(@RequestHeader Map<String, String> headers, Integer sourceId, Integer upType, Integer month) {
        try {
            // 获取当前用户的版本
            Map<String, Object> userVersion = memberService.FindUserVersion(redisUtils.getUUID(headers.get("token")));

            Integer day = null;
            if (!userVersion.get("userDeadLine").toString().equals("无限期")) {
                Long endTime = this.getTime(userVersion.get("userDeadLine").toString());
                Long startTime = this.getTime(new Date().toString());
                Long thisTime = endTime - startTime;
                day = Math.round(thisTime / 1000 / 60 / 60 / 24);
            }

            Float money = 0F;
            if (month < 1 || month > 12) {
                throw new Exception("不能设置超过范围的时间!");
            }
            if (upType < 1 || upType > 2) {
                throw new Exception("不能选择超过范围的套餐!");
            }
            Float deductMoney = 0F;
            Float dayMoney;
            switch (upType) {
                case 1:
                    money = Float.parseFloat("25.00") * month;
                    if (day != null) {
                        // 算出一天多少钱
                        dayMoney = Float.parseFloat("25.00") / Float.parseFloat("30.00");
                        // 算出总共扣多少钱
                        money = money - (day * dayMoney);
                        deductMoney = day * dayMoney;
                    }
                    break;

                case 2:
                    money = Float.parseFloat("150.00") * month;
                    if (day != null) {
                        // 算出一天多少钱
                        dayMoney = Float.parseFloat("150.00") / Float.parseFloat("30.00");
                        // 算出总共扣多少钱
                        money = money - (day * dayMoney);
                        deductMoney = day * dayMoney;
                    }
                    break;
            }
            money = new BigDecimal(money).setScale(2, RoundingMode.HALF_UP).floatValue();
            deductMoney = new BigDecimal(deductMoney).setScale(2, RoundingMode.HALF_UP).floatValue();
            String codeUrl = null;
            Orders orders = orderService.CreateOrder(null, sourceId, redisUtils.getUUID(headers.get("token")), money, month + "个月", 4);
            // 生成二维码返回
            if (sourceId == 0) {
                PayServ payServ = new PayServ();
                WxPayKv wxPayKv = new WxPayKv();
                wxPayKv.setDescription("元豚科技-续费升级-" + month + "个月");
                wxPayKv.setMoney(money);
                wxPayKv.setOutTradeNo(orders.getOrderId());
                // 回调地址
                wxPayKv.setCallBackUrl("http://backend.api.aidolphins.com/callback/index");
                codeUrl = payServ.wxPay(wxPayKv);
                // 更新回调的二维码地址
                orderService.UpdateOrderCode(orders.getOrderId(), codeUrl);
            }

            Map<String, Object> response = new HashMap<>();
            // 返回结果
            response.put("id", orders.getId());
            response.put("orderId", orders.getOrderId());
            response.put("codeUrl", codeUrl);
            response.put("deductMoney", deductMoney);
            response.put("money", money);

            return JsonResponseMap(1, response, "创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "读取失败!");
        }
    }

    @ApiOperation("是否完善信息--0:未完善,1:已完善")
    @RequestMapping(value = "/perfectInfo", method = RequestMethod.POST)
    public Map<String, Object> PerfectInfo(@RequestHeader Map<String, String> headers) {
        try {
            String uuid = redisUtils.getUUID(headers.get("token"));
            Map<String, Object> results = memberService.PerfectInfo(uuid);
            if (!results.containsKey("error")) {
                return JsonResponseStr(1, results.get("data").toString(), "读取成功!");
            } else {
                return JsonResponseStr(-1, "failed", results.get("error").toString());
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error" + e, "读取失败!");
        }
    }

    @ApiOperation("个人用户-引导信息提交")
    @RequestMapping(value = "/guideInfoUsers", method = RequestMethod.POST)
    public Map<String, Object> GuideInfoUsers(@RequestHeader Map<String, String> headers, String commonName) {
        try {
            if (commonName == null) {
                return JsonResponseStr(-2, "failed", "参数缺失!");
            }
            String uuid = redisUtils.getUUID(headers.get("token"));
            Map<String, Object> results = memberService.GuideInfoUsers(uuid, commonName);
            if (!results.containsKey("error")) {
                return JsonResponseStr(1, "ok", "完善信息成功!");
            } else {
                return JsonResponseStr(-1, "failed", results.get("error").toString());
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error" + e, "提交失败!");
        }
    }

    @ApiOperation("企业用户-引导信息提交")
    @RequestMapping(value = "/guideInfoCompany", method = RequestMethod.POST)
    public Map<String, Object> GuideInfoCompany(@RequestHeader Map<String, String> headers, String companyName, Integer companyType, Long merchantId, String commonName) {
        try {
            memberService.GuideInfoCompany(redisUtils.getUUID(headers.get("token")), companyName, companyType, merchantId, commonName);
            return JsonResponseStr(1, "ok", "完善信息成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 商户-用户列表
     *
     * @param page 页码
     * @return
     */
    @ApiOperation("商户-用户列表")
    @RequestMapping(value = "/userLists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1, memberService.FindUserLists(page, pageSize, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "查询失败!");
        }
    }

    /**
     * 商户-信息详情
     *
     * @return
     */
    @ApiOperation("商户-信息详情")
    @RequestMapping(value = "/merchantInfo", method = RequestMethod.POST)
    public Map<String, Object> MerchantInfo(@RequestHeader Map<String, String> headers) {
        try {
            Map<String, Object> results = memberService.MerchantInfo(redisUtils.getUUID(headers.get("token")));
            return JsonResponseObj(1, results, "商户-信息读取详情成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "商户-信息读取详情失败:" + e.getMessage());
        }
    }

    /**
     * 获取可用的组织用户下拉
     *
     * @return
     */
    @ApiOperation("获取可用的组织用户下拉")
    @RequestMapping(value = "/userOptions", method = RequestMethod.POST)
    public Map<String, Object> UserOptions(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1, memberService.UserOptions(redisUtils.getUUID(headers.get("token"))), "获取用户数据成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 操作组织成员状态
     *
     * @return
     */
    @ApiOperation("操作组织成员状态")
    @RequestMapping(value = "/userOrgStatus", method = RequestMethod.POST)
    public Map<String, Object> UserOrgStatus(@RequestHeader Map<String, String> headers, Long merchantId, Integer status, String changeUUid) {
        try {
            merchantService.Operate(redisUtils.getUUID(headers.get("token")), merchantId, status, changeUUid);
            return JsonResponseStr(1, "ok", "修改成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 上传图片
     *
     * @return
     */
    @ApiOperation("上传图片")
    @RequestMapping(value = "/uploadimg", method = RequestMethod.POST)
    @ApiImplicitParam(name = "file", value = "文件", dataType = "MultipartFile")
    public Map<String, Object> UploadImgs(@RequestHeader Map<String, String> headers, @RequestPart("file") MultipartFile file) {
        try {
            Map<String, Object> results = new HashMap<>();
            String uuid = redisUtils.getUUID(headers.get("token"));

            COSCredentials cred = new BasicCOSCredentials(this.getTX_SECRETLD(), this.getTX_SECRETKEY());
            Region region = new Region("ap-beijing");
            ClientConfig clientConfig = new ClientConfig(region);
            COSClient cosClient = new COSClient(cred, clientConfig);
            String originFileName;
            InputStream inputStream;
            String newName;

            try {
                originFileName = file.getOriginalFilename();
                inputStream = file.getInputStream();

                // 判断后缀名
                boolean lastCheck = false;
                List<String> lastHead = Arrays.asList("png", "jpeg", "jpg");
                for (String item : lastHead) {
                    if (originFileName.endsWith(item)) {
                        lastCheck = true;
                        break;
                    }
                }
                if (!lastCheck) {
                    throw new Exception("上传文件格式不对，请重新上传! 文件名: " + originFileName);
                }

                // 新随机文件名
                newName = UUID.randomUUID() + originFileName.substring(originFileName.lastIndexOf("."));

                // 上传文件
                cosClient.putObject(new PutObjectRequest("pic-1255049862", uuid + "/" + newName, inputStream, null));
                cosClient.setBucketAcl("pic-1255049862", CannedAccessControlList.PublicRead);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            } finally {
                cosClient.shutdown();
            }

            // 输出地址
            String imagePath = uuid + "/" + newName;

            // 保存上传地址
            memberService.SaveAvatarImg(uuid, imagePath);
            results.put("image", imagePath);

            return JsonResponseMap(1, results, "上传成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 综合修改信息接口
     *
     * @param headers
     * @param changeUserInfoInputs
     * @return
     */
    @ApiOperation("综合修改信息接口")
    @RequestMapping(value = "/changeCodes", method = RequestMethod.POST)
    public Map<String, Object> changeCodes(@RequestHeader Map<String, String> headers, ChangeUserInfoInputs changeUserInfoInputs) {
        try {
            Random random = new Random();
            int SmsCode = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;

            // 通过UUID找到手机号
            String phoneNumber = memberService.GetMemberPhone(redisUtils.getUUID(headers.get("token")));

            String haskKey = null;
            switch (changeUserInfoInputs.getType()) {
                case 1:
                    // 手机号修改(验证旧的)
                    haskKey = "changePhone." + phoneNumber;
                    break;

                case 2:
                    // 邮箱修改
                    haskKey = "changeEmail." + phoneNumber;
                    break;

                case 3:
                    // 手机号修改(验证旧的，校验验证码)
                    if (changeUserInfoInputs.getCode() == null) {
                        throw new Exception("验证码必填!");
                    }
                    haskKey = "changePhone." + phoneNumber;
                    if (!redisUtils.hasKeys(haskKey)) {
                        throw new Exception("旧的手机号短信验证已失效，请重新发送！");
                    }
                    if (changeUserInfoInputs.getNewPhone() == null) {
                        throw new Exception("需要输入新的手机号!");
                    }
                    // 手机号修改（验证新的）
                    haskKey = "changeNewPhone." + changeUserInfoInputs.getNewPhone();
                    phoneNumber = changeUserInfoInputs.getNewPhone();
                    break;

                case 5:
                    // 手机号修改(验证新的，校验验证码)
                    if (changeUserInfoInputs.getNewPhone() == null || changeUserInfoInputs.getCode() == null) {
                        throw new Exception("新的手机号和验证码必填!");
                    }
                    haskKey = "changeNewPhone." + changeUserInfoInputs.getNewPhone();
                    if (!redisUtils.hasKeys(haskKey)) {
                        throw new Exception("短信验证已失效，请重新发送!");
                    }
                    if (!redisUtils.get(haskKey).equals(changeUserInfoInputs.getCode())) {
                        throw new Exception("短信验证码不对!");
                    }
                    memberService.ChangeUserInfoDetial(redisUtils.getUUID(headers.get("token")), 2, changeUserInfoInputs.getNewPhone());
                    // 删除历史的KEY
                    redisUtils.deleteKey(haskKey);
                    return JsonResponseStr(1, "ok", "修改手机号成功!");

                case 6:
                    // 邮箱修改的短信验证
                    if (changeUserInfoInputs.getEmail() == null || changeUserInfoInputs.getCode() == null) {
                        throw new Exception("新的邮箱和验证码必填!");
                    }
                    haskKey = "changeEmail." + phoneNumber;
                    if (!redisUtils.hasKeys(haskKey)) {
                        throw new Exception("短信验证已失效，请重新发送!");
                    }
                    if (!redisUtils.get(haskKey).equals(changeUserInfoInputs.getCode())) {
                        throw new Exception("短信验证码不对!");
                    }
                    memberService.ChangeUserInfoDetial(redisUtils.getUUID(headers.get("token")), 3, changeUserInfoInputs.getEmail());
                    // 删除历史的KEY
                    redisUtils.deleteKey(haskKey);
                    return JsonResponseStr(1, "ok", "修改邮箱成功!");

                case 7:
                    // 修改昵称
                    if (changeUserInfoInputs.getUname() == null) {
                        throw new Exception("新的昵称必填!");
                    }
                    memberService.ChangeUserInfoDetial(redisUtils.getUUID(headers.get("token")), 1, changeUserInfoInputs.getUname().replaceAll("[\\pP|~|$|^|<|>|\\||\\+|=]*", ""));
                    return JsonResponseStr(1, "ok", "修改昵称成功!");

                default:
                    throw new Exception("Type类型需要指定!");
            }

            if (redisUtils.hasKeys(haskKey)) {
                throw new Exception("请勿高频发送，60秒之后再试!");
            }

            // 发送短信逻辑
            Credential cred = new Credential(this.getTX_SECRETLD(), this.getTX_SECRETKEY());
            HttpProfile httpProfile = new HttpProfile();

            SendSmsServ sendSmsServ = new SendSmsServ(cred, httpProfile);
            sendSmsServ.SendCode(phoneNumber, String.valueOf(SmsCode));
            redisUtils.setMinute(haskKey, String.valueOf(SmsCode));
            return JsonResponseStr(1, "ok", "短信发送成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "failed", e.getMessage());
        }
    }

    /**
     * 获取用户信息接口
     *
     * @return
     */
    @ApiOperation("获取用户信息接口")
    @RequestMapping(value = "/getUserInfoDetial", method = RequestMethod.POST)
    public Map<String, Object> GetUserInfoDetial(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1, memberService.GetUserInfoDetial(redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 判断用户是否加入组织是否管理员
     *
     * @return
     */
    @ApiOperation("判断用户是否加入组织是否管理员")
    @RequestMapping(value = "/checkUserInMerchant", method = RequestMethod.POST)
    public Map<String, Object> CheckUserInMerchant(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1, memberService.CheckUserInMerchant(redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }


}
