package com.sys.community.service;

import com.sys.community.dao.LoginTicketMapper;
import com.sys.community.dao.UserMapper;
import com.sys.community.entity.LoginTicket;
import com.sys.community.entity.User;
import com.sys.community.util.CommunityConstant;
import com.sys.community.util.CommunityUtil;
import com.sys.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        // 空值判断
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMassage", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMassage", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMassage", "邮箱不能为空");
            return map;
        }

        // 验证账号与邮箱
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1 != null){
            map.put("usernameMassage","账号已存在");
            return map;
        }
        user1 = userMapper.selectByEmail(user.getEmail());
        if (user1 != null){
            map.put("emailMassage","该邮箱已被注册！");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));

        user.setType(0);
        user.setStatus(0);//0为未激活
        user.setActivationCode(CommunityUtil.generateUUID());//生成激活码
        user.setHeaderUrl("https://c-ssl.duitang.com/uploads/item/201803/15/20180315231803_vuoir.thumb.1000_0.jpg");
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http"//localhost:8080/community/activation/用户id/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);

        if (user.getStatus() == 1) {
            //已激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAIL;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)){
            map.put("usernameMassage", "用户名不能空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMassage", "密码不能空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMassage", "该账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMassage", "该账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMassage", "密码不正确！");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000L * expiredSeconds));

        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }
    //推出登录
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }
    //
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    //更新图片路径
    public int updateHeadUrl(int userId, String headUrl) {
        return userMapper.updateHeader(userId, headUrl);
    }

    // 更改密码
    public int updatePassword(int userId, String password) {
        return userMapper.updatePassword(userId, password);
    }
}
