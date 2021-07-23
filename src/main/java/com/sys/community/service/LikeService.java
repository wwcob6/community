package com.sys.community.service;

import com.sys.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        /*String key = RedisUtil.getEntityKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (isMember) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityKey = RedisUtil.getEntityKey(entityType, entityId);
                String userLikeKey = RedisUtil.getUserLikeKey(entityUserId);

                Boolean isMember = redisOperations.opsForSet().isMember(entityKey, userId);

                redisOperations.multi();
                if (isMember) {
                    redisOperations.opsForSet().remove(entityKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }
    // 统计点赞
    public long findEntityLikeCount(int entityType, int entityId) {
        String key = RedisUtil.getEntityKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    // 查询某人的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisUtil.getEntityKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
