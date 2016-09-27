package com.galaxy.rpc.demo.server;

import com.galaxy.rpc.demo.model.User;
import com.galaxy.rpc.demo.service.DemoService;
import com.galaxy.rpc.server.RpcService;
@RpcService(DemoService.class)
public class DemoServiceImpl implements DemoService
{

	@Override
	public String hello(String param)
	{
		System.out.println("call hello, param "+param);
		return "return "+param;
	}

	@Override
	public String hello(User user)
	{
		System.out.println("call hello, param "+user);
		return "return "+user.toString();
	}

}
