package com.avalon.forum.controller;

import com.avalon.forum.entity.User;
import com.avalon.forum.service.UserService;
import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.ForumTools;
import com.avalon.forum.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("register")
    public String registerIndex(){
        System.out.println("check register");
        return "/site/register";
    }

    @GetMapping("login")
    public String getLoginPage(){
        return "/site/login";
    }

    @PostMapping("register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请点击链接完成激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ForumConstants.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功");
            model.addAttribute("target", "/login");
        } else if (result == ForumConstants.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "该账号已经激活过了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("kaptcha")
    public void getKaptcha(HttpServletResponse httpServletResponse){
        String securityText = kaptchaProducer.createText();
        BufferedImage securityImage = kaptchaProducer.createImage(securityText);

        String kaptchaIdentify = ForumTools.generateUUID();
        Cookie cookie = new Cookie("kaptchaIdentify", kaptchaIdentify);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        httpServletResponse.addCookie(cookie);
        String redisKey = RedisKeyUtil.getPrefixKaptcha(kaptchaIdentify);
        redisTemplate.opsForValue().set(redisKey, securityText, 60, TimeUnit.SECONDS);

        httpServletResponse.setContentType("image/png");
        try{
            OutputStream os = httpServletResponse.getOutputStream();
            ImageIO.write(securityImage, "png", os);
        } catch (IOException e){
            logger.error("响应验证码失败: " + e.getMessage());
        }

    }

    @PostMapping("login")
    public String login(String username, String password, String vericode,
                        boolean rememberme, Model model, HttpServletResponse httpServletResponse,
                        @CookieValue("kaptchaIdentify") String kaptchaIdentify ){
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaIdentify)){
            String redisKey = RedisKeyUtil.getPrefixKaptcha(kaptchaIdentify);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(vericode)||!kaptcha.equalsIgnoreCase(vericode)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号密码
        int expireTime = rememberme ? ForumConstants.REMEMBER_EXPIRED_SECONDS : ForumConstants.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expireTime);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expireTime);
            httpServletResponse.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @GetMapping("logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
