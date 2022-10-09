package com.avalon.forum;

import com.avalon.forum.service.initService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
class ForumApplicationTests implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        initService initService1 = applicationContext.getBean(initService.class);
        System.out.println(initService1.init());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
