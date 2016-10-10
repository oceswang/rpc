package com.galaxy.rpc.demo.server;

import org.springframework.stereotype.Service;

import com.galaxy.rpc.demo.model.User;
import com.galaxy.rpc.demo.service.DemoService;
import com.galaxy.rpc.server.RpcService;
@RpcService(DemoService.class)
@Service
public class DemoServiceImpl implements DemoService
{
	public DemoServiceImpl()
	{
		System.err.println("===================DemoServiceImpl=====================");
	}

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
