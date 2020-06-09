package com.zym;

import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//##ZKServerSocket服务端
public class ZKServerSocket implements Runnable {
    private int port;

    public static void main(String[] args) throws IOException {
        int port = 18081;
        ZKServerSocket server = new ZKServerSocket(port);
        Thread thread = new Thread(server);
        thread.start();
    }

    public ZKServerSocket(int port) {
        this.port = port;
    }

    public void regServer() {
        String zkServer = "127.0.0.1:2181";
        ZkClient zkClient = new ZkClient(zkServer,6000,1000);
        //父节点作为持久节点
        String memberPath = "/member";
        String path = memberPath+"/server-"+port;
        if (zkClient.exists(path)) {
            zkClient.delete(path);
        }
        String value = "127.0.0.1:"+port;
        //注册的服务地址作为临时节点，服务名称作为节点名称，服务ip+端口号作为节点值
        zkClient.createEphemeral(path,value);
        System.out.println("####服务注册成功，"+path);
    }

    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            regServer();
            System.out.println("Server start port:" + port);
            Socket socket = null;
            while (true) {
                //监听
                socket = serverSocket.accept();
                new Thread(new ServerHandler(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e2) {

            }
        }
    }

}

