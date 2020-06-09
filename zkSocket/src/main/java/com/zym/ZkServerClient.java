	package com.zym;

	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.io.PrintWriter;
	import java.net.Socket;
	import java.util.ArrayList;
	import java.util.List;

	import org.I0Itec.zkclient.IZkChildListener;
	import org.I0Itec.zkclient.ZkClient;

	public class ZkServerClient {
		// 获取所有的服务地址
		public static List<String> listServer = new ArrayList<String>();

		public static void main(String[] args) {
			initServer();
			ZkServerClient client = new ZkServerClient();
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String name;
				try {
					name = console.readLine();
					if ("exit".equals(name)) {
						System.exit(0);
					}
					client.send(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// 获取所有注册的服务
		public static void initServer() {
			listServer.clear();
			final String zkServer = "127.0.0.1:2181";
			final ZkClient zkClient = new ZkClient(zkServer,6000,1000);
			String memberPath = "/member";
			//获取所有子节点
			List<String> children = zkClient.getChildren(memberPath);
			listServer.clear();
			for (String child:children) {
				//读取节点值，即服务地址信息
				listServer.add((String) zkClient.readData(memberPath+"/"+child));
			}
			System.out.println("####获取到所有服务信息："+listServer);
			//订阅子节点事件监听，watcher机制
			zkClient.subscribeChildChanges(memberPath, new IZkChildListener() {
				//子节点有发生变化
				public void handleChildChange(String parentPath, List<String> list) throws Exception {
					System.out.println("####服务注册产生变动，正在重新获取注册服务...");
					listServer.clear();
					for (String sub:list) {
						//读取节点值，即服务地址信息
						listServer.add((String) zkClient.readData(parentPath+"/"+sub));
					}
					System.out.println("####获取到所有服务信息："+listServer);
				}
			});
		}

		// 服务调用次数
		private static int count = 1;
		// 会员服务集群数量，实际开发中不要写死，
		private static int memberServerCount = 2;

		// 获取当前server信息
		public static String getServer() {
			//轮询负载均衡策略，按请求次数进行取模，相当于进行轮询
			String serverName = listServer.get(count % memberServerCount);
			++count;
			return serverName;
		//每次只取第一个
			/*String serverName = listServer.get(0);
			return serverName;*/
		}

		public void send(String name) {

			String server = ZkServerClient.getServer();
			String[] cfg = server.split(":");

			Socket socket = null;
			BufferedReader in = null;
			PrintWriter out = null;
			try {
				socket = new Socket(cfg[0], Integer.parseInt(cfg[1]));
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				out.println(name);
				while (true) {
					String resp = in.readLine();
					if (resp == null)
						break;
					else if (resp.length() > 0) {
						System.out.println("Receive : " + resp);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
