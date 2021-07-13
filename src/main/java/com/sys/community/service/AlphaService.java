package com.sys.community.service;

import com.sys.community.dao.AlphaDao;
import com.sys.community.dao.DiscussPostMapper;
import com.sys.community.dao.UserMapper;
import com.sys.community.entity.DiscussPost;
import com.sys.community.entity.User;
import com.sys.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service

public class AlphaService {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private AlphaDao alphaDao;

    public String find() {
        return alphaDao.select();
    }

    // REQUIRED: 支持当前事务（外部事物），如果外部事物不存在则创建新事物
    // REQUIRES_NEW：创建新事物，并且暂停当前事务（外部事物）
    // NESTED：如果当前存在事务（外部事物），则嵌套在该事务中执行，否则和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setHeaderUrl("http://images.nowcoder.com/head/25t.png");
        user.setEmail("alpha@qq.com");
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("hello");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        Integer.valueOf("abc");

        return "ok";
    }

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setHeaderUrl("http://images.nowcoder.com/head/25t.png");
                user.setEmail("alpha@qq.com");
                user.setCreateTime(new Date());

                userMapper.insertUser(user);

                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("你好");
                discussPost.setContent("hello");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

}

