package com.zym;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;
import java.util.concurrent.CountDownLatch;

//实现尝试获取锁和等待逻辑
public class ZookeeperDistributeLock extends ZookeeperAbstractLock {
    //获取锁，是要临时节点创建成功，那么就获得了锁
    public boolean tryLock() {
        try {
            zk.createEphemeral(PATH);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //若节点已存在那么用信号量实现代码阻塞等待，注入节点监听器，监听临时节点是否删除，删除则唤醒等待。
    public void waitLock() {
        final IZkDataListener dataListener = new IZkDataListener() {
            //节点发生修改
            public void handleDataChange(String s, Object o) throws Exception {

            }

            //节点被删除
            public void handleDataDeleted(String s) throws Exception {
                if (count!=null) {
                    //唤醒
                    count.countDown();
                }
            }
        };
        //往节点注入监听器
        zk.subscribeDataChanges(PATH,dataListener);
        //节点已存在
        if (zk.exists(PATH)) {
            try {
                count = new CountDownLatch(1);
                //使用信号量阻塞用户程序往下执行，直到事件通知唤醒
                count.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //删除监听器
        zk.unsubscribeDataChanges(PATH,dataListener);
    }
}
