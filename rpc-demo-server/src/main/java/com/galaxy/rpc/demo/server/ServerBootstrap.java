package com.galaxy.rpc.demo.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerBootstrap
{

	@SuppressWarnings("resource")
	public static void main(String[] args)
	{
		//使用spring注入
		new ClassPathXmlApplicationContext("spring/*.xml");
		//不使用spring
		//new RpcServer().startRpcServer();
	}

}
