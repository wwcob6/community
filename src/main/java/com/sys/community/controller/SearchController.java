package com.sys.community.controller;

import com.sys.community.entity.DiscussPost;
import com.sys.community.entity.Page;
import com.sys.community.service.ESService;
import com.sys.community.service.LikeService;
import com.sys.community.service.UserService;
import com.sys.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ESService esService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // /search?keyword=***
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(Model model, String keyword, Page page) throws IOException {
        // 搜索所有
        int size = esService.searchDiscussPost(keyword, 0, 300).size();
        // 搜索帖子
        List<DiscussPost> discussPosts = esService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                map.put("user", userService.findUserById(discussPost.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));

                list.add(map);
            }
        }
        model.addAttribute("discussPosts", list);
        model.addAttribute("keyword", keyword);

        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPosts == null?0:size);

        return "/site/search";
    }

}
