package com.avalon.forum;

import com.avalon.forum.dao.DiscussPostDao;
import com.avalon.forum.dao.MessageDao;
import com.avalon.forum.dao.UserDao;
import com.avalon.forum.entity.DiscussPost;
import com.avalon.forum.entity.LoginTicket;
import com.avalon.forum.entity.Message;
import com.avalon.forum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MapperTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private DiscussPostDao discussPostDao;



    @Autowired
    MessageDao messageDao;


    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test2");
        user.setPassword("123456");;
        user.setEmail("666@qq.com");
        user.setHeaderUrl("https://picsum.photos/id/51/300/200");
        user.setCreateTime(new Date());
        int rows = userDao.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testInsertPost(){
        for (int i = 0; i < 30; i++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setUserId(152);
            discussPost.setContent("Fight and believe in a better tomorrow");
            discussPost.setCreateTime(new Date());
            discussPost.setStatus(0);
            discussPost.setTitle("Look at here!");
            discussPost.setType(0);
            int rows = discussPostDao.insertDiscussPost(discussPost);
            System.out.println(rows);
            System.out.println(discussPost.getId());
        }

    }






    @Test
    public void msgTest(){
        Message message = new Message();
        message.setContent("Hi");
        message.setFromId(152);
        message.setToId(157);
        message.setStatus(1);
        message.setConversationId("152_157");
        message.setCreateTime(new Date());
        messageDao.insertMessage(message);

        Message message1 = new Message();
        message1.setContent("Hi nihao");
        message1.setFromId(152);
        message1.setToId(157);
        message1.setStatus(1);
        message1.setConversationId("152_157");
        message1.setCreateTime(new Date());
        messageDao.insertMessage(message1);
    }

}
