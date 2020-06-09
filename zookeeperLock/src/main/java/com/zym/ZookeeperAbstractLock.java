package com.zym;

import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

//实现分布式锁的公共逻辑，如zk连接
public abstract class ZookeeperAbstractLock  implements Lock{

    private static final String CONNECT_STRING = "127.0.0.1:2181";

    protected static final String PATH = "/lock";

    protected ZkClient zk = new ZkClient(CONNECT_STRING);

    protected CountDownLatch count = null;
    public void getLock() {
        //尝试获取锁，即创建临时节点
        if (tryLock()) {
            System.out.println("###获取锁成功");
        } else {
            //等待
            waitLock();
            //重新获取锁
            getLock();
        }
    }

    public abstract boolean tryLock();

    public abstract void waitLock();

    public void unlock() {
        if (zk!=null) {
            zk.close();
            System.out.println("###释放锁资源");
        }
    }
}
