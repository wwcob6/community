package com.sys.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void redisTest1() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }
    @Test
    public void redisTest2() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "ruowang");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }
    @Test
    public void redisTest3() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void redisTest4() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "ruowang", "guanyu", "zhangfei");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    // 编程式事务
    @Test
    public void redisTest5() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";

                redisOperations.multi(); // 启用事务

                redisOperations.opsForSet().add(redisKey, "zhangsan1");
                redisOperations.opsForSet().add(redisKey, "zhangsan2");
                redisOperations.opsForSet().add(redisKey, "zhangsan3");
                redisOperations.opsForSet().add(redisKey, "zhangsan4");

                System.out.println(redisOperations.opsForSet().members(redisKey));

                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }

    // hyperloglog:统计20w个重复数据的独立总数
    @Test
    public void hyperloglog() {
        String rediskey = "test:hll01";

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey, i);
        }
        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(rediskey, r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(rediskey));
    }

    // 将三组数据合并，再统计合并后重复数据的独立总数
    @Test
    public void hyperloglogUnion() {
        String rediskey = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey, i);
        }
        String rediskey2 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey2, i);
        }
        String rediskey3 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey3, i);
        }
        String unionkey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionkey, rediskey, rediskey2, rediskey3);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionkey));
    }
    // 统计一组数据的布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }
    // 统计3组数据的布尔值， 并进行OR运算
    @Test
    public void testBitMapUnion() {
        String redisKey2 = "test:bm:02";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String key = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        key.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return redisConnection.bitCount(key.getBytes());
            }
        });

        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(key, 0));
        System.out.println(redisTemplate.opsForValue().getBit(key, 1));
        System.out.println(redisTemplate.opsForValue().getBit(key, 2));
        System.out.println(redisTemplate.opsForValue().getBit(key, 3));
        System.out.println(redisTemplate.opsForValue().getBit(key, 4));
        System.out.println(redisTemplate.opsForValue().getBit(key, 5));
        System.out.println(redisTemplate.opsForValue().getBit(key, 6));
        System.out.println(redisTemplate.opsForValue().getBit(key, 7));
    }
}
