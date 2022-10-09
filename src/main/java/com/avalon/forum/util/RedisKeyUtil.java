package com.avalon.forum.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";

    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

    /**
     * like:entity:entityType:entityId -> set(userId)
     * @param type EntityType: 帖子/评论
     * @param id EntityId
     * @return
     */
    public static String getEntityLikeKey(int type, int id){
        return PREFIX_ENTITY_LIKE + SPLIT + type + SPLIT + id;
    }

    /**
     * like:user:userId -> int
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * followee:userId:entityType -> zset(entityId,time)
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * follower:entityType:entityId -> zset(userId,time)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getPrefixKaptcha(String identity){
        return PREFIX_KAPTCHA + SPLIT + identity;
    }

    public static String getPrefixTicket(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getPrefixUser(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
