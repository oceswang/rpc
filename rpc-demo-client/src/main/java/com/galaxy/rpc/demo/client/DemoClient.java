package com.galaxy.rpc.demo.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.galaxy.rpc.client.RpcProxy;
import com.galaxy.rpc.demo.model.User;
import com.galaxy.rpc.demo.service.DemoService;

public class DemoClient
{

	@SuppressWarnings("resource")
	public static void main(String[] args)
	{
		//普通方式
	/*	
		RpcProxy proxy = new RpcProxy();
		DemoService service = proxy.create(DemoService.class);
		service.hello("param1");
		User user = new User();
		user.setName("uname");
		user.setAge(20);
		service.hello(user);
		*/
		//spring方式
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/spring.xml");
		DemoBean bean = ctx.getBean(DemoBean.class);
		bean.call();

	}

}
