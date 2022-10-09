package com.avalon.forum.controller;


import com.avalon.forum.annotation.LoginRequired;
import com.avalon.forum.entity.User;
import com.avalon.forum.service.FollowService;
import com.avalon.forum.service.LikeService;
import com.avalon.forum.service.UserService;
import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.ForumTools;
import com.avalon.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.network.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 收到用户上传头像并且检验，然后更新本地文件和用户头像路径参数
     * @param headerImage 用户传入头像
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadPortrait(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error", "请选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(fileType)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        fileName = ForumTools.generateUUID() + fileType;

        File file = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("用户上传文件失败: " + e.getMessage());
            throw new RuntimeException("用户上传文件失败", e);
        }

        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;

        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse httpServletResponse){
        fileName = uploadPath + "/" + fileName;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        httpServletResponse.setContentType("image/"+fileType);
        FileInputStream fileInputStream = null;
        try {
            OutputStream outputStream = httpServletResponse.getOutputStream();
            fileInputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer))!=-1){
                outputStream.write(buffer, 0 , b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }finally {
            if(fileInputStream!=null){
                try {
                    fileInputStream.close();
                }catch (Exception e){
                    logger.error("关闭IO流异常: " + e);
                }
            }
        }
    }

    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public String changePassword(String oldpassword, String newpassword, String newpasswordcheck, Model model){
        User user = hostHolder.getUser();
        if(user!=null){
            if(!ForumTools.match(oldpassword, user.getPassword())){
                model.addAttribute("passwordMsg","密码不正确");
                return "/site/setting";
            }
            if(newpassword.length()<6){
                model.addAttribute("passwordLengthMsg","密码长度应大于六位");
                return "/site/setting";
            }
            if(!newpassword.equals(newpasswordcheck)){
                model.addAttribute("passwordCheckMsg","输入的密码不一致");
                return "/site/setting";
            }
            userService.updatePassword(user.getId(), newpassword);
        }

        return "redirect:/logout";
    }

    @GetMapping("profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        long followeeCount = followService.findFolloweeCount(userId, ForumConstants.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        long followerCount = followService.findFollowerCount(ForumConstants.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ForumConstants.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";

    }

}
