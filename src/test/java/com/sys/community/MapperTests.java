package com.sys.community;

import com.sys.community.dao.CommentMapper;
import com.sys.community.dao.DiscussPostMapper;
import com.sys.community.dao.LoginTicketMapper;
import com.sys.community.dao.UserMapper;
import com.sys.community.entity.Comment;
import com.sys.community.entity.DiscussPost;
import com.sys.community.entity.LoginTicket;
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
    private CommentMapper commentMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

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

    @Test
    public void loginTicketTest() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",0);
        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }
    @Test
    public void commentTest() {
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 275, 0, Integer.MAX_VALUE);
        for (Comment comment : comments) {
            System.out.println(comment);
            // System.out.println(userMapper.selectById(comment.getUserId()));
        }
    }

}
