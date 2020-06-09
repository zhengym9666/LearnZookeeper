package com.zym;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

public  class ZookeeperSelectMaster implements IRedisMaster{

    private static final String CONNECT_STRING = "127.0.0.1:2181";

    private ZkClient zk = new ZkClient(CONNECT_STRING);

    private static final String MASTER = "/master";

    private CountDownLatch count = null;

    public void getMaster(String redisName) {
        if (tryMaster(redisName)) {
            System.out.println("####新的主节点已选举出，master:"+redisName);
        }else {
            //等待监听
            waitChance(redisName);
            //唤醒等待后，再次尝试创建临时节点以获得master
            getMaster(redisName);
        }
    }

    public boolean tryMaster(String redisName) {
        try {
            //能创建临时节点成功，就能成为master
            zk.createEphemeral(MASTER,redisName.getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitChance(String redisName) {
        //注册节点事件通知
        IZkDataListener listener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {

            }

            //节点被删除，就唤醒等待
            public void handleDataDeleted(String s) throws Exception {
                if (count!=null) {
                    count.countDown();
                }
            }
        };
        zk.subscribeDataChanges(MASTER, listener);
        if (zk.exists(MASTER)) {
            try {
                //等待
                count = new CountDownLatch(1);//注意信号量只针对当前这次监听有效，下一次需要用新的信号量控制
                count.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("####master节点挂了，"+redisName+"准备去竞争master...");
        }
        zk.unsubscribeDataChanges(MASTER,listener);
    }
}
