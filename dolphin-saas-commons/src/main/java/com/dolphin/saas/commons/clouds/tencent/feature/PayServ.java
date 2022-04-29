package com.dolphin.saas.commons.clouds.tencent.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dolphin.saas.commons.clouds.tencent.entity.WxPayKv;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PayServ {
    static String schema = "";
    static String MCH_ID = "";
    static String SERIAL_NO = "";
    static String APP_ID = "";
    static String PATH_PEM = "/wechat/apiclient_key.pem";
    static String NATIVE_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
    static String APIV3_KEY = "";

    /**
     * 生成数据
     *
     * @param orderId
     * @param money
     * @param description
     * @return
     */
    static String OrderData(String orderId, double money, String description, String callBackUrl) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mchid", MCH_ID);
        jsonObject.put("out_trade_no", orderId);
        jsonObject.put("appid", APP_ID);
        jsonObject.put("description", description);
        jsonObject.put("notify_url", callBackUrl);
        Map<String, Object> map = new HashMap<>();
        map.put("total", new Double(money * 100).intValue());
//        map.put("total", new Double(money * 100).intValue());
        map.put("currency", "CNY");
        jsonObject.put("amount", map);
        return String.valueOf(jsonObject);
    }

    /**
     * 获取签名信息所有值
     *
     * @param method 请求方法
     * @param url    URL地址
     * @param body   BOdy参数
     * @return
     */
    static String getToken(String method, HttpUrl url, String body, String orderId) throws Exception {
        String nonceStr = orderId;
        //获得系统时间，把毫秒换算成秒 /1000
        long timestamp = System.currentTimeMillis() / 1000;
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        String signature = sign(message.getBytes(StandardCharsets.UTF_8));
        return "mchid=\"" + MCH_ID + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + SERIAL_NO + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     * 拼接明文数值
     *
     * @param method    请求方法 GET or POST
     * @param url       网络请求方法地址 取除域名项
     * @param timestamp 时间戳
     * @param nonceStr  随机数
     * @param body      GET请求不需要Body参数，POST需要Body
     * @return
     */
    static String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }

    /**
     * 签名加密
     *
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws IOException
     * @throws InvalidKeyException
     */
    static String sign(byte[] message) throws NoSuchAlgorithmException, SignatureException, IOException, InvalidKeyException {
        //加密方式
        Signature sign = Signature.getInstance("SHA256withRSA");
        //私钥，通过getPrivateKey来获取，这是个方法可以接调用 ，需要的是_key.pem文件的绝对路径配上文件名
        sign.initSign(getPrivateKey(PATH_PEM));
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /**
     * 获取私钥。
     *
     * @param filename 私钥文件路径  (required)
     * @return 私钥对象
     * <p>
     * 完全不需要修改，注意此方法也是去掉了头部和尾部，注意文件路径名
     */
    public static PrivateKey getPrivateKey(String filename) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }

    /**
     * Post请求带Body参数
     *
     * @param actionUrl
     * @param params
     * @param requestString
     * @return
     * @throws IOException
     */
    public static String nativePostBody(String actionUrl, String params, String requestString)
            throws IOException {
        String serverURL = actionUrl;
        StringBuffer sbf = new StringBuffer();
        String strRead = null;
        URL url = new URL(serverURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //请求post方式
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        //header内的的参数在这里set
        connection.setRequestProperty("Content-Type", "application/json");
        //Native支付需要的参数表头参数
        connection.setRequestProperty("Authorization", requestString);
        connection.connect();
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
        //body参数放这里
        writer.write(params);
        writer.flush();
        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        while ((strRead = reader.readLine()) != null) {
            sbf.append(strRead);
            sbf.append("\r\n");
        }
        reader.close();
        connection.disconnect();
        String results = sbf.toString();
        return results;
    }


    /**
     * 微信支付生成二维码
     *
     * @param wxPayKv
     * @return
     * @throws Exception
     */
    public String wxPay(WxPayKv wxPayKv) throws Exception {
        String wxQRcode = "";
        try {
            HttpUrl httpurl = HttpUrl.parse(NATIVE_URL);
            //签名表头信息 Authorization
            String body = OrderData(wxPayKv.getOutTradeNo(), wxPayKv.getMoney(), wxPayKv.getDescription(), wxPayKv.getCallBackUrl());
            String authorization = schema + " " + getToken("POST", httpurl, body, wxPayKv.getOutTradeNo());
            //下单调用的接口，JSON格式
            String codeUrl = nativePostBody(NATIVE_URL, body, authorization);
            Map results = (Map) JSON.parse(codeUrl);
            wxQRcode = (String) results.get("code_url");
        } catch (Exception e) {
            log.error("PayServ.wxPay:" + e);
            throw new Exception(e.getMessage());
        }
        return wxQRcode;
    }

    /**
     * 回调解密返回内容
     *
     * @param inputJson 密文
     * @return
     * @throws Exception
     */
    public Map<String, Object> callBackDecrypt(String inputJson) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            // 先解析数据
            Map map = JSON.parseObject(inputJson, Map.class);

            // 只处理支付成功的
            if (map.get("summary").equals("支付成功")) {
                // 获取原始数据
                Map<String, String> resourceMap = JSON.parseObject(map.get("resource").toString(), Map.class);

                // 解密
                AesUtil aesUtil = new AesUtil(APIV3_KEY.getBytes(StandardCharsets.UTF_8));

                String OrderResponse = aesUtil.decryptToString(
                        resourceMap.get("associated_data").getBytes(StandardCharsets.UTF_8),
                        resourceMap.get("nonce").getBytes(StandardCharsets.UTF_8),
                        resourceMap.get("ciphertext"));
                results = JSON.parseObject(OrderResponse, Map.class);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }
}
