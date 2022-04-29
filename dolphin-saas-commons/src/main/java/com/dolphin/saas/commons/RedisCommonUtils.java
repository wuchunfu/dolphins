package com.dolphin.saas.commons;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisCommonUtils {

    @Resource
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
     * 设置缓存30分钟有效
     * @param key
     * @param value
     * @throws Exception
     */
    public void setData(String key, Object value) throws Exception {
        try {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        }catch (Exception e){
            throw new Exception("设置缓存失败!");
        }
    }

    /**
     * 判断这个HASH KEY是否存在
     * @param key
     * @return
     */
    public Boolean hashCheck(String key) {
        Boolean results = false;
        try {
            if (redisTemplate.hasKey(key)) {
                if (redisTemplate.opsForHash().size(key) > 0){
                    results = true;
                }
            }
        }catch (Exception e){
            log.error("[RedisCommonUtils]异常信息: {}",e.getMessage());
        }
        return results;
    }

    /**
     * 获取hash的最原始对象
     * @return
     */
    public HashOperations hashObj() {
        return redisTemplate.opsForHash();
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

    /**
     * 删除key
     * @param key
     * @throws Exception
     */
    public void deleteKey(final String key) throws Exception {
        try {
            if (!redisTemplate.delete(key)){
                throw new Exception("内部错误，请联系客服, code -1");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 设置60秒超时
     * @param key
     * @param value
     * @return
     */
    public void setMinute(final String key, Object value) throws Exception{
        try {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
