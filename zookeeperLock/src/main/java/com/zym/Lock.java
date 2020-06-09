package com.zym;

//定义分布式锁统一规范
public interface Lock {

    public void getLock();

    public void unlock();

}
