package com.avalon.forum.controller;

import com.avalon.forum.entity.DiscussPost;
import com.avalon.forum.entity.Page;
import com.avalon.forum.entity.User;
import com.avalon.forum.service.DiscussPostService;
import com.avalon.forum.service.LikeService;
import com.avalon.forum.service.UserService;
import com.avalon.forum.util.ForumConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("index")
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> iniData = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if( iniData != null){
            for (DiscussPost post:
                    iniData) {
                Map<String, Object> integratedPost = new HashMap<>();
                integratedPost.put("post", post);
                User user = userService.findUserById(post.getUserId());
                integratedPost.put("user", user);
                integratedPost.put("likeCount",likeService.findEntityLikeCount(ForumConstants.ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(integratedPost);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @GetMapping("error")
    public String getErrorPage(){
        return "/error/500";
    }





}
