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
		System.err.println("call hello, param "+param);
		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "return "+param;
	}

	@Override
	public String hello(User user)
	{
		System.err.println("call hello, param "+user);
		return "return "+user.toString();
	}

}
