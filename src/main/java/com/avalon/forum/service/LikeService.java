package com.avalon.forum.service;


import com.avalon.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 实现用户点赞
     * @param userId
     * @param type
     * @param id
     */
    public void like(int userId, int type, int id,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = RedisKeyUtil.getEntityLikeKey(type, id);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
                boolean isLiked = operations.opsForSet().isMember(key, userId);
                operations.multi();
                if(isLiked){
                    operations.opsForSet().remove(key, userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(key, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * 根据size查询点赞数量
     * @param type
     * @param id
     * @return
     */
    public long findEntityLikeCount(int type, int id){
        String key = RedisKeyUtil.getEntityLikeKey(type, id);
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 查询用户点赞状态
     * @param userId
     * @param type
     * @param id
     * @return
     */
    public int findEntityLikeStatus(int userId, int type, int id) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(type, id);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    public int findUserLikeCount(int userId){
        Integer count = (Integer) redisTemplate.opsForValue().get(RedisKeyUtil.getUserLikeKey(userId));
        return count==null?0: count;
    }




}
