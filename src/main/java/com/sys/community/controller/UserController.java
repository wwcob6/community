package com.sys.community.controller;


import com.sys.community.annotation.LoginRequired;
import com.sys.community.entity.Comment;
import com.sys.community.entity.DiscussPost;
import com.sys.community.entity.Page;
import com.sys.community.entity.User;
import com.sys.community.service.*;
import com.sys.community.util.CommunityConstant;
import com.sys.community.util.CommunityUtil;
import com.sys.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHead(MultipartFile headerImage, Model model) {
        if (headerImage == null){
            model.addAttribute("error", "????????????????????????");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(substring)){
            model.addAttribute("error", "??????????????????");
            return "/site/setting";
        }

        //?????????????????????
        filename  = CommunityUtil.generateUUID() + substring;
        //????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("????????????" + e.getMessage());
            throw new RuntimeException("??????????????????", e);
        }
        // ??????????????????
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeadUrl(user.getId(), headUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String substring = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + substring);
        try(FileInputStream inputStream = new FileInputStream(fileName)) {
            ServletOutputStream outputStream = response.getOutputStream();

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("??????????????????" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatepassword", method = RequestMethod.POST)
    public String updatePassword(String password, String newPassword, String confirmPassword,Model model) {
        User user = hostHolder.getUser();
        if (StringUtils.isBlank(password)) {
            model.addAttribute("oldPasswordMassage", "?????????????????????");
            return "/site/setting";
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            model.addAttribute("oldPasswordMassage", "?????????????????????");
            return "/site/setting";
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if (newPassword.equals(password)) {
            model.addAttribute("newPasswordMassage", "??????????????????????????????");
            return "/site/setting";
        }
        confirmPassword = CommunityUtil.md5(confirmPassword + user.getSalt());
        if (!confirmPassword.equals(newPassword)) {
            model.addAttribute("conPasswordMassage", "??????????????????????????????");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(), confirmPassword);
        return "redirect:/index";
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model,
                                 @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????");
        }
        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("orderMode", orderMode);
        return "/site/profile";
    }
    @RequestMapping(path = "/profile/{userId}/post", method = RequestMethod.GET)
    public String getMypost(Model model, Page page, @PathVariable("userId") int userId) {
        // ??????????????????springmvc??????????????????model???page?????????page??????model
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/user/profile/" + userId + "/post");
        User userr = userService.findUserById(userId);
        List<DiscussPost> list = discussPostService.findDiscussPost(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost discussPost : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        int postCount = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("postCount", postCount);
        // ??????
        model.addAttribute("user", userr);
        model.addAttribute("discussPosts",discussPosts);
        return "/site/my-post";
    }
    @RequestMapping(path = "/profile/{userId}/comment", method = RequestMethod.GET)
    public String getMyComment(Model model, Page page, @PathVariable("userId") int userId) {
        // ??????????????????springmvc??????????????????model???page?????????page??????model
        page.setRows(commentService.findCommentCountByUserId(userId));
        page.setPath("/user/profile/" + userId + "/comment");
        User userr = userService.findUserById(userId);
        List<Comment> list = commentService.findCommentsByUserId(userId,page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (Comment comment : list){
                DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                if (discussPost != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("comment", comment);
                    map.put("discussPost", discussPost);
                    long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                    map.put("likeCount", likeCount);

                    discussPosts.add(map);
                }
            }
        }
        int commentCount = commentService.findCommentCountByUserId(userId);
        model.addAttribute("commentCount", commentCount);
        // ??????
        model.addAttribute("user", userr);
        model.addAttribute("comments",discussPosts);
        return "/site/my-reply";
    }
}
