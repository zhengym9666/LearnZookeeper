package com.zym;

public class RedisSlave implements Runnable {

    private String redisName;

    private ZookeeperSelectMaster zkSM = new ZookeeperSelectMaster();

    public RedisSlave(String redisName){
        this.redisName = redisName;
    }

    public void run() {
        //尝试竞选master
        zkSM.getMaster(this.redisName);
    }

    public static void main(String[] args) {
        System.out.println("###模拟zookeeper实现redis选举策略开始...");
        //用多线程模拟是个redis节点
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new RedisSlave("redis-"+i));
            thread.start();
        }
    }
}
