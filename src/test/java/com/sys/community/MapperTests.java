package com.sys.community;

import com.sys.community.dao.*;
import com.sys.community.entity.*;
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
    private MessageMapper messageMapper;

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
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(0, 0, 10,0);
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
    @Test
    public void messageTest() {
        List<Message> messages = messageMapper.selectConversations(111, 0, Integer.MAX_VALUE);
        for (Message comment : messages) {
            System.out.println(comment);
            // System.out.println(userMapper.selectById(comment.getUserId()));
        }
        System.out.println(messageMapper.selectConversationCount(111));
        messages = messageMapper.selectLetters("111_112", 0, Integer.MAX_VALUE);
        for (Message comment : messages) {
            System.out.println(comment);
            // System.out.println(userMapper.selectById(comment.getUserId()));
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, null));
    }

}
