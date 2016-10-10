package com.galaxy.rpc.demo.client;

import org.springframework.stereotype.Component;

import com.galaxy.rpc.client.RpcReference;
import com.galaxy.rpc.demo.model.User;
import com.galaxy.rpc.demo.service.DemoService;

@Component
public class DemoBean
{
	@RpcReference
	private DemoService service;
	
	public void call()
	{
		service.hello("param1");
		User user = new User();
		user.setName("uname");
		user.setAge(20);
		service.hello(user);
	}
}
