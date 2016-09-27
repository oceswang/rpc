package com.galaxy.rpc.demo.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerBootstrap
{

	@SuppressWarnings("resource")
	public static void main(String[] args)
	{
		new ClassPathXmlApplicationContext("spring/*.xml");
	}

}
