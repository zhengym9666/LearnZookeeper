package com.zym;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/*
注意步骤：
1）在封装时，new ZooKeeper对象时就创建了守护线程，只是把process方法抽离出来而已，
因此为了保证必须要等待zk连接信号发出后，主线程再继续往下执行，必须在创建了守护线程之后就立即使用信号量将
主线程阻塞掉，等到连接信号发送后，再继续执行。
2）但是，信号量仅仅能保证zk连接信号的发送，而不能保证，节点的操作信号发送，如进行新增节点后，主线程可能会
继续往下执行了，执行完毕后就销毁，守护线程也随之销毁，因此不会再次进入到process方法发出事件通知。

解决：
问题1）在Test001中已经给出解决方案，
问题2）的解决是针对节点操作注册watcher监听，这样，当发生节点增加后，就会触发watcher事件通知，从而
会再次进入到process方法中

 */

//封装zk连接
public class Test002 implements Watcher {

    private static final String CONNECT_STRING = "127.0.0.1:2181";

    private static final int SESSION_OUTTIME = 6000;

    CountDownLatch count = new CountDownLatch(1);

    ZooKeeper zk = null;

    public void createConnection() {
        try {
            zk = new ZooKeeper(CONNECT_STRING,SESSION_OUTTIME,this);//开启了守护线程
            count.await();//让主线程阻塞等待释放连接成功信号，即count减为0后主线程会继续执行
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println();
        System.out.println("####事件通知开始####");
        //获取事件状态
        Event.KeeperState state = watchedEvent.getState();
        //获取节点路径
        String path = watchedEvent.getPath();
        //获取事件类型
        Event.EventType type = watchedEvent.getType();
        System.out.println("####当前方法process,KeeperState:"+state+",path:"+path+",type:"+type);
        //判断事件连接状态
        if (Event.KeeperState.SyncConnected==state) {
            //判断事件类型，1.创建连接成功
            if (Event.EventType.None==type) {
                System.out.println("####zk连接成功....###");
                try {
                    Thread.sleep(5000);//模拟网络延迟原因连接信号的发送
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count.countDown();//减1
            } else if (Event.EventType.NodeCreated==type) {
                System.out.println("####创建节点成功，node:"+path);
            } else if (Event.EventType.NodeDataChanged==type) {
                System.out.println("####修改节点成功，node:"+path);
            }
        }
        System.out.println("####事件通知结束####");
        System.out.println();
    }

    public Stat exists(String path, boolean isWatch) {
        try {
            return zk.exists(path,isWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createNode(String path,String data) {
        try {
            exists(path,true);//注入watcher事件通知
            System.out.println("###正在创建节点...path"+path+",data:"+data);
            zk.create(path,data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNode(String path,String data) {
        try {
            zk.exists(path,true);//注入watcher事件通知
            System.out.println("正在修改节点，node:"+path+", data:"+data);
            zk.setData(path,data.getBytes(),-1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (zk!=null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Test002 test002 = new Test002();
        test002.createConnection();
//        test002.createNode("/test002_6","zym_0602");
        test002.updateNode("/test002_6","zym_update_0602");
        test002.close();
    }

}
