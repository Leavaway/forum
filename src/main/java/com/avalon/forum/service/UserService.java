package com.avalon.forum.service;

import com.avalon.forum.dao.UserDao;
import com.avalon.forum.entity.LoginTicket;
import com.avalon.forum.entity.User;
import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.ForumTools;
import com.avalon.forum.util.MailService;
import com.avalon.forum.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MailService mailService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        User user = getCacheUser(id);
        if(user == null){
            user = init(id);
        }
        return user;
    }

    public Map<String, Object> register(User user){
        Map<String, Object> registerMap = new HashMap<>();

        //空值处理
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            registerMap.put("usernameMsg","账号不能为空");
            return registerMap;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            registerMap.put("passwordMsg", "密码不能为空");
            return registerMap;
        }
        if (user.getPassword().length()<6) {
            registerMap.put("passwordMsg", "密码长度不能小于六位");
            return registerMap;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            registerMap.put("emailMsg", "邮箱不能为空");
            return registerMap;
        }


        // 验证账号
        User u = userDao.selectByName(user.getUsername());
        if (u != null) {
            registerMap.put("usernameMsg", "该账号已存在");
            return registerMap;
        }

        // 验证邮箱
        u = userDao.selectByEmail(user.getEmail());
        if (u != null) {
            registerMap.put("emailMsg", "该邮箱已被注册");
            return registerMap;
        }

        // 注册用户
        System.out.println("ForumTools.encode(user.getPassword()): " + ForumTools.encode(user.getPassword()) );
        user.setPassword(ForumTools.encode(user.getPassword()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(ForumTools.generateUUID());
        user.setHeaderUrl(String.format("https://picsum.photos/id/%d/300/200", new Random().nextInt(100)));
        user.setCreateTime(new Date());
        userDao.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation" + "/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        mailService.sendMail(user.getEmail(),"Forum账号激活",templateEngine.process("/mail/activation",context));

        return registerMap;
    }

    public int activation(int userId, String code){
        User user = userDao.selectById(userId);
        if (user.getStatus() == 1) {
            return ForumConstants.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userDao.updateStatus(userId, 1);
            delete(userId);
            return ForumConstants.ACTIVATION_SUCCESS;
        } else {
            return ForumConstants.ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, long expire){
        Map<String, Object> loginMap = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            loginMap.put("usernameMsg","账号不能为空");
            return loginMap;
        }
        if (StringUtils.isBlank(password)) {
            loginMap.put("passwordMsg", "密码不能为空");
            return loginMap;
        }

        //验证账号密码
        User user = userDao.selectByName(username);
        if(user == null){
            loginMap.put("usernameMsg","账号不存在");
            return loginMap;
        }
        if(user.getStatus()==0){
            loginMap.put("usernameMsg","账号未激活");
            return loginMap;
        }
        if(!ForumTools.match(password, user.getPassword())){
            loginMap.put("passwordMsg","密码不正确");
            return loginMap;
        }

        //生成LoginTicket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ForumTools.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expire * 1000));
        String redisKey = RedisKeyUtil.getPrefixTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        loginMap.put("ticket", loginTicket.getTicket());

        return loginMap;
    }

    public void logout(String ticket){
        String redisKey = RedisKeyUtil.getPrefixTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getPrefixTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }

    public int updateHeader(int userId, String headerUrl){
        int rows = userDao.updateHeader(userId, headerUrl);
        delete(userId);
        return rows;
    }

    public int updatePassword(int userId, String password){
        return userDao.updatePassword(userId, ForumTools.encode(password));
    }

    public User findUserByName(String username) {
        return userDao.selectByName(username);
    }

    private User getCacheUser(int userId){
        String redisKey = RedisKeyUtil.getPrefixUser(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    private User init(int userId){
        User user = userDao.selectById(userId);
        String redisKey = RedisKeyUtil.getPrefixUser(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    private void delete(int userId){
        String redisKey = RedisKeyUtil.getPrefixUser(userId);
        redisTemplate.delete(redisKey);
    }

}
