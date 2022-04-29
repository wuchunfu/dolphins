package com.dolphin.saas.commons.clouds.comFinal;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class MasterCommon {

    private final String TX_SECRETLD = "";
    private final String TX_SECRETKEY = "";
    private final String ALI_SECRETLD = "";
    private final String ALI_SECRETKEY = "";
    /**
     * MD5加密用
     *
     * @param pwd
     * @return
     */
    public static String MD5(String pwd) {
        // 用于加密的字符
        char[] md5String = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            // 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
            byte[] btInput = pwd.getBytes();

            // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            // MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(btInput);

            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) { // i = 0
                byte byte0 = md[i]; // 95
                str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
                str[k++] = md5String[byte0 & 0xf]; // F
            }
            // 返回经过加密后的字符串
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取即时时间
     *
     * @return
     */
    public static String getTimes() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 秒返回
     * @param dateStr
     * @return
     */
    public Long getTime(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateStr)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 秒返回
        return date.getTime() / 1000;
    }

    /**
     * entry转map
     *
     * @param obj entry实体类
     * @return
     */
    public static Map<String, Object> objectMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 通用的返回控制
     *
     * @param status 状态码
     * @param data   数据，字典
     * @param msg    返回查询信息
     * @return
     */
    public <T> Map<String, Object> JsonResponseMap(int status, Map<String, T> data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("data", data);
        map.put("msg", msg);
        return map;
    }

    /**
     * 通用的返回控制
     *
     * @param status 状态码
     * @param data   数据、数组
     * @param msg    返回查询信息
     * @return
     */
    public Map<String, Object> JsonResponse(int status, ArrayList data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("data", data);
        map.put("msg", msg);
        return map;
    }

    /**
     * 通用的返回控制
     *
     * @param status 状态码
     * @param data   字符串
     * @param msg    返回查询信息
     * @return
     */
    public Map<String, Object> JsonResponseStr(int status, String data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("data", data);
        map.put("msg", msg);
        return map;
    }

    /**
     * 通用的返回控制
     *
     * @param status 状态码
     * @param data   对象
     * @param msg    返回查询信息
     * @return
     */
    public Map<String, Object> JsonResponseObj(int status, Object data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("data", data);
        map.put("msg", msg);
        return map;
    }
}
