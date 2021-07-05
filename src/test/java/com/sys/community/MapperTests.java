package com.sys.community;

import com.sys.community.dao.DiscussPostMapper;
import com.sys.community.dao.UserMapper;
import com.sys.community.entity.DiscussPost;
import com.sys.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DiscussPostMapper discussPostMapper;
    
    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }
    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("password");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("https://opgg-static.akamaized.net/images/lol/champion/Sett.png?image=c_scale,q_auto,w_140&v=1624418935");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdateUser(){
        userMapper.updatePassword(150,"1231456");

        User user = userMapper.selectById(150);
        System.out.println(user.getPassword());
    }
    @Test
    public void selectPostsTest(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(0, 0, 10);
        for (DiscussPost discussPost : discussPosts){
            System.out.println(discussPost);
        }
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }
}
