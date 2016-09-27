package com.galaxy.rpc.demo.service;

import com.galaxy.rpc.demo.model.User;

public interface DemoService
{
	public String hello(String param);
	public String hello(User user);
}
