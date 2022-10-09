package com.avalon.forum.controller;

import com.avalon.forum.entity.Comment;
import com.avalon.forum.entity.DiscussPost;
import com.avalon.forum.entity.Event;
import com.avalon.forum.event.EventProducer;
import com.avalon.forum.service.CommentService;
import com.avalon.forum.service.DiscussPostService;
import com.avalon.forum.util.ForumConstants;
import com.avalon.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements ForumConstants {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @PostMapping("add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        Event event = new Event().setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setData("postId", discussPostId);

        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost targetDiscussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(targetDiscussPost.getUserId());
        } else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment targetComment = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(targetComment.getUserId());
        }

        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
