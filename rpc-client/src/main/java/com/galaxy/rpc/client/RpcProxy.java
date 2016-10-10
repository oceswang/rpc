package com.galaxy.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.UUID;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.common.util.ConfigUtil;
import com.galaxy.rpc.registry.ServiceDiscovery;
import com.galaxy.rpc.registry.ServiceDiscoveryFactory;
public class RpcProxy
{
	private ServiceDiscovery serviceDiscovery;
	private String registryAddress;
	public RpcProxy()
	{
		Properties config = ConfigUtil.getClasspathProp("rpc.properties");
		registryAddress = config.getProperty("registry.address");
		String serverAddress = config.getProperty("server.address");
		String protocol = config.getProperty("registry.protocol");
		if(registryAddress != null && serverAddress != null && protocol != null)
		{
			serviceDiscovery = ServiceDiscoveryFactory.create(protocol, registryAddress);
		}
	}
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
