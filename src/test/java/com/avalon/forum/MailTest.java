package com.avalon.forum;


import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MailTest {

    @Autowired
    private MailService mailService;

    @Autowired
    TemplateEngine templateEngine;

    @Test
    public void test(){
        Context context = new Context();
        context.setVariable("username","Zhangsan");
        mailService.sendMail("505871488@qq.com","trdt",templateEngine.process("/mail/demo",context));

    }

    @Test
    public void test1(){


    }


}
