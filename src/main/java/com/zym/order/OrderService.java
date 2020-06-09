package com.zym.order;

/*
同步与lock的区别：
同步会自动释放锁，lock必须要手动上锁和释放锁
 */

import java.util.concurrent.locks.ReentrantLock;

//模拟生成订单号,通过多线程模拟多个用户
public class OrderService implements Runnable{

    OrderNumGenerator generator = new OrderNumGenerator();

    //使用重入锁
    ReentrantLock lock = new ReentrantLock();

    public void run() {
        String orderNum = null;
        //同步必须使用同一把锁
        /*synchronized (this) {
            orderNum = getOrderNum();
            System.out.println(Thread.currentThread().getName()+"###number:"+orderNum);
        }*/
        //使用重入锁,释放锁必须放在finally代码块,避免代码发生异常锁无法释放
        try {
            //上锁
            lock.lock();
            orderNum = getOrderNum();
            System.out.println(Thread.currentThread().getName()+"###number:"+orderNum);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁资源
            lock.unlock();
        }
    }

    public String getOrderNum() {
        return generator.createOrderNum();
    }

    public static void main(String[] args) {
        System.out.println("###模拟生成订单号开始...");
        OrderService orderService = new OrderService();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(orderService);
            thread.start();
        }
    }
}
