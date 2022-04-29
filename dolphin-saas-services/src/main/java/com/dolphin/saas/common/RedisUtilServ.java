package com.dolphin.saas.common;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtilServ {

    @Autowired
    RedisTemplate redisTemplate;

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 根据token获取uuid
     *
     * @param token
     * @return
     */
    public String getUUID(String token) {
        String uuid = null;
        if (this.hasKeys(token)) {
            String userData = JSON.toJSONString(this.get(token));
            uuid = JSON.parseObject(userData).getString("uuid");
        }
        return uuid;
    }

    public Boolean hasKeys(String key) {
        return redisTemplate.hasKey(key);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 向redis插入值(有时效性的)
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean set(final String key, Object value) {
        Boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, Duration.ofDays(3600));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 没有时效性的设置，永久存在
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean noExpireSset(final String key, Object value) throws Exception {
        Boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            result = true;
        } catch (Exception e) {
            throw new Exception(e);
        }
        return result;
    }
}
