package com.avalon.forum.service;

import com.avalon.forum.dao.DiscussPostDao;
import com.avalon.forum.entity.DiscussPost;
import com.avalon.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostDao discussPostDao;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return discussPostDao.selectDiscussPosts(userId, offset, limit,1);
    }

    public int findDiscussPostRows(int userId){
        return discussPostDao.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostDao.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostDao.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostDao.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostDao.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostDao.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostDao.updateScore(id, score);
    }

}
