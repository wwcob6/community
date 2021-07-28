package com.sys.community.service;

import com.sys.community.util.CommunityConstant;
import com.sys.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    // 将置顶的ip计入uv
    public void recordUV(String ip) {
        String key = RedisUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(key, ip);
    }

    // 统计指定范围呢的uv
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }
        // 合并这些数据
        String redisKey = RedisUtil.getUVKey(dateFormat.format(start), dateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }
    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        String dauKey = RedisUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);


    }
    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        // 进行或运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String dauKey = RedisUtil.getDAUKey(dateFormat.format(start), dateFormat.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(dauKey.getBytes());
            }
        });
    }
}
