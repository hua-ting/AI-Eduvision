package com.learning.recommend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 改进版，包含异常处理
 */
@Component
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return 0;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return 0;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return null;
        }
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public boolean del(String... key) {
        try {
            if (key != null && key.length > 0) {
                if (key.length == 1) {
                    redisTemplate.delete(key[0]);
                } else {
                    redisTemplate.delete((Collection<String>) java.util.Arrays.asList(key));
                }
            }
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 批量删除
     * @param keys
     * @return
     */
    public boolean batchDel(List<String> keys) {
        try {
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 根据模式删除缓存
     * @param pattern
     * @return
     */
    public boolean delByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            return true;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return false;
        }
    }

    /**
     * 将值放入集合
     *
     * @param key 键
     * @param values 值
     * @return 成功个数
     */
    public long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return 0;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key 键
     * @param time 时间
     * @param values 值
     * @return
     */
    public long sAdd(String key, long time, Object... values) {
        try {
            long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) expire(key, time);
            return count;
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return 0;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return 0;
        }
    }

    /**
     * 获取set缓存
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (DataAccessException e) {
            logger.warn("Redis连接失败，操作已跳过: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Redis操作异常: ", e);
            return null;
        }
    }
}