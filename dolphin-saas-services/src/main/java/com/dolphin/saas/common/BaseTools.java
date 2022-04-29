package com.dolphin.saas.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.entity.MerchantOrganization;
import com.dolphin.saas.entity.User;
import com.dolphin.saas.mapper.MemberMapper;
import com.dolphin.saas.mapper.MerchantOrgMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class BaseTools {
    @Resource
    private MemberMapper memberMapper;

    @Resource
    private MerchantOrgMapper merchantOrgMapper;

    /**
     * 根据uuid判断是否已经加入到组织
     * 如果加入到组织并且审核通过，则把组织里所有的UUID找出来，把这些uuid做成IN条件用于查询所有关联的数据。
     *
     * @param uuid
     * @return
     */
    public ArrayList<String> orgUUidList(String uuid) {
        ArrayList<String> uuidOrgLists = new ArrayList<>();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 首先查用户是否存在，必须没有删除，并可以的登录
        queryWrapper.eq("uuid", uuid);
        queryWrapper.eq("u_login_status", 1);
        queryWrapper.eq("u_delete", 0);
        queryWrapper.select("u_identity", "merchant_id");
        User user = memberMapper.selectOne(queryWrapper);

        if (user != null) {
            // 查是否加入到组织，如果是个人跳过
            if (user.getUidentity() != 0) {
                // 获取这个组织里面的所有有效的用户
                QueryWrapper<MerchantOrganization> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("merchant_id", user.getMerchantId());
                queryWrapper1.eq("status", 1);
                queryWrapper1.eq("user_delete", 0);
                queryWrapper1.select("merchant_id", "uuid");
                List<MerchantOrganization> organizationList = merchantOrgMapper.selectList(queryWrapper1);

                // 检查下用户是否在这个组织里
                MerchantOrganization merchantOrganization = new MerchantOrganization();
                merchantOrganization.setMerchantId(user.getMerchantId());
                merchantOrganization.setUuid(uuid);

                if (organizationList.contains(merchantOrganization)) {
                    // 如果在组织里，则把组织里所有的uuid暴露出来，用于查询
                    for (MerchantOrganization merchantOrganization1 : organizationList) {
                        uuidOrgLists.add(merchantOrganization1.getUuid());
                    }
                }
            }
            // 如果都不通过，则保留自己的，能查自己的
            if (uuidOrgLists.size() < 1) {
                uuidOrgLists.add(uuid);
            }
        }

        return uuidOrgLists;
    }

    public Map<String, Object> orgIdentity(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            // 首先查用户是否存在，必须没有删除，并可以的登录
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("u_login_status", 1);
            queryWrapper.eq("u_delete", 0);
            queryWrapper.select("u_identity", "merchant_id");
            User user = memberMapper.selectOne(queryWrapper);

            // 默认就是普通用户
            results.put("uidentity", "user");
            if (user != null) {
                // 查是否加入到组织，如果是个人跳过
                if (user.getUidentity() != 0) {
                    results.put("uidentity", "enterprise");
                    // 获取这个组织里面的所有有效的用户
                    QueryWrapper<MerchantOrganization> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("merchant_id", user.getMerchantId());
                    queryWrapper1.eq("status", 1);
                    queryWrapper1.eq("user_delete", 0);
                    queryWrapper1.eq("uuid", uuid);
                    queryWrapper1.select("merchant_id", "uuid");
                    if (merchantOrgMapper.selectCount(queryWrapper1) < 1) {
                        results.put("confirm", 0);
                    } else {
                        results.put("confirm", 1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("orgIdentity:" + e);
            throw new Exception(e.getMessage());
        }
        return results;
    }


    /**
     * 返回一个时间版本号
     *
     * @return 2021_12_24:214315
     */
    public String refDate() {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        return formater.format(new Date());
    }

    /**
     * entry转map
     *
     * @param obj entry实体类
     * @return
     */
    public Map<String, Object> objectMap(Object obj) {
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
     * 格式化时间参数
     *
     * @param dateTime
     * @return
     */
    public String formatData(Date dateTime) {
        try {
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat2.format(dateTime);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 返回秒数
     *
     * @param dateStr
     * @return
     */
    public Long getTimes(String dateStr) {
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
     * 对比两个列表
     *
     * @param OldData 旧数据
     * @param NewData 新数据
     * @return {insert=[cc, eef], delete=[a, c, d]} 返回需要更新/插入的，还有需要删除的
     */
    public Map<String, Object> diffSet(Set<String> OldData, Set<String> NewData) {
        Map<String, Object> results = new HashMap<>();

        List<String> removed = new ArrayList<>(OldData);
        removed.removeAll(NewData);

        List<String> update = new ArrayList<>(OldData);
        update.retainAll(NewData);

        List<String> added = new ArrayList<>(NewData);
        added.removeAll(OldData);

        results.put("add", added);
        results.put("delete", removed);
        results.put("update", update);

        return results;
    }
}
