package com.galaxy.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.registry.ServiceDiscovery;
@Component
public class RpcProxy
{
	@Autowired
	private ServiceDiscovery serviceDiscovery;
	private String registryAddress;
	
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass)
	{
		return (T)Proxy.newProxyInstance(
				interfaceClass.getClassLoader(), 
				new Class<?>[]{interfaceClass}, 
				new InvocationHandler(){

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
					{
						RpcRequest req = new RpcRequest();
						req.setRequestId(UUID.randomUUID().toString());
						req.setClassName(method.getDeclaringClass().getName());
						req.setMethodName(method.getName());
						req.setParamTypes(method.getParameterTypes());
						req.setParams(args);
						if(serviceDiscovery != null)
						{
							registryAddress = serviceDiscovery.discover();
						}
						
						String[] arr = registryAddress.split(":");
						String host = arr[0];
						int port = Integer.valueOf(arr[1]);
						RpcClient client = new RpcClient(host,port);
						client.send(req);
						RpcResponse resp = client.getResp();
						
						if(resp.getError() != null)
						{
							throw resp.getError();
						}
						else
						{
							return resp.getResult();
						}
					}
				});
	}

	public ServiceDiscovery getServiceDiscovery()
	{
		return serviceDiscovery;
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery)
	{
		this.serviceDiscovery = serviceDiscovery;
	}
	
}
