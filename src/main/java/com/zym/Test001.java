package com.zym;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

//Zookeeper创建的是守护线程，当主线程执行完毕之后守护线程也会销毁，因此可能会导致zk未及时连接上，创建节点就已经失败
//为了保证每次都等zk连接信号发出以后再执行用户程序，引入信号量进行用户程序阻塞。
//CountDownLatch，并发包中的信号量，可以使用户程序进行阻塞，等待连接信号释放后，再执行。
public class Test001 {

    private static final String CONNECT_STRING = "127.0.0.1:2181";

    private static final int SESSION_OUTTIME = 6000;

    private static final CountDownLatch count = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZooKeeper zk = new ZooKeeper(CONNECT_STRING, SESSION_OUTTIME, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                //获取事件状态
                Event.KeeperState state = watchedEvent.getState();
                //获取事件类型
                Event.EventType type = watchedEvent.getType();
                //判断事件连接状态，发出连接信号
                if (Event.KeeperState.SyncConnected==state) {
                    if (Event.EventType.None==type) {
                        /*try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        count.countDown();//减1
                        System.out.println("####zk启动连接....###");
                    }
                }
            }
        });
        count.await();//阻塞用户程序，只有count==0时才会放行，非0时均会阻塞
        //用户程序
        String result = zk.create("/member","member-services".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("###创建了一个节点"+result+"####");
    }

}
