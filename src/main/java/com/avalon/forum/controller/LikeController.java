package com.avalon.forum.controller;


import com.avalon.forum.annotation.LoginRequired;
import com.avalon.forum.entity.Event;
import com.avalon.forum.entity.User;
import com.avalon.forum.event.EventProducer;
import com.avalon.forum.service.LikeService;
import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.ForumTools;
import com.avalon.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements ForumConstants {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType, entityId, entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("likeCount", likeCount);
        likeMap.put("likeStatus", likeStatus);

        if(likeStatus == 1){
            Event event = new Event().setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(entityId)
                    .setEntityType(entityType)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        return ForumTools.getJSONString(0, null, likeMap);

    }
}
