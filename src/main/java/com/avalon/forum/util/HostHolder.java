package com.avalon.forum.util;

import com.avalon.forum.entity.User;
import org.springframework.stereotype.Component;

//持有用户信息用于代替session对象，线程隔离。
@Component
public class HostHolder{

    private ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public void setUser(User user){
        userThreadLocal.set(user);
    }

    public User getUser(){
        return userThreadLocal.get();
    }

    public void clear(){
        userThreadLocal.remove();
    }
}
